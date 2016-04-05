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
package org.openremote.controller.protocol.enocean.packet.radio;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openremote.controller.protocol.enocean.ConfigurationException;
import org.openremote.controller.protocol.enocean.ConnectionException;
import org.openremote.controller.protocol.enocean.DeviceID;
import org.openremote.controller.protocol.enocean.EspException;
import org.openremote.controller.protocol.enocean.packet.Esp3Packet;
import org.openremote.controller.protocol.enocean.packet.Esp3PacketHeader;
import org.openremote.controller.protocol.enocean.packet.Esp3ResponsePacket;
import org.openremote.controller.protocol.enocean.packet.EspProcessor;

/**
 * Unit tests for {@link Esp31BSTelegram} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp31BSTelegramTest
{
  private DeviceID senderID;
  private byte payload;
  private byte status;

  // Test Lifecycle -------------------------------------------------------------------------------

  @Before public void setUp() throws Exception
  {
    senderID = DeviceID.fromString("0xFF800001");
    payload = 0x22;
    status = 0x11;
  }


  // Tests ----------------------------------------------------------------------------------------


  @Test public void testBasicConstruction() throws Exception
  {
    Esp31BSTelegram telegram = new Esp31BSTelegram(senderID, payload, status);

    assertTelegramAttributes(telegram, senderID, payload, status);


    telegram = new Esp31BSTelegram(telegram.getData(), telegram.getOptionalData());

    assertTelegramAttributes(telegram, senderID, payload, status);
  }

  @Test public void testSenderID() throws Exception
  {
    DeviceID senderID = DeviceID.fromString("0x44332211");

    Esp31BSTelegram telegram = new Esp31BSTelegram(senderID, payload, status);

    assertTelegramAttributes(telegram, senderID, payload, status);


    telegram = new Esp31BSTelegram(telegram.getData(), telegram.getOptionalData());

    assertTelegramAttributes(telegram, senderID, payload, status);



    senderID = DeviceID.fromString("0xFFFFFFFF");

    telegram = new Esp31BSTelegram(senderID, payload, status);

    assertTelegramAttributes(telegram, senderID, payload, status);


    telegram = new Esp31BSTelegram(telegram.getData(), telegram.getOptionalData());

    assertTelegramAttributes(telegram, senderID, payload, status);



    senderID = DeviceID.fromString("0x00000000");

    telegram = new Esp31BSTelegram(senderID, payload, status);

    assertTelegramAttributes(telegram, senderID, payload, status);


    telegram = new Esp31BSTelegram(telegram.getData(), telegram.getOptionalData());

    assertTelegramAttributes(telegram, senderID, payload, status);
  }

  @Test public void testPayload() throws Exception
  {
    payload = 0x21;
    Esp31BSTelegram telegram = new Esp31BSTelegram(senderID, payload, status);

    assertTelegramAttributes(telegram, senderID, payload, status);


    payload = (byte)0xFF;
    telegram = new Esp31BSTelegram(senderID, payload, status);

    assertTelegramAttributes(telegram, senderID, payload, status);


    payload = (byte)0x00;
    telegram = new Esp31BSTelegram(senderID, payload, status);

    assertTelegramAttributes(telegram, senderID, payload, status);
  }

  @Test public void testStatus() throws Exception
  {
    status = 0x12;
    Esp31BSTelegram telegram = new Esp31BSTelegram(senderID, payload, status);

    assertTelegramAttributes(telegram, senderID, payload, status);


    status = (byte)0xFF;
    telegram = new Esp31BSTelegram(senderID, payload, status);

    assertTelegramAttributes(telegram, senderID, payload, status);


    status = (byte)0x00;
    telegram = new Esp31BSTelegram(senderID, payload, status);

    assertTelegramAttributes(telegram, senderID, payload, status);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNullArg1() throws Exception
  {
    Esp31BSTelegram telegram = new Esp31BSTelegram(null, payload, status);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNullArg2() throws Exception
  {
    Esp31BSTelegram telegram = new Esp31BSTelegram(senderID, payload, status);

    telegram = new Esp31BSTelegram(null, telegram.getOptionalData());
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNullArg3() throws Exception
  {
    Esp31BSTelegram telegram = new Esp31BSTelegram(senderID, payload, status);

    telegram = new Esp31BSTelegram(telegram.getData(), null);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testInvalidRORG() throws Exception
  {
    Esp31BSTelegram telegram = new Esp31BSTelegram(senderID, payload, status);

    byte[] dataBytes = telegram.getData();

    dataBytes[AbstractEsp3RadioTelegram.ESP3_RADIO_RORG_INDEX] =
        AbstractEsp3RadioTelegram.RORG.RPS.getValue();

    telegram = new Esp31BSTelegram(dataBytes, telegram.getOptionalData());
  }

  @Test (expected = IllegalArgumentException.class)
  public void testInvalidDataGroupLength() throws Exception
  {
    Esp31BSTelegram telegram = new Esp31BSTelegram(senderID, payload, status);

    byte[] dataBytes = telegram.getData();
    byte[] dataBytesTooLong = new byte[dataBytes.length + 1];

    System.arraycopy(
        dataBytes, 0, dataBytesTooLong, 0, dataBytes.length
    );

    telegram = new Esp31BSTelegram(dataBytesTooLong, telegram.getOptionalData());
  }

  @Test (expected = IllegalArgumentException.class)
  public void testInvalidOptDataGroupLength() throws Exception
  {
    Esp31BSTelegram telegram = new Esp31BSTelegram(senderID, payload, status);

    byte[] optDataBytes = telegram.getOptionalData();
    byte[] optDataBytesTooLong = new byte[optDataBytes.length + 1];

    System.arraycopy(
        optDataBytes, 0, optDataBytesTooLong, 0, optDataBytes.length
    );

    telegram = new Esp31BSTelegram(telegram.getData(), optDataBytesTooLong);
  }

  @Test public void testSend() throws Exception
  {
    Esp3ResponsePacket.ReturnCode returnCode = Esp3ResponsePacket.ReturnCode.RET_OK;
    Esp3ResponsePacket response = createResponse(returnCode);

    TestProcessor processor = new TestProcessor(response);

    Esp31BSTelegram telegram = new Esp31BSTelegram(senderID, payload, status);

    telegram.send(processor);

    Assert.assertEquals(returnCode, telegram.getReturnCode());
  }

  @Test public void testRepeatedSend() throws Exception
  {
    Esp3ResponsePacket.ReturnCode returnCode = Esp3ResponsePacket.ReturnCode.RET_OK;
    Esp3ResponsePacket response = createResponse(returnCode);

    TestProcessor processor = new TestProcessor(response);

    Esp31BSTelegram telegram = new Esp31BSTelegram(senderID, payload, status);

    telegram.send(processor);


    processor = new TestProcessor(null);
    telegram.send(processor);

    Assert.assertEquals(Esp3ResponsePacket.ReturnCode.RET_CODE_NOT_SET, telegram.getReturnCode());


    processor = new TestProcessor(response);
    telegram.send(processor);

    Assert.assertEquals(returnCode, telegram.getReturnCode());
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSendNullArg() throws Exception
  {
    Esp31BSTelegram telegram = new Esp31BSTelegram(senderID, payload, status);

    telegram.send(null);
  }


  // Helpers --------------------------------------------------------------------------------------

  private void assertTelegramAttributes(Esp31BSTelegram telegram, DeviceID senderID,
                                        byte payload, byte status)
  {
    Assert.assertEquals(Esp3PacketHeader.PacketType.RADIO, telegram.getPacketType());
    Assert.assertEquals(AbstractEsp3RadioTelegram.RORG.BS1, telegram.getRORG());
    Assert.assertEquals(senderID, telegram.getSenderID());

    Assert.assertArrayEquals(createDataGroup(senderID, payload, status), telegram.getData());
    Assert.assertArrayEquals(
        Esp3RadioTelegramOptData.ESP3_RADIO_OPT_DATA_TX.asByteArray(),
        telegram.getOptionalData()
    );
    Assert.assertArrayEquals(
        createTelegramAsByteArray(senderID, payload, status), telegram.asByteArray()
    );
  }

  private byte[] createTelegramAsByteArray(DeviceID deviceID, byte payload, byte status)
  {
    int dataGroupLength = 0x07;

    int length = Esp3PacketHeader.ESP3_HEADER_SIZE + dataGroupLength +
        Esp3RadioTelegramOptData.ESP3_RADIO_OPT_DATA_LENGTH + 1;


    byte[] packetBytes = new byte[length];

    // Header ...

    Esp3PacketHeader header = new Esp3PacketHeader(
        Esp3PacketHeader.PacketType.RADIO, dataGroupLength,
        Esp3RadioTelegramOptData.ESP3_RADIO_OPT_DATA_LENGTH
    );

    int copyIndex = 0;

    System.arraycopy(
        header.asByteArray(), 0, packetBytes, copyIndex,
        Esp3PacketHeader.ESP3_HEADER_SIZE
    );

    // Data group ...

    copyIndex += Esp3PacketHeader.ESP3_HEADER_SIZE;

    System.arraycopy(
        createDataGroup(deviceID, payload, status), 0, packetBytes,
        copyIndex, dataGroupLength
    );

    // Optional data group ...

    copyIndex += dataGroupLength;

    System.arraycopy(
        Esp3RadioTelegramOptData.ESP3_RADIO_OPT_DATA_TX.asByteArray(), 0, packetBytes,
        copyIndex, Esp3RadioTelegramOptData.ESP3_RADIO_OPT_DATA_LENGTH
    );

    // CRC8D ...

    packetBytes[packetBytes.length - 1] = Esp3Packet.CRC8.calculate(
        packetBytes, Esp3PacketHeader.ESP3_HEADER_SIZE,
        packetBytes.length - Esp3PacketHeader.ESP3_HEADER_SIZE - 1
    );

    return packetBytes;
  }

  private byte[] createDataGroup(DeviceID deviceID, byte payload, byte status)
  {
    int dataGroupLength = 0x07;

    byte[] dataGroupBytes = new byte[dataGroupLength];

    // RORG...

    dataGroupBytes[0] = AbstractEsp3RadioTelegram.RORG.BS1.getValue();

    // Payload...

    dataGroupBytes[1] = payload;

    // Sender ID...

    System.arraycopy(
        deviceID.asByteArray(), 0, dataGroupBytes,
        2, DeviceID.ENOCEAN_ESP_ID_LENGTH
    );

    // Status...

    dataGroupBytes[dataGroupBytes.length - 1] = status;

    return dataGroupBytes;
  }

  private Esp3ResponsePacket createResponse(Esp3ResponsePacket.ReturnCode returnCode)
  {
    byte[] responseDataBytes = new byte[] {returnCode.getValue()};
    Esp3ResponsePacket response = null;

    try
    {
      response = new Esp3ResponsePacket(responseDataBytes, new byte[]{});
    }
    catch (EspException e)
    {

    }

    return response;
  }

  // Inner Classes --------------------------------------------------------------------------------

  private static class TestProcessor implements EspProcessor<Esp3Packet>
  {
    private Esp3Packet responsePacket;

    public TestProcessor(Esp3Packet responsePacket)
    {
      this.responsePacket = responsePacket;
    }

    @Override public void start() throws ConfigurationException, ConnectionException
    {

    }

    @Override public void stop() throws ConnectionException
    {

    }

    @Override public Esp3Packet sendRequest(Esp3Packet packet) throws ConnectionException, InterruptedException
    {
      return responsePacket;
    }

    @Override public void sendResponse(Esp3Packet packet) throws ConnectionException
    {

    }
  }
}
