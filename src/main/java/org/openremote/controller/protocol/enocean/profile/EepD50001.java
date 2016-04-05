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

import static org.openremote.controller.protocol.enocean.profile.EepConstants.EEP_CONTACT_DATA_FIELD_NAME;
import static org.openremote.controller.protocol.enocean.Constants.CONTACT_STATUS_COMMAND;
import static org.openremote.controller.protocol.enocean.profile.EepConstants.*;

/**
 * Represents the EnOcean equipment profile (EEP) 'D5-00-01'. <p>
 *
 * <pre>
 *
 *     +------+------+-----------------------+
 *     | RORG |  D5  |      1BS Telegram     |
 *     +------+------+-----------------------+
 *     | FUNC |  00  | Contacts and Switches |
 *     +------+------+-----------------------+
 *     | TYPE |  01  | Single Input Contact  |
 *     +------+------+-----------------------+
 *
 * </pre>
 *
 * The 'D5-00-01' profile data is transmitted by means of 1BS radio telegrams.
 * The EnOcean Equipment Profiles (EEP) 2.1 specification defines the profile
 * structure as follows:
 *
 * <pre>
 *
 *
 *
 *            +-------------------------------+-------+---------------+-------+
 *            |            Not Used           |Learn  |   Not used    |Contact|
 *            |                               |Button |               | (CO)  |
 *            |                               |(LRN)  |               |       |
 *            +-------+-------+-------+-------+-------+-------+-------+-------+
 *     bits   |  7    |   6   |   5   |   4   |   3   |   2   |   1   |   0   |
 *            +-------+-------+-------+-------+-------+-------+-------+-------+
 *     offset |0                              |4                      |7      |
 *            +-------+-------+-------+-------+-------+-------+-------+-------+
 *     byte   |                              DB0                              |
 *            +---------------------------------------------------------------+
 *
 *
 *     +------------------------------------------------------------------------------------------+
 *     |Offset|Size|  Bitrange  |    Data   |ShortCut|   Description     |Valid Range| Scale |Unit|
 *     +------------------------------------------------------------------------------------------+
 *     |4     |1   |DB3.0       |  Learn    |  LRN   |                   |Enum:                   |
 *     |      |    |            |  Button   |        |                   +------------------------+
 *     |      |    |            |           |        |                   |0: pressed              |
 *     |      |    |            |           |        |                   +------------------------+
 *     |      |    |            |           |        |                   |1: not pressed          |
 *     +------------------------------------------------------------------------------------------+
 *     |7     |1   |DB0.0       |  Contact  |  CO    |                   |Enum:                   |
 *     |      |    |            |           |        |                   +------------------------+
 *     |      |    |            |           |        |                   |0: open                 |
 *     |      |    |            |           |        |                   +------------------------+
 *     |      |    |            |           |        |                   |1: closed               |
 *     +------------------------------------------------------------------------------------------+
 *
 * </pre>
 *
 * @see org.openremote.controller.protocol.enocean.packet.radio.Esp31BSTelegram
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepD50001 implements EepReceive
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * EnOcean equipment profile (EEP) 'learn button' data field name.
   */
  public static final String EEP_D50001_LRN_NAME = "LRN";

  /**
   * Description for an EnOcean learn telegram indicated by the
   * {@link #EEP_D50001_LRN_PRESSED_VALUE} value.
   */
  public static final String EEP_D50001_LRN_PRESSED_DESC = "pressed";

  /**
   * EnOcean equipment profile (EEP) 'learn button' data field value for
   * indicating a learn telegram.
   */
  public static final int EEP_D50001_LRN_PRESSED_VALUE = 0;

  /**
   * Description for a regular EnOcean data telegram indicated byte the
   * {@link #EEP_D50001_LRN_NOT_PRESSED_VALUE} value.
   */
  public static final String EEP_D50001_LRN_NOT_PRESSED_DESC = "not pressed";

  /**
   * EnOcean equipment profile (EEP) 'learn button' data field value for
   * indicating a regular data telegram.
   */
  public static final int EEP_D50001_LRN_NOT_PRESSED_VALUE = 1;

  /**
   * EnOcean equipment profile (EEP) input contact data field name.
   */
  public static final String EEP_D50001_CO_NAME = EEP_CONTACT_DATA_FIELD_NAME;

  /**
   * Bit offset of EnOcean equipment profile (EEP) contact data field.
   */
  public static final int EEP_D50001_CO_OFFSET = 7;

  /**
   * Bit size of EnOcean equipment profile (EEP) contact data field.
   */
  public static final int EEP_D50001_CO_SIZE = 1;

  /**
   * Description for the contact open state indicated by the
   * {@link #EEP_D50001_CO_OPEN_VALUE} value.
   */
  public static final String EEP_D50001_CO_OPEN_DESC = "open";

  /**
   * EnOcean equipment profile (EEP) contact data field value for
   * indicating that the contact is open.
   */
  public static final int EEP_D50001_CO_OPEN_VALUE = 0;

  /**
   * Description for the contact closed state indicated by the
   * {@link #EEP_D50001_CO_CLOSED_VALUE} value.
   */
  public static final String EEP_D50001_CO_CLOSED_DESC = "closed";

  /**
   * EnOcean equipment profile (EEP) contact data field value for
   * indicating that the contact is closed.
   */
  public static final int EEP_D50001_CO_CLOSED_VALUE = 1;


  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Indicates if the learn button has been pressed
   */
  private Bool learn;

  /**
   * Data instance for extracting the learn button pressed state.
   *
   * @see #learn
   */
  private EepData learnData;

  /**
   * Contact status (open/closed).
   */
  private Bool contact;

  /**
   * Data instance for extracting the contact state.
   *
   * @see #contact
   */
  private EepData contactData;

  /**
   * EnOcean equipment profile (EEP) type.
   */
  private EepType eepType;

  /**
   * EnOcean device ID for filtering received radio telegrams.
   */
  private DeviceID deviceID;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a 'D5-00-01' EnOcean equipment profile (EEP) instance with given
   * EnOcean device ID and command string.
   *
   * @param  deviceID       EnOcean device ID for filtering received radio telegrams
   *
   * @param  commandString  command string from command configuration
   *
   * @throws ConfigurationException
   *           if the command string cannot be used in combination with this profile
   */
  public EepD50001(DeviceID deviceID, String commandString) throws ConfigurationException
  {
    if(deviceID == null)
    {
      throw new IllegalArgumentException("null device ID");
    }

    if(commandString == null)
    {
      throw new IllegalArgumentException("null command string");
    }

    if(!CONTACT_STATUS_COMMAND.equalsIgnoreCase(commandString))
    {
      throw new ConfigurationException(
          "Invalid command ''{0}'' for EnOcean equipment profile (EEP) ''{1}''.",
          commandString, eepType
      );
    }

    this.eepType = EepType.EEP_TYPE_D50001;
    this.deviceID = deviceID;

    this.contact = createContactFlag();
    this.contactData = new EepData(eepType, 1, this.contact);

    this.learn = createLearnButtonFlag();
    this.learnData = new EepData(eepType, 1, this.learn);
  }

  // Object Overrides -----------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public String toString()
  {
    String contactState = "N/A";

    if(contact.boolValue() != null)
    {
      contactState = contact.boolValue() ? EEP_D50001_CO_CLOSED_DESC : EEP_D50001_CO_OPEN_DESC;
    }

    return "EEP (Type = '" + eepType + "', Contact = '" + contactState + "')";
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

    learnData.update(telegram.getPayload());

    if(isLearnButtonPressed())
    {
      return false;
    }

    Boolean oldContactState = contact.boolValue();

    contactData.update(telegram.getPayload());

    Boolean newContactState = contact.boolValue();

    return ((oldContactState == null && newContactState != null) ||
            (oldContactState != null && newContactState != null &&
             oldContactState.booleanValue() != newContactState.booleanValue()));
  }

  /**
   * {@inheritDoc}
   */
  @Override public void updateSensor(Sensor sensor) throws ConfigurationException
  {
    if(isLearnButtonPressed())
    {
      return;
    }

    contact.updateSensor(sensor);
  }


  // Package Private Methods ----------------------------------------------------------------------

  /**
   * Returns the contact state.
   *
   * @return  <tt>true</tt> if contact is closed, <tt>false</tt> if the contact is open,
   *          <tt>null</tt> if the contact state is unknown
   */
  Boolean isClosed()
  {
    return contact.boolValue();
  }


  // Private Instance Methods ---------------------------------------------------------------------

  /**
   * Checks if the learn button has been pressed.
   *
   * @return <tt>true</tt> if learn button has been pressed otherwise <tt>false</tt>
   */
  private boolean isLearnButtonPressed()
  {
     return (learn.boolValue() != null && learn.boolValue());
  }

  /**
   * Creates a bool data type which represents the EnOcean equipment profile (EEP)
   * 'learn button' data field.
   *
   * @return new bool data type instance
   */
  private Bool createLearnButtonFlag()
  {
    return Bool.createBool(
        EEP_D50001_LRN_NAME, EEP_LEARN_BIT_DATA_FIELD_OFFSET_1BS, EEP_LEARN_BIT_DATA_FIELD_SIZE,
        EEP_D50001_LRN_PRESSED_DESC, EEP_D50001_LRN_PRESSED_VALUE,
        EEP_D50001_LRN_NOT_PRESSED_DESC, EEP_D50001_LRN_NOT_PRESSED_VALUE
    );
  }

  /**
   * Creates a bool data type which represents the EnOcean equipment profile (EEP)
   * contact data field.
   *
   * @return new bool data type instance
   */
  private Bool createContactFlag()
  {
    return Bool.createBool(
        EEP_D50001_CO_NAME, EEP_D50001_CO_OFFSET, EEP_D50001_CO_SIZE,
        EEP_D50001_CO_CLOSED_DESC, EEP_D50001_CO_CLOSED_VALUE,
        EEP_D50001_CO_OPEN_DESC, EEP_D50001_CO_OPEN_VALUE
    );
  }
}

