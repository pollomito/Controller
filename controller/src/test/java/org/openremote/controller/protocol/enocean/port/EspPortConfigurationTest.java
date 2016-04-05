/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2012, OpenRemote Inc.
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
package org.openremote.controller.protocol.enocean.port;

import org.junit.Test;
import org.junit.Assert;


/**
 * Unit tests for {@link EspPortConfiguration} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EspPortConfigurationTest
{
  @Test public void testEquals1() throws Exception
  {
    EspPortConfiguration c1 = new EspPortConfiguration();
    c1.setCommLayer(EspPortConfiguration.CommLayer.PAD);
    c1.setComPort("/dev/ttyUSB0");
    c1.setSerialProtocol(EspPortConfiguration.SerialProtocol.ESP3);

    EspPortConfiguration c2 = new EspPortConfiguration();
    c2.setCommLayer(EspPortConfiguration.CommLayer.PAD);
    c2.setComPort("/dev/ttyUSB0");
    c2.setSerialProtocol(EspPortConfiguration.SerialProtocol.ESP3);

    EspPortConfiguration c3 = new EspPortConfiguration();
    c3.setCommLayer(EspPortConfiguration.CommLayer.RXTX);
    c3.setComPort("/dev/ttyUSB0");
    c3.setSerialProtocol(EspPortConfiguration.SerialProtocol.ESP3);

    EspPortConfiguration c4 = new EspPortConfiguration();
    c4.setCommLayer(EspPortConfiguration.CommLayer.PAD);
    c4.setComPort("/dev/ttyUSB1");
    c4.setSerialProtocol(EspPortConfiguration.SerialProtocol.ESP3);

    EspPortConfiguration c5 = new EspPortConfiguration();
    c5.setCommLayer(EspPortConfiguration.CommLayer.PAD);
    c5.setComPort("/dev/ttyUSB0");
    c5.setSerialProtocol(EspPortConfiguration.SerialProtocol.ESP2);

    Assert.assertTrue(c1.equals(c2));
    Assert.assertTrue(c2.equals(c1));

    Assert.assertFalse(c1.equals(c3));
    Assert.assertFalse(c3.equals(c1));

    Assert.assertFalse(c1.equals(c4));
    Assert.assertFalse(c1.equals(c5));

    Assert.assertFalse(c1.equals(null));
    Assert.assertFalse(c1.equals(new Object()));
  }

  @Test public void testEquals2() throws Exception
  {
    EspPortConfiguration c1 = new EspPortConfiguration();
    EspPortConfiguration c2 = new EspPortConfiguration();

    EspPortConfiguration c3 = new EspPortConfiguration();
    c3.setCommLayer(EspPortConfiguration.CommLayer.PAD);
    c3.setComPort("/dev/ttyUSB0");
    c3.setSerialProtocol(EspPortConfiguration.SerialProtocol.ESP3);

    Assert.assertTrue(c1.equals(c2));

    Assert.assertFalse(c3.equals(c1));
    Assert.assertFalse(c1.equals(c3));
  }

  @Test public void testHash() throws Exception
  {
    EspPortConfiguration c1 = new EspPortConfiguration();
    c1.setCommLayer(EspPortConfiguration.CommLayer.PAD);
    c1.setComPort("/dev/ttyUSB0");
    c1.setSerialProtocol(EspPortConfiguration.SerialProtocol.ESP3);

    EspPortConfiguration c2 = new EspPortConfiguration();
    c2.setCommLayer(EspPortConfiguration.CommLayer.PAD);
    c2.setComPort("/dev/ttyUSB0");
    c2.setSerialProtocol(EspPortConfiguration.SerialProtocol.ESP3);

    Assert.assertTrue(c1.equals(c1));

    Assert.assertTrue(c1.hashCode() == c2.hashCode());
    Assert.assertTrue(c1.equals(c2));


    EspPortConfiguration c3 = new EspPortConfiguration();

    Assert.assertTrue(c3.hashCode() == c3.hashCode());
  }
}
