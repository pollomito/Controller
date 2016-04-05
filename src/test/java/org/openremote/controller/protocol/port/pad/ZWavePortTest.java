package org.openremote.controller.protocol.port.pad;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openremote.controller.protocol.port.Message;
import org.openremote.controller.protocol.port.PortException;
import org.openremote.controller.protocol.port.pad.AbstractPort;

public class ZWavePortTest {
   private AbstractPort port;
   private Message message;
   private Object lock;
   private PortListener listener;

   @Before
   public void setUp() throws IOException, InterruptedException {
      this.lock = new Object();
      this.listener = new PortListener();
   }

   @After
   public void tearDown() throws InterruptedException, IOException {
   }

   @Test
   public void testPort() {
      this.port = new AbstractPort();
      this.listener.start();

      Map<String, Object> cfg = new HashMap<String, Object>();
      cfg.put(AbstractPort.PORT_ID, "/dev/cu.SLAB_USBtoUART");
      cfg.put(AbstractPort.PORT_TYPE, "serial");
      cfg.put(AbstractPort.PORT_SPEED, "115200");
      cfg.put(AbstractPort.PORT_NB_BITS, "8");
      cfg.put(AbstractPort.PORT_PARITY, "no");
      try {
         this.port.configure(cfg);
         this.port.start();
         synchronized (this.lock) {
            this.message = null;

            byte NODE_ID = 0x03;
            byte STATE_ON = (byte) 0xff;
            byte STATE_OFF = (byte) 0x00;
            byte[] data = new byte[] { 0x01, 0x09, 0x00, 0x13, NODE_ID, 0x03, 0x20, 0x01, STATE_ON, 0x05, 0x00 };
            data[data.length - 1] = checksum(data);
            this.port.send(new Message(data));
            System.out.println("ON sent");
            this.lock.wait(300);
            if (this.message == null) Assert.fail();
            this.port.send(new Message(new byte[] { 0x06 }));
            System.out.println("ACK sent");
            this.lock.wait(300);
            if (this.message == null) Assert.fail();
            data = new byte[] { 0x01, 0x09, 0x00, 0x13, NODE_ID, 0x03, 0x20, 0x01, STATE_OFF, 0x05, 0x00 };
            data[data.length - 1] = checksum(data);
            this.port.send(new Message(data));
            System.out.println("OFF sent");
            this.lock.wait(300);
            if (this.message == null) Assert.fail();
            this.port.send(new Message(new byte[] { 0x06 }));
            System.out.println("ACK sent");
         }
         this.port.stop();
      } catch (IOException e) {
         Assert.fail(e.getMessage());
      } catch (PortException e) {
         e.printStackTrace();
         Assert.fail(e.getMessage());
      } catch (InterruptedException e) {
         Assert.fail(e.getMessage());
      }

   }

   private static byte checksum(byte[] data) {
      byte ret = (byte) 0xff;
      for (int i = 1; i < (data.length - 1); i++) {
         ret ^= data[i];
      }
      return ret;
   }

   private class PortListener extends Thread {
      @Override
      public void run() {
         while (!this.isInterrupted()) {
            try {
               Message m = ZWavePortTest.this.port.receive();
               synchronized (ZWavePortTest.this.lock) {
                  ZWavePortTest.this.message = m;
                  ZWavePortTest.this.lock.notify();
               }
            } catch (IOException e) {
               Assert.fail(e.getMessage());
            }
         }
      }
   }
}
