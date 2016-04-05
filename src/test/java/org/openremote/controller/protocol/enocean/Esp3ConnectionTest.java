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
import org.junit.Before;
import org.junit.Test;
import org.openremote.controller.protocol.enocean.packet.Esp3Packet;
import org.openremote.controller.protocol.enocean.packet.Esp3Processor;
import org.openremote.controller.protocol.enocean.packet.Esp3ResponsePacket;
import org.openremote.controller.protocol.enocean.packet.EspProcessor;
import org.openremote.controller.protocol.enocean.packet.command.Esp3RdIDBaseResponse;
import org.openremote.controller.protocol.enocean.packet.radio.Esp31BSTelegram;
import org.openremote.controller.protocol.enocean.packet.radio.Esp3RadioTelegramOptData;
import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram;
import org.openremote.controller.protocol.enocean.port.Esp3ComPortAdapter;
import org.openremote.controller.protocol.enocean.port.EspPortConfiguration;
import org.openremote.controller.protocol.enocean.port.MockPort;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Unit tests for {@link EnOceanGateway} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp3ConnectionTest
{

  // Private Instance Fields ----------------------------------------------------------------------

  private MockEsp3Processor mockProcessor;
  private MockRadioTelegramListener mockListener;
  private DeviceID baseID;


  // Test Lifecycle -------------------------------------------------------------------------------

  @Before public void setUp() throws Exception
  {
    mockProcessor = new MockEsp3Processor();
    mockListener = new MockRadioTelegramListener();
    baseID = DeviceID.fromString("0xFF800000");
  }


  // Tests ----------------------------------------------------------------------------------------

  @Test public void testConnectDisconnect() throws Exception
  {
    mockProcessor.addExpectedMethodCall(MockEsp3Processor.Method.START);
    mockProcessor.addExpectedMethodCall(MockEsp3Processor.Method.SEND_REQUEST);
    mockProcessor.addExpectedMethodCall(MockEsp3Processor.Method.STOP);

    Esp3RdIDBaseResponse response = createRdIDBaseResponse(
        Esp3ResponsePacket.ReturnCode.RET_OK, baseID
    );

    mockProcessor.addPacketToReturn(response);


    Esp3Connection conn = new Esp3Connection(mockProcessor, mockListener);
    conn.connect();
    conn.disconnect();

    mockProcessor.verifyMethodCalls();
  }

  @Test (expected = ConnectionException.class)
  public void testReadBaseIDFailed() throws Exception
  {
    EspPortConfiguration config = new EspPortConfiguration();
    config.setCommLayer(EspPortConfiguration.CommLayer.PAD);
    config.setComPort("COM1");
    config.setSerialProtocol(EspPortConfiguration.SerialProtocol.ESP3);

    Esp3ComPortAdapter portAdapter = new Esp3ComPortAdapter(new MockPort(), config);
    Esp3Processor processor = new Esp3Processor(portAdapter);

    Esp3Connection conn = new Esp3Connection(processor, mockListener);

    conn.connect();
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNullArg() throws Exception
  {
    Esp3Connection conn = new Esp3Connection(null, mockListener);
  }

  @Test public void testSendRadio() throws Exception
  {
    Esp3Connection conn = new Esp3Connection(mockProcessor, mockListener);

    Esp3ResponsePacket response = createRdIDBaseResponse(
        Esp3ResponsePacket.ReturnCode.RET_OK, baseID
    );
    mockProcessor.addPacketToReturn(response);

    conn.connect();


    DeviceID deviceID = DeviceID.fromString("0x01");
    EspRadioTelegram.RORG rorg = EspRadioTelegram.RORG.BS1;
    byte[] payload = new byte[] {0x02};
    byte status = 0x03;
    response = createRadioTelegramResponse(Esp3ResponsePacket.ReturnCode.RET_OK);

    mockProcessor.addPacketToReturn(response);
    mockProcessor.addExpectedRadioTelegram(deviceID.resolve(baseID), rorg, payload, status);

    conn.sendRadio(rorg, deviceID, payload, status);


    deviceID = DeviceID.fromString("0x02");
    rorg = EspRadioTelegram.RORG.BS1_ESP2;
    payload = new byte[] {0x02};
    status = 0x03;
    response = createRadioTelegramResponse(Esp3ResponsePacket.ReturnCode.RET_OK);

    mockProcessor.addPacketToReturn(response);
    mockProcessor.addExpectedRadioTelegram(deviceID.resolve(baseID), EspRadioTelegram.RORG.BS1, payload, status);

    conn.sendRadio(rorg, deviceID, payload, status);


    deviceID = DeviceID.fromString("0x03");
    rorg = EspRadioTelegram.RORG.RPS;
    payload = new byte[] {0x03};
    status = 0x04;
    response = createRadioTelegramResponse(Esp3ResponsePacket.ReturnCode.RET_OK);

    mockProcessor.addPacketToReturn(response);
    mockProcessor.addExpectedRadioTelegram(deviceID.resolve(baseID), rorg, payload, status);

    conn.sendRadio(rorg, deviceID, payload, status);


    deviceID = DeviceID.fromString("0x04");
    rorg = EspRadioTelegram.RORG.RPS_ESP2;
    payload = new byte[] {0x04};
    status = 0x05;
    response = createRadioTelegramResponse(Esp3ResponsePacket.ReturnCode.RET_OK);

    mockProcessor.addPacketToReturn(response);
    mockProcessor.addExpectedRadioTelegram(deviceID.resolve(baseID), EspRadioTelegram.RORG.RPS, payload, status);

    conn.sendRadio(rorg, deviceID, payload, status);


    deviceID = DeviceID.fromString("0x05");
    rorg = EspRadioTelegram.RORG.BS4;
    payload = new byte[] {0x01, 0x02, 0x03, 0x04};
    status = 0x04;
    response = createRadioTelegramResponse(Esp3ResponsePacket.ReturnCode.RET_OK);

    mockProcessor.addPacketToReturn(response);
    mockProcessor.addExpectedRadioTelegram(deviceID.resolve(baseID), rorg, payload, status);

    conn.sendRadio(rorg, deviceID, payload, status);


    deviceID = DeviceID.fromString("0x06");
    rorg = EspRadioTelegram.RORG.BS4_ESP2;
    payload = new byte[] {0x01, 0x02, 0x03, 0x04};
    status = 0x05;
    response = createRadioTelegramResponse(Esp3ResponsePacket.ReturnCode.RET_OK);

    mockProcessor.addPacketToReturn(response);
    mockProcessor.addExpectedRadioTelegram(deviceID.resolve(baseID), EspRadioTelegram.RORG.BS4, payload, status);

    conn.sendRadio(rorg, deviceID, payload, status);


    mockProcessor.verifyData();
  }

  @Test (expected = ConnectionException.class)
  public void testMissingConnectCall() throws Exception
  {
    Esp3Connection conn = new Esp3Connection(mockProcessor, mockListener);

    conn.sendRadio(EspRadioTelegram.RORG.BS1, DeviceID.fromString("0x01"), new byte[] {0x01}, (byte)0x01);
  }

  @Test public void testRadioTelegramListener() throws Exception
  {
    Esp3Connection conn = new Esp3Connection(mockProcessor, mockListener);

    Esp31BSTelegram telegram = new Esp31BSTelegram(DeviceID.fromString("0xFF800000"), (byte)0x00, (byte)0x00);


    Assert.assertNull(mockListener.receivedTelegram);

    conn.radioTelegramReceived(telegram);

    Assert.assertEquals(telegram.getSenderID(), mockListener.receivedTelegram.getSenderID());
    Assert.assertEquals(telegram.getRORG(), mockListener.receivedTelegram.getRORG());
    Assert.assertArrayEquals(telegram.getPayload(), mockListener.receivedTelegram.getPayload());
    Assert.assertEquals(telegram.getStatusByte(), mockListener.receivedTelegram.getStatusByte());
  }

// Helpers --------------------------------------------------------------------------------------


  private Esp3RdIDBaseResponse createRdIDBaseResponse(Esp3ResponsePacket.ReturnCode returnCode, DeviceID baseID)
      throws EspException
  {
    byte[] dataBytes = new byte[Esp3RdIDBaseResponse.ESP3_RESPONSE_RD_IDBASE_DATA_LENGTH];

    dataBytes[Esp3ResponsePacket.ESP3_RESPONSE_RETURN_CODE_INDEX] = returnCode.getValue();

    System.arraycopy(
        baseID.asByteArray(), 0, dataBytes,
        Esp3ResponsePacket.ESP3_RESPONSE_RETURN_CODE_INDEX + 1,
        DeviceID.ENOCEAN_ESP_ID_LENGTH
    );

    return new Esp3RdIDBaseResponse(dataBytes);
  }

  private Esp3ResponsePacket createRadioTelegramResponse(Esp3ResponsePacket.ReturnCode returnCode)
      throws EspException
  {
    return new Esp3ResponsePacket(
        new byte[] {returnCode.getValue()},
        Esp3RadioTelegramOptData.ESP3_RADIO_OPT_DATA_TX.asByteArray()
    );
  }


  // Inner Classes --------------------------------------------------------------------------------

  private static class MockEsp3Processor implements EspProcessor<Esp3Packet>
  {

    // Enums --------------------------------------------------------------------------------------

    public enum Method
    {
      START,
      STOP,
      SEND_REQUEST,
      SEND_RESPONSE
    }


    // Private Instance Fields --------------------------------------------------------------------

    private List<Method> expectedMethodCalls = new ArrayList<Method>();
    private List<Method> actualMethodCalls = new ArrayList<Method>();

    private List<RadioTelegram> expectedRadioTelegrams = new ArrayList<RadioTelegram>();
    private List<EspRadioTelegram> actualRadioTelegrams = new ArrayList<EspRadioTelegram>();

    private LinkedList<Esp3ResponsePacket> packetsToReturn = new LinkedList<Esp3ResponsePacket>();


    // Public Instance Methods --------------------------------------------------------------------

    public void addExpectedMethodCall(Method method)
    {
      expectedMethodCalls.add(method);
    }

    public void addExpectedRadioTelegram(DeviceID senderID, EspRadioTelegram.RORG rorg, byte[] payload, byte status)
    {
      RadioTelegram telegram = new RadioTelegram();
      telegram.senderID = senderID;
      telegram.rorg = rorg;
      telegram.payload = payload;
      telegram.status = status;

      expectedRadioTelegrams.add(telegram);
    }

    public void addPacketToReturn(Esp3ResponsePacket packet)
    {
      packetsToReturn.addLast(packet);
    }

    public void verifyMethodCalls()
    {
      Assert.assertEquals(expectedMethodCalls, actualMethodCalls);
    }

    public void verifyData()
    {
      Assert.assertEquals(expectedRadioTelegrams.size(), actualRadioTelegrams.size());

      for(int i = 0; i < expectedRadioTelegrams.size(); i++)
      {
        RadioTelegram expected = expectedRadioTelegrams.get(i);
        EspRadioTelegram actual = actualRadioTelegrams.get(i);

        Assert.assertEquals(expected.senderID, actual.getSenderID());
        Assert.assertEquals(expected.rorg, actual.getRORG());
        Assert.assertArrayEquals(expected.payload, actual.getPayload());
        Assert.assertEquals(expected.status, actual.getStatusByte());
      }
    }

    // Implements EspProcessor --------------------------------------------------------------------

    @Override public void start() throws ConfigurationException, ConnectionException
    {
      actualMethodCalls.add(Method.START);
    }

    @Override public void stop() throws ConnectionException
    {
      actualMethodCalls.add(Method.STOP);
    }

    @Override public Esp3Packet sendRequest(Esp3Packet packet) throws ConnectionException, InterruptedException
    {
      if(packet instanceof EspRadioTelegram)
      {
        actualRadioTelegrams.add((EspRadioTelegram)packet);
      }

      actualMethodCalls.add(Method.SEND_REQUEST);

      return packetsToReturn.removeFirst();
    }

    @Override public void sendResponse(Esp3Packet packet) throws ConnectionException
    {
      actualMethodCalls.add(Method.SEND_RESPONSE);
    }
  }


  private static class MockRadioTelegramListener implements RadioTelegramListener
  {
    EspRadioTelegram receivedTelegram;

    @Override public void radioTelegramReceived(EspRadioTelegram telegram)
    {
      receivedTelegram = telegram;
    }
  }

  private static class RadioTelegram
  {
    DeviceID senderID;
    EspRadioTelegram.RORG rorg;
    byte[] payload;
    byte status;
  }
}
