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

/**
 * Represents the EnOcean equipment profile (EEP) 'A5-10-0A'. <p>
 *
 * <pre>
 *
 *     +------+------+--------------------------------------------------------------+
 *     | RORG |  A5  |                       4BS Telegram                           |
 *     +------+------+--------------------------------------------------------------+
 *     | FUNC |  10  |                   Room Operating Panel                       |
 *     +------+------+--------------------------------------------------------------+
 *     | TYPE |  0A  |Temperature Sensor, Set Point Adjust and Single Input Contact |
 *     +------+------+--------------------------------------------------------------+
 *
 * </pre>
 *
 * The 'A5-10-0A' profile data is transmitted by means of 4BS radio telegrams.
 * The EnOcean Equipment Profiles (EEP) 2.1 specification defines the profile
 * structure as follows:
 *
 * <pre>
 *
 *
 *                                                              Learn Bit   Contact State
 *                                                                   LRNB   CTST
 *                                                                    | |   | |
 *            +-------------------------------------------------------+ +---+ +
 *            |   Not Used    |   Set Point   |  Temperature  |       | |   | |
 *            |               |      SP       |      TMP      |       | |   | |
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
 *     |0     |8   |DB3.7..DB3.0|Not Used                                                                |
 *     +-------------------------------------------------------------------------------------------------+
 *     |8     |8   |DB2.7..DB2.0|Set point     |  SP    |Set point(linear)   |   0..255  | 0..255   |N/A |
 *     +-------------------------------------------------------------------------------------------------+
 *     |16    |8   |DB1.7..DB1.0|Temperature   |  TMP   |Temperature(linear) |   255..0  | 0..+40   |°C  |
 *     +-------------------------------------------------------------------------------------------------+
 *     |24    |4   |DB0.7..DB0.4|Not Used                                                                |
 *     +-------------------------------------------------------------------------------------------------+
 *     |28    |1   |DB0.3       |Learn Bit     |  LRNB  |Learn bit           |Enum:                      |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |0: Teach-in telegram       |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |1: Data telegram           |
 *     +-------------------------------------------------------------------------------------------------+
 *     |29    |1   |DB0.2..DB0.1|Not Used                                                                |
 *     +-------------------------------------------------------------------------------------------------+
 *     |31    |1   |DB0.0       |Contact State |  CTST  |Contact state       |Enum:                      |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |0: closed                  |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |1: open                    |
 *     +-------------------------------------------------------------------------------------------------+
 *
 * </pre>
 *
 * @see org.openremote.controller.protocol.enocean.packet.radio.Esp34BSTelegram
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA5100A extends EepA510XX
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Bit offset of 'set point' data field.
   */
  static final int EEP_A5100A_SP_OFFSET = 8;

  /**
   * Bit offset of temperature data field.
   */
  static final int EEP_A5100A_TMP_OFFSET = 16;

  /**
   * Bit offset of contact state data field.
   */
  static final int EEP_A5100A_CTST_OFFSET = 31;


  // Constructors ---------------------------------------------------------------------------------

  public EepA5100A(DeviceID deviceID, String commandString) throws ConfigurationException
  {
    super(EepType.EEP_TYPE_A5100A, deviceID);

    initCommand(commandString);

    this.setPoint = createSetPointRange8Bit(EEP_A5100A_SP_OFFSET);
    this.temperature = createTemperatureRange8Bit(EEP_A5100A_TMP_OFFSET);
    this.contact = createContactStateBool(EEP_A5100A_CTST_OFFSET);

    this.sensorData = new EepData(EepType.EEP_TYPE_A5100A, 4, setPoint, temperature, contact);
  }


  // Private Instance Methods ---------------------------------------------------------------------

  private void initCommand(String commandString) throws ConfigurationException
  {
    command = Command.toCommand(commandString, getType());

    if(command != Command.TEMPERATURE &&
       command != Command.CONTACT     &&
       command != Command.SET_POINT)
    {
      throw new ConfigurationException(
          "Invalid command ''{0}'' in combination with " +
          "EnOcean equipment profile (EEP) ''{1}''.", commandString, getType()
      );
    }
  }
}

