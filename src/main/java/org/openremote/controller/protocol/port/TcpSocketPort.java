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
package org.openremote.controller.protocol.port;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Map;

import org.openremote.controller.Constants;
import org.openremote.controller.utils.Logger;

/**
 * A physical port (bus) using a tcp socket to send and receive messages.
 * 
 * Can optionally specify the start and end bytes of a message frame, the packet size
 * or a packet processor implementation for customised packet processing
 * 
 * @see Port
 *
 * @author Richard Turner
 */
public class TcpSocketPort implements Port
{
  public static final String TCP_PORT_CONFIGURATION_PACKET_SIZE = "packetSize";
  public static final String TCP_PORT_CONFIGURATION_END_BYTE = "endByte";
  public static final String TCP_PORT_CONFIGURATION_START_BYTE = "startByte";
  public static final String TCP_PORT_CONFIGURATION_PROCESSOR = "processor";
  public static final String TCP_PORT_CONFIGURATION_SOCKET = "socket";

/**
   * A Packet Processor delegate that builds the packet and indicates
   * when a packet is completely assembled.
   * 
   * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
   */
  public interface PacketProcessor {
    /**
     * Process the new byte read from the socket and optionally
     * add it to the packet being built; return true if the
     * packet is completely assembled and ready to be returned
     * otherwise return false.
     * 
     * @param packet a ByteArrayOutputStream representing the packet currently being built
     * @param newByte newly read byte
     * @return true if packet is fully assembled, false otherwise
     */
    boolean processByte(ByteArrayOutputStream packet, byte newByte);
    
    /**
     * Called just before a packet is returned to the caller; allows
     * the packet to be validated; return true if valid otherwise false.
     * Packet indicated as invalid are dropped, packet buffer is emptied
     * and reading continues with the next byte in search for valid packet. 
     * 
     * @param packet a ByteArrayOutputStream representing the packet currently being built
     * @return true if packet is valid, false otherwise
     */
    boolean packetIsValid(ByteArrayOutputStream packet);
  }

  public final static String TCP_PORT_LOG_CATEGORY = Constants.CONTROLLER_PROTOCOL_LOG_CATEGORY + "port.tcp";
  private final static Logger log = Logger.getLogger(TCP_PORT_LOG_CATEGORY);

  private Byte startByte;
  private Byte endByte;
  private Integer packetSize;
  private PacketProcessor processor;
  
  private Socket socket;

  @Override
  public void configure(Map<String, Object> configuration)
  {
    this.socket = (Socket) configuration.get(TCP_PORT_CONFIGURATION_SOCKET);
    this.processor = (PacketProcessor) configuration.get(TCP_PORT_CONFIGURATION_PROCESSOR);
    this.startByte = (Byte)configuration.get(TCP_PORT_CONFIGURATION_START_BYTE);
    this.endByte = (Byte)configuration.get(TCP_PORT_CONFIGURATION_END_BYTE);
    this.packetSize = (Integer)configuration.get(TCP_PORT_CONFIGURATION_PACKET_SIZE);
  }

   @Override
   public void start() {
   }

   @Override
   public void stop() {
    if (this.socket != null) {
      try {
        this.socket.close();
      } catch (IOException e) {
        log.error(e.getMessage(), e);
      }
    }
   }

   @Override
   public void send(Message message) throws IOException {
     OutputStream out = socket.getOutputStream();
     out.write(message.getContent());
     out.flush();
   }


  @Override
  public Message receive() throws IOException {
    if (socket == null) {
      throw new IOException("Configuration error, listening socket is null and cannot receive!");
    }
    
    InputStream is = socket.getInputStream();
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] buffer = new byte[1];
    
    while (is.read(buffer, 0, 1)  != -1) {
      // If packet processor defined then delegate packet logic to it
      if (processor != null) {
        if (processor.processByte(baos, buffer[0])) {

          // Validate the packet
          if (!processor.packetIsValid(baos)) {
            baos.reset();
            log.info("Processor deemed packet as invalid");
          } else {
            break;
          }
        }
        continue;
      }      
      
      if (baos.size() == 0 && startByte != null) {
        // If start byte is defined check that it has been found
        if (buffer[0] != startByte.byteValue()) {
          // Ignore and continue
          continue;
        }
      }

      // Add read byte to the packet
      baos.write(buffer, 0, 1);
      
      // If packet size defined check if that has been reached
      if (packetSize != null && baos.size() == packetSize.intValue()) {
        break;
      }
      
      // If end byte defined check if it has been found
      if (baos.size() > 0 && endByte != null) {
        if (buffer[0] == endByte.byteValue()) {
          break;
        }
      }
    }
    
    byte[] packetBytes = baos.toByteArray();
    return new Message(packetBytes);
  }
  
}
