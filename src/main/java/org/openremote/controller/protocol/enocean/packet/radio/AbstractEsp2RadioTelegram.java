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
import org.openremote.controller.protocol.enocean.packet.AbstractEsp2RequestPacket;
import org.openremote.controller.protocol.enocean.packet.Esp2Packet;
import org.openremote.controller.protocol.enocean.packet.Esp2PacketHeader;

/**
 * A common superclass for ESP2 radio telegrams which represents a generic radio telegram. <p>
 *
 * The main purpose of the class is to manage the data group fields common to all radio
 * telegram types. <p>
 *
 * According to the TCM120 and TCM300 user manuals the generic radio telegram
 * structure is as follows:
 *
 * <pre>
 *         |--------  Header  --------|------------------------------------------ Data ...
 *         +--------+--------+--------+--------+--------+--------+--------+--------+-- ...
 *         |  Sync  |  Sync  | H_SEQ/ |   ORG  | Data   | Data   | Data   | Data   |
 *         |  Byte  |  Byte  | LENGTH |        | Byte3  | Byte2  | Byte1  | Byte0  |
 *         +--------+--------+--------+--------+--------+--------+--------+--------+-- ...
 *           1 byte   1 byte   1 byte   1 byte   1 byte   1 byte   1 byte   1 byte
 *
 *   ... Group -----------------------------------------|
 *   ... --+--------+--------+--------+--------+--------+--------+
 *         |SenderID|SenderID|SenderID|SenderID| Status |Checksum|
 *         | Byte3  | Byte2  | Byte1  | Byte0  |        |        |
 *   ... --+--------+--------+--------+--------+--------+--------+
 *           1 byte   1 byte   1 byte   1 byte   1 byte   1 byte
 * </pre>
 *
 * The ORG field is used to store the radio telegram type. The EnOcean
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
 * @see Esp2PacketHeader
 * @see DeviceID
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class AbstractEsp2RadioTelegram extends AbstractEsp2RequestPacket implements EspRadioTelegram
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Length of ORG field: {@value}
   */
  public static final int ESP2_RADIO_ORG_LENGTH = 1;

  /**
   * Byte order index of ORG field relative to data group: {@value}
   */
  public static final int ESP2_RADIO_ORG_INDEX = 0x00;

  /**
   * Length of status field: {@value}
   */
  public static final int ESP2_RADIO_STATUS_LENGTH = 1;

  /**
   * Fixed size of the ESP2 radio telegram payload field: {@value}
   *
   * Note that the payload field has a fixed size but
   * the payload length within the payload field for each
   * radio telegram is different.
   */
  public static final int ESP2_RADIO_PAYLOAD_FIELD_LENGTH = 4;

  /**
   * Fixed size of data group: {@value}
   */
  public static final int ESP2_RADIO_DATA_LENGTH = ESP2_RADIO_ORG_LENGTH +
                                                   ESP2_RADIO_PAYLOAD_FIELD_LENGTH +
                                                   DeviceID.ENOCEAN_ESP_ID_LENGTH +
                                                   ESP2_RADIO_STATUS_LENGTH;

  /**
   * Fixed size of a ESP2 radio telegram: {@value}
   */
  public static final int ESP2_RADIO_PACKET_LENGTH = Esp2PacketHeader.ESP2_HEADER_SIZE +
                                                     ESP2_RADIO_DATA_LENGTH +
                                                     Esp2Packet.ESP2_PACKET_CHECKSUM_LENGTH;


  // Class Members --------------------------------------------------------------------------------

  /**
   * Retrieves ORG value from data group and returns it.
   *
   * @param  data data group
   *
   * @return ORG value
   *
   * @throws UnknownRorgException
   *           if RORG type is unknown
   */
  public static RORG getORGFromDataGroup(byte[] data)
      throws UnknownRorgException
  {
    if(data == null)
    {
      throw new IllegalArgumentException("null data group");
    }

    if(data.length != ESP2_RADIO_DATA_LENGTH)
    {
      throw new IllegalArgumentException("Invalid data group length.");
    }

    return RORG.resolve(data[ESP2_RADIO_ORG_INDEX] & 0xFF);
  }

  /**
   * Creates a new data group and initializes it with given ORG value and sender ID
   * and returns it.
   *
   * @param  org       telegram type
   *
   * @param  senderID  unique sender device ID
   *
   * @return returns new data group
   */
  protected static byte[] createDataGroup(RORG org, DeviceID senderID, byte[] payload, byte status)
  {
    if(senderID == null)
    {
      throw new IllegalArgumentException("null sender ID");
    }

    if(payload == null)
    {
      throw new IllegalArgumentException("null payload data");
    }

    if(payload.length != ESP2_RADIO_PAYLOAD_FIELD_LENGTH)
    {
      throw new IllegalArgumentException("Invalid payload length.");
    }

    byte[] dataBytes = new byte[ESP2_RADIO_DATA_LENGTH];

    // Radio telegram type...

    dataBytes[ESP2_RADIO_ORG_INDEX] = org.getValue();

    // Payload...

    System.arraycopy(
        payload, 0, dataBytes, ESP2_RADIO_ORG_LENGTH, ESP2_RADIO_PAYLOAD_FIELD_LENGTH
    );

    // Sender ID...

    System.arraycopy(
        senderID.asByteArray(), 0 , dataBytes,
        ESP2_RADIO_ORG_LENGTH + ESP2_RADIO_PAYLOAD_FIELD_LENGTH, DeviceID.ENOCEAN_ESP_ID_LENGTH
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
   * @return sender ID
   */
  private static DeviceID getSenderIDFromDataGroup(byte[] data)
  {
    byte[] senderIDBytes = new byte[DeviceID.ENOCEAN_ESP_ID_LENGTH];

    System.arraycopy(
        data, ESP2_RADIO_ORG_LENGTH + ESP2_RADIO_PAYLOAD_FIELD_LENGTH, senderIDBytes,
        0 , DeviceID.ENOCEAN_ESP_ID_LENGTH
    );

    return DeviceID.fromByteArray(senderIDBytes);
  }


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Radio telegram type.
   */
  private RORG org;

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
   * @param type  ESP2 packet type (H_SEQ)
   *
   * @param org   radio telegram type
   *
   * @param data  data group
   */
  public AbstractEsp2RadioTelegram(Esp2PacketHeader.PacketType type, RORG org, int payloadLength, byte[] data)
  {
    super(type, data);

    if(org == null)
    {
      throw new IllegalArgumentException("null ORG type");
    }

    if(data == null)
    {
      throw new IllegalArgumentException("null data");
    }

    if(data.length != ESP2_RADIO_DATA_LENGTH)
    {
      throw new IllegalArgumentException("Invalid data length.");
    }

    if(data[ESP2_RADIO_ORG_INDEX] != org.getValue())
    {
      throw new IllegalArgumentException("Inconsistent ORG value.");
    }

    this.org = org;

    this.payloadLength = payloadLength;

    this.senderID = AbstractEsp2RadioTelegram.getSenderIDFromDataGroup(data);
  }


  // Object Overrides -----------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public String toString()
  {
    StringBuilder builder = new StringBuilder();

    String rorgString = "ORG=" + org + ", ";
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
    return org;
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

    System.arraycopy(data, ESP2_RADIO_ORG_LENGTH, payload, 0, payloadLength);

    return payload;
  }

  /**
   * {@inheritDoc}
   */
  @Override public byte getStatusByte()
  {
    int statusIndex = ESP2_RADIO_ORG_LENGTH + ESP2_RADIO_PAYLOAD_FIELD_LENGTH +
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
