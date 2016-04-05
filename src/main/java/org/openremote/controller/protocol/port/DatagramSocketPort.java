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
package org.openremote.controller.protocol.port;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Map;

import org.openremote.controller.protocol.knx.KNXCommandBuilder;
import org.openremote.controller.utils.Logger;

/**
 * A physical port (bus) using datagram sockets to send and receive messages.
 * 
 * @see Port
 *
 * @author Olivier Gandit
 */
public class DatagramSocketPort implements Port
{
  private final static Logger log = Logger.getLogger(KNXCommandBuilder.KNX_LOG_CATEGORY);

  private DatagramSocket inSocket, outSocket;

  @Override public void configure(Map<String, Object> configuration)
  {
    this.inSocket = (DatagramSocket) configuration.get("inSocket");
    this.outSocket = (DatagramSocket) configuration.get("outSocket");
  }

   @Override
   public void start() {
   }

   @Override
   public void stop() {
      if (this.inSocket != null) this.inSocket.close();
      if (this.outSocket != null) this.outSocket.close();
   }

   @Override
   public void send(Message message) throws IOException {
      DatagramSocketMessage m = (DatagramSocketMessage) message;
      this.outSocket.send(new DatagramPacket(m.getContent(), m.getContent().length, m.getDestAddr()));
   }


  @Override public Message receive() throws IOException
  {
    if (inSocket == null)
    {
      throw new IOException("Configuration error, listening socket is null and cannot receive!");
    }

    byte[] buffer = new byte[1024];
    DatagramPacket p = new DatagramPacket(buffer, buffer.length);

    this.inSocket.receive(p);

    return new DatagramSocketMessage((InetSocketAddress) this.inSocket.getLocalSocketAddress(), p.getData());
  }
}
