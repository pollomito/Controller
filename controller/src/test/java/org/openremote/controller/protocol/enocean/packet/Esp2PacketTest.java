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

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link Esp2Packet} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp2PacketTest
{

  // Tests ----------------------------------------------------------------------------------------

  @Test public void testBasicConstruction() throws Exception
  {
    Esp2PacketHeader.PacketType packetType = Esp2PacketHeader.PacketType.TRT;

    byte[] data = new byte[] {0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, (byte)0xFF};

    byte[] expectedByteArray = createPacketAsByteArray(packetType, data);

    Esp2Packet packet = new Esp2Packet(packetType, data);

    Assert.assertEquals(packetType, packet.getPacketType());
    Assert.assertArrayEquals(expectedByteArray, packet.asByteArray());
  }

  @Test public void testNullData() throws Exception
  {
    Esp2PacketHeader.PacketType packetType = Esp2PacketHeader.PacketType.TCT;

    byte[] expectedByteArray = createPacketAsByteArray(
        packetType, new byte[10]
    );

    byte[] data = null;

    Esp2Packet packet = new Esp2Packet(packetType, data);

    Assert.assertArrayEquals(expectedByteArray, packet.asByteArray());
  }


  // Helpers --------------------------------------------------------------------------------------

  private byte[] createPacketAsByteArray(Esp2PacketHeader.PacketType packetType, byte[] data)
  {
    Esp2PacketHeader header = new Esp2PacketHeader(packetType, data.length + 1);

    int packetSize = Esp2PacketHeader.ESP2_HEADER_SIZE + 11;
    byte[] byteArray = new byte[packetSize];

    System.arraycopy(
        header.asByteArray(), 0, byteArray, 0, Esp2PacketHeader.ESP2_HEADER_SIZE
    );

    System.arraycopy(
        data, 0, byteArray, Esp2PacketHeader.ESP2_HEADER_SIZE, data.length
    );

    byteArray[packetSize - 1] = checksum(byteArray, 2, packetSize - 3);

    return byteArray;
  }

  private byte checksum(byte[] data, int pos, int length)
  {
    int checksum = 0;

    for(int index = pos; index < (pos + length) ; index++)
    {
      checksum += (data[index] & 0xFF);
    }

    return (byte)checksum;
  }
}
