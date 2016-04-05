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
package org.openremote.controller.protocol.enocean.packet;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.util.Arrays;

/**
 * Unit tests for {@link Esp3PacketHeader} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp3PacketHeaderTest
{
  // Instance Fields ------------------------------------------------------------------------------

  /**
   * ESP3 read base ID command (see EnOcean Serial Protocol 3.0 V1.17 Specification
   * chapter 2.2.4: CO_RD_IDBASE).
   */
  private byte[] readBaseIDCommand;


  // Test Lifecycle -------------------------------------------------------------------------------

  @Before public void setUp() throws Exception
  {
    readBaseIDCommand = new byte[]
    {
        (byte)0x55, (byte)0x00, (byte)0x01, (byte)0x00,
        (byte)0x05, (byte)0x70, (byte)0x08, (byte)0x38
    };
  }


  // Tests ----------------------------------------------------------------------------------------

  @Test public void testBasicConstruction1() throws Exception
  {
    Esp3PacketHeader header = new Esp3PacketHeader(readBaseIDCommand);

    Assert.assertTrue(header.getDataLength() == 1);
    Assert.assertTrue(header.getOptionalDataLength() == 0);
    Assert.assertTrue(header.getPacketType() == Esp3PacketHeader.PacketType.COMMON_COMMAND);
  }

  @Test public void testBasicConstruction2() throws Exception
  {
    int dataLength = 0x07;
    int optDataLength = 0x10;
    Esp3PacketHeader.PacketType packetType = Esp3PacketHeader.PacketType.RADIO;

    Esp3PacketHeader header = new Esp3PacketHeader(
        packetType, dataLength, optDataLength
    );

    Assert.assertTrue(dataLength == header.getDataLength());
    Assert.assertTrue(optDataLength == header.getOptionalDataLength());
    Assert.assertTrue(header.getPacketType() == packetType);
  }


  @Test public void testDataLength() throws Exception
  {
    int dataLength = 0x1FF;

    byte[] headerData = headerAsByteArray(
        dataLength, 1, Esp3PacketHeader.PacketType.RADIO
    );

    Esp3PacketHeader header = new Esp3PacketHeader(headerData);

    Assert.assertTrue(header.getDataLength() == dataLength);
  }


  @Test public void testOptionalDataLength() throws Exception
  {
    int optionalDataLength = 0xFF;

    byte[] headerData = headerAsByteArray(
        1, optionalDataLength, Esp3PacketHeader.PacketType.RADIO
    );

    Esp3PacketHeader header = new Esp3PacketHeader(headerData);

    Assert.assertTrue(header.getOptionalDataLength() == optionalDataLength);
  }


  @Test (expected = IllegalArgumentException.class)
  public void testNullArg() throws Exception
  {
    Esp3PacketHeader header = new Esp3PacketHeader(null);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testDataLengthOutOfRange() throws Exception
  {
    Esp3PacketHeader header = new Esp3PacketHeader(
        Esp3PacketHeader.PacketType.RADIO,
        0x1FFFF,
        0x00
    );
  }

  @Test (expected = IllegalArgumentException.class)
  public void testOptDataLengthOutOfRange() throws Exception
  {
    Esp3PacketHeader header = new Esp3PacketHeader(
        Esp3PacketHeader.PacketType.RADIO,
        0x00,
        0x1FF
    );
  }

  @Test (expected = IllegalArgumentException.class)
  public void testInvalidSyncByte() throws Exception
  {
    byte[] headerData = headerAsByteArray(
        1, 0 , Esp3PacketHeader.PacketType.COMMON_COMMAND
    );

    byte syncByte = Esp3PacketHeader.ESP3_SYNC_BYTE + 1;

    headerData[Esp3PacketHeader.ESP3_HEADER_SYNC_BYTE_INDEX] = syncByte;

    Esp3PacketHeader header = new Esp3PacketHeader(headerData);
  }


  @Test (expected = IllegalArgumentException.class)
  public void testHeaderDataTooShort() throws Exception
  {
    byte[] headerData = headerAsByteArray(
        1, 0, Esp3PacketHeader.PacketType.COMMON_COMMAND
    );

    byte[] shortHeaderData = Arrays.copyOfRange(headerData, 0, headerData.length - 1 );

    Esp3PacketHeader header = new Esp3PacketHeader(shortHeaderData);
  }


  @Test (expected = Esp3PacketHeader.CRC8Exception.class)
  public void testCRC8Exception() throws Exception
  {
    byte[] headerData = headerAsByteArray(
        1, 0, Esp3PacketHeader.PacketType.COMMON_COMMAND
    );

    headerData[Esp3PacketHeader.ESP3_HEADER_CRC8_INDEX] = 0;

    Esp3PacketHeader header = new Esp3PacketHeader(headerData);
  }


  @Test (expected = Esp3PacketHeader.UnknownPacketTypeException.class)
  public void testUnknownPacketTypeException() throws Exception
  {
    byte[] headerData = headerAsByteArray(
        1, 0, Esp3PacketHeader.PacketType.COMMON_COMMAND
    );

    headerData[Esp3PacketHeader.ESP3_HEADER_PACKET_TYPE_INDEX] = (byte)0xFF;

    Esp3PacketHeader header = new Esp3PacketHeader(headerData);
  }


// Helpers --------------------------------------------------------------------------------------

  private byte[] headerAsByteArray(int dataLength, int optionalDataLength,
        Esp3PacketHeader.PacketType packetType)
  {
    byte[] header = new byte[Esp3PacketHeader.ESP3_HEADER_SIZE];

    header[Esp3PacketHeader.ESP3_HEADER_SYNC_BYTE_INDEX] =
        Esp3PacketHeader.ESP3_SYNC_BYTE;

    header[Esp3PacketHeader.ESP3_HEADER_DATA_LENGTH_HIBYTE_INDEX] =
        (byte)((dataLength >> 8) & 0xFF);

    header[Esp3PacketHeader.ESP3_HEADER_DATA_LENGTH_LOBYTE_INDEX] =
        (byte)(dataLength & 0xFF);

    header[Esp3PacketHeader.ESP3_HEADER_OPTIONAL_DATA_LENGTH_INDEX] =
        (byte)optionalDataLength;

    header[Esp3PacketHeader.ESP3_HEADER_PACKET_TYPE_INDEX] =
        packetType.getValue();

    int offset = Esp3PacketHeader.ESP3_HEADER_DATA_LENGTH_HIBYTE_INDEX;
    int length = Esp3PacketHeader.ESP3_HEADER_PACKET_TYPE_INDEX -
                 Esp3PacketHeader.ESP3_HEADER_DATA_LENGTH_HIBYTE_INDEX + 1;

    header[Esp3PacketHeader.ESP3_HEADER_CRC8_INDEX] =
        Esp3Packet.CRC8.calculate(header, offset, length);

    return header;
  }
}
