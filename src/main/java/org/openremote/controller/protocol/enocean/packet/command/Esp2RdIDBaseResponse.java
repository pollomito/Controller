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
package org.openremote.controller.protocol.enocean.packet.command;

import org.openremote.controller.protocol.enocean.DeviceID;
import org.openremote.controller.protocol.enocean.EspException;
import org.openremote.controller.protocol.enocean.packet.Esp2Packet;
import org.openremote.controller.protocol.enocean.packet.Esp2ResponsePacket;

/**
 * Represents a INF_BASEID response packet which is returned from the EnOcean module after
 * a {@link Esp2RdIDBaseCommand RD_BASEID command} has been sent. <p>
 *
 * The TCM120 and TCM300 user manuals define the INF_BASEID structure as follows:
 *
 * <pre>
 *                                     |--------- Data -------|- Optional Data --|
 *                                     |--------------- Response ----------------|
 *   +--------+------...------+--------+--------+-----...-----+------------------+--------+
 *   |  Sync  |    Header     |  CRC8  | Return |  Base ID    | Remaining write  |  CRC8  |
 *   |  Byte  |               | Header |  Code  |             |cycles for BaseID |  Data  |
 *   +--------+------...------+--------+--------+-----...-----+------------------+--------+
 *     1 byte      4 bytes      1 byte   1 byte    4 bytes          1 byte       1 byte
 *</pre>
 *
 *
 * @see Esp2RdIDBaseCommand
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp2RdIDBaseResponse extends Esp2ResponsePacket
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Byte order index of the base ID field relative to the data group: {@value}
   */
  public static final int ESP2_RESPONSE_RD_IDBASE_ID_INDEX = 0x01;

  /**
   * Data group length: {@value}
   */
  public static final int ESP2_RESPONSE_RD_IDBASE_DATA_LENGTH =
      Esp2Packet.ESP2_PACKET_DATA_LENGTH;


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * EnOcean gateway base ID.
   */
  DeviceID baseID;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a read ID base response instance with a given data group.
   *
   * @param data data group
   *
   * @throws EspException
   *           if the {@link Esp2ResponsePacket.ReturnCode return code} is unknown or
   *           the data group does not have the expected length
   */
  public Esp2RdIDBaseResponse(byte[] data) throws EspException
  {
    super(data);

    if(data.length != ESP2_RESPONSE_RD_IDBASE_DATA_LENGTH)
    {
      throw new EspException(
          EspException.ErrorCode.RESP_INVALID_RESPONSE,
          "Failed to create RD_BASEID response instance. " +
          "Expected {0} data bytes, got {1}.",
          ESP2_RESPONSE_RD_IDBASE_DATA_LENGTH, data.length
      );
    }

    byte[] deviceIdBytes = new byte[DeviceID.ENOCEAN_ESP_ID_LENGTH];
    System.arraycopy(
        data, ESP2_RESPONSE_RD_IDBASE_ID_INDEX, deviceIdBytes,
        0, DeviceID.ENOCEAN_ESP_ID_LENGTH
    );

    this.baseID = DeviceID.fromByteArray(deviceIdBytes);
  }

  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Returns base ID
   *
   * @return base ID
   */
  public DeviceID getBaseID()
  {
    return baseID;
  }
}
