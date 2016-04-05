/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2014, OpenRemote Inc.
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
package org.openremote.controller.proxy;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import javax.net.ssl.SSLSocketFactory;

import org.apache.log4j.Logger;
import org.openremote.controller.Constants;

public class ControllerProxy extends Proxy {
   private static Logger logger = Logger.getLogger(Constants.PROXY_LOG_CATEGORY);
   private int controllerPort;
   private String controllerIP;

   public ControllerProxy(Socket srcSocket, String controllerIP, int controllerPort, int timeout)
         throws IOException {
      super(srcSocket, timeout);
      this.controllerIP = controllerIP;
      this.controllerPort = controllerPort;
   }

   public static Socket makeClientSocket(String urlString, String token, int timeout) throws IOException {
      logger.info("Opening socket to beehive at "+urlString);
      URL url = new URL(urlString);
      Socket socket = null;
      try{
         if (urlString.startsWith("https")) {
            logger.info("Use SSL socket");
            SSLSocketFactory sslsocketfactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
            socket = sslsocketfactory.createSocket();
         } else {
            logger.info("Use plain socket");
            socket = new Socket();
         }
         logger.info("Trying to connect");
         socket.connect(new InetSocketAddress(url.getHost(), url.getPort()));
         logger.info("Got socket to beehive");
         byte[] buffer = token.getBytes("ASCII");

         logger.info("Writing token handshake to beehive");
         OutputStream out = socket.getOutputStream();
         out.write(buffer);
         out.flush();
         return socket;
      }catch(IOException x){
         // don't log the stack since we re-throw
         logger.info("Got an exception while connecting: "+x.getMessage());
         // failed to connect, at least close the socket
         try{
            socket.close();
         }catch(IOException x2){
            // ignore
         }
         throw x;
      }
   }

   @Override
   protected Socket openDestinationSocket() throws IOException {
      Socket socket = new Socket();
      socket.connect(new InetSocketAddress(controllerIP, controllerPort));
      return socket;
   }
}
