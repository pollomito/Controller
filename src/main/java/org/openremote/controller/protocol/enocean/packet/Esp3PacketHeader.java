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

import org.openremote.controller.utils.Strings;

/**
 * This class represents the ESP3 packet header as defined in EnOcean Serial Protocol 3 V1.17
 * specification chapter 1.3: Packet structure. <p>
 *
 * All ESP3 packets which are sent or received from the EnOcean module have the header
 * prepended. <p>
 *
 * The ESP3 header structure is as follows:
 *
 * <pre>
 *   +- - - - +-----------------------+-----------+-----------+- - - - -+
 *   .  Sync  |         Data          | Optional  |  Packet   |  CRC8   .
 *   .  Byte  |        Length         |Data Length|   Type    |         .
 *   +- - - - +-----------------------+-----------+-----------+- - - - -+
 *     1 byte         2 bytes            1 byte      1 byte      1 byte
 * </pre>
 *
 * Each ESP3 packet consists of a header followed by a data and optional data section (see
 * {@link Esp3Packet}). The header contains the size of the data and optional data section.
 * The Packet Type field determines the content of the data section (see
 * {@link Esp3PacketHeader.PacketType}). <p>
 *
 * Note that this ESP3 header implementation contains the Synchronization Byte field at the
 * beginning and the CRC8 field at the end as opposed to the Serial Protocol 3.0 Specification.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp3PacketHeader
{

  // Enums ----------------------------------------------------------------------------------------

  /**
   * EnOcean Serial Protocol 3.0 (ESP3) packet types used as part of the packet header to
   * indicate the purpose of a packet.
   */
  public enum PacketType
  {
    /**
     * Radio telegram to be sent to the EnOcean module or received from the EnOcean module.
     */
    RADIO(0x01),

    /**
     * Response from the EnOcean module for a previously sent packet (see {@link #RADIO},
     * {@link #COMMON_COMMAND}, {@link #SMART_ACK_COMMAND}, {@link #REMOTE_MAN_COMMAND})
     * or response as part of a previously received {@link #EVENT} packet.
     */
    RESPONSE(0x02),

    /**
     * Radio subtelegram.
     */
    RADIO_SUB_TEL(0x03),

    /**
     * Event message received from EnOcean module.
     */
    EVENT(0x04),

    /**
     * Common Command to be sent to EnOcean module.
     */
    COMMON_COMMAND(0x05),

    /**
     * Smart Acknowledge Command to be sent to EnOcean module.
     */
    SMART_ACK_COMMAND(0x06),

    /**
     * Remote Management Command to be sent to the EnOcean module or received from the EnOcean
     * module.
     */
    REMOTE_MAN_COMMAND(0x07);


    // Members ------------------------------------------------------------------------------------

    public static PacketType resolve(int value) throws UnknownPacketTypeException
    {
      PacketType[] allTypes = PacketType.values();

      byte packetTypeByte = (byte)(value & 0xFF);

      for (PacketType packetType : allTypes)
      {
        if (packetType.value == packetTypeByte)
        {
          return packetType;
        }
      }

      throw new UnknownPacketTypeException(
          "Unknown ESP3 packet type value : " +
          Strings.byteToUnsignedHexString(packetTypeByte)
      );
    }

    private byte value;

    private PacketType(int value)
    {
      this.value = (byte)(value & 0xFF);
    }

    public byte getValue()
    {
      return value;
    }
  }


  // Constants ------------------------------------------------------------------------------------

  /**
   * The value of the synchronization byte at the beginning of an ESP packet : {@value}
   */
  public static final byte ESP3_SYNC_BYTE = 0x55;

  /**
   * The fixed size of a ESP3 header : {@value}
   */
  public static final int ESP3_HEADER_SIZE = 0x06;

  /**
   * Byte order index of the synchronization byte field (see {@link #ESP3_SYNC_BYTE}) in
   * ESP3 header : {@value}
   */
  public static final int ESP3_HEADER_SYNC_BYTE_INDEX = 0;

  /**
   * Byte order index of the data length high byte field in ESP3 header : {@value}
   */
  public static final int ESP3_HEADER_DATA_LENGTH_HIBYTE_INDEX = 1;

  /**
   * Byte order index of the data length low byte field in ESP3 header : {@value}
   */
  public static final int ESP3_HEADER_DATA_LENGTH_LOBYTE_INDEX = 2;

  /**
   * Byte order index of the optional data length field in ESP3 header : {@value}
   */
  public static final int ESP3_HEADER_OPTIONAL_DATA_LENGTH_INDEX = 3;

  /**
   * Byte order index of the packet type field in ESP3 header : {@value}
   */
  public static final int ESP3_HEADER_PACKET_TYPE_INDEX = 4;

  /**
   * Byte order index of the CRC8 field in ESP3 header : {@value}
   */
  public static final int ESP3_HEADER_CRC8_INDEX = 5;


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Size of the ESP3 packet data section.
   *
   * @see #ESP3_HEADER_DATA_LENGTH_HIBYTE_INDEX
   * @see #ESP3_HEADER_DATA_LENGTH_LOBYTE_INDEX
   */
  private int dataLength;

  /**
   * Size of the ESP3 packet optional data section.
   *
   * @see #ESP3_HEADER_OPTIONAL_DATA_LENGTH_INDEX
   */
  private int optionalDataLength;

  /**
   * ESP3 packet type.
   *
   * @see Esp3PacketHeader.PacketType
   * @see #ESP3_HEADER_PACKET_TYPE_INDEX
   */
  private PacketType packetType;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a ESP3 header instance from a header data byte array.
   *
   * @param  headerData  header data. The header starts with the {@link #ESP3_SYNC_BYTE} value
   *                     and ends with the CRC-8 value. The header data size has to be at least
   *                     {@link #ESP3_HEADER_SIZE} bytes
   *
   * @throws UnknownPacketTypeException
   *           if ESP3 packet header contains an unknown {@link Esp3PacketHeader.PacketType}
   *
   * @throws CRC8Exception
   *           if the given CRC-8 value does not match the calculated CRC-8 value
   */
  public Esp3PacketHeader(byte[] headerData) throws UnknownPacketTypeException, CRC8Exception
  {
    if(headerData == null)
    {
      throw new IllegalArgumentException("null header data");
    }

    if(headerData.length < ESP3_HEADER_SIZE)
    {
      throw new IllegalArgumentException(
          "Invalid header data size. Expected size is " +
          ESP3_HEADER_SIZE + " bytes, got " + headerData.length + " bytes."
      );
    }

    if(headerData[ESP3_HEADER_SYNC_BYTE_INDEX] != ESP3_SYNC_BYTE)
    {
      throw new IllegalArgumentException(
          "Header data does not start with Sync. Byte " +
          String.format("0x%02X", ESP3_SYNC_BYTE) + "."
      );
    }

    this.dataLength = ((headerData[ESP3_HEADER_DATA_LENGTH_HIBYTE_INDEX] & 0xFF) << 8) +
                       (headerData[ESP3_HEADER_DATA_LENGTH_LOBYTE_INDEX] & 0xFF);

    this.optionalDataLength = headerData[ESP3_HEADER_OPTIONAL_DATA_LENGTH_INDEX] & 0xFF;

    this.packetType = PacketType.resolve(
        headerData[ESP3_HEADER_PACKET_TYPE_INDEX] & 0xFF
    );

    if(headerData[ESP3_HEADER_CRC8_INDEX] != getCRC8Value(headerData))
    {
      throw new CRC8Exception("CRC-8 ESP3 header verification failed.");
    }
  }


  /**
   * Constructs a ESP3 header instance with given packet type and data length values.
   *
   * @param packetType  ESP3 packet type
   * @param dataLength  length of data group with valid range: [0x0000..0xFFFF]
   * @param optionalDataLength length of optional data group with valid range: [0x00..0xFF]
   */
  public Esp3PacketHeader(PacketType packetType, int dataLength, int optionalDataLength)
  {
    if(packetType == null)
    {
      throw new IllegalArgumentException("null packet type");
    }

    if(dataLength > 0xFFFF || dataLength < 0)
    {
      throw new IllegalArgumentException("Data length out of valid range.");
    }

    if(optionalDataLength > 0xFF || optionalDataLength < 0)
    {
      throw new IllegalArgumentException("Optional data length out of valid range.");
    }

    this.packetType = packetType;
    this.dataLength = dataLength;
    this.optionalDataLength = optionalDataLength;
  }


  // Object Overrides -----------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public String toString()
  {
    StringBuilder builder = new StringBuilder();

    String length = String.format("length=0x%04X, ", dataLength);
    String optLength = String.format("optional length=0x%02X, ", optionalDataLength);
    String type = "type=" + packetType;

    builder
        .append("[HEADER: ")
        .append(length)
        .append(optLength)
        .append(type)
        .append("]");

    return builder.toString();
  }


    // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Returns size of the ESP3 packet data section.
   *
   * @return data length
   */
  public int getDataLength()
  {
    return dataLength;
  }

  /**
   * Returns size of the ESP3 packet optional data section.
   *
   * @return optional data length
   */
  public int getOptionalDataLength()
  {
    return optionalDataLength;
  }

  /**
   * Returns the ESP3 packet type
   *
   * @return packet type
   */
  public PacketType getPacketType()
  {
    return packetType;
  }

  /**
   * Returns ESP3 packet header as byte array. <p>
   *
   * The returned header data starts with the {@link #ESP3_SYNC_BYTE} value and ends with the
   * calculated CRC-8 value.
   *
   * @return ESP3 packet header
   */
  public byte[] asByteArray()
  {
    byte[] headerBytes = new byte[ESP3_HEADER_SIZE];

    headerBytes[ESP3_HEADER_SYNC_BYTE_INDEX] = ESP3_SYNC_BYTE;

    headerBytes[ESP3_HEADER_DATA_LENGTH_HIBYTE_INDEX] = (byte)((dataLength >> 8) & 0xFF);
    headerBytes[ESP3_HEADER_DATA_LENGTH_LOBYTE_INDEX] = (byte)(dataLength & 0xFF);

    headerBytes[ESP3_HEADER_OPTIONAL_DATA_LENGTH_INDEX] = (byte)optionalDataLength;

    headerBytes[ESP3_HEADER_PACKET_TYPE_INDEX] = packetType.getValue();

    headerBytes[ESP3_HEADER_CRC8_INDEX] = getCRC8Value(headerBytes);

    return headerBytes;
  }

  /**
   * Returns size of a complete packet including first sync. byte and last CRC-8 value.
   *
   * @return packet size
   */
  public int getPacketSize()
  {
    return Esp3PacketHeader.ESP3_HEADER_SIZE +
           getDataLength() +
           getOptionalDataLength() +
           1;  // last CRC-8 byte
  }

  // Private Instance Methods ---------------------------------------------------------------------

  /**
   * Returns CRC-8 value for ESP3 header. <p>
   *
   * CRC-8 calculation is performed in the range from {@link #ESP3_HEADER_DATA_LENGTH_HIBYTE_INDEX}
   * to {@link #ESP3_HEADER_PACKET_TYPE_INDEX}.
   *
   * @param  header  ESP3 header data with {@link #ESP3_SYNC_BYTE} as first byte. Size of header
   *                 data has to be at least ({@link #ESP3_HEADER_SIZE} - 1) bytes
   *
   * @return CRC-8 value
   */
  private byte getCRC8Value(byte[] header)
  {
    int offset = ESP3_HEADER_DATA_LENGTH_HIBYTE_INDEX;
    int length = ESP3_HEADER_PACKET_TYPE_INDEX - ESP3_HEADER_DATA_LENGTH_HIBYTE_INDEX + 1;

    return Esp3Packet.CRC8.calculate(header, offset, length);
  }


  // Nested Classes -------------------------------------------------------------------------------

  /**
   * Indicates an unknown {@link Esp3PacketHeader.PacketType}.
   */
  public static class UnknownPacketTypeException extends Exception
  {
    public UnknownPacketTypeException(String msg)
    {
      super(msg);
    }
  }

  /**
   * Indicates a CRC-8 ESP3 header verification failure.
   */
  public static class CRC8Exception extends Exception
  {
    public CRC8Exception(String msg)
    {
      super(msg);
    }
  }
}
