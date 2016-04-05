/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2015, OpenRemote Inc.
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
package org.openremote.controller.protocol.domintell;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openremote.controller.DomintellConfig;
import org.openremote.controller.protocol.domintell.model.DimmerModule;
import org.openremote.controller.protocol.domintell.model.DomintellModule;
import org.openremote.controller.protocol.domintell.model.InputModule;
import org.openremote.controller.protocol.domintell.model.RelayModule;
import org.openremote.controller.protocol.domintell.model.TemperatureModule;
import org.openremote.controller.protocol.MessageQueueWithPriorityAndTTL;

/**
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class DomintellGateway {

   // Class Members --------------------------------------------------------------------------------

   /**
    * Domintell logger. Uses a common category for all Domintell related logging.
    */
   private final static Logger log = Logger.getLogger(DomintellCommandBuilder.DOMINTELL_LOG_CATEGORY);

   /**
    * Interval in ms between 2 pings to the Domintell system (using HELLO command)
    */
   private static final int PING_INTERVAL = 59000;

   private static HashMap<String, DomintellModule> moduleCache = new HashMap<String, DomintellModule>();
   
   private static HashMap<String, Class<? extends DomintellModule>> moduleClasses = new HashMap<String, Class<? extends DomintellModule>>();

   // Don't ask this to the config factory when instantiating the bean, this
   // results in infinite recursion
   private DomintellConfig domintellConfig;

   private MessageQueueWithPriorityAndTTL<DomintellCommandPacket> queue = new MessageQueueWithPriorityAndTTL<DomintellCommandPacket>();

   private LoginState loginState = new LoginState();

   // A timer to ping the DETH02, ensuring session stays opened.
   private Timer pingTimer = new Timer();
   // Tasks that does the pinging using the HELLO command
   private TimerTask pingTask = null;
   
   private DomintellConnectionThread connectionThread;
   
   static {
      moduleClasses.put("BIR", RelayModule.class);
      moduleClasses.put("DMR", RelayModule.class);
      moduleClasses.put("TRP", RelayModule.class);
      moduleClasses.put("DIM", DimmerModule.class);
      moduleClasses.put("D10", DimmerModule.class);
      moduleClasses.put("TSB", TemperatureModule.class);
      moduleClasses.put("TE1", TemperatureModule.class);
      moduleClasses.put("TE2", TemperatureModule.class);
      moduleClasses.put("LC3", TemperatureModule.class);
      moduleClasses.put("PBL", TemperatureModule.class);
      moduleClasses.put("IS4", InputModule.class);
      moduleClasses.put("IS8", InputModule.class);
   }
   
   public synchronized void startGateway() {
     if (domintellConfig == null) {
        domintellConfig = DomintellConfig.readXML();
       log.info("Got Domintell config");
       log.info("Address >" + domintellConfig.getAddress() + "<");
       log.info("Port >" + domintellConfig.getPort() + "<");
     }

     if (connectionThread == null) {
       // Starts some thread that has the responsibility to establish connection and keep it alive
       connectionThread = new DomintellConnectionThread();
       connectionThread.start();
     }
   }
   
   public void sendCommand(String command) {
      log.info("Asked to send command " + command);

      // Ask to start gateway, if it's already done, this will do nothing
      startGateway();
      queue.add(new DomintellCommandPacket(command));
      
      // As we sent a command, we don't need an explicit ping (HELLO command)
      resetPingTask();
    }
   
   private void resetPingTask() {
      synchronized (pingTimer) {
        if (pingTask != null) {
           pingTask.cancel();
           pingTimer.purge();
        }
        pingTask = new TimerTask() {
          @Override
          public void run() {
             queue.add(new DomintellCommandPacket("HELLO"));
          }
           
        };
        pingTimer.schedule(pingTask, PING_INTERVAL, PING_INTERVAL);
      }
   }

   /**
    * Gets the Domintell Module from the cache, creating it if not already
    * present.
    * 
    * @param address
    * @return
    * @return
    * @throws DomintellModuleException 
    */
   public DomintellModule getDomintellModule(String moduleType, DomintellAddress address, Class<? extends DomintellModule> moduleClass) throws DomintellModuleException {      
     DomintellModule module = moduleCache.get(moduleType + address);
     if (module == null) {
       // No device yet in the cache, try to create one
       try {
         Constructor<? extends DomintellModule> constructor = moduleClass.getConstructor(DomintellGateway.class, String.class, DomintellAddress.class);
         module = constructor.newInstance(this, moduleType, address);
       } catch (SecurityException e) {
         throw new DomintellModuleException("Impossible to create device instance", moduleType, address, moduleClass, e);
       } catch (NoSuchMethodException e) {
         throw new DomintellModuleException("Impossible to create device instance", moduleType, address, moduleClass, e);
       } catch (IllegalArgumentException e) {
         throw new DomintellModuleException("Impossible to create device instance", moduleType, address, moduleClass, e);
       } catch (InstantiationException e) {
         throw new DomintellModuleException("Impossible to create device instance", moduleType, address, moduleClass, e);
       } catch (IllegalAccessException e) {
         throw new DomintellModuleException("Impossible to create device instance", moduleType, address, moduleClass, e);
       } catch (InvocationTargetException e) {
         throw new DomintellModuleException("Impossible to create device instance", moduleType, address, moduleClass, e);
       }
     }
     if (!(moduleClass.isInstance(module))) {
       throw new DomintellModuleException("Invalid device type found at given address", moduleType, address, moduleClass, null);
     }
     moduleCache.put(moduleType + address, module);
     return module;
   }

   private class DomintellConnectionThread extends Thread {

      /**
       * Timeout on the reader thread i.e. we must receive a packet within that delay since the last read
       * We should receive the clock update every minute from the Domintell master, so using 61s for this should be safe.
       */
      private static final int READ_TIMEOUT = 61000;

      DatagramSocket socket;
      
      private DomintellReaderThread readerThread;
      private DomintellWriterThread writerThread;

      @Override
      public void run() {
        if (socket == null) {
          while (!isInterrupted()) {
            try {
              socket = new DatagramSocket();
              socket.setSoTimeout(READ_TIMEOUT);

              log.info("Trying to connect to " + domintellConfig.getAddress() + " on port " + domintellConfig.getPort());
              socket.connect(InetAddress.getByName(domintellConfig.getAddress()), domintellConfig.getPort());
              log.info("Socket connected");
              readerThread = new DomintellReaderThread(socket);
              readerThread.start();
              log.info("Reader thread started");
              writerThread = new DomintellWriterThread(socket);
              writerThread.start();
              log.info("Writer thread started");
              // Wait for the read thread to die, this would indicate the connection was dropped
              while (readerThread != null) {
                readerThread.join(1000);
                if (!readerThread.isAlive()) {
                  log.info("Reader thread is dead, clean and re-try to connect");
                  socket.disconnect();
                  socket.close();
                  readerThread = null;
                  writerThread.interrupt();
                  writerThread = null;
                }
              }
            } catch (SocketException e) {
              log.warn("Failed to connect to Domintell, retrying later", e);
              // We could not connect, sleep for a while before trying again
              try {
                Thread.sleep(15000);
              } catch (InterruptedException e1) {
                log.trace("Been interrupted while waiting to re-connect", e1);
              }

            } catch (IOException e) {
               log.warn("Failed to connect to Domintell, retrying later", e);
              // We could not connect, sleep for a while before trying again
              try {
                Thread.sleep(15000);
              } catch (InterruptedException e1) {
                 log.trace("Been interrupted while waiting to re-connect", e1);
              }
            } catch (InterruptedException e) {
               log.trace("Connection thread has been interrupted", e);
            }
          }
        }

        // For now do not support discovery, use information defined in config
      }

    }
   
   private class DomintellWriterThread extends Thread {

      private DatagramSocket socket;

      public DomintellWriterThread(DatagramSocket socket) {
        super();
        this.socket = socket;
      }

      @Override
      public void run() {

        log.info("Writer thread starting");

        while (!isInterrupted()) {
          synchronized (loginState) {
            while (!loginState.loggedIn) {
              try {
                while (!loginState.needsLogin && !loginState.loggedIn) {
                  log.info("Not logged in, waiting to be woken up");
                  // We're not logged in, wait until the reader thread ask to login and confirms we're logged in
                  loginState.wait();
                  log.info("Woken up on loggedIn, loggedIn: " + loginState.loggedIn + "- needsLogin: " + loginState.needsLogin);
                }
                if (!loginState.loggedIn) {
                  // We've been awakened and we're not yet logged in. It means we need to send login info
                   byte[] buf = "LOGIN".getBytes();
                   try {
                      DatagramPacket p = new DatagramPacket(buf, buf.length, InetAddress.getByName(domintellConfig.getAddress()), domintellConfig.getPort());
                     socket.send(p);
                  } catch (IOException e) {
                     log.warn("Could not send LOGIN packet");
                  }
                   loginState.needsLogin = false;
                  log.info("Sent log in info");
                } else {
                   // We're logged in, send a PING command to make sure we get a current view of system status
                   queue.add(new DomintellCommandPacket("PING"));
                   // and start the ping task
                   resetPingTask();
                }
                // We've been awakened and we're logged in, we'll just go out of the loop and proceed with normal execution
              } catch (InterruptedException e) {
                // We'll loop and test again for login
              }
            }
          }

          DomintellCommandPacket cmd = null;
          try {
               cmd = queue.blockingPoll();
          } catch (InterruptedException e) {
             break;
          }
          if (cmd != null) {
            log.info("Sending >" + cmd.toString() + "< on socket");
            byte[] buf = cmd.toString().getBytes();
            try {
               DatagramPacket p = new DatagramPacket(buf, buf.length, InetAddress.getByName(domintellConfig.getAddress()), domintellConfig.getPort());
               socket.send(p);
            } catch (IOException e) {
               log.warn("Could not send packet >" + cmd.toString() + "<");
            }
            // Domintell specifies 25ms delay between messages
            try {
               Thread.sleep(25);
            } catch (InterruptedException e) {
               break;
            }
          }
        }
        log.debug("Writer thread stopping");
      }

   }

   private class DomintellReaderThread extends Thread {
      private DatagramSocket socket;
      
      // A pattern to match the date/time packet sent by the DETH02
      private Pattern dateTimePattern;

      public DomintellReaderThread(DatagramSocket socket) {
        super();
        this.socket = socket;
        this.dateTimePattern = Pattern.compile("\\d{1,2}:\\d{1,2} \\d{1,2}/\\d{1,2}/\\d{1,2}");
      }

      @Override
      public void run() {
        log.info("Reader thread starting");
        // We're starting, login
        synchronized (loginState) {
           // If we though we were already logged in, reset that
           loginState.loggedIn = false;
           loginState.needsLogin = true;
           // Notify writer thread to send login
           loginState.notify();
        }

        do {
          try {
             byte[] buffer = new byte[256];
             DatagramPacket p = new DatagramPacket(buffer, buffer.length);
             socket.receive(p);
             String packetText = new String(p.getData(), 0, p.getLength(), "ISO-8859-1");
             
             // Get rid of line ending
             if (packetText.endsWith("\n")) {
                packetText = packetText.substring(0, packetText.lastIndexOf("\n") - 1);
             }
             
            log.info("Reader thread got packet >" + packetText + "<");
            if (packetText.startsWith("INFO:Session opened:INFO")) {
              synchronized (loginState) {
                loginState.loggedIn = true;
                loginState.invalidLogin = false;
                // We're logged in, notify writer thread to start sending messages
                loginState.notify();
              }
            } /*else if (line.startsWith("LOGIN:")) {
              log.info("Asked to login, wakening writer thread");
              synchronized (loginState) {
                // If we though we were already logged in, reset that
                loginState.loggedIn = false;

                loginState.needsLogin = true;
                // System asks for login, notify writer thread to send it
                loginState.notify();
              }
            } */else if (packetText.startsWith("INFO:Auth failed:INFO")) {
              // TODO: close the connection, nothing we can do
              synchronized (loginState) {
                loginState.loggedIn = false;
                loginState.invalidLogin = true;
              }
            } else if (packetText.startsWith("INFO:Access denied. Close current session:INFO")) {
              synchronized (loginState) {
                loginState.loggedIn = false;
              }
              // Get out of our read loop, this will terminate the thread
              break;
            } else if (packetText.equals("INFO:Session timeout:INFO")) {
               synchronized (loginState) {
                  loginState.loggedIn = false;
                }
                // Get out of our read loop, this will terminate the thread
               break;
            } else if (packetText.equals("INFO:World:INFO")) {
               log.debug("Domintell system replied to HELLO");
            } else if (packetText.equals("PONG")) {
               log.debug("Domintell system replied to PING");
            } else if (dateTimePattern.matcher(packetText).matches()) {
               log.debug("Domintell system reported date/time: " + packetText);
            } else {
               // First 3 chars in module type
               // Next 6 is address
               // Then 1 char is "type of data"

               // Note that time / date info can also be reported
               String moduleType = packetText.substring(0, 3);
               String address = packetText.substring(3, 9);

               log.info("Module type " + moduleType + " address " + address);
               log.info("Module classes " + moduleClasses);
               try {
                  Class<? extends DomintellModule> moduleClass = moduleClasses.get(moduleType);
                  log.info("Module class " + moduleClass);
                  if (moduleClass != null) {
                     DomintellAddress domintellAddress;
                     domintellAddress = new DomintellAddress("0x" + address);
                     DomintellModule module = getDomintellModule(moduleType, domintellAddress, moduleClass);                     
                    if (module != null) {
                       module.processUpdate(packetText.substring(9).trim());
                    }
                  }
               } catch (DomintellModuleException e) {
                  log.error("Impossible to get module", e);
               } catch (InvalidDomintellAddressException e) {
                  log.error("Impossible to get module", e);
               }
              } /*else {
                // Unknown response
                // TODO
              }*/
          } catch (IOException e) {
            log.error("Error receiving packet from Domintell system", e);
            break;
          }
        } while (!isInterrupted());
      }

   }
   
   private class DomintellCommandPacket {
   
      private String command;

      public DomintellCommandPacket(String command) {
        this.command = command;
      }

      public String toString() {
         return command;
      }
   }
   
   private class LoginState {

      /**
       * Indicates that we must send the login information.
       */
      public boolean needsLogin;

      /**
       * Indicates if we're logged into the system, if not commands must be queued.
       */
      public boolean loggedIn;

      /**
       * Indicates if we tried logging in and been refused the login, if so do not try again.
       * TODO: there must be a way to reset this.
       */
      public boolean invalidLogin;
    }
}
