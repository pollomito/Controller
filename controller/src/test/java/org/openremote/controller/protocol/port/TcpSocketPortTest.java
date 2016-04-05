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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openremote.controller.protocol.port.TcpSocketPort.PacketProcessor;

public class TcpSocketPortTest {

   @Test
   public void testReadingFullMessage() throws IOException, PortException {
      Port socketPort = PortFactory
            .createPhysicalBus("org.openremote.controller.protocol.port.TcpSocketPort");

      Socket mockedSocket = mock(Socket.class);
      when(mockedSocket.getInputStream()).thenReturn(new ByteArrayInputStream("test_packet".getBytes()));

      Map<String, Object> portConfiguration = new HashMap<String, Object>();
      portConfiguration.put(TcpSocketPort.TCP_PORT_CONFIGURATION_SOCKET, mockedSocket);
      socketPort.configure(portConfiguration);

      Message message = socketPort.receive();
      Assert.assertNotNull("A message should have been read", message);
      Assert.assertNotNull("Read message should have content", message.getContent());
      Assert.assertEquals("Read message content invalid", "test_packet", new String(message.getContent()));

      // EBR : This is testing what the current implementation of TcpSocketPort does.
      // According to documentation in Port interface, the call to receive() should have blocked.
      message = socketPort.receive();
      Assert.assertNotNull("A message should have been read", message);
      Assert.assertNotNull("Read message should have content", message.getContent());
      Assert.assertEquals("Read message content should be empty", "", new String(message.getContent()));
   }
   
   @Test
   public void testReadingMessagesWithLength() throws IOException, PortException {
      Port socketPort = PortFactory
            .createPhysicalBus("org.openremote.controller.protocol.port.TcpSocketPort");

      Socket mockedSocket = mock(Socket.class);
      when(mockedSocket.getInputStream()).thenReturn(new ByteArrayInputStream("123456".getBytes()));

      Map<String, Object> portConfiguration = new HashMap<String, Object>();
      portConfiguration.put(TcpSocketPort.TCP_PORT_CONFIGURATION_SOCKET, mockedSocket);
      portConfiguration.put(TcpSocketPort.TCP_PORT_CONFIGURATION_PACKET_SIZE, 3);
      socketPort.configure(portConfiguration);

      Message message = socketPort.receive();
      Assert.assertNotNull("A message should have been read", message);
      Assert.assertNotNull("Read message should have content", message.getContent());
      Assert.assertEquals("Read message content invalid", "123", new String(message.getContent()));

      message = socketPort.receive();
      Assert.assertNotNull("A message should have been read", message);
      Assert.assertNotNull("Read message should have content", message.getContent());
      Assert.assertEquals("Read message content invalid", "456", new String(message.getContent()));
   }
   
   @Test
   public void testReadingMessagesWithStartByteAndLength() throws IOException, PortException {
      Port socketPort = PortFactory
            .createPhysicalBus("org.openremote.controller.protocol.port.TcpSocketPort");

      Socket mockedSocket = mock(Socket.class);
      when(mockedSocket.getInputStream()).thenReturn(new ByteArrayInputStream("1x234x56".getBytes()));

      Map<String, Object> portConfiguration = new HashMap<String, Object>();
      portConfiguration.put(TcpSocketPort.TCP_PORT_CONFIGURATION_SOCKET, mockedSocket);
      portConfiguration.put(TcpSocketPort.TCP_PORT_CONFIGURATION_PACKET_SIZE, 3);
      portConfiguration.put(TcpSocketPort.TCP_PORT_CONFIGURATION_START_BYTE, "x".getBytes()[0]);
      socketPort.configure(portConfiguration);

      Message message = socketPort.receive();
      Assert.assertNotNull("A message should have been read", message);
      Assert.assertNotNull("Read message should have content", message.getContent());
      Assert.assertEquals("Read message content invalid", "x23", new String(message.getContent()));

      message = socketPort.receive();
      Assert.assertNotNull("A message should have been read", message);
      Assert.assertNotNull("Read message should have content", message.getContent());
      Assert.assertEquals("Read message content invalid", "x56", new String(message.getContent()));
   }

