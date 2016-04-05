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
import org.openremote.controller.protocol.enocean.packet.radio.Esp3RPSTelegram;
import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram;
import org.openremote.controller.protocol.enocean.port.EspPortConfiguration;

/**
 * Unit tests for {@link EnOceanGateway} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EnOceanGatewayTest
{

  // Tests ----------------------------------------------------------------------------------------

  @Test public void testRadioListener() throws Exception
  {
    DeviceID id1 = DeviceID.fromString("0xFF800001");
    DeviceID id2 = DeviceID.fromString("0xFF800002");

    TestRadioListener listener1 = new TestRadioListener();
    TestRadioListener listener2_1 = new TestRadioListener();
    TestRadioListener listener2_2 = new TestRadioListener();

    Esp3RPSTelegram telegram1 = new Esp3RPSTelegram(id1, (byte)0x00, (byte)0x00);
    Esp3RPSTelegram telegram2 = new Esp3RPSTelegram(id2, (byte)0x00, (byte)0x00);

    EnOceanGateway gateway = new EnOceanGateway(new EnOceanConnectionManager(), new EspPortConfiguration());

    gateway.addRadioListener(id1, listener1);
    gateway.addRadioListener(id2, listener2_1);
    gateway.addRadioListener(id2, listener2_2);

    gateway.radioTelegramReceived(telegram1);

    Assert.assertEquals(id1, listener1.receivedTelegram.getSenderID());
    Assert.assertEquals(1, listener1.receiveCount);
    Assert.assertNull(listener2_1.receivedTelegram);
    Assert.assertNull(listener2_2.receivedTelegram);

    gateway.radioTelegramReceived(telegram2);
    gateway.radioTelegramReceived(telegram2);

    Assert.assertEquals(id2, listener2_1.receivedTelegram.getSenderID());
    Assert.assertEquals(2, listener2_1.receiveCount);
    Assert.assertEquals(id2, listener2_2.receivedTelegram.getSenderID());
    Assert.assertEquals(2, listener2_2.receiveCount);
  }

  @Test public void testRegisterRadioListenerTwice() throws Exception
  {
    DeviceID id = DeviceID.fromString("0xFF800001");
    TestRadioListener listener = new TestRadioListener();
    Esp3RPSTelegram telegram = new Esp3RPSTelegram(id, (byte)0x00, (byte)0x00);

    EnOceanGateway gateway = new EnOceanGateway(new EnOceanConnectionManager(), new EspPortConfiguration());

    gateway.addRadioListener(id, listener);
    gateway.addRadioListener(id, listener);

    gateway.radioTelegramReceived(telegram);

    Assert.assertEquals(id, listener.receivedTelegram.getSenderID());
    Assert.assertEquals(1, listener.receiveCount);
  }

  @Test public void testRemoveRadioListener() throws Exception
  {
    DeviceID id = DeviceID.fromString("0xFF800001");

    TestRadioListener listener1 = new TestRadioListener();
    TestRadioListener listener2 = new TestRadioListener();

    Esp3RPSTelegram telegram = new Esp3RPSTelegram(id, (byte)0x00, (byte)0x00);

    EnOceanGateway gateway = new EnOceanGateway(new EnOceanConnectionManager(), new EspPortConfiguration());


    gateway.addRadioListener(id, listener1);
    gateway.addRadioListener(id, listener2);
    gateway.removeRadioListener(id, listener1);
    gateway.radioTelegramReceived(telegram);

    Assert.assertNull(listener1.receivedTelegram);
    Assert.assertEquals(id, listener2.receivedTelegram.getSenderID());
    Assert.assertEquals(1, listener2.receiveCount);


    gateway.addRadioListener(id, listener1);
    gateway.radioTelegramReceived(telegram);

    Assert.assertEquals(id, listener1.receivedTelegram.getSenderID());
    Assert.assertEquals(1, listener1.receiveCount);
    Assert.assertEquals(id, listener2.receivedTelegram.getSenderID());
    Assert.assertEquals(2, listener2.receiveCount);


    gateway.removeRadioListener(id, listener1);
    gateway.removeRadioListener(id, listener2);
    gateway.radioTelegramReceived(telegram);

    Assert.assertEquals(id, listener1.receivedTelegram.getSenderID());
    Assert.assertEquals(1, listener1.receiveCount);
    Assert.assertEquals(id, listener2.receivedTelegram.getSenderID());
    Assert.assertEquals(2, listener2.receiveCount);


    gateway.addRadioListener(id, listener1);
    gateway.addRadioListener(id, listener2);
    gateway.radioTelegramReceived(telegram);

    Assert.assertEquals(id, listener1.receivedTelegram.getSenderID());
    Assert.assertEquals(2, listener1.receiveCount);
    Assert.assertEquals(id, listener2.receivedTelegram.getSenderID());
    Assert.assertEquals(3, listener2.receiveCount);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNullArg1() throws Exception
  {
    EnOceanGateway gateway = new EnOceanGateway(null, new EspPortConfiguration());
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNullArg2() throws Exception
  {
    EnOceanGateway gateway = new EnOceanGateway(new EnOceanConnectionManager(), null);
  }

  @Test public void testConnect() throws Exception
  {
    TestConnectionManager mgr = new TestConnectionManager();
    EnOceanGateway gateway = new EnOceanGateway(mgr, new EspPortConfiguration());

    gateway.connect();

    Assert.assertEquals(1, mgr.getConnectionCallCount);
  }

  @Test public void testSend() throws Exception
  {
    TestConnectionManager mgr = new TestConnectionManager();
    EnOceanGateway gateway = new EnOceanGateway(mgr, new EspPortConfiguration());

    gateway.connect();

    gateway.sendRadio(
        EspRadioTelegram.RORG.BS1, DeviceID.fromString("0xFF800001"), new byte[] {0x01}, (byte)0x00
    );

    Assert.assertEquals(2, mgr.getConnectionCallCount);
    Assert.assertEquals(1, mgr.connection.sendCallCount);
  }

  // Inner Classes --------------------------------------------------------------------------------

  private static class TestRadioListener implements RadioTelegramListener
  {
    EspRadioTelegram receivedTelegram;
    int receiveCount;

    @Override public void radioTelegramReceived(EspRadioTelegram telegram)
    {
      this.receivedTelegram = telegram;
      ++receiveCount;
    }
  }

  private static class TestConnectionManager extends EnOceanConnectionManager
  {
    int getConnectionCallCount;
    TestConnection connection = new TestConnection();

    @Override public EnOceanConnection getConnection(EspPortConfiguration configuration, RadioTelegramListener listener)
        throws ConfigurationException, ConnectionException
    {
      ++getConnectionCallCount;

      return connection;
    }
  }

  private static class TestConnection implements EnOceanConnection
  {
    int sendCallCount;

    @Override public void connect() throws ConnectionException, ConfigurationException
    {

    }

    @Override public void disconnect() throws ConnectionException
    {

    }

    @Override public void sendRadio(EspRadioTelegram.RORG rorg, DeviceID deviceID, byte[] payload, byte statusByte) throws ConnectionException, ConfigurationException
    {
      ++sendCallCount;
    }
  }

  @Test (expected = ConfigurationException.class)
  public void testMissingComPort() throws ConfigurationException
  {
    EspPortConfiguration config = new EspPortConfiguration();
    config.setSerialProtocol(EspPortConfiguration.SerialProtocol.ESP3);
    config.setCommLayer(EspPortConfiguration.CommLayer.RXTX);

    EnOceanGateway gateway1 = new EnOceanGateway(new EnOceanConnectionManager(), config);

    try {
      gateway1.connect();
    }
    catch (ConnectionException e)
    {
    }
  }

  @Test (expected = ConfigurationException.class)
  public void testMissingSerialProtocol() throws ConfigurationException
  {
    EspPortConfiguration config = new EspPortConfiguration();
    config.setComPort("/dev/cu.usbserial-FTUOKF2Q");
    config.setCommLayer(EspPortConfiguration.CommLayer.RXTX);

    EnOceanGateway gateway1 = new EnOceanGateway(new EnOceanConnectionManager(), config);

    try {
      gateway1.connect();
    }
    catch (ConnectionException e)
    {
    }
  }

  @Test (expected = ConfigurationException.class)
  public void testMissingCommLayerProtocol() throws ConfigurationException
  {
    EspPortConfiguration config = new EspPortConfiguration();
    config.setComPort("/dev/cu.usbserial-FTUOKF2Q");
    config.setSerialProtocol(EspPortConfiguration.SerialProtocol.ESP3);

    EnOceanGateway gateway1 = new EnOceanGateway(new EnOceanConnectionManager(), config);

    try {
      gateway1.connect();
    }
    catch (ConnectionException e)
    {
    }
  }
}
