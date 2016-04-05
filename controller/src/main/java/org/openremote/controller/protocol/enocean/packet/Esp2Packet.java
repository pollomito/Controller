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

/**
 * Represents an EnOcean Serial Protocol 2 (ESP2) packet. <p>
 *
 * The EnOcean Serial Protocol 2.0 is defined follows: <p>
 *
 * <pre>
 *   |-----------   Header  ----------|
 *   +---------+---------+------------+------...------+--------+
 *   |Sync Byte|Sync Byte|H_SEQ|LENGTH|      Data     |CHECKSUM|
 *   |  (0xA5) |  (0x5A) |     |      |               |        |
 *   +---------+---------+------------+------...------+--------+
 *     1 byte     1 byte     1 byte       10 bytes      1 byte
 *</pre>
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp2Packet implements EspPacket
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * The fixed size of a ESP2 packet data field : {@value}
   *
   * Note: it seems that all ESP2 packets have the same fixed size.
   */
  public static final byte ESP2_PACKET_DATA_LENGTH = 10;

  /**
   * Byte order index of the ESP2 packet data field.
   */
  public static final int ESP2_PACKET_DATA_INDEX = 3;

  /**
   * The fixed size of a ESP2 checksum data field : {@value}
   */
  public static final byte ESP2_PACKET_CHECKSUM_LENGTH = 1;

  /**
   * Byte order index of the ESP2 checksum data field.
   */
  public static final int ESP2_PACKET_CHECKSUM_INDEX = 13;

  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Packet header.
   */
  private Esp2PacketHeader header;

  /**
   * Data group.
   */
  protected byte[] data;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new ESP2 packet instance with a given packet type and packet data.
   *
   * @see Esp3PacketHeader.PacketType
   *
   * @param packetType  ESP2 packet type
   *
   * @param data        data group as byte array
   */
  public Esp2Packet(Esp2PacketHeader.PacketType packetType, byte[] data)
  {
    if(packetType == null)
    {
      throw new IllegalArgumentException("null packet type");
    }

    this.data = (data == null) ? new byte[ESP2_PACKET_DATA_LENGTH] : data;

    this.header = new Esp2PacketHeader(packetType, this.data.length + 1);
  }


  // Implements EspPacket -------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public byte[] asByteArray()
  {
    int size = Esp2PacketHeader.ESP2_HEADER_SIZE + data.length + ESP2_PACKET_CHECKSUM_LENGTH;

    byte[] packetBytes = new byte[size];

    // Copy header ...

    byte[] src = header.asByteArray();
    byte[] dest = packetBytes;
    int srcPos = 0;
    int destPos = 0;
    int length = src.length;

    System.arraycopy(src, srcPos, dest, destPos, length);

    // Copy data ...

    destPos += length;
    src = data;
    length = data.length;

    System.arraycopy(src, srcPos, dest, destPos, length);

    // Checksum ...

    packetBytes[size - 1] = checksum(
        packetBytes, Esp2PacketHeader.ESP2_HEADER_H_SEQ_LENGTH_INDEX,
        size - ESP2_PACKET_CHECKSUM_LENGTH -
        Esp2PacketHeader.ESP2_HEADER_SYNC_BYTE_1_LENGTH -
        Esp2PacketHeader.ESP2_HEADER_SYNC_BYTE_2_LENGTH
    );

    return packetBytes;
  }


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Returns ESP2 packet type.
   *
   * @return ESP2 packet type
   */
  public Esp2PacketHeader.PacketType getPacketType()
  {
    return header.getPacketType();
  }

  /**
   * Returns a copy of the data group.
   *
   * @return data group copy
   */
  public byte[] getData()
  {
    if(data == null)
    {
      return new byte[] {};
    }

    return data.clone();
  }


  // Private Instance Methods ---------------------------------------------------------------------

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