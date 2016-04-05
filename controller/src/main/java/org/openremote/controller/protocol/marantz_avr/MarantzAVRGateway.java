/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2013, OpenRemote Inc.
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.controller.protocol.marantz_avr;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;

import org.openremote.controller.MarantzAVRConfig;
import org.openremote.controller.protocol.MessageQueueWithPriorityAndTTL;
import org.openremote.controller.protocol.MessageQueueWithPriorityAndTTL.Coalescable;
import org.openremote.controller.utils.Logger;

/**
 * Gateway implements TCP/IP connection and protocol handling to device.
 * 
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class MarantzAVRGateway {

   // Class Members --------------------------------------------------------------------------------

   private static final int TCP_TIMEOUT = 10000;
   private static final int COMMUNICATION_ERROR_RETRY_DELAY = 15000;

   /**
    * Marantz AVR logger. Uses a common category for all Marantz AVR related logging.
    */
   private final static Logger log = Logger.getLogger(MarantzAVRCommandBuilder.MARANTZ_AVR_LOG_CATEGORY);

   // Don't ask this to the config factory when instantiating the bean, this
   // results in infinite recursion
   private MarantzAVRConfig marantzConfig;

   private MessageQueueWithPriorityAndTTL<MarantzCommand> queue = new MessageQueueWithPriorityAndTTL<MarantzCommand>(2 * 60 * 1000); // 2 min TTL

   private MarantzConnectionThread connectionThread;
   
   // Flag indicates if a message had been sent to the device while ping thread was sleeping
   private boolean messageSent = false;

   private Map<String, List<MarantzAVRCommand>> registeredCommands = new HashMap<String, List<MarantzAVRCommand>>();
   
   public synchronized void startGateway() {
      if (marantzConfig == null) {
         marantzConfig = MarantzAVRConfig.readXML();
         // Check config, report error if any
         log.info("Got Marantz config");
         log.info("Address >" + marantzConfig.getAddress() + "<");
      }

      if (connectionThread == null) {
         // Starts some thread that has the responsibility to establish connection and keep it alive
         connectionThread = new MarantzConnectionThread();
         connectionThread.start();
      }
   }

   public void sendCommand(String command, String parameter) {
      log.info("Asked to send command " + command + parameter);

      // Ask to start gateway, if it's already done, this will do nothing
      startGateway();
      queue.add(new MarantzCommand(command, parameter));
   }
   
   private synchronized boolean isMessageSent() {
      return messageSent;
   }

   private synchronized void setMessageSent(boolean messageSent) {
      this.messageSent = messageSent;
   }

   // ---

   private class MarantzConnectionThread extends Thread {

      private MarantzReaderThread readerThread;
      private MarantzWriterThread writerThread;
      private MarantzPingThread pingThread;

      @Override
      public void run() {
         Socket socket;
         
         // Start a ping thread that regularly sends a packet to the device.
         // Allows to detect sooner when connection goes down (ORCJAVA-410).
         pingThread = new MarantzPingThread();
         pingThread.start();
         
         while (!isInterrupted()) {
            try {
               log.info("Trying to connect to " + marantzConfig.getAddress());
               socket = new Socket();
               socket.connect(new InetSocketAddress(marantzConfig.getAddress(), 23), TCP_TIMEOUT);
               log.info("Socket client connected");
               readerThread = new MarantzReaderThread(socket.getInputStream());
               readerThread.start();
               log.info("Reader thread asked to start");
               writerThread = new MarantzWriterThread(socket.getOutputStream());
               writerThread.start();
               log.info("Writer thread asked to start");
               // Wait for the read thread to die, this would indicate the connection was dropped
               while (readerThread != null) {
                  readerThread.join(1000);
                  if (!readerThread.isAlive()) {
                     log.info("Reader thread is dead, clean and re-try to connect");
                     socket.close();
                     readerThread = null;
                     writerThread.interrupt();
                     writerThread = null;
                  }
               }
            } catch (SocketException e) {
               log.error("Connection to Marantz receiver impossible, sleeping and re-trying later", e);
               // We could not connect, sleep for a while before trying again
               try {
                  Thread.sleep(COMMUNICATION_ERROR_RETRY_DELAY);
               } catch (InterruptedException e1) {
                  log.warn("Interrupted during our sleep", e1);
               }
            } catch (IOException e) {
               log.error("Connection to Marantz receiver impossible, sleeping and re-trying later", e);
               // We could not connect, sleep for a while before trying again
               try {
                  Thread.sleep(COMMUNICATION_ERROR_RETRY_DELAY);
               } catch (InterruptedException e1) {
                  log.warn("Interrupted during our sleep", e1);
               }
            } catch (InterruptedException e) {
               log.warn("Interrupted during our sleep", e);
            }
         }
         
         pingThread.interrupt();
         
         // For now do not support discovery, use information defined in config
      }

   }

   // ---

   private class MarantzWriterThread extends Thread {

      private OutputStream os;

      public MarantzWriterThread(OutputStream os) {
         super();
         this.os = os;
      }

      @Override
      public void run() {

         log.info("Writer thread starting");

         PrintWriter pr = new PrintWriter(new OutputStreamWriter(os));

         while (!isInterrupted()) {
            
            /*
             * EBR : this implementation dequeues a message before sending it.
             * If a problem occurs during sending, the message is not retried and simply lost.
             * 
             * A more robust implementation (for a protocol where the device always sends a response
             * to commands) is to only dequeue commands from write queue when the corresponding reply
             * has been received from the device (or after a number of failed retries).
             */
            
            MarantzCommand cmd = null;
            try {
               cmd = queue.blockingPoll();
            } catch (InterruptedException e) {
               break;
            }
            if (cmd != null) {
               log.info("Sending >" + cmd.toString() + "< on print writer " + pr);
               
               pr.print(cmd.toString());
               pr.flush();
               
               setMessageSent(true);
               
               try {
                 Thread.sleep(100); // Allow 100ms between messages or device does not provide feedback on initial status requests
               } catch (InterruptedException e) {
                  break;
               }
            }
         }
         log.info("Out of writer thread");
      }
   }

   // ---

   private class MarantzReaderThread extends Thread {

      private InputStream is;

      public MarantzReaderThread(InputStream is) {
         super();
         this.is = is;
      }

      @Override
      public void run() {
         log.info("Reader thread starting");
         log.info("TC input stream " + is);

         // Can not use BufferedReader.readLine because it accepts any of CR, LF or CRLF as line termination
         // The protocol defines only CR as line end marker, data before it might contain LF byte as value
         Scanner s = new Scanner(new BufferedInputStream(is));
         s.useDelimiter("\r");
         log.info("Scanner " + s);
         
         String line = null;
         try {
           line = s.next();
         } catch (NoSuchElementException e) {
            log.error("Could not read from Marantz", e);
            line = null;
         } catch (IllegalStateException e) {
            log.error("Could not read from Marantz", e);
            line = null;
         }
         do {
           try {
              log.info("Reader thread got line >" + line + "<");

                MarantzResponse response = parseResponse(line);
                if (response != null) {
                  List<MarantzAVRCommand> commands = registeredCommands.get(response.command);
                  if (commands != null) { 
                     for (MarantzAVRCommand command : commands) {
                        command.updateWithResponse(response);
                     }
                  }
                } else {
                   log.info("Received unknown information from Marantz >" + line + "<");
                }
              line = s.next();
           } catch (NoSuchElementException e) {
              log.error("Could not read from Marantz", e);
              line = null;
           } catch (IllegalStateException e) {
              log.error("Could not read from Marantz", e);
              line = null;
            }
          } while (line != null && !isInterrupted());
         log.info("Out of reader thread");
      }
   }

   private class MarantzPingThread extends Thread {

      @Override
      public void run() {
         log.info("Ping thread starting");
         
         // Threads awakes every 30 seconds and check if message has been send while it was asleep.
         // If none, then sends a message to keep some activity on the connection.
         while (!isInterrupted()) {
            try {
               Thread.sleep(30000);
             } catch (InterruptedException e) {
                break;
             }
            if (!isMessageSent()) {
               queue.add(new MarantzCommand("PW", "?"));
               // When message is actually de-queued and sent by the writer thread, flag is reset.
               // This means that the ping message is actually sent only every other time,
               // so half the wake-up frequency (i.e. every minute with the 30s wake-up used).
            } else {
               setMessageSent(false);
            }
         }
      }

   }
   
   private MarantzResponse parseResponse(String responseText) {
      if (responseText == null) {
         return null;
      }
      
      MarantzResponse response = null;

      // First try to match with a known command
      for (String key : registeredCommands.keySet()) {
         if (responseText.startsWith(key)) {
            response = new MarantzResponse();
            response.command = key;
            response.parameter = responseText.substring(key.length());
            
            return response;
         }
      }

      // Then by default (which is basically the commands we're not supporting anyway)
      // consider that the command is using the first 2 characters
      if (responseText.length() >= 2) {
         response = new MarantzResponse();
         response.command = responseText.substring(0, 2);
         response.parameter = responseText.substring(2);
      }
      return response;
   }
   
   public void registerCommand(String commandString, MarantzAVRCommand command) {
      List<MarantzAVRCommand> commands = registeredCommands.get(commandString);
      if (commands == null) {
         commands = new ArrayList<MarantzAVRCommand>();
         registeredCommands.put(commandString, commands);
      }
      commands.add(command);
   }
   
   public void unregisterCommand(String commandString, MarantzAVRCommand command) {
      List<MarantzAVRCommand> commands = registeredCommands.get(commandString);
      if (commands != null) {
         commands.remove(command);
      }
   }

   public class MarantzCommand implements Coalescable {

      private String command;
      private String parameter;

      public MarantzCommand(String command, String parameter) {
         this.command = command;
         this.parameter = parameter;
      }

      public String toString() {
         return command + parameter + "\r";
      }

      @Override
      public boolean isCoalesable(Coalescable other) {
         if (!(other instanceof MarantzCommand)) {
            return false;
         }
//         MarantzCommand otherCommand = (MarantzCommand) other;
         return false;

         // TODO: To be coalescable requires quite a bit of knowledge on commands
         // For instance, 2 absolute volume settings can be coalesced into the last occurrence
         // where as 2 UP volume commands must not.
      }

   }

   public class MarantzResponse {

      public String command;
      public String parameter;

   }

}
