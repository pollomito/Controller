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
 * This class represents the ESP2 packet header. <p>
 *
 * All ESP2 packets which are sent or received from the EnOcean module have the header
 * prepended. <p>
 *
 * The ESP2 header structure is as follows:
 *
 * <pre>
 *       +---------------+---------------+---------------+
 *       |   Sync Byte   |   Sync Byte   |H_SEQ| LENGTH  |
 *       |     (0xA5)    |     (0x5A)    |     |         |
 *       +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * bits  |7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|
 *       +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 *</pre>
 *
 * The ESP2 header starts with two synchronization bytes followed by the H_SEQ field (3 bits)
 * which contains the packet type. The last LENGTH field (5 bits) contains the size of the following
 * data section (including the CHECKSUM field) and the value is always 11 bytes.
 *
 * @see Esp2Packet
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp2PacketHeader
{

  // Enums ----------------------------------------------------------------------------------------

  public enum PacketType
  {
    /**
     * Unknown transmitter ID received.
     *
     * For RPS also:
     * - Known transmitter ID and unknown rocker
     * - U-message from known transmitter ID received
     * For HRC also:
     * - Known transmitter ID and unknown rocker
     * - Scene switch command from known transmitter ID
     */
    RRT_UNKNOWN(0x00),

    /**
     * For 1BS and 4BS known transmitter ID received
     * For RPS known transmitter ID and at least 1 known rocker
     * (1 or 2 rockers operated)
     * For HRC: Known transmitter ID and known rocker
     */
    RRT_KNOWN(0x01),

    /**
     * New transmitter learned.
     */
    LEARN(0x02),

    /**
     * Transmitter just deleted.
     */
    DELETE(0x06),

    /**
     * Transmit radio telegram.
     */
    TRT(0x03),

    /**
     * Receive command telegram.
     */
    RCT(0x04),

    /**
     * Transmit command telegram.
     */
    TCT(0x05);

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
          "Unknown ESP2 packet type (H_SEQ) value : " +
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
   * The value of the first synchronization byte at the beginning of an ESP2 packet : {@value}
   */
  public static final byte ESP2_SYNC_BYTE_1 = (byte)0xA5;

  /**
   * The value of the second synchronization byte at the beginning of an ESP2 packet : {@value}
   */
  public static final byte ESP2_SYNC_BYTE_2 = (byte)0x5A;

  /**
   * The fixed size of an ESP2 header : {@value}
   */
  public static final int ESP2_HEADER_SIZE = 0x03;

  /**
   * Byte order index of the first synchronization byte field (see {@link #ESP2_SYNC_BYTE_1} in
   * ESP2 header : {@value}
   */
  public static final int ESP2_HEADER_SYNC_BYTE_1_INDEX = 0;

  /**
   * The fixed size of the first ESP2 sync. byte field  : {@value}
   */
  public static final int ESP2_HEADER_SYNC_BYTE_1_LENGTH = 1;

  /**
   * Byte order index of the second synchronization byte field (see {@link #ESP2_SYNC_BYTE_2} in
   * ESP2 header : {@value}
   */
  public static final int ESP2_HEADER_SYNC_BYTE_2_INDEX = 1;

  /**
   * The fixed size of the second ESP2 sync. byte field  : {@value}
   */
  public static final int ESP2_HEADER_SYNC_BYTE_2_LENGTH = 1;

  /**
   * Byte order index of the H_SEQ and LENGTH field : {@value}
   */
  public static final int ESP2_HEADER_H_SEQ_LENGTH_INDEX = 2;


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * ESP2 packet type.
   */
  private PacketType packetType;

  /**
   * Size of the ESP2 data section (size of data and checksum field).
   */
  private int length;


  // Constructors ---------------------------------------------------------------------------------

  public Esp2PacketHeader(byte[] headerData) throws UnknownPacketTypeException
  {
    if(headerData == null)
    {
      throw new IllegalArgumentException("null header data");
    }

    if(headerData.length < ESP2_HEADER_SIZE)
    {
      throw new IllegalArgumentException(
          "Invalid header data size. Expected size is " +
          ESP2_HEADER_SIZE + " bytes, got " + headerData.length + " bytes."
      );
    }

    if(headerData[ESP2_HEADER_SYNC_BYTE_1_INDEX] != ESP2_SYNC_BYTE_1 ||
       headerData[ESP2_HEADER_SYNC_BYTE_2_INDEX] != ESP2_SYNC_BYTE_2 )
    {
      throw new IllegalArgumentException(
          "Header data does not start with two sync. bytes : " +
          String.format("0x%02X 0x%02X", ESP2_SYNC_BYTE_1, ESP2_SYNC_BYTE_2) + "."
      );
    }

    this.length = headerData[ESP2_HEADER_H_SEQ_LENGTH_INDEX] & 0x1F;

    this.packetType = PacketType.resolve(
        (headerData[ESP2_HEADER_H_SEQ_LENGTH_INDEX] & 0xE0) >> 5
    );
  }

  public Esp2PacketHeader(PacketType packetType, int length)
  {
    if(packetType == null)
    {
      throw new IllegalArgumentException("null packet type");
    }

    if(length > 0x1F)
    {
      throw new IllegalArgumentException("Data length out of valid range.");
    }

    this.packetType = packetType;
    this.length = length;
  }


  // Object Overrides -------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public String toString()
  {
    StringBuilder builder = new StringBuilder();

    String lengthString = String.format("length=0x%02X, ", length);
    String typeString = "type=" + packetType;

    builder
        .append("[HEADER: ")
        .append(lengthString)
        .append(typeString)
        .append("]");

    return builder.toString();
  }

  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Returns the ESP2 packet size without ESP2 header.
   *
   * @return the ESP2 packet size without header
   */
  public int getLength()
  {
    return length;
  }

  /**
   * Returns the ESP2 packet type
   *
   * @return the ESP2 packet type
   */
  public PacketType getPacketType()
  {
    return packetType;
  }

  /**
   * Returns ESP2 packet header as byte array. <p>
   *
   * The returned header data starts with the {@link #ESP2_SYNC_BYTE_1} value.
   *
   * @return ESP2 packet header
   */
  public byte[] asByteArray()
  {
    byte[] headerBytes = new byte[ESP2_HEADER_SIZE];

    headerBytes[ESP2_HEADER_SYNC_BYTE_1_INDEX] = ESP2_SYNC_BYTE_1;
    headerBytes[ESP2_HEADER_SYNC_BYTE_2_INDEX] = ESP2_SYNC_BYTE_2;

    headerBytes[ESP2_HEADER_H_SEQ_LENGTH_INDEX] |= (byte)(packetType.getValue() << 5);
    headerBytes[ESP2_HEADER_H_SEQ_LENGTH_INDEX] |= (byte)(length & 0x1F);

    return headerBytes;
  }

  /**
   * Returns the size of a complete packet including sync. bytes and checksum.
   *
   * @return packet size
   */
  public int getPacketSize()
  {
    return ESP2_HEADER_SIZE + length;
  }

  // Nested Classes -------------------------------------------------------------------------------

  /**
   * Indicates an unknown {@link Esp2PacketHeader.PacketType}.
   */
  public static class UnknownPacketTypeException extends Exception
  {
    public UnknownPacketTypeException(String msg)
    {
      super(msg);
    }
  }
}

