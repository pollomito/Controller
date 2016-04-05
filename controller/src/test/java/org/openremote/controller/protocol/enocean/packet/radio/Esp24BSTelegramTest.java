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
import org.openremote.controller.protocol.enocean.packet.Esp2Packet;
import org.openremote.controller.protocol.enocean.packet.Esp2PacketHeader;
import org.openremote.controller.protocol.enocean.packet.Esp2ResponsePacket;
import org.openremote.controller.protocol.enocean.packet.EspProcessor;


/**
 * Unit tests for {@link Esp24BSTelegram} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp24BSTelegramTest
{

  // Private Instance Fields ----------------------------------------------------------------------

  private DeviceID senderID;
  private byte[] payload;
  private byte status;


  // Test Lifecycle -------------------------------------------------------------------------------

  @Before public void setUp() throws Exception
  {
    senderID = DeviceID.fromString("0xFF800001");
    payload = new byte[] {0x01, 0x02, 0x03, 0x04};
    status = 0x11;
  }

  // Tests ----------------------------------------------------------------------------------------


  @Test public void testBasicConstruction() throws Exception
  {
    Esp24BSTelegram telegram = new Esp24BSTelegram(senderID, payload, status);

    assertTelegramAttributes(telegram, Esp2PacketHeader.PacketType.TRT, senderID, payload, status);


    telegram = new Esp24BSTelegram(Esp2PacketHeader.PacketType.RRT_UNKNOWN, telegram.getData());

    assertTelegramAttributes(telegram, Esp2PacketHeader.PacketType.RRT_UNKNOWN, senderID, payload, status);
  }

  @Test public void testSenderID() throws Exception
  {
    DeviceID senderID = DeviceID.fromString("0x44332211");

    Esp24BSTelegram telegram = new Esp24BSTelegram(senderID, payload, status);

    assertTelegramAttributes(telegram, Esp2PacketHeader.PacketType.TRT, senderID, payload, status);


    telegram = new Esp24BSTelegram(Esp2PacketHeader.PacketType.RRT_UNKNOWN, telegram.getData());

    assertTelegramAttributes(telegram, Esp2PacketHeader.PacketType.RRT_UNKNOWN, senderID, payload, status);



    senderID = DeviceID.fromString("0xFFFFFFFF");

    telegram = new Esp24BSTelegram(senderID, payload, status);

    assertTelegramAttributes(telegram, Esp2PacketHeader.PacketType.TRT, senderID, payload, status);


    telegram = new Esp24BSTelegram(Esp2PacketHeader.PacketType.RRT_UNKNOWN, telegram.getData());

    assertTelegramAttributes(telegram, Esp2PacketHeader.PacketType.RRT_UNKNOWN, senderID, payload, status);



    senderID = DeviceID.fromString("0x00000000");

    telegram = new Esp24BSTelegram(senderID, payload, status);

    assertTelegramAttributes(telegram, Esp2PacketHeader.PacketType.TRT, senderID, payload, status);


    telegram = new Esp24BSTelegram(Esp2PacketHeader.PacketType.RRT_UNKNOWN, telegram.getData());

    assertTelegramAttributes(telegram, Esp2PacketHeader.PacketType.RRT_UNKNOWN, senderID, payload, status);
  }

  @Test public void testPayload() throws Exception
  {
    payload = new byte[] {0x01, 0x02, 0x03, 0x04};
    Esp24BSTelegram telegram = new Esp24BSTelegram(senderID, payload, status);

    assertTelegramAttributes(telegram, Esp2PacketHeader.PacketType.TRT, senderID, payload, status);


    payload = new byte[] {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
    telegram = new Esp24BSTelegram(senderID, payload, status);

    assertTelegramAttributes(telegram, Esp2PacketHeader.PacketType.TRT, senderID, payload, status);


    payload = new byte[] {0x00, 0x00, 0x00, 0x00};
    telegram = new Esp24BSTelegram(senderID, payload, status);

    assertTelegramAttributes(telegram, Esp2PacketHeader.PacketType.TRT, senderID, payload, status);
  }

  @Test public void testStatus() throws Exception
  {
    status = 0x12;
    Esp24BSTelegram telegram = new Esp24BSTelegram(senderID, payload, status);

    assertTelegramAttributes(telegram, Esp2PacketHeader.PacketType.TRT, senderID, payload, status);


    status = (byte)0xFF;
    telegram = new Esp24BSTelegram(senderID, payload, status);

    assertTelegramAttributes(telegram, Esp2PacketHeader.PacketType.TRT, senderID, payload, status);


    status = (byte)0x00;
    telegram = new Esp24BSTelegram(senderID, payload, status);

    assertTelegramAttributes(telegram, Esp2PacketHeader.PacketType.TRT, senderID, payload, status);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNullArg1() throws Exception
  {
    Esp24BSTelegram telegram = new Esp24BSTelegram(null, payload, status);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNullArg2() throws Exception
  {
    byte[] data = null;
    Esp24BSTelegram telegram = new Esp24BSTelegram(Esp2PacketHeader.PacketType.RRT_UNKNOWN, data);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testInvalidRORG() throws Exception
  {
    Esp24BSTelegram telegram = new Esp24BSTelegram(senderID, payload, status);

    byte[] dataBytes = telegram.getData();

    dataBytes[0] = AbstractEsp2RadioTelegram.RORG.RPS_ESP2.getValue();

    telegram = new Esp24BSTelegram(Esp2PacketHeader.PacketType.RRT_UNKNOWN, dataBytes);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testInvalidDataGroupLength() throws Exception
  {
    Esp24BSTelegram telegram = new Esp24BSTelegram(senderID, payload, status);

    byte[] dataBytes = telegram.getData();
    byte[] dataBytesTooLong = new byte[dataBytes.length + 1];

    System.arraycopy(
        dataBytes, 0, dataBytesTooLong, 0, dataBytes.length
    );

    telegram = new Esp24BSTelegram(Esp2PacketHeader.PacketType.RRT_UNKNOWN, dataBytesTooLong);
  }

  @Test public void testSend() throws Exception
  {
    Esp2ResponsePacket.ReturnCode returnCode = Esp2ResponsePacket.ReturnCode.OK;
    Esp2ResponsePacket response = createResponse(returnCode);

    TestProcessor processor = new TestProcessor(response);

    Esp24BSTelegram telegram = new Esp24BSTelegram(senderID, payload, status);

    telegram.send(processor);

    Assert.assertEquals(returnCode, telegram.getReturnCode());
  }

  @Test public void testRepeatedSend() throws Exception
  {
    Esp2ResponsePacket.ReturnCode returnCode = Esp2ResponsePacket.ReturnCode.OK;
    Esp2ResponsePacket response = createResponse(returnCode);

    TestProcessor processor = new TestProcessor(response);

    Esp24BSTelegram telegram = new Esp24BSTelegram(senderID, payload, status);

    telegram.send(processor);


    processor = new TestProcessor(null);
    telegram.send(processor);

    Assert.assertEquals(Esp2ResponsePacket.ReturnCode.RET_CODE_NOT_SET, telegram.getReturnCode());


    processor = new TestProcessor(response);
    telegram.send(processor);

    Assert.assertEquals(returnCode, telegram.getReturnCode());
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSendNullArg() throws Exception
  {
    Esp24BSTelegram telegram = new Esp24BSTelegram(senderID, payload, status);

    telegram.send(null);
  }


  // Helpers --------------------------------------------------------------------------------------

  private void assertTelegramAttributes(Esp24BSTelegram telegram, Esp2PacketHeader.PacketType type,
                                        DeviceID senderID, byte[] payload, byte status)
  {
    Assert.assertEquals(type, telegram.getPacketType());
    Assert.assertEquals(AbstractEsp2RadioTelegram.RORG.BS4_ESP2, telegram.getRORG());
    Assert.assertEquals(senderID, telegram.getSenderID());

    Assert.assertArrayEquals(createDataGroup(senderID, payload, status), telegram.getData());
    Assert.assertArrayEquals(
        createTelegramAsByteArray(type, senderID, payload, status), telegram.asByteArray()
    );
  }

  private byte[] createTelegramAsByteArray(Esp2PacketHeader.PacketType type, DeviceID deviceID, byte[] payload, byte status)
  {
    byte[] packetBytes = new byte[14];

    // Header ...

    Esp2PacketHeader header = new Esp2PacketHeader(type, 11);

    int copyIndex = 0;

    System.arraycopy(
        header.asByteArray(), 0, packetBytes, copyIndex,
        Esp2PacketHeader.ESP2_HEADER_SIZE
    );

    // Data group ...

    copyIndex += Esp2PacketHeader.ESP2_HEADER_SIZE;

    System.arraycopy(
        createDataGroup(deviceID, payload, status), 0, packetBytes,
        copyIndex, 10
    );


    // Checksum ...

    int checksum = 0;

    for(int index = 2; index < 13; index++)
    {
      checksum += packetBytes[index];
    }

    packetBytes[13] = (byte)checksum;

    return packetBytes;
  }

  private byte[] createDataGroup(DeviceID deviceID, byte[] payload, byte status)
  {
    byte[] dataGroupBytes = new byte[10];

    // ORG...

    dataGroupBytes[0] = AbstractEsp3RadioTelegram.RORG.BS4_ESP2.getValue();

    // Payload...

    System.arraycopy(payload, 0, dataGroupBytes, 1, 4);

    // Sender ID...

    System.arraycopy(
        deviceID.asByteArray(), 0, dataGroupBytes,
        5, DeviceID.ENOCEAN_ESP_ID_LENGTH
    );

    // Status...

    dataGroupBytes[dataGroupBytes.length - 1] = status;

    return dataGroupBytes;
  }

  private Esp2ResponsePacket createResponse(Esp2ResponsePacket.ReturnCode returnCode)
  {
    byte[] responseDataBytes = new byte[10];
    responseDataBytes[0] = returnCode.getValue();

    Esp2ResponsePacket response = null;

    try
    {
      response = new Esp2ResponsePacket(responseDataBytes);
    }
    catch (EspException e)
    {

    }

    return response;
  }

  // Inner Classes --------------------------------------------------------------------------------

  private static class TestProcessor implements EspProcessor<Esp2Packet>
  {
    private Esp2Packet responsePacket;

    public TestProcessor(Esp2Packet responsePacket)
    {
      this.responsePacket = responsePacket;
    }

    @Override public void start() throws ConfigurationException, ConnectionException
    {

    }

    @Override public void stop() throws ConnectionException
    {

    }

    @Override public Esp2Packet sendRequest(Esp2Packet packet) throws ConnectionException, InterruptedException
    {
      return responsePacket;
    }

    @Override public void sendResponse(Esp2Packet packet) throws ConnectionException
    {

    }
  }
}
