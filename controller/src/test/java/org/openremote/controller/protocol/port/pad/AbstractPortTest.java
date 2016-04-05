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

public class AbstractPortTest {
   private AbstractPort port;
   private Message message;
   private Object lock;
   private PortListener listener;

   @Before
   public void setUp() throws IOException, InterruptedException {
      // Runtime.getRuntime().exec("pad/src/main/c/pad");
      // synchronized (this) {
      // this.wait(1000);
      // }
      this.lock = new Object();
      this.listener = new PortListener();
   }

   @After
   public void tearDown() throws InterruptedException, IOException {
      // Runtime.getRuntime().exec("pkill pad");
   }

   @Test
   public void testPort() {
      this.port = new AbstractPort();
      this.listener.start();

      Map<String, Object> cfg = new HashMap<String, Object>();
//      cfg.put(AbstractPort.PORT_ID, "/dev/cu.usbserial-0000103D");
      cfg.put(AbstractPort.PORT_ID, "COM6");
      cfg.put(AbstractPort.PORT_TYPE, "serial");
      cfg.put(AbstractPort.PORT_SPEED, "19200");
      cfg.put(AbstractPort.PORT_NB_BITS, "8");
      cfg.put(AbstractPort.PORT_PARITY, "even");
      try {
         this.port.configure(cfg);
         this.port.start();
         for (int i = 0; i < 3; ++i) {
            synchronized (this.lock) {
               this.message = null;
               this.port.send(new Message(new byte[] { 0x10, 0x40, 0x40, 0x16 }));
               this.lock.wait(3000);
               Assert.assertNotNull(this.message);
               Assert.assertEquals(1, this.message.getContent().length);
               Assert.assertEquals((byte) 0xE5, this.message.getContent()[0]);
               this.message = null;
            }
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

   private class PortListener extends Thread {
      @Override
      public void run() {
         while (!this.isInterrupted()) {
            try {
               Message m = AbstractPortTest.this.port.receive();
               synchronized (AbstractPortTest.this.lock) {
                  AbstractPortTest.this.message = m;
                  AbstractPortTest.this.lock.notify();
               }
            } catch (IOException e) {
               Assert.fail(e.getMessage());
            }
         }
      }
   }
}
