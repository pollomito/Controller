/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2011, OpenRemote Inc.
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
package org.openremote.controller.protocol.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.protocol.EventListener;

/**
 * TODO
 *
 * @author Marcus Redeker 2009-4-26
 * @author Phillip Lavender
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 * @author Ivan Martinez
 */
public class TCPSocketCommand implements ExecutableCommand, EventListener, Runnable {

   // Class Members --------------------------------------------------------------
   /**
    * Common logging category.
    */
   private static Logger logger = Logger.getLogger(TCPSocketCommandBuilder.TCP_PROTOCOL_LOG_CATEGORY);
   
   
   // Instance Fields ------------------------------------------------------------
   /** A pipe separated list of command string that are sent over the socket */
   private String command;

   /** The IP to which the socket is opened */
   private String ip;

   /** The port that is opened */
   private String port;

   /** The regex which is used to extract sensor data from received result */
   private String regex;

   /** The polling interval which is used for the sensor update thread */
   private Integer pollingInterval;
   
   /** The thread that is used to peridically update the sensor */
   private Thread pollingThread;
   
   /** The sensor which is updated */
   private Sensor sensor;
   
   /** Boolean to indicate if polling thread should run */
   boolean doPoll = false;
  
   // Constructors  ----------------------------------------------------------------
   public TCPSocketCommand(String ipAddress, String port, String command, String regex, Integer intervalInMillis) {
      this.ip = ipAddress;
      this.port = port;
      this.command = command;
      this.regex = regex;
      this.pollingInterval = intervalInMillis;
   }

   
   // Public Instance Methods ----------------------------------------------------------------------
   /**
    * Gets the command.
    *
    * @return the command
    */
   public String getCommand() {
      return command;
   }

   /**
    * Sets the command.
    *
    * @param command the new command
    */
   public void setCommand(String command) {
      this.command = command;
   }

   /**
    * Gets the ip
    * @return the ip
    */
   public String getIp() {
      return ip;
   }

   /**
    * Sets the ip
    * @param ip the new ip
    */
   public void setIp(String ip) {
      this.ip = ip;
   }

   /**
    * Gets the port
    * @return the port
    */
   public String getPort() {
      return port;
   }

	/**
	 * Sets the port
	 * @param port the new port
	 */
   public void setPort(String port) {
      this.port = port;
   }


   public Integer getPollingInterval() {
      return pollingInterval;
   }


   public void setPollingInterval(Integer pollingInterval) {
      this.pollingInterval = pollingInterval;
   }


   /**
    * {@inheritDoc}
    */
   @Override
   public void send() {
      requestSocket();
   }

   private String requestSocket() {
      Socket socket = null;
      try {
         socket = new Socket(getIp(), Integer.parseInt(getPort()));
         OutputStream out = socket.getOutputStream();

         StringTokenizer st = new StringTokenizer(getCommand(), "|");
         while (st.hasMoreElements()) {
            String cmd = (String) st.nextElement();
            byte[] bytes;
            if (cmd.startsWith("0x")) {
               String tmp = getCommand().substring(2);
               bytes = hexStringToByteArray(tmp.replaceAll(" ", "").toLowerCase());
            } else {
               bytes = (cmd + "\r").getBytes();
            }
            out.write(bytes);
         }

         String result = readReply(socket);
         logger.debug("received message: " + result);
         return result;
      } catch (Exception e) {
         logger.error("SocketCommand could not execute", e);
      } finally {
         if (socket != null) {
            try {
               socket.close();
            } catch (IOException e) {
               logger.error("Socket could not be closed", e);
            }
         }
      }
      return "";
   }

   private String readReply(java.net.Socket socket) throws IOException {
      BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      char[] buffer = new char[200];
      int readChars = bufferedReader.read(buffer, 0, 200); // blocks until message received
      if (readChars > 0) {
         String reply = new String(buffer, 0, readChars);
         return reply;
      } else {
         return "";
      }
   }

   
   private byte[] hexStringToByteArray(String s) {
      int len = s.length();
      byte[] data = new byte[len / 2];
      for (int i = 0; i < len; i += 2) {
          data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
      }
      return data;
   }

   @Override
   public void setSensor(Sensor sensor)
   {
     logger.debug("*** setSensor called as part of EventListener init *** sensor is: " + sensor);
     if (pollingInterval == null) {
       throw new RuntimeException("Could not set sensor because no polling interval was given");
     }
     this.sensor = sensor;
     this.doPoll = true;
     pollingThread = new Thread(this);
     pollingThread.setName("Polling thread for sensor: " + sensor.getName());
     pollingThread.start();
   }

   @Override
   public void stop(Sensor sensor)
   {
     this.doPoll = false;
   }

   @Override
   public void run() {
      logger.debug("Sensor thread started for sensor: " + sensor);
      while (doPoll) {
         // Strip response from control characters (non-ASCII)...
         // Patch provided by Phillip Lavender
         String rawResult = this.requestSocket();
         Pattern p = Pattern.compile("\\p{Cntrl}");
         Matcher m = p.matcher(rawResult);
         String readValue = m.replaceAll("");
         
         if (regex != null) {
           Pattern regexPattern = Pattern.compile(regex);
           Matcher matcher = regexPattern.matcher(readValue);
           if (matcher.find()) {
             String result = matcher.group();
             logger.info("result of regex evaluation: " + result);
             sensor.update(result);
           } else {
             logger.info("regex evaluation did not find a match");
             sensor.update("N/A");
           }
         } else {
           sensor.update(readValue);
         }
         try {
            Thread.sleep(pollingInterval); // We wait for the given pollingInterval before reading socket again
         } catch (InterruptedException e) {
            doPoll = false;
            pollingThread.interrupt();
         }
      }
      logger.debug("*** Out of run method: " + sensor);
   }

}
