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
 * Unit tests for {@link Esp3Packet} class.
 *
 * @author Rainer Hitz
 */
public class Esp3PacketTest
{

  // Tests ----------------------------------------------------------------------------------------

  @Test public void testBasicConstruction() throws Exception
  {
    Esp3PacketHeader.PacketType packetType = Esp3PacketHeader.PacketType.RADIO;

    byte[] data = new byte[] {0x01, 0x02};
    byte[] optData = new byte[] {0x03};

    byte[] expectedByteArray = createPacketAsByteArray(packetType, data, optData);

    Esp3Packet packet = new Esp3Packet(packetType, data, optData);

    Assert.assertEquals(packetType, packet.getPacketType());
    Assert.assertArrayEquals(expectedByteArray, packet.asByteArray());
  }

  @Test public void testNullData() throws Exception
  {
    Esp3PacketHeader.PacketType packetType = Esp3PacketHeader.PacketType.RADIO;

    byte[] data = null;
    byte[] optData = null;

    byte[] expectedByteArray = createPacketAsByteArray(
        packetType, new byte[]{}, new byte[]{}
    );

    Esp3Packet packet = new Esp3Packet(packetType, data, data);

    Assert.assertArrayEquals(expectedByteArray, packet.asByteArray());
  }

  // Helpers --------------------------------------------------------------------------------------

  private byte[] createPacketAsByteArray(Esp3PacketHeader.PacketType packetType,
                                         byte[] data, byte[] optData)
  {
    Esp3PacketHeader header = new Esp3PacketHeader(
        packetType, data.length, optData.length
    );

    int packetSize = Esp3PacketHeader.ESP3_HEADER_SIZE + data.length + optData.length + 1;
    byte[] byteArray = new byte[packetSize];

    System.arraycopy(
        header.asByteArray(), 0, byteArray, 0, Esp3PacketHeader.ESP3_HEADER_SIZE
    );
    System.arraycopy(
        data, 0, byteArray, Esp3PacketHeader.ESP3_HEADER_SIZE, data.length
    );
    System.arraycopy(
        optData, 0, byteArray,
        Esp3PacketHeader.ESP3_HEADER_SIZE + data.length, optData.length
    );

    byteArray[packetSize - 1] = Esp3Packet.CRC8.calculate(
        byteArray, Esp3PacketHeader.ESP3_HEADER_SIZE, data.length + optData.length
    );

    return byteArray;
  }
}
