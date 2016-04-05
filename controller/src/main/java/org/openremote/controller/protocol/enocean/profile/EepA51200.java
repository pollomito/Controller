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
 * Represents the EnOcean equipment profile (EEP) 'A5-12-00'. <p>
 *
 * <pre>
 *
 *     +------+------+-----------------------------+
 *     | RORG |  A5  |        4BS Telegram         |
 *     +------+------+-----------------------------+
 *     | FUNC |  12  |Automated meter reading (AMR)|
 *     +------+------+-----------------------------+
 *     | TYPE |  00  |          Counter            |
 *     +------+------+-----------------------------+
 *
 * </pre>
 *
 * The 'A5-12-00' profile data is transmitted by means of 4BS radio telegrams.
 * The EnOcean Equipment Profiles (EEP) 2.1 specification defines the profile
 * structure as follows:
 *
 * <pre>
 *                                                                      Data Type
 *                                                                      DT
 *                                                             Learn Bit| |Divisor
 *                                                                  LRNB| |DIV
 *                                                                    | | |   |
 *            +-------------------------------------------------------+ + +   +
 *            |                  Meter Reading                |Channel| | |   |
 *            |                      MR                       |  CH   | | |   |
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
 *     |0     |24  |DB3.7..DB1.0|Meter Reading |   MR   |Cumulative value in |0..16777215|according |see |
 *     |      |    |            |              |        |m3 or current value |           |to DIV    |DT  |
 *     |      |    |            |              |        |in liter/s          |           |          |    |
 *     +-------------------------------------------------------------------------------------------------+
 *     |24    |4   |DB0.7..DB0.4|Measurement   |  CH    |                    |0..15      | 0..15    | 1  |
 *     |      |    |            |channel       |        |                    |           |          |    |
 *     +-------------------------------------------------------------------------------------------------+
 *     |28    |1   |DB0.3       |Learn Bit     |  LRNB  |Learn bit           |Enum:                      |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |0: Teach-in telegram       |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |1: Data telegram           |
 *     +-------------------------------------------------------------------------------------------------+
 *     |29    |1   |DB0.2       |data type     |  DT    |Current value or    |Enum:                      |
 *     |      |    |            |(unit)        |        |cumulative value    +---------------------------+
 *     |      |    |            |              |        |                    |0: Cumulative value m3     |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |1: Current value liter/s   |
 *     +-------------------------------------------------------------------------------------------------+
 *     |31    |2   |DB0.1..DB0.0|divisor       |  DIV   |Divisor for value   |Enum:                      |
 *     |      |    |            |(scale)       |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |0: x/1                     |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |1: x/10                    |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |1: x/100                   |
 *     |      |    |            |              |        |                    +---------------------------+
 *     |      |    |            |              |        |                    |1: x/1000                  |
 *     +-------------------------------------------------------------------------------------------------+
 *
 * </pre>
 *
 * @see org.openremote.controller.protocol.enocean.packet.radio.Esp34BSTelegram
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA51200 extends EepA512XX
{

  // Constructors ---------------------------------------------------------------------------------

  public EepA51200(DeviceID deviceID, String commandString) throws ConfigurationException
  {
    super(EepType.EEP_TYPE_A51200, deviceID, commandString);
  }
}
