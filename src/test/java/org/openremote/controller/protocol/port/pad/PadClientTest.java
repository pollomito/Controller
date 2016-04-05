package org.openremote.controller.protocol.port.pad;

import java.io.IOException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openremote.controller.protocol.port.PortException;

public class PadClientTest {
   @Before
   public void setUp() throws IOException, InterruptedException {
      // Runtime.getRuntime().exec("pad/src/main/c/pad");
      // synchronized (this) {
      // this.wait(1000);
      // }
   }

   @After
   public void tearDown() throws InterruptedException, IOException {
      // Runtime.getRuntime().exec("pkill pad");
   }

   @Test
   public void testClient() {
      PadClient c = PadClient.instance();
      try {
         c.service(new PingMessage());
 //        c.service(new ShutdownMessage());
      } catch (IOException e) {
         Assert.fail(e.getMessage());
      } catch (PortException e) {
         e.printStackTrace();
         Assert.fail(e.getMessage());
      }
//      try {
//         c.service(new PingMessage());
//         Assert.fail("Ping should have failed");
//      } catch (IOException e) {
//         Assert.fail(e.getMessage());
//      } catch (PortException e) {
//         if(e.getCode() != PortException.SERVICE_TIMEOUT) {
//            Assert.fail(e.getMessage());
//         }
//      }
   }
}
