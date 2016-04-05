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
package org.openremote.controller.protocol.enocean.profile;

import org.openremote.controller.protocol.enocean.ConfigurationException;
import org.openremote.controller.protocol.enocean.DeviceID;
import org.openremote.controller.protocol.enocean.EnOceanCommandBuilder;
import org.openremote.controller.protocol.enocean.datatype.Range;
import org.openremote.controller.utils.Logger;

import static org.openremote.controller.protocol.enocean.profile.EepConstants.EEP_TEMPERATURE_DATA_FIELD_NAME;

/**
 * Represents the EnOcean equipment profile (EEP) 'A5-02-04'. <p>
 *
 * <pre>
 *
 *     +------+------+-----------------------------------------+
 *     | RORG |  A5  |             4BS Telegram                |
 *     +------+------+-----------------------------------------+
 *     | FUNC |  02  |          Temperature Sensor             |
 *     +------+------+-----------------------------------------+
 *     | TYPE |  04  | Temperature Sensor Range -10°C to +30°C |
 *     +------+------+-----------------------------------------+
 *
 * </pre>
 *
 * The 'A5-02-04' profile data is transmitted by means of 4BS radio telegrams.
 * The EnOcean Equipment Profiles (EEP) 2.1 specification defines the profile
 * structure as follows:
 *
 * <pre>
 *                                                                  Learn Bit
 *                                                                    LRNB
 *                                                                    | |
 *            +---------------------------------------------------------------+
 *            |   Not Used    |   Not Used    |  Temperature  |       | |     |
 *            |               |               |     TMP       |       | |     |
 *            +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     bits   |7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|
 *            +---------------------------------------------------------------+
 *     offset |0              |8              |16             |24             |
 *            +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     byte   |      DB3      |      DB2      |      DB1      |      DB0      |
 *            +-------------------------------+-------------------------------+
 *
 *
 *     +---------------------------------------------------------------------------------------------+
 *     |Offset|Size|  Bitrange  |    Data   |ShortCut|   Description     |Valid Range|  Scale   |Unit|
 *     +---------------------------------------------------------------------------------------------+
 *     |0     |16  |DB3.7..DB2.0|Not Used                                                            |
 *     +---------------------------------------------------------------------------------------------+
 *     |16    |8   |DB1.7..DB1.0|Temperature|  TMP   |Temperature(linear)|  255..0   | -10..+30 |°C  |
 *     +---------------------------------------------------------------------------------------------+
 *     |24    |4   |DB0.7..DB0.4|Not Used                                                            |
 *     +---------------------------------------------------------------------------------------------+
 *     |28    |1   |DB0.3       |Learn Bit  |  LRNB  |Learn bit          |Enum:                      |
 *     |      |    |            |           |        |                   +---------------------------+
 *     |      |    |            |           |        |                   |0: Teach-in telegram       |
 *     |      |    |            |           |        |                   +---------------------------+
 *     |      |    |            |           |        |                   |1: Data telegram           |
 *     +---------------------------------------------------------------------------------------------+
 *     |29    |3   |DB0.2..DB0.0|Not Used                                                            |
 *     +---------------------------------------------------------------------------------------------+
 *
 * </pre>
 *
 * @see org.openremote.controller.protocol.enocean.packet.radio.Esp34BSTelegram
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA50204 extends EepA502XX
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Start bit of temperature data field.
   */
  static final int EEP_A50204_TMP_OFFSET = 16;

  /**
   * Bit size of temperature data field.
   */
  static final int EEP_A50204_TMP_SIZE = 8;

  /**
   * Begin of raw temperature value range.
   */
  static final int EEP_A50204_TMP_RAW_DATA_RANGE_MIN = 255;

  /**
   * End of raw temperature value range.
   */
  static final int EEP_A50204_TMP_RAW_DATA_RANGE_MAX = 0;

  /**
   * Begin of scaled temperature value range.
   */
  static final double EEP_A50204_TMP_UNITS_DATA_RANGE_MIN = -10;

  /**
   * End of scaled temperature value range.
   */
  static final double EEP_A50204_TMP_UNITS_DATA_RANGE_MAX = 30;


  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a 'A5-02-04' EnOcean equipment profile (EEP) instance with given
   * EnOcean device ID and command string.
   *
   * @param  deviceID       EnOcean device ID for filtering received radio telegrams
   *
   * @param  commandString  command string from command configuration
   *
   * @throws ConfigurationException
   *           if the command string cannot be used in combination with this profile
   */
  public EepA50204(DeviceID deviceID, String commandString) throws ConfigurationException
  {
    super(
        EepType.EEP_TYPE_A50204, deviceID, commandString,
        Range.createRange(
            EEP_TEMPERATURE_DATA_FIELD_NAME, EEP_A50204_TMP_OFFSET, EEP_A50204_TMP_SIZE,
            EEP_A50204_TMP_RAW_DATA_RANGE_MIN, EEP_A50204_TMP_RAW_DATA_RANGE_MAX,
            EEP_A50204_TMP_UNITS_DATA_RANGE_MIN, EEP_A50204_TMP_UNITS_DATA_RANGE_MAX,
            EEP_A502XX_TMP_FRACTIONAL_DIGITS
        )
    );
  }
}
