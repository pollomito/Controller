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

import org.openremote.controller.protocol.enocean.DeviceID;
import org.openremote.controller.protocol.enocean.packet.AbstractEsp3RequestPacket;
import org.openremote.controller.protocol.enocean.packet.Esp3PacketHeader;

/**
 * A common superclass for ESP3 radio telegrams which represents a generic radio telegram. <p>
 *
 * The main purpose of the class is to manage the data group fields common to all radio
 * telegram types. <p>
 *
 * According to the EnOcean Serial Protocol 3 specification chapter
 * 1.7: Packet Type 1: RADIO the generic radio telegram structure is as follows:
 *
 * <pre>
 *                                           |-------------------- Data ...
 *         +--------+------...------+--------+--------+-----...-----+-- ...
 *         |  Sync  |    Header     |  CRC8  | Choice |   Payload   |
 *         |  Byte  |               | Header | (RORG) |             |
 *         +--------+------...------+--------+--------+-----...-----+-- ...
 *           1 byte      4 bytes      1 byte   1 byte     n bytes
 *
 *   ... Group ---------------------|
 *   ... --+------...------+--------+----------...---------+--------+
 *         |     Sender    | Status |     Optional Data    |  CRC8  |
 *         |      ID       |        |                      |  Data  |
 *   ... --+------...------+--------+----------...---------+--------+
 *              4 bytes      1 byte          n bytes         1 byte
 * </pre>
 *
 * The choice field is used to store the radio telegram type. The EnOcean
 * Equipment Profiles (EEP) specification uses the alternative name RORG for this field which
 * is an abbreviation for radio organization number. The value of this field is defined in
 * {@link RORG}. <p>
 *
 * The sender ID field contains the unique transmitter {@link DeviceID device ID} value for
 * addressing. <p>
 *
 * The status field is mainly used to store a repeater count value (least significant nibble).
 *
 * <pre>
 *         |-------- Status field --------|
 *         +--------------+---+---+---+---+
 *         |              |RC3|RC2|RC1|RC0|
 *         |              |   |   |   |   |
 *         +--------------+---+---+---+---+
 *                      1 byte
 * </pre>
 *
 *
 * @see Esp3PacketHeader
 * @see Esp3RadioTelegramOptData
 * @see DeviceID
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public abstract class AbstractEsp3RadioTelegram extends AbstractEsp3RequestPacket implements EspRadioTelegram
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Length of RORG field: {@value}
   */
  public static final int ESP_RADIO_RORG_LENGTH = 0x01;

  /**
   * Length of status field: {@value}
   */
  public static final int ESP_RADIO_STATUS_LENGTH = 0x01;

  /**
   * Data group length without payload field: {@value}
   */
  public static final int ESP3_RADIO_MIN_DATA_LENGTH =
      ESP_RADIO_RORG_LENGTH +
      DeviceID.ENOCEAN_ESP_ID_LENGTH +
      ESP_RADIO_STATUS_LENGTH;

  /**
   * Radio packet size without payload field: {@value}
   */
  public static final int ESP3_RADIO_MIN_PACKET_LENGTH =
      Esp3PacketHeader.ESP3_HEADER_SIZE +
      ESP3_RADIO_MIN_DATA_LENGTH +
      Esp3RadioTelegramOptData.ESP3_RADIO_OPT_DATA_LENGTH +
      1;

  // Class Members --------------------------------------------------------------------------------

  /**
   * Retrieves RORG value from data group and returns it.
   *
   * @param data data group
   *
   * @return RORG value
   *
   * @throws UnknownRorgException
   *           if RORG type is unknown
   */
  public static RORG getRORGFromDataGroup(byte[] data)
      throws UnknownRorgException
  {
    if(data == null)
    {
      throw new IllegalArgumentException("null data group");
    }

    if(data.length < ESP3_RADIO_MIN_DATA_LENGTH)
    {
      throw new IllegalArgumentException("Invalid data group length.");
    }

    return RORG.resolve(data[ESP3_RADIO_RORG_INDEX] & 0xFF);
  }

  /**
   * Creates a new data group and initializes it with given RORG value and sender ID
   * and returns it.
   *
   * @param  rorg           telegram type
   *
   * @param  payloadLength  length of payload field
   *
   * @param  senderID       unique sender device ID
   *
   * @return returns new data group
   */
  protected static byte[] createDataGroup(RORG rorg, int payloadLength, DeviceID senderID, byte[] payload, byte status)
  {
    if(senderID == null)
    {
      throw new IllegalArgumentException("null sender ID");
    }

    if(payload == null)
    {
      throw new IllegalArgumentException("null payload data");
    }

    if(payload.length != payloadLength)
    {
      throw new IllegalArgumentException("Invalid payload length.");
    }

    int length = ESP_RADIO_RORG_LENGTH + payloadLength +
                 DeviceID.ENOCEAN_ESP_ID_LENGTH + ESP_RADIO_STATUS_LENGTH;

    byte[] dataBytes = new byte[length];

    // Radio telegram type...

    dataBytes[ESP3_RADIO_RORG_INDEX] = rorg.getValue();

    // Payload...

    System.arraycopy(
        payload, 0, dataBytes, ESP_RADIO_RORG_LENGTH, payloadLength
    );

    // Sender ID...

    System.arraycopy(
        senderID.asByteArray(), 0 , dataBytes,
        ESP_RADIO_RORG_LENGTH + payloadLength, DeviceID.ENOCEAN_ESP_ID_LENGTH
    );

    // Status...

    dataBytes[dataBytes.length - 1] = status;


    return dataBytes;
  }

  /**
   * Retrieves sender ID from data group and returns it.
   *
   * @param  data           data group
   *
   * @param  payloadLength  length of payload field
   *
   * @return sender ID
   */
  private static DeviceID getSenderIDFromDataGroup(byte[] data, int payloadLength)
  {
    byte[] senderIDBytes = new byte[DeviceID.ENOCEAN_ESP_ID_LENGTH];

    System.arraycopy(
        data, ESP_RADIO_RORG_LENGTH + payloadLength, senderIDBytes,
        0 , DeviceID.ENOCEAN_ESP_ID_LENGTH
    );

    return DeviceID.fromByteArray(senderIDBytes);
  }


  // Constants ------------------------------------------------------------------------------------

  /**
   * Byte order index of RORG field relative to data group: {@value}
   */
  public static final int ESP3_RADIO_RORG_INDEX = 0x00;


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Radio telegram type.
   */
  private RORG rorg;

  /**
   * Length of payload field.
   */
  private int payloadLength;

  /**
   * Sender ID.
   */
  private DeviceID senderID;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new radio telegram instance.
   *
   * @param rorg           radio telegram type
   *
   * @param payloadLength  length of payload field
   *
   * @param data           data group
   *
   * @param optionalData   optional data group
   *
   */
  public AbstractEsp3RadioTelegram(RORG rorg, int payloadLength,
                                   byte[] data, byte[] optionalData)
  {
    super(Esp3PacketHeader.PacketType.RADIO, data, optionalData);

    if(rorg == null)
    {
      throw new IllegalArgumentException("null RORG type");
    }

    if(data == null)
    {
      throw new IllegalArgumentException("null data");
    }

    int expectedDataLength = ESP_RADIO_RORG_LENGTH + payloadLength +
        DeviceID.ENOCEAN_ESP_ID_LENGTH + ESP_RADIO_STATUS_LENGTH;

    if(data.length != expectedDataLength)
    {
      throw new IllegalArgumentException("Invalid data length.");
    }

    if(data[ESP3_RADIO_RORG_INDEX] != rorg.getValue())
    {
      throw new IllegalArgumentException("Inconsistent RORG value.");
    }

    if(optionalData == null)
    {
      throw new IllegalArgumentException("null optional data");
    }

    if(optionalData.length != Esp3RadioTelegramOptData.ESP3_RADIO_OPT_DATA_LENGTH)
    {
      throw new IllegalArgumentException("Invalid optional data length.");
    }

    this.rorg = rorg;

    this.payloadLength = payloadLength;

    this.senderID = AbstractEsp3RadioTelegram.getSenderIDFromDataGroup(data, payloadLength);
  }


  // Object Overrides -----------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public String toString()
  {
    StringBuilder builder = new StringBuilder();

    String rorgString = "RORG=" + rorg + ", ";
    String idString = "sender ID=" + senderID + ", ";
    String payloadString = "payload=" + getPayloadAsString() + ", ";
    String statusString = "status=" + String.format("0x%02X", getStatusByte());

    builder
        .append("[RADIO: ")
        .append(rorgString)
        .append(idString)
        .append(payloadString)
        .append(statusString)
        .append("]");

    return builder.toString();
  }


  // Implements EspRadioTelegram ------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public RORG getRORG()
  {
    return rorg;
  }

  /**
   * {@inheritDoc}
   */
  @Override public DeviceID getSenderID()
  {
    return senderID;
  }

  /**
   * {@inheritDoc}
   */
  @Override public byte[] getPayload()
  {
    byte[] payload = new byte[payloadLength];

    System.arraycopy(data, ESP_RADIO_RORG_LENGTH, payload, 0, payloadLength);

    return payload;
  }

  /**
   * {@inheritDoc}
   */
  @Override public byte getStatusByte()
  {
    int statusIndex = ESP_RADIO_RORG_LENGTH + payloadLength +
                      DeviceID.ENOCEAN_ESP_ID_LENGTH;

    return data[statusIndex];
  }


  // Private Instance Methods ---------------------------------------------------------------------

  /**
   * Returns payload as a string with hexadecimal encoding.
   *
   * @return payload string
   */
  private String getPayloadAsString()
  {
    StringBuilder builder = new StringBuilder();

    byte[] payload = getPayload();

    for(int i = 0; i < payload.length; i++)
    {
      builder.append(
          String.format("0x%02X%s", payload[i], i == (payload.length - 1) ? "" : " ")
      );
    }

    return builder.toString();
  }
}
