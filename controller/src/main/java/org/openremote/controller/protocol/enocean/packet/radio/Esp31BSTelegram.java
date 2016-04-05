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

/**
 * Represents a 1 byte communication (1BS) radio telegram. <p>
 *
 * The EnOcean Equipment Profiles (EEP) 2.1 specification defines the 1BS packet structure
 * as follows:
 *
 * <pre>
 *                                           |-------------- Data ...
 *         +--------+------...------+--------+--------+-------+-- ...
 *         |  Sync  |    Header     |  CRC8  | Choice |Payload|
 *         |  Byte  |               | Header | (RORG) |       |
 *         +--------+------...------+--------+--------+-------+-- ...
 *           1 byte      4 bytes      1 byte   1 byte  1 byte
 *
 *   ... Group ---------------------|
 *   ... --+------...------+--------+-------...-------+-------+
 *         |     Sender    | Status |     Optional    |  CRC8 |
 *         |      ID       |        |       Data      |  Data |
 *   ... --+------...------+--------+-------...-------+-------+
 *              4 bytes      1 byte       n bytes      1 byte
 * </pre>
 *
 * 1BS radio telegrams have a 1 byte data payload field. The structure of this payload field
 * is defined by means of EnOcean Equipment Profiles (EEP).
 *
 *
 * @see org.openremote.controller.protocol.enocean.packet.Esp3PacketHeader
 * @see AbstractEsp3RadioTelegram
 * @see Esp3RadioTelegramOptData
 * @see DeviceID
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp31BSTelegram extends AbstractEsp3RadioTelegram
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Byte order index of 1BS payload field relative to data group: {@value}
   */
  public static final int ESP3_RADIO_1BS_PAYLOAD_INDEX = 0x01;

  /**
   * Length of 1BS payload field: {@value}
   */
  public static final int ESP3_RADIO_1BS_PAYLOAD_LENGTH = 0x01;

  /**
   * Byte order index of sender ID field: {@value}
   */
  public static final int ESP3_RADIO_1BS_SENDER_ID_INDEX = 0x02;

  /**
   * Byte order index of status field: {@value}
   */
  public static final int ESP3_RADIO_1BS_STATUS_INDEX = 0x06;

  /**
   * Data group length: {@value}
   */
  public static final int ESP3_RADIO_1BS_DATA_LENGTH =
      ESP3_RADIO_MIN_DATA_LENGTH +
      ESP3_RADIO_1BS_PAYLOAD_LENGTH;

  /**
   * 1BS radio telegram packet size: {@value}
   */
  public static final int ESP3_RADIO_1BS_PACKET_LENGTH =
      ESP3_RADIO_MIN_PACKET_LENGTH +
      ESP3_RADIO_1BS_PAYLOAD_LENGTH;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a 1BS radio telegram instance with given sender ID, payload and status field value.
   *
   * @param senderID unique sender device ID
   *
   * @param payload  payload field value
   *
   * @param status   status field value
   */
  public Esp31BSTelegram(DeviceID senderID, byte payload, byte status)
  {
    super(
        RORG.BS1, ESP3_RADIO_1BS_PAYLOAD_LENGTH,
        AbstractEsp3RadioTelegram.createDataGroup(
            RORG.BS1, ESP3_RADIO_1BS_PAYLOAD_LENGTH, senderID,
            new byte[] {payload}, status
        ),
        Esp3RadioTelegramOptData.ESP3_RADIO_OPT_DATA_TX.asByteArray()
    );
  }

  /**
   * Constructs a 1BS radio telegram instance with given data.
   *
   * @param data          data group
   *
   * @param optionalData  optional data group
   */
  public Esp31BSTelegram(byte[] data, byte[] optionalData)
  {
    super(
        RORG.BS1, ESP3_RADIO_1BS_PAYLOAD_LENGTH,
        data, optionalData
    );
  }
}
