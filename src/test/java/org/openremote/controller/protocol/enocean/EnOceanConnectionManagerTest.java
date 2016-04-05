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
package org.openremote.controller.protocol.enocean;

import org.junit.Assert;
import org.junit.Test;
import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram;
import org.openremote.controller.protocol.enocean.port.EspPortConfiguration;

/**
 * Unit tests for {@link EnOceanConnectionManager} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EnOceanConnectionManagerTest
{

  // Tests ----------------------------------------------------------------------------------------

  @Test public void testGetConnection() throws Exception
  {
    TestConnectionManager mgr = new TestConnectionManager();

    EspPortConfiguration config = new EspPortConfiguration();
    config.setComPort("COM1");
    config.setCommLayer(EspPortConfiguration.CommLayer.PAD);
    config.setSerialProtocol(EspPortConfiguration.SerialProtocol.ESP3);

    RadioTelegramListener listener = new TestListener();

    EnOceanConnection conn1 = mgr.getConnection(config, listener);
    EnOceanConnection conn2 = mgr.getConnection(config, listener);

    Assert.assertNotNull(conn1);
    Assert.assertNotNull(conn2);
    Assert.assertTrue(conn1 instanceof TestEsp3Connection);
    Assert.assertEquals(conn1, conn2);
    Assert.assertEquals(1, ((TestEsp3Connection) conn1).connectCount);


    mgr = new TestConnectionManager();

    config = new EspPortConfiguration();
    config.setComPort("COM2");
    config.setCommLayer(EspPortConfiguration.CommLayer.PAD);
    config.setSerialProtocol(EspPortConfiguration.SerialProtocol.ESP2);

    EnOceanConnection conn3 = mgr.getConnection(config, listener);

    Assert.assertNotNull(conn3);
    Assert.assertTrue(conn3 instanceof TestEsp2Connection);
    Assert.assertEquals(1, ((TestEsp2Connection) conn3).connectCount);
  }

  @Test public void testDisconnect() throws Exception
  {
    TestConnectionManager mgr = new TestConnectionManager();

    EspPortConfiguration config = new EspPortConfiguration();
    config.setComPort("COM1");
    config.setCommLayer(EspPortConfiguration.CommLayer.PAD);
    config.setSerialProtocol(EspPortConfiguration.SerialProtocol.ESP3);

    RadioTelegramListener listener = new TestListener();

    EnOceanConnection conn1 = mgr.getConnection(config, listener);

    mgr.disconnect();

    Assert.assertEquals(1, ((TestEsp3Connection) conn1).disconnectCount);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNullConfiguration() throws Exception
  {
    RadioTelegramListener listener = new TestListener();
    EnOceanConnectionManager mgr = new EnOceanConnectionManager();

    EnOceanConnection conn1 = mgr.getConnection(null, listener);
  }

  @Test (expected = ConfigurationException.class)
  public void testMissingComPort() throws Exception
  {
    RadioTelegramListener listener = new TestListener();
    EnOceanConnectionManager mgr = new EnOceanConnectionManager();


    EspPortConfiguration config = new EspPortConfiguration();

    EnOceanConnection conn1 = mgr.getConnection(config, listener);
  }


  // Inner Classes --------------------------------------------------------------------------------

  private static class TestConnectionManager extends EnOceanConnectionManager
  {
    @Override protected EnOceanConnection createEsp3Connection(EspPortConfiguration configuration, RadioTelegramListener listener)
        throws ConfigurationException, ConnectionException
    {
      return new TestEsp3Connection();
    }

    @Override protected EnOceanConnection createEsp2Connection(EspPortConfiguration configuration, RadioTelegramListener listener)
        throws ConfigurationException, ConnectionException
    {
      return new TestEsp2Connection();
    }
  }

  static class TestEsp3Connection implements EnOceanConnection
  {
    int connectCount;
    int disconnectCount;

    @Override public void connect() throws ConnectionException, ConfigurationException
    {
      connectCount++;
    }

    @Override public void disconnect() throws ConnectionException
    {
      disconnectCount++;
    }

    @Override public void sendRadio(EspRadioTelegram.RORG rorg, DeviceID deviceID, byte[] payload, byte statusByte)
        throws ConnectionException, ConfigurationException
    {

    }
  }

  private static class TestEsp2Connection implements EnOceanConnection
  {
    int connectCount;
    int disconnectCount;

    @Override public void connect() throws ConnectionException, ConfigurationException
    {
      connectCount++;
    }

    @Override public void disconnect() throws ConnectionException
    {
      disconnectCount++;
    }

    @Override public void sendRadio(EspRadioTelegram.RORG rorg, DeviceID deviceID, byte[] payload, byte statusByte)
        throws ConnectionException, ConfigurationException
    {

    }
  }

  private static class TestListener implements RadioTelegramListener
  {
    @Override public void radioTelegramReceived(EspRadioTelegram telegram)
    {

    }
  }
}
