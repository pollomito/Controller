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
import org.openremote.controller.protocol.enocean.datatype.Range;

/**
 * Represents the EnOcean equipment profile (EEP) 'A5-08-03'. <p>
 *
 * <pre>
 *
 *     +------+------+-------------------------------------------------+
 *     | RORG |  A5  |               4BS Telegram                      |
 *     +------+------+-------------------------------------------------+
 *     | FUNC |  08  |   Light, Temperature and Occupancy Sensor       |
 *     +------+------+-------------------------------------------------+
 *     | TYPE |  03  |Range 0lx to 1530lx, -30°C to +50°C and Occupancy|
 *     +------+------+-------------------------------------------------+
 *
 * </pre>
 *
 * The 'A5-08-03' profile data is transmitted by means of 4BS radio telegrams.
 * The EnOcean Equipment Profiles (EEP) 2.1 specification defines the profile
 * structure as follows:
 *
 * <pre>
 *                                                                        PIR Status
 *                                                                        PIRS
 *                                                              Learn Bit | |Occupancy
 *                                                                   LRNB | |OCC
 *                                                                    | | | | |
 *            +-------------------------------------------------------+ +-+ + +
 *            |Supply Voltage | Illumination  |  Temperature  |       | | | | |
 *            |      SVC      |     ILL       |      TMP      |       | | | | |
 *            +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     bits   |7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|
 *            +---------------------------------------------------------------+
 *     offset |0              |8              |16             |24             |
 *            +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     byte   |      DB3      |      DB2      |      DB1      |      DB0      |
 *            +-------------------------------+-------------------------------+
 *
 *
 *     +-------------------------------------------------------------------------------------------------+
 *     |Offset|Size|  Bitrange  |    Data      |ShortCut|   Description      |Valid Range| Scale    |Unit|
 *     +-------------------------------------------------------------------------------------------------+
 *     |0     |8   |DB3.7..DB3.0|Supply Voltage|  SVC   |Supply Voltage      |   0..255  |  0..5.1  |V   |
 *     +-------------------------------------------------------------------------------------------------+
 *     |8     |8   |DB2.7..DB2.0|Illumination  |  ILL   |Illumination(linear)|   0..255  |  0..1530 |lx  |
 *     +-------------------------------------------------------------------------------------------------+
 *     |16    |8   |DB1.7..DB1.0|Temperature   |  TMP   |Temperature(linear) |   0..255  |-30..+50  |°C  |
 *     +-------------------------------------------------------------------------------------------------+
 *     |24    |4   |DB0.7..DB0.4|Not Used                                                                |
 *     +-------------------------------------------------------------------------------------------------+
 *     |28    |1   |DB0.3       |Learn Bit     |  LRNB  |Learn bit           |Enum:                      |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |0: Teach-in telegram       |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |1: Data telegram           |
 *     +-------------------------------------------------------------------------------------------------+
 *     |29    |1   |DB0.2       |Not Used                                                                |
 *     +-------------------------------------------------------------------------------------------------+
 *     |30    |1   |DB0.1       |PIR Status    |  PIRS  |PIR Status          |Enum:                      |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |0: PIR on                  |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |1: PIR off                 |
 *     +-------------------------------------------------------------------------------------------------+
 *     |31    |1   |DB0.0       |Occupancy     |  OCC   |                    |Enum:                      |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |0: Button pressed          |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |1: Button released         |
 *     +-------------------------------------------------------------------------------------------------+
 *
 * </pre>
 *
 * @see org.openremote.controller.protocol.enocean.packet.radio.Esp34BSTelegram
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA50803 extends EepA508XX
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Begin of scaled illumination value range.
   */
  static final double EEP_A50803_ILL_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled illumination value range.
   */
  static final double EEP_A50803_ILL_UNITS_DATA_RANGE_MAX = 1530;

  /**
   * Begin of scaled temperature value range.
   */
  static final double EEP_A50803_TMP_UNITS_DATA_RANGE_MIN = -30;

  /**
   * End of scaled temperature value range.
   */
  static final double EEP_A50803_TMP_UNITS_DATA_RANGE_MAX = 50;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a 'A5-08-03' EnOcean equipment profile (EEP) instance with given
   * EnOcean device ID and command string.
   *
   * @param  deviceID       EnOcean device ID for filtering received radio telegrams
   *
   * @param  commandString  command string from command configuration
   *
   * @throws org.openremote.controller.protocol.enocean.ConfigurationException
   *           if the command string cannot be used in combination with this profile
   */
  public EepA50803(DeviceID deviceID, String commandString) throws ConfigurationException
  {
    super(
        EepType.EEP_TYPE_A50803, deviceID, commandString,
        Range.createRange(
            EEP_A508XX_ILL_DATA_FIELD_NAME, EEP_A508XX_ILL_OFFSET, EEP_A508XX_ILL_SIZE,
            EEP_A508XX_ILL_RAW_DATA_RANGE_MIN, EEP_A508XX_ILL_RAW_DATA_RANGE_MAX,
            EEP_A50803_ILL_UNITS_DATA_RANGE_MIN, EEP_A50803_ILL_UNITS_DATA_RANGE_MAX,
            EEP_A508XX_ILL_FRACTIONAL_DIGITS
        ),
        Range.createRange(
            EEP_A508XX_TMP_DATA_FIELD_NAME, EEP_A508XX_TMP_OFFSET, EEP_A508XX_TMP_SIZE,
            EEP_A508XX_TMP_RAW_DATA_RANGE_MIN, EEP_A508XX_TMP_RAW_DATA_RANGE_MAX,
            EEP_A50803_TMP_UNITS_DATA_RANGE_MIN, EEP_A50803_TMP_UNITS_DATA_RANGE_MAX,
            EEP_A508XX_TMP_FRACTIONAL_DIGITS
        )
    );
  }
}
