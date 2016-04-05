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
package org.openremote.controller.protocol.socket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

import junit.framework.Assert;

import org.junit.Test;
import org.openremote.controller.component.EnumSensorType;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.protocol.Event;
import org.openremote.controller.protocol.EventProducer;

/**
 * Tests for UDPListenerCommand implementation.
 * See {@link org.openremote.controller.protocol.socket.UDPListenerCommand}.
 * 
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class UDPListenerCommandTest {

   /**
    * Test receive of UDP packet on configured port.
    */
   @Test
   public void testReceiveAnything() throws IOException, InterruptedException {
      UDPListenerCommand cmd = new UDPListenerCommand("20000", "");
      SensorMock s = new SensorMock();      
      cmd.setSensor(s);

      sendUDPPacket(20000, "Hello");
      synchronized (s) {
         s.wait(2000);
      }
      Assert.assertNotNull("Should have received update, UDP packet sent", s.getLastUpdate());
      cmd.stop(s);
   }

   /**
    * Test UDP packet is not received when sent to different port.
    */
   @Test
   public void testDoNotReceiveForOtherPort() throws IOException, InterruptedException {
      UDPListenerCommand cmd = new UDPListenerCommand("20000", "");
      SensorMock s = new SensorMock();      
      cmd.setSensor(s);

      sendUDPPacket(21000, "Hello");
      synchronized (s) {
         s.wait(2000);
      }
      Assert.assertNull("Should not have received update, UDP packet sent to different port", s.getLastUpdate());
      cmd.stop(s);
   }

   /**
    * Test UDP packet is received or not based on regular expression.
    */
   @Test
   public void testReceiveMatchingRegexp()  throws IOException, InterruptedException {
      UDPListenerCommand cmd = new UDPListenerCommand("20000", "a+");
      SensorMock s = new SensorMock();      
      cmd.setSensor(s);

      sendUDPPacket(20000, "b");
      synchronized (s) {
         s.wait(2000);
      }
      Assert.assertNull("Should not have been updated, UDP packet does not match our regular expression", s.getLastUpdate());

      sendUDPPacket(20000, "aa");
      synchronized (s) {
         s.wait(2000);
      }
      Assert.assertNotNull("Should have received update, UDP packet matches our regular expression", s.getLastUpdate());
      cmd.stop(s);
   }
   
   /**
    * Test UDP packet is received or not based on regular expression and that the value of the sensor
    * is updated with a capturing group is one is defined on the regular expression.
    */
   @Test
   public void testReceiveMatchingRegexpWithCaptureGroup()  throws IOException, InterruptedException {
      UDPListenerCommand cmd = new UDPListenerCommand("20000", "b(a+)");
      SensorMock s = new SensorMock();      
      cmd.setSensor(s);

      sendUDPPacket(20000, "b");
      synchronized (s) {
         s.wait(2000);
      }
      Assert.assertNull("Should not have been updated, UDP packet does not match our regular expression", s.getLastUpdate());

      sendUDPPacket(20000, "baa");
      synchronized (s) {
         s.wait(2000);
      }
      Assert.assertNotNull("Should have received update, UDP packet matches our regular expression", s.getLastUpdate());
      Assert.assertEquals("Should have received update and its content should be matched group",  "aa", s.getLastUpdate());
      cmd.stop(s);
   }

   /**
    * Helper method to send a packet containing the given string on the given UDP port
    * 
    * @param port UDP port to send the message to
    * @param message String to use as content of packet
    */
   private void sendUDPPacket(int port, String message) throws IOException {
      DatagramSocket socket = new DatagramSocket();

      InetAddress address = InetAddress.getLocalHost();
      byte[] bytes = message.getBytes();
      DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address, port);
      
      socket.send(packet);
   }
   
   /**
    * Private class mocking basic functionality of a custom sensor,
    * allowing to receive any string and store for further examination by the testing code. 
    */
   private class SensorMock extends Sensor {
      
      private String lastUpdate;
      
      public SensorMock() {
         super("Mock", 1, null, new EventProducer() {}, 1, null, EnumSensorType.CUSTOM);
      }
      
      @SuppressWarnings("rawtypes")
      @Override
      protected Event processEvent(String value) {
         return null;
      }
      
      @Override
      synchronized public void update(String state) {
         this.lastUpdate = state;
         notify();
      }

      private String getLastUpdate() {
         return lastUpdate;
      }
      
   }
}
