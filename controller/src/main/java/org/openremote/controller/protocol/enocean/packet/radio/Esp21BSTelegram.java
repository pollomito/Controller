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
import org.openremote.controller.protocol.enocean.packet.Esp2PacketHeader;

/**
 * Represents a 1 byte communication (1BS) radio telegram. <p>
 *
 * The TCM120 and TCM300 user manuals define the RPS packet structure as
 * follows:
 *
 * <pre>
 *         |--------  Header  --------|------------------------------------------ Data ...
 *         +--------+--------+--------+--------+--------+--------+--------+--------+-- ...
 *         |  Sync  |  Sync  | H_SEQ/ |  ORG   | Data   |   X    |    X   |    X   |
 *         |  Byte  |  Byte  | LENGTH | (0x06) | Byte3  |        |        |        |
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
 * 1BS radio telegrams have a 1 byte data payload field. The structure of this payload field
 * is defined by means of EnOcean Equipment Profiles (EEP).
 *
 * @see org.openremote.controller.protocol.enocean.packet.Esp2PacketHeader
 * @see DeviceID
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp21BSTelegram extends AbstractEsp2RadioTelegram
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Length of 1BS payload field: {@value}
   */
  public static final int ESP2_RADIO_1BS_PAYLOAD_LENGTH = 1;


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
  public Esp21BSTelegram(DeviceID senderID, byte payload, byte status)
  {
    super(
        Esp2PacketHeader.PacketType.TRT,
        RORG.BS1_ESP2,
        ESP2_RADIO_1BS_PAYLOAD_LENGTH,
        AbstractEsp2RadioTelegram.createDataGroup(
            RORG.BS1_ESP2, senderID,
            new byte[] {payload, 0x00, 0x00, 0x00},
            status
        )
    );
  }

  /**
   * Constructs a 1BS radio telegram instance with given packet type and data.
   *
   * @param type  ESP2 packet type (H_SEQ)
   *
   * @param data  data group
   */
  public Esp21BSTelegram(Esp2PacketHeader.PacketType type, byte[] data)
  {
    super(type, RORG.BS1_ESP2, ESP2_RADIO_1BS_PAYLOAD_LENGTH, data);
  }
}