   @Test
   public void testReadingMessagesWithStopByte() throws IOException, PortException {
      Port socketPort = PortFactory
            .createPhysicalBus("org.openremote.controller.protocol.port.TcpSocketPort");

      Socket mockedSocket = mock(Socket.class);
      when(mockedSocket.getInputStream()).thenReturn(new ByteArrayInputStream("1x234x56".getBytes()));

      Map<String, Object> portConfiguration = new HashMap<String, Object>();
      portConfiguration.put(TcpSocketPort.TCP_PORT_CONFIGURATION_SOCKET, mockedSocket);
      portConfiguration.put(TcpSocketPort.TCP_PORT_CONFIGURATION_END_BYTE, "x".getBytes()[0]);
      socketPort.configure(portConfiguration);

      Message message = socketPort.receive();
      Assert.assertNotNull("A message should have been read", message);
      Assert.assertNotNull("Read message should have content", message.getContent());
      Assert.assertEquals("Read message content invalid", "1x", new String(message.getContent()));

      message = socketPort.receive();
      Assert.assertNotNull("A message should have been read", message);
      Assert.assertNotNull("Read message should have content", message.getContent());
      Assert.assertEquals("Read message content invalid", "234x", new String(message.getContent()));
      
      // EBR: Current implementation just returns left bytes as last packet.
      // One might have assumed that those would be dropped if endByte is not encountered
      message = socketPort.receive();
      Assert.assertNotNull("A message should have been read", message);
      Assert.assertNotNull("Read message should have content", message.getContent());
      Assert.assertEquals("Read message content invalid", "56", new String(message.getContent()));
   }

   @Test
   public void testReadingMessagesWithStartAndStopByte() throws IOException, PortException {
      Port socketPort = PortFactory
            .createPhysicalBus("org.openremote.controller.protocol.port.TcpSocketPort");

      Socket mockedSocket = mock(Socket.class);
      when(mockedSocket.getInputStream()).thenReturn(new ByteArrayInputStream("1x23y4x5y6".getBytes()));

      Map<String, Object> portConfiguration = new HashMap<String, Object>();
      portConfiguration.put(TcpSocketPort.TCP_PORT_CONFIGURATION_SOCKET, mockedSocket);
      portConfiguration.put(TcpSocketPort.TCP_PORT_CONFIGURATION_START_BYTE, "x".getBytes()[0]);
      portConfiguration.put(TcpSocketPort.TCP_PORT_CONFIGURATION_END_BYTE, "y".getBytes()[0]);
      socketPort.configure(portConfiguration);

      Message message = socketPort.receive();
      Assert.assertNotNull("A message should have been read", message);
      Assert.assertNotNull("Read message should have content", message.getContent());
      Assert.assertEquals("Read message content invalid", "x23y", new String(message.getContent()));

      message = socketPort.receive();
      Assert.assertNotNull("A message should have been read", message);
      Assert.assertNotNull("Read message should have content", message.getContent());
      Assert.assertEquals("Read message content invalid", "x5y", new String(message.getContent()));
      
      message = socketPort.receive();
      Assert.assertNotNull("A message should have been read", message);
      Assert.assertNotNull("Read message should have content", message.getContent());
      Assert.assertEquals("Read message content should be empty", "", new String(message.getContent()));
   }
   
   @Test
   public void testReadingMessagesWithPacketProcessor() throws IOException, PortException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

      Port socketPort = PortFactory
            .createPhysicalBus("org.openremote.controller.protocol.port.TcpSocketPort");

      Socket mockedSocket = mock(Socket.class);
      when(mockedSocket.getInputStream()).thenReturn(new ByteArrayInputStream("123456".getBytes()));

      TestProcessor testProcessor = new TestProcessor();

      Map<String, Object> portConfiguration = new HashMap<String, Object>();
      portConfiguration.put(TcpSocketPort.TCP_PORT_CONFIGURATION_SOCKET, mockedSocket);
      portConfiguration.put(TcpSocketPort.TCP_PORT_CONFIGURATION_PROCESSOR, testProcessor);
      socketPort.configure(portConfiguration);

      Message message = socketPort.receive();
      Assert.assertNotNull("A message should have been read", message);
      Assert.assertNotNull("Read message should have content", message.getContent());
      Assert.assertEquals("Read message content invalid", "456", new String(message.getContent()));
      
      Assert.assertEquals("Processor processByte should have been called 6 times", 6, testProcessor.getProcessorByteCount());
      Assert.assertEquals("Processor packetIsValid should have been called 2 times", 2, testProcessor.getPacketIsValidCount());
   }
   
   private class TestProcessor implements PacketProcessor
   {
      private int processByteCount = 0;
      private int packetIsValidCount = 0;
      
      @Override
      public boolean processByte(ByteArrayOutputStream packet, byte newByte) {
         processByteCount++;
         byte[] buffer = { newByte }; 
         try {
            packet.write(buffer);
         } catch (IOException e) {
            Assert.fail("Failed processing byte");
         }
         return (processByteCount % 3) == 0;
      }

      @Override
      public boolean packetIsValid(ByteArrayOutputStream packet) {
         packetIsValidCount++;
         return (processByteCount > 3);
      }
      
      public int getProcessorByteCount() {
         return processByteCount;
      }
      
      public int getPacketIsValidCount() {
         return packetIsValidCount;
      }
   }

}
