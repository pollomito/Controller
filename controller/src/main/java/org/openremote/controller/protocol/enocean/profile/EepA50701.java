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

import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.protocol.enocean.ConfigurationException;
import org.openremote.controller.protocol.enocean.DeviceID;
import org.openremote.controller.protocol.enocean.EnOceanCommandBuilder;
import org.openremote.controller.protocol.enocean.datatype.Bool;
import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram;
import org.openremote.controller.utils.Logger;

import static org.openremote.controller.protocol.enocean.Constants.PIR_STATUS_COMMAND;

/**
 * Represents the EnOcean equipment profile (EEP) 'A5-07-01'. <p>
 *
 * <pre>
 *
 *     +------+------+---------------------------------------+
 *     | RORG |  A5  |            4BS Telegram               |
 *     +------+------+---------------------------------------+
 *     | FUNC |  07  |          Occupancy Sensor             |
 *     +------+------+---------------------------------------+
 *     | TYPE |  01  |             Occupancy                 |
 *     +------+------+---------------------------------------+
 *
 * </pre>
 *
 * The 'A5-07-01' profile data is transmitted by means of 4BS radio telegrams.
 * The EnOcean Equipment Profiles (EEP) 2.1 specification defines the profile
 * structure as follows:
 *
 * <pre>
 *                                                                 Learn Bit
 *                                                                    LRNB
 *                                                                    | |
 *            +-------------------------------------------------------+ +-----+
 *            |   Not Used    |   Not Used    |   PIR Status  |       | |     |
 *            |               |               |      PIRS     |       | |     |
 *            +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     bits   |7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|
 *            +---------------------------------------------------------------+
 *     offset |0              |8              |16             |24             |
 *            +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     byte   |      DB3      |      DB2      |      DB1      |      DB0      |
 *            +-------------------------------+-------------------------------+
 *
 *
 *     +--------------------------------------------------------------------------------------------+
 *     |Offset|Size|  Bitrange  |    Data   |ShortCut|   Description       |Valid Range| Scale |Unit|
 *     +--------------------------------------------------------------------------------------------+
 *     |0     |16  |DB3.7..DB2.0|Not Used                                                           |
 *     +--------------------------------------------------------------------------------------------+
 *     |16    |8   |DB1.7..DB1.0|PIR Status |  PIRS  |PIR Status           |Enum:                   |
 *     |      |    |            |           |        |                     +------------------------+
 *     |      |    |            |           |        |                     |0..127:   PIR off       |
 *     |      |    |            |           |        |                     +------------------------+
 *     |      |    |            |           |        |                     |128..255: PIR on        |
 *     +--------------------------------------------------------------------------------------------+
 *     |24    |4   |DB0.7..DB0.4|Not Used                                                           |
 *     +--------------------------------------------------------------------------------------------+
 *     |28    |1   |DB0.3       |Learn Bit  |  LRNB  |Learn bit            |Enum:                   |
 *     |      |    |            |           |        |                     +------------------------+
 *     |      |    |            |           |        |                     |0: Teach-in telegram    |
 *     |      |    |            |           |        |                     +------------------------+
 *     |      |    |            |           |        |                     |1: Data telegram        |
 *     +--------------------------------------------------------------------------------------------+
 *     |29    |2   |DB0.2..DB0.1|Not Used                                                           |
 *     +--------------------------------------------------------------------------------------------+
 *
 * </pre>
 *
 * @see org.openremote.controller.protocol.enocean.packet.radio.Esp34BSTelegram
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA50701 implements EepReceive
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * EnOcean equipment profile (EEP) 'PIR status' data field name.
   */
  public static final String EEP_A50701_PIRS_DATA_FIELD_NAME = "PIRS";

  /**
   * Bit offset of EnOcean equipment profile (EEP) 'PIR status' data field.
   */
  public static final int EEP_A50701_PIRS_OFFSET = 16;

  /**
   * Bit size of EnOcean equipment profile (EEP) 'PIR status' data field.
   */
  public static final int EEP_A50701_PIRS_SIZE = 8;

  /**
   * Description for the OFF state of the 'PIR status' data field.
   */
  public static final String EEP_A50701_PIRS_OFF_DESC = "PIR off";

  /**
   * Begin of the value range which represents the OFF state of the
   * 'PIR status' data field.
   */
  public static final int EEP_A50701_PIRS_OFF_DATA_RANGE_MIN = 0;

  /**
   * End of the value range which represents the OFF state of the
   * 'PIR status' data field.
   */
  public static final int EEP_A50701_PIRS_OFF_DATA_RANGE_MAX = 127;

  /**
   * Description for the ON state of the 'PIR status' data field.
   */
  public static final String EEP_A50701_PIRS_ON_DESC = "PIR on";

  /**
   * Begin of the value range which represents the ON state of the
   * 'PIR status' data field.
   */
  public static final int EEP_A50701_PIRS_ON_DATA_RANGE_MIN = 128;

  /**
   * End of the value range which represents the ON state of the
   * 'PIR status' data field.
   */
  public static final int EEP_A50701_PIRS_ON_DATA_RANGE_MAX = 255;

  // Class Members --------------------------------------------------------------------------------


  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * EnOcean equipment profile (EEP) type.
   */
  private EepType eepType;

  /**
   * EnOcean device ID for filtering received radio telegrams.
   */
  private DeviceID deviceID;

  /**
   * PIR status.
   */
  private Bool pirStatus;

  /**
   * Indicates if a teach in telegram has been received.
   */
  private Bool teachInFlag;

  /**
   * Contains the EnOcean equipment profile (EEP) data for
   * extracting the PIR sensor value.
   *
   * @see #pirStatus
   */
  private EepData sensorData;

  /**
   * Contains the EnOcean equipment profile (EEP) data for
   * extracting the teach in control flag.
   *
   * @see #teachInFlag
   */
  private EepData controlData;

  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a 'A5-07-01' EnOcean equipment profile (EEP) instance with given
   * EnOcean device ID and command string.
   *
   * @param  deviceID       EnOcean device ID for filtering received radio telegrams
   *
   * @param  commandString  command string from command configuration
   *
   * @throws org.openremote.controller.protocol.enocean.ConfigurationException
   *           if the command string cannot be used in combination with this profile
   */
  public EepA50701(DeviceID deviceID, String commandString) throws ConfigurationException
  {
    if(deviceID == null)
    {
      throw new IllegalArgumentException("null device ID");
    }

    if(commandString == null)
    {
      throw new IllegalArgumentException("null command string");
    }

    if(!commandString.equalsIgnoreCase(PIR_STATUS_COMMAND))
    {
      throw new ConfigurationException(
          "Invalid command ''{0}'' in combination with " +
          "EnOcean equipment profile (EEP) ''A5-07-01''.", commandString
      );
    }


    this.eepType = EepType.EEP_TYPE_A50701;
    this.deviceID = deviceID;

    this.teachInFlag = Bool.createTeachInFlag4BS();
    this.controlData = new EepData(this.eepType, 4, this.teachInFlag);

    this.pirStatus = createPirStatusBool();
    this.sensorData = new EepData(this.eepType, 4, this.pirStatus);
  }


  // Implements EepReceive ------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public EepType getType()
  {
    return eepType;
  }

  /**
   * {@inheritDoc}
   */
  @Override public boolean update(EspRadioTelegram telegram)
  {
    if(!deviceID.equals(telegram.getSenderID()))
    {
      return false;
    }

    if(!eepType.isValidRadioTelegramRORG(telegram))
    {
      log.warn(
          "Discarded received radio telegram from device " +
          "with ID {0} because of a configuration error: " +
          "Command for device with ID {0} has been configured " +
          "with an invalid EEP {1} for this device.",
          deviceID, getType()
      );

      return false;
    }


    this.controlData.update(telegram.getPayload());

    if(isTeachInTelegram())
    {
      return false;
    }

    return updatePirStatus(telegram);
  }

  /**
   * {@inheritDoc}
   */
  @Override public void updateSensor(Sensor sensor) throws ConfigurationException
  {
    if(isTeachInTelegram())
    {
      return;
    }

    pirStatus.updateSensor(sensor);
  }


  // Package Private Methods ----------------------------------------------------------------------

  /**
   * Return the PIR status.
   *
   * @return <tt>true</tt> if the PIR status in ON, <tt>false</tt> if the PIR status is OFF,
   *         <tt>null</tt> if no PIR status has been received
   */
  Boolean getPirStatus()
  {
    return pirStatus.boolValue();
  }


  // Private Instance Methods ---------------------------------------------------------------------

  /**
   * Updates the {@link #pirStatus} value.
   *
   * @param  telegram EnOcean radio telegram
   * @return <tt>true</tt> if the PIR status value has been update, <tt>false</tt>
   *         otherwise
   */
  private boolean updatePirStatus(EspRadioTelegram telegram)
  {
    boolean isUpdate = false;
    Boolean oldPirStatus = pirStatus.boolValue();

    this.sensorData.update(telegram.getPayload());

    Boolean newPirStatus = pirStatus.boolValue();

    isUpdate = ((oldPirStatus == null && newPirStatus != null) ||
                (oldPirStatus != null && newPirStatus != null &&
                 newPirStatus != oldPirStatus));

    return isUpdate;
  }

  /**
   * Checks if the last received radio telegram was a teach in telegram.
   *
   * @return <tt>true</tt> if the last received radio telegram was a tech in telegram,
   *         <tt>false</tt> otherwise
   */
  private boolean isTeachInTelegram()
  {
    return (teachInFlag.boolValue() != null && teachInFlag.boolValue());
  }

  /**
   * Creates a bool data type which represents the {@link #EEP_A50701_PIRS_DATA_FIELD_NAME}
   * data field.
   *
   * @return new bool data type instance
   */
  private Bool createPirStatusBool()
  {
    return Bool.createBoolWithRange(
        EEP_A50701_PIRS_DATA_FIELD_NAME, EEP_A50701_PIRS_OFFSET, EEP_A50701_PIRS_SIZE,
        EEP_A50701_PIRS_ON_DESC, EEP_A50701_PIRS_ON_DATA_RANGE_MIN, EEP_A50701_PIRS_ON_DATA_RANGE_MAX,
        EEP_A50701_PIRS_OFF_DESC, EEP_A50701_PIRS_OFF_DATA_RANGE_MIN, EEP_A50701_PIRS_OFF_DATA_RANGE_MAX
    );
  }
}
