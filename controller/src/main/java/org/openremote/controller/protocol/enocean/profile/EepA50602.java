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

/**
 * Represents the EnOcean equipment profile (EEP) 'A5-06-02'. <p>
 *
 * <pre>
 *
 *     +------+------+---------------------------------------+
 *     | RORG |  A5  |            4BS Telegram               |
 *     +------+------+---------------------------------------+
 *     | FUNC |  06  |            Light Sensor               |
 *     +------+------+---------------------------------------+
 *     | TYPE |  02  |        Range 0lx to 1.020lx           |
 *     +------+------+---------------------------------------+
 *
 * </pre>
 *
 * The 'A5-06-02' profile data is transmitted by means of 4BS radio telegrams.
 * The EnOcean Equipment Profiles (EEP) 2.1 specification defines the profile
 * structure as follows:
 *
 * <pre>
 *                                                              Learn Bit   Range Select
 *                                                                   LRNB   RS
 *                                                                    | |   | |
 *            +-------------------------------------------------------+ +---+ +
 *            |Supply Voltage | Illumination  |  Illumination |       | |   | |
 *            |      SVC      |     ILL2      |      ILL1     |       | |   | |
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
 *     |0     |8   |DB3.7..DB3.0|Supply Voltage|  SVC   |Supplay Voltage     |   0..255  |  0..5.1  |V   |
 *     +-------------------------------------------------------------------------------------------------+
 *     |8     |8   |DB2.7..DB2.0|Illumination  |  ILL2  |Illumination(linear)|   0..255  |  0..510  |lx  |
 *     +-------------------------------------------------------------------------------------------------+
 *     |16    |8   |DB1.7..DB1.0|Illumination  |  ILL1  |Illumination(linear)|   0..255  |  0..1020 |lx  |
 *     +-------------------------------------------------------------------------------------------------+
 *     |24    |4   |DB0.7..DB0.4|Not Used                                                                |
 *     +-------------------------------------------------------------------------------------------------+
 *     |28    |1   |DB0.3       |Learn Bit     |  LRNB  |Learn bit           |Enum:                      |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |0: Teach-in telegram       |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |1: Data telegram           |
 *     +-------------------------------------------------------------------------------------------------+
 *     |29    |2   |DB0.2..DB0.1|Not Used                                                                |
 *     +-------------------------------------------------------------------------------------------------+
 *     |31    |1   |DB0.0       |Range         |   RS   |Range               |Enum:                      |
 *     |      |    |            |Select        |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |0: Range DB_1 (ILL1)       |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |1: Range DB_2 (ILL2)       |
 *     +-------------------------------------------------------------------------------------------------+
 *
 *
 * </pre>
 *
 * @see org.openremote.controller.protocol.enocean.packet.radio.Esp34BSTelegram
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA50602 extends EepA506XX
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * EnOcean equipment profile (EEP) 'illumination 1' data field name.
   */
  static final String EEP_A50602_ILL1_DATA_FIELD_NAME = "ILL1";

  /**
   * Start bit of 'illumination 1' data field.
   */
  static final int EEP_A50602_ILL1_OFFSET = 16;

  /**
   * Bit size of 'illumination 1' data field.
   */
  static final int EEP_A50602_ILL1_SIZE = 8;

  /**
   * Begin of raw illumination value range (first range).
   */
  static final int EEP_A50602_ILL1_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw illumination value range (first range).
   */
  static final int EEP_A50602_ILL1_RAW_DATA_RANGE_MAX = 255;

  /**
   * Begin of scaled illumination value range (first range).
   */
  static final double EEP_A50602_ILL1_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled illumination value range (first range).
   */
  static final double EEP_A50602_ILL1_UNITS_DATA_RANGE_MAX = 1020;

  /**
   * EnOcean equipment profile (EEP) 'illumination 2' data field name.
   */
  static final String EEP_A50602_ILL2_DATA_FIELD_NAME = "ILL2";

  /**
   * Start bit of 'illumination 2' data field.
   */
  static final int EEP_A50602_ILL2_OFFSET = 8;

  /**
   * Bit size of 'illumination 2' data field.
   */
  static final int EEP_A50602_ILL2_SIZE = 8;

  /**
   * Begin of raw illumination value range (second range).
   */
  static final int EEP_A50602_ILL2_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw illumination value range (second range).
   */
  static final int EEP_A50602_ILL2_RAW_DATA_RANGE_MAX = 255;

  /**
   * Begin of scaled illumination value range (second range).
   */
  static final double EEP_A50602_ILL2_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled illumination value range (second range).
   */
  static final double EEP_A50602_ILL2_UNITS_DATA_RANGE_MAX = 510;


  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a 'A5-06-02' EnOcean equipment profile (EEP) instance with given
   * EnOcean device ID and command string.
   *
   * @param  deviceID       EnOcean device ID for filtering received radio telegrams
   *
   * @param  commandString  command string from command configuration
   *
   * @throws ConfigurationException
   *           if the command string cannot be used in combination with this profile
   */
  public EepA50602(DeviceID deviceID, String commandString) throws ConfigurationException
  {
    super(
        EepType.EEP_TYPE_A50602, deviceID, commandString,
        Range.createRange(
            EEP_A50602_ILL1_DATA_FIELD_NAME, EEP_A50602_ILL1_OFFSET, EEP_A50602_ILL1_SIZE,
            EEP_A50602_ILL1_RAW_DATA_RANGE_MIN, EEP_A50602_ILL1_RAW_DATA_RANGE_MAX,
            EEP_A50602_ILL1_UNITS_DATA_RANGE_MIN, EEP_A50602_ILL1_UNITS_DATA_RANGE_MAX,
            EEP_A506XX_ILL_FRACTIONAL_DIGITS
        ),
        Range.createRange(
            EEP_A50602_ILL2_DATA_FIELD_NAME, EEP_A50602_ILL2_OFFSET, EEP_A50602_ILL2_SIZE,
            EEP_A50602_ILL2_RAW_DATA_RANGE_MIN, EEP_A50602_ILL2_RAW_DATA_RANGE_MAX,
            EEP_A50602_ILL2_UNITS_DATA_RANGE_MIN, EEP_A50602_ILL2_UNITS_DATA_RANGE_MAX,
            EEP_A506XX_ILL_FRACTIONAL_DIGITS
        )
    );
  }
}

