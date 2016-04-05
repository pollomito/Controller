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
 * Represents the EnOcean equipment profile (EEP) 'A5-10-1A'. <p>
 *
 * <pre>
 *
 *     +------+------+--------------------------------------------------------------------+
 *     | RORG |  A5  |                       4BS Telegram                                 |
 *     +------+------+--------------------------------------------------------------------+
 *     | FUNC |  10  |                   Room Operating Panel                             |
 *     +------+------+--------------------------------------------------------------------+
 *     | TYPE |  1A  |Supply voltage, Temperature Set Point, Temperature Sensor, Fan Speed|
 *     |      |      |                    and Occupancy Control                           |
 *     +------+------+--------------------------------------------------------------------+
 *
 * </pre>
 *
 * The 'A5-10-1A' profile data is transmitted by means of 4BS radio telegrams.
 * The EnOcean Equipment Profiles (EEP) 2.1 specification defines the profile
 * structure as follows:
 *
 * <pre>
 *                                                                        Occupancy enable/disable
 *                                                                        OED
 *                                                              Learn Bit | |Occupancy button
 *                                                                   LRNB | |OB
 *                                                                    | | | | |
 *            +-------------------------------------------------------+ +-+ + +
 *            |Supply Voltage | Temp Setpoint |  Temperature  | |Fan  | | | | |
 *            |      SV       |      TMPSP    |      TMP      | |Speed| | | | |
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
 *     |0     |8   |DB3.7..DB3.0|Supply Voltage|  SV    |0..5.0 V linear;    |   0..250  | 0..5     |V   |
 *     |      |    |            |              |        |251-255 reserved for|           |          |    |
 *     |      |    |            |              |        |error code          |           |          |    |
 *     +-------------------------------------------------------------------------------------------------+
 *     |8     |8   |DB2.7..DB2.0|Temp Setpoint |  TMPSP |Temperature         | 250..0    | 0..+40   |°C  |
 *     |      |    |            |              |        |Set point (linear)  |           |          |    |
 *     +-------------------------------------------------------------------------------------------------+
 *     |16    |8   |DB1.7..DB1.0|Temperature   |  TMP   |Temperature(linear) | 250..0    | 0..+40   |°C  |
 *     +-------------------------------------------------------------------------------------------------+
 *     |24    |1   |DB0.7       |Not Used                                                                |
 *     +-------------------------------------------------------------------------------------------------+
 *     |25    |3   |DB0.6..DB0.4|Fan Speed     |  FAN   |Fan Speed           |Enum:                      |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |0: Auto                    |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |1: Speed 0                 |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |2: Speed 1                 |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |3: Speed 2                 |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |4: Speed 3                 |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |5: Speed 4                 |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |6: Speed 5                 |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |7: Off                     |
 *     +-------------------------------------------------------------------------------------------------+
 *     |28    |1   |DB0.3       |Learn Bit     |  LRNB  |Learn bit           |Enum:                      |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |0: Teach-in telegram       |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |1: Data telegram           |
 *     +-------------------------------------------------------------------------------------------------+
 *     |29    |1   |DB0.2       |Not Used                                                                |
 *     +-------------------------------------------------------------------------------------------------+
 *     |30    |1   |DB0.1       |Occupancy     |  OED   |                    |Enum:                      |
 *     |      |    |            |enable/disable|        |                    +---------------------------+
 *     |      |    |            |              |        |                    |0: Occupancy enabled       |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |1: Occupancy disabled      |
 *     +-------------------------------------------------------------------------------------------------+
 *     |31    |1   |DB0.0       |Occupancy     |  OB    |                    |Enum:                      |
 *     |      |    |            |button        |        |                    +---------------------------+
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
public class EepA5101A extends EepA510XX
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Bit offset of 'supply voltage' data field.
   */
  static final int EEP_A5101A_SV_OFFSET = 0;

  /**
   * Bit offset of 'temperature set point' data field.
   */
  static final int EEP_A5101A_TMPSP_OFFSET = 8;

  /**
   * Bit offset of temperature data field.
   */
  static final int EEP_A5101A_TMP_OFFSET = 16;

  /**
   * Bit offset of 'fan speed' data field.
   */
  static final int EEP_A5101A_FAN_OFFSET = 25;

  /**
   * Bit offset of 'occupancy enable/disable' data field.
   */
  static final int EEP_A5101A_OED_OFFSET = 30;

  /**
   * Bit offset of 'occupancy button' data field.
   */
  static final int EEP_A5101A_OB_OFFSET = 31;


  // Constructors ---------------------------------------------------------------------------------

  public EepA5101A(DeviceID deviceID, String commandString) throws ConfigurationException
  {
    super(EepType.EEP_TYPE_A5101A, deviceID);

    initCommand(commandString);

    this.supplyVoltage = createSupplyVoltageRange(EEP_A5101A_SV_OFFSET);
    this.setPoint = createTempSetPointRange(EEP_A5101A_TMPSP_OFFSET);
    this.temperature = createTemperatureRange8Bit3(EEP_A5101A_TMP_OFFSET);
    this.fanSpeed = createFanSpeedRange2(EEP_A5101A_FAN_OFFSET);
    this.occupancyEnabled = createOccupancyButtonEnableDisableBool(EEP_A5101A_OED_OFFSET);
    this.occupancy = createOccupancyButtonBool(EEP_A5101A_OB_OFFSET);

    this.sensorData = new EepData(
        EepType.EEP_TYPE_A5101A, 4, supplyVoltage, setPoint,
        temperature, fanSpeed, occupancyEnabled, occupancy
    );
  }


  // Private Instance Methods ---------------------------------------------------------------------

  private void initCommand(String commandString) throws ConfigurationException
  {
    command = Command.toCommand(commandString, getType());

    if( command != Command.SUPPLY_VOLTAGE        &&
        command != Command.TEMPERATURE_SET_POINT &&
        command != Command.TEMPERATURE           &&
        command != Command.FAN_SPEED             &&
        command != Command.OCCUPANCY_ENABLE      &&
        command != Command.OCCUPANCY_BUTTON)
    {
      throw new ConfigurationException(
          "Invalid command ''{0}'' in combination with " +
          "EnOcean equipment profile (EEP) ''{1}''.", commandString, getType()
      );
    }
  }
}
