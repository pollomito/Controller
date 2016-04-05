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

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;
import org.openremote.controller.protocol.enocean.ConfigurationException;
import org.openremote.controller.protocol.enocean.ConnectionException;
import org.openremote.controller.protocol.port.PortException;

import java.io.IOException;
import java.util.Arrays;

/**
 * Unit tests for {@link Esp2ComPortAdapter} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp2ComPortAdapterTest
{

  // Class Members --------------------------------------------------------------------------------

  private static final String COM_PORT = "/dev/cu.usbserial-FTUOKF2Q";


  // Instance Fields ------------------------------------------------------------------------------

  private EspPortConfiguration portConfig;
  private MockPort mockPort;


  // Test Lifecycle -------------------------------------------------------------------------------

  @Before public void setUp()
  {
    portConfig = new EspPortConfiguration();
    portConfig.setComPort(COM_PORT);
    portConfig.setCommLayer(EspPortConfiguration.CommLayer.PAD);
    portConfig.setSerialProtocol(EspPortConfiguration.SerialProtocol.ESP2);

    mockPort = new MockPort();
  }


  // Tests ----------------------------------------------------------------------------------------

  @Test public void testConstructor1() throws Exception
  {
    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(mockPort, portConfig);

    Assert.assertFalse(portAdapter.isStarted());

    mockPort.verifyData();
    mockPort.verifyMethodCalls();
  }

  @Test public void testConstructor2() throws Exception
  {
    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(portConfig);

    Assert.assertFalse(portAdapter.isStarted());
  }

  @Test (expected = IllegalArgumentException.class)
  public void testConstructorNullArg1() throws Exception
  {
    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(null, portConfig);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testConstructorNullArg2() throws Exception
  {
    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(mockPort, null);
  }


  @Test public void testStart() throws Exception
  {
    mockPort.addExpectedMethodCall(MockPort.Method.CONFIGURE);
    mockPort.addExpectedMethodCall(MockPort.Method.START);

    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(mockPort, portConfig);

    portAdapter.start();

    Assert.assertTrue(portAdapter.isStarted());

    mockPort.verifyMethodCalls();
  }


  @Test public void testStop() throws Exception
  {
    mockPort.addExpectedMethodCall(MockPort.Method.CONFIGURE);
    mockPort.addExpectedMethodCall(MockPort.Method.START);
    mockPort.addExpectedMethodCall(MockPort.Method.STOP);

    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(mockPort, portConfig);

    portAdapter.start();
    portAdapter.stop();

    Assert.assertFalse(portAdapter.isStarted());

    mockPort.verifyMethodCalls();
  }


  @Test public void testStartAfterStop() throws Exception
  {
    mockPort.addExpectedMethodCall(MockPort.Method.CONFIGURE);
    mockPort.addExpectedMethodCall(MockPort.Method.START);
    mockPort.addExpectedMethodCall(MockPort.Method.STOP);
    mockPort.addExpectedMethodCall(MockPort.Method.START);

    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(mockPort, portConfig);

    portAdapter.start();
    portAdapter.stop();
    portAdapter.start();

    Assert.assertTrue(portAdapter.isStarted());

    mockPort.verifyMethodCalls();
  }


  @Test public void testSend() throws Exception
  {
    byte[] packet1 = new byte[] {0x00, 0x01};
    byte[] packet2 = new byte[] {0x02, 0x03};
    byte[] packet3 = new byte[] {(byte)0xFF};

    mockPort.addExpectedDataToSend(packet1);
    mockPort.addExpectedDataToSend(packet2);
    mockPort.addExpectedDataToSend(packet3);

    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(mockPort, portConfig);

    portAdapter.start();

    portAdapter.send(packet1);
    portAdapter.send(packet2);
    portAdapter.send(packet3);

    mockPort.verifyData();
  }

  @Test (expected = ConnectionException.class)
  public void testSendWithoutStart() throws Exception
  {
    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(mockPort, portConfig);

    byte[] packet = new byte[] {0x00};

    portAdapter.send(packet);
  }


  @Test public void testReceive() throws Exception
  {
    byte[] packet1 = new byte[] {0x00, 0x01};
    byte[] packet2 = new byte[] {(byte)0xEF, (byte)0xFF};

    mockPort.addDataToReturn(packet1);
    mockPort.addDataToReturn(packet2);

    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(mockPort, portConfig);
    portAdapter.start();

    byte[] receivedPacket = portAdapter.receive();

    Assert.assertTrue(Arrays.equals(receivedPacket, packet1));

    receivedPacket = portAdapter.receive();

    Assert.assertTrue(Arrays.equals(receivedPacket, packet2));
  }

  @Test (expected = ConnectionException.class)
  public void testReceiveWithoutStart() throws Exception
  {
    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(mockPort, portConfig);

    byte[] receivedPacket = portAdapter.receive();
  }


  @Test (expected = ConnectionException.class)
  public void testConfigureIOException() throws ConnectionException, ConfigurationException
  {
    mockPort.setupThrowExceptionOnConfigure(new IOException());

    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(mockPort, portConfig);

    portAdapter.start();
  }

  @Test (expected = ConnectionException.class)
  public void testConfigurePortException1() throws ConnectionException, ConfigurationException
  {
    mockPort.setupThrowExceptionOnConfigure(new PortException(PortException.SERVICE_TIMEOUT));

    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(mockPort, portConfig);

    portAdapter.start();
  }

  @Test (expected = ConfigurationException.class)
  public void testConfigurePortException2() throws ConnectionException, ConfigurationException
  {
    mockPort.setupThrowExceptionOnConfigure(new PortException(PortException.INVALID_CONFIGURATION));

    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(mockPort, portConfig);

    portAdapter.start();
  }


  @Test (expected = ConnectionException.class)
  public void testStartIOException() throws ConnectionException, ConfigurationException
  {
    mockPort.setupThrowExceptionOnStart(new IOException());

    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(mockPort, portConfig);

    portAdapter.start();
  }

  @Test (expected = ConnectionException.class)
  public void testStartPortException() throws ConnectionException, ConfigurationException
  {
    mockPort.setupThrowExceptionOnStart(new PortException(PortException.SERVICE_TIMEOUT));

    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(mockPort, portConfig);

    portAdapter.start();
  }


  @Test (expected = ConnectionException.class)
  public void testStopIOException() throws ConnectionException, ConfigurationException
  {
    mockPort.setupThrowExceptionOnStop(new IOException());

    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(mockPort, portConfig);
    portAdapter.start();

    portAdapter.stop();
  }

  @Test (expected = ConnectionException.class)
  public void testStopPortException() throws ConnectionException, ConfigurationException
  {
    mockPort.setupThrowExceptionOnStop(new PortException(PortException.SERVICE_TIMEOUT));

    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(mockPort, portConfig);
    portAdapter.start();

    portAdapter.stop();
  }


  @Test (expected = ConnectionException.class)
  public void testSendIOException() throws ConnectionException, ConfigurationException
  {
    mockPort.setupThrowExceptionOnSend(new IOException());

    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(mockPort, portConfig);
    portAdapter.start();

    portAdapter.send(new byte[] {0x00});
  }

  @Test (expected = ConnectionException.class)
  public void testSendPortException() throws ConnectionException, ConfigurationException
  {
    mockPort.setupThrowExceptionOnSend(new PortException(PortException.SERVICE_TIMEOUT));

    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(mockPort, portConfig);
    portAdapter.start();

    portAdapter.send(new byte[] {0x00});
  }


  @Test (expected = ConnectionException.class)
  public void testReceiveIOException() throws ConnectionException, ConfigurationException
  {
    mockPort.setupThrowExceptionOnReceive(new IOException());

    mockPort.addDataToReturn(new byte[] {0x00});

    Esp2ComPortAdapter portAdapter = new Esp2ComPortAdapter(mockPort, portConfig);
    portAdapter.start();

    portAdapter.receive();
  }

}
