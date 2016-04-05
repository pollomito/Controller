package org.openremote.controller.protocol.port;

import junit.framework.Assert;

import org.junit.Test;
import org.openremote.controller.protocol.port.Port;
import org.openremote.controller.protocol.port.PortFactory;

public class PortFactoryTest {
   @Test
   public void testCreatePhysicalBus() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
      Port b = PortFactory
            .createPhysicalBus("org.openremote.controller.protocol.port.DatagramSocketPort");
      Assert.assertTrue(b instanceof Port);
   }
}
