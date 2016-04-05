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
 * Unit tests for {@link Esp2PacketHeader} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp2PacketHeaderTest
{
  // Instance Fields ------------------------------------------------------------------------------

  private byte[] headerBytes;


  // Test Lifecycle -------------------------------------------------------------------------------

  @Before public void setUp() throws Exception
  {
    headerBytes = new byte[]
    {
        (byte)0xA5, (byte)0x5A, (byte)((0x01 << 5) + 11)
    };
  }


  // Tests ----------------------------------------------------------------------------------------

  @Test public void testBasicConstruction1() throws Exception
  {
    Esp2PacketHeader header = new Esp2PacketHeader(headerBytes);

    Assert.assertTrue(header.getLength() == 11);
    Assert.assertTrue(header.getPacketType() == Esp2PacketHeader.PacketType.RRT_KNOWN);
  }

  @Test public void testBasicConstruction2() throws Exception
  {
    int length = 11;
    Esp2PacketHeader.PacketType packetType = Esp2PacketHeader.PacketType.TCT;

    Esp2PacketHeader header = new Esp2PacketHeader(packetType, length);

    Assert.assertTrue(length == header.getLength());
    Assert.assertTrue(packetType == header.getPacketType());
  }

  @Test public void testLength() throws Exception
  {
    int length = 0x0F;

    byte[] headerData = headerAsByteArray(length, Esp2PacketHeader.PacketType.TCT);

    Esp2PacketHeader header = new Esp2PacketHeader(headerData);

    Assert.assertTrue(header.getLength() == length);
  }

  @Test public void testPacketSize() throws Exception
  {
    int length = 11;

    byte[] headerData = headerAsByteArray(length, Esp2PacketHeader.PacketType.TCT);

    Esp2PacketHeader header = new Esp2PacketHeader(headerData);

    Assert.assertTrue(14 == header.getPacketSize());
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNullArg() throws Exception
  {
    Esp2PacketHeader header = new Esp2PacketHeader(null);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testLengthOutOfRange() throws Exception
  {
    Esp2PacketHeader header = new Esp2PacketHeader(
        Esp2PacketHeader.PacketType.TRT, 0x2F
    );
  }

  @Test (expected = IllegalArgumentException.class)
  public void testInvalidSyncByte1() throws Exception
  {
    byte[] headerData = headerAsByteArray(11, Esp2PacketHeader.PacketType.RCT);

    headerData[0] += 1;

    Esp2PacketHeader header = new Esp2PacketHeader(headerData);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testInvalidSyncByte2() throws Exception
  {
    byte[] headerData = headerAsByteArray(11, Esp2PacketHeader.PacketType.RCT);

    headerData[1] += 1;

    Esp2PacketHeader header = new Esp2PacketHeader(headerData);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testHeaderDataTooShort() throws Exception
  {
    byte[] headerData = headerAsByteArray(11, Esp2PacketHeader.PacketType.RCT);

    byte[] shortHeaderData = Arrays.copyOfRange(headerData, 0, headerData.length - 1 );

    Esp2PacketHeader header = new Esp2PacketHeader(shortHeaderData);
  }

  @Test (expected = Esp2PacketHeader.UnknownPacketTypeException.class)
  public void testUnknownPacketTypeException() throws Exception
  {
    byte[] headerData = headerAsByteArray(11, Esp2PacketHeader.PacketType.RCT);

    headerData[2] = (byte)(headerData[2] | 0xF0);

    Esp2PacketHeader header = new Esp2PacketHeader(headerData);
  }


  // Helpers --------------------------------------------------------------------------------------

  private byte[] headerAsByteArray(int length, Esp2PacketHeader.PacketType packetType)
  {
    byte[] header = new byte[3];

    header[0] = (byte)0xA5;
    header[1] = (byte)0x5A;
    header[2] = (byte)((packetType.getValue() << 5) + (length & 0x1F));

    return header;
  }
}
