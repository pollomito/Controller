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
import org.openremote.controller.protocol.enocean.InvalidDeviceIDException;
import org.openremote.controller.protocol.enocean.packet.Esp3Packet;
import org.openremote.controller.protocol.enocean.packet.Esp3ResponsePacket;

/**
 * Response for {@link Esp3RdIDBaseCommand read ID base command}. <p>
 *
 * The EnOcean Serial Protocol 3 specification chapter 1.11.10 Code 08: CO_RD_IDBASE defines
 * the response structure as follows:
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
 * @see Esp3RdIDBaseCommand
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp3RdIDBaseResponse extends Esp3ResponsePacket
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Byte order index of the base ID field relative to the data group: {@value}
   */
  public static final int ESP3_RESPONSE_RD_IDBASE_ID_INDEX = 0x01;

  /**
   * Data group length: {@value}
   */
  public static final int ESP3_RESPONSE_RD_IDBASE_DATA_LENGTH =
      DeviceID.ENOCEAN_ESP_ID_LENGTH + 1;


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
   *           if the {@link Esp3ResponsePacket.ReturnCode return code} is unknown or
   *           the data group does not have the expected length
   */
  public Esp3RdIDBaseResponse(byte[] data) throws EspException
  {
    super(data, null);

    if(data.length != ESP3_RESPONSE_RD_IDBASE_DATA_LENGTH)
    {
      throw new EspException(
          EspException.ErrorCode.RESP_INVALID_RESPONSE,
          "Failed to create CO_RD_IDBASE response instance. " +
          "Expected {0} data bytes, got {1}.",
          ESP3_RESPONSE_RD_IDBASE_DATA_LENGTH, data.length
      );
    }

    byte[] deviceIdBytes = new byte[DeviceID.ENOCEAN_ESP_ID_LENGTH];
    System.arraycopy(
        data, ESP3_RESPONSE_RD_IDBASE_ID_INDEX, deviceIdBytes,
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
