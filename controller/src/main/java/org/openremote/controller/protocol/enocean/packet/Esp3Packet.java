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
 * Represents an EnOcean Serial Protocol 3 (ESP3) packet. <p>
 *
 * The EnOcean Serial Protocol 3.0 Specification defines the generic ESP3 packet structure
 * as follows: <p>
 *
 * <pre>
 *            |-------------   Header - ----------|
 *   +--------+-----------------+--------+--------+--------+----...-----+----...-----+--------+
 *   |  Sync  |      Data       |Optional| Packet |  CRC8  |    Data    |  Optional  |  CRC8  |
 *   |  Byte  |     Length      | Length |  Type  | Header |            |    Data    |  Data  |
 *   +--------+-----------------+--------+--------+--------+-----...----+----...-----+--------+
 *     1 byte       2 bytes       1 byte   1 byte   1 byte     n bytes      n bytes    1 byte
 *</pre>
 *
 * Each ESP 3 packet starts with a header (see {@link Esp3PacketHeader}). The header is
 * followed by the Data and Optional Data group. The content of these data groups is determined
 * by the Packet Type inside the header (as defined in {@link Esp3PacketHeader.PacketType}).
 * The packet is completed with a CRC-8 value at the end which verifies the validity of
 * the Data and Optional Data group.
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp3Packet implements EspPacket
{

  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Packet header.
   */
  private Esp3PacketHeader header;

  /**
   * Data group.
   */
  protected byte[] data;

  /**
   * Optional data group.
   */
  protected byte[] optionalData;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new ESP3 packet instance with a given packet type and packet data.
   *
   * @see Esp3PacketHeader.PacketType
   *
   * @param packetType    ESP3 packet type
   *
   * @param data          data group as byte array
   *
   * @param optionalData  optional data group as byte array
   */
  public Esp3Packet(Esp3PacketHeader.PacketType packetType, byte[] data, byte[] optionalData)
  {
    if(packetType == null)
    {
      throw new IllegalArgumentException("null packet type");
    }

    this.data = (data == null) ? new byte[]{} : data;
    this.optionalData = (optionalData == null) ? new byte[] {} : optionalData;

    this.header = new Esp3PacketHeader(
        packetType, this.data.length, this.optionalData.length
    );
  }

  // Implements EspPacket -------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public byte[] asByteArray()
  {
    int size = Esp3PacketHeader.ESP3_HEADER_SIZE + data.length + optionalData.length + 1;

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

    // Copy optional data ...

    destPos += length;
    src = optionalData;
    length = optionalData.length;

    System.arraycopy(src, srcPos, dest, destPos, length);

    // Data CRC-8 value ...

    byte crc8 = CRC8.calculate(
        packetBytes, Esp3PacketHeader.ESP3_HEADER_SIZE,
        packetBytes.length - Esp3PacketHeader.ESP3_HEADER_SIZE - 1
    );

    packetBytes[packetBytes.length - 1] = crc8;

    return packetBytes;
  }

  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Returns ESP3 packet type.
   *
   * @return ESP3 packet type
   */
  public Esp3PacketHeader.PacketType getPacketType()
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

  /**
   * Returns a copy of the optional data group.
   *
   * @return optional data group copy
   */
  public byte[] getOptionalData()
  {
    if(optionalData == null)
    {
      return new byte[] {};
    }

    return optionalData.clone();
  }

  // Nested Classes -------------------------------------------------------------------------------

  /**
   * Class for calculating EnOcean Serial Protocol 3 (ESP3) Cyclic Redundancy Check (CRC) values
   * according to EnOcean Serial Protocol Specification 3 V1.17 chapter 2.3: CRC8 calculation.
   */
  public static class CRC8
  {

    // Class Members ------------------------------------------------------------------------------

    /**
     * CRC-8 lookup table.
     */
    private static final byte[] CRC8_TABLE;

    static
    {
      CRC8_TABLE = calculateCRC8LookupTable();
    }

    /**
     * Calculates and returns the CRC-8 lookup table. <p>
     *
     * The lookup table calculation is based on the following parameters:
     * Polynomial: x^8 + x^2 + x^1 + 1 -> 0x107
     * Reversed:   false
     * Init value: 0x00
     *
     * @return CRC-8 lookup table
     */
    static byte[] calculateCRC8LookupTable()
    {
      byte[] table = new byte[256];

      int poly = 0x07;
      int crc;

      for(int value = 0; value < table.length; value++)
      {
        crc = value;

        for(int i = 0; i < 8; i++)
        {
          if((crc & 0x80) != 0)
          {
            crc = (crc << 1) ^ poly;
          }
          else
          {
            crc <<= 1;
          }
        }

        table[value] = (byte)crc;
      }

      return table;
    }


    /**
     * Calculates the CRC-8 value for given byte array.
     *
     * @param  data  data for CRC-8 calculation
     *
     * @return calculated CRC-8 value
     */
    public static byte calculate(byte[] data)
    {
      if(data == null)
      {
        throw new IllegalArgumentException("null data");
      }

      return calculate(data, 0, data.length);
    }

    /**
     * Calculates CRC-8 value for specified range within given byte array. <p>
     *
     * The range has to be within the limits of the byte array.
     *
     * @param  data    data for CRC-8 calculation
     *
     * @param  offset  start index of range, inclusive
     *
     * @param  length  length of range
     *
     * @return calculated CRC-8 value
     */
    public static byte calculate(byte[] data, int offset , int length)
    {
      if(data == null)
      {
        throw new IllegalArgumentException("null data");
      }

      if(((offset + length) > data.length) || offset < 0 || length < 0)
      {
        throw new IllegalArgumentException("Invalid range.");
      }

      int crc8 = 0;

      for (int dataIndex = offset; dataIndex < (offset + length); dataIndex++)
      {
        crc8 = CRC8_TABLE[(crc8 ^ data[dataIndex]) & 0xFF];
      }

      return (byte)crc8;
    }

    // Constructors -------------------------------------------------------------------------------

    private CRC8()
    {
    }
  }

}
