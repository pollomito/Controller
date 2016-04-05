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
import org.openremote.controller.protocol.enocean.datatype.Range;
import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram;
import org.openremote.controller.utils.Logger;

import static org.openremote.controller.protocol.enocean.Constants.TEMPERATURE_STATUS_COMMAND;
import static org.openremote.controller.protocol.enocean.Constants.HUMIDITY_STATUS_COMMAND;
import static org.openremote.controller.protocol.enocean.profile.EepConstants.EEP_HUMIDITY_DATA_FIELD_NAME;
import static org.openremote.controller.protocol.enocean.profile.EepConstants.EEP_TEMPERATURE_DATA_FIELD_NAME;

/**
 * Represents the EnOcean equipment profile (EEP) 'A5-04-01'. <p>
 *
 * <pre>
 *
 *     +------+------+---------------------------------------+
 *     | RORG |  A5  |            4BS Telegram               |
 *     +------+------+---------------------------------------+
 *     | FUNC |  04  |    Temperature and Humidity Sensor    |
 *     +------+------+---------------------------------------+
 *     | TYPE |  01  |   Range 0°C to 40°C and 0% to 100%    |
 *     +------+------+---------------------------------------+
 *
 * </pre>
 *
 * The 'A5-04-01' profile data is transmitted by means of 4BS radio telegrams.
 * The EnOcean Equipment Profiles (EEP) 2.1 specification defines the profile
 * structure as follows:
 *
 * <pre>
 *                                                              Learn Bit T-Sensor
 *                                                                   LRNB TSN
 *                                                                    | | | |
 *            +-------------------------------------------------------+ +-+ --+
 *            |   Not Used    |   Humidity    |  Temperature  |       | | | | |
 *            |               |               |     TMP       |       | | | | |
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
 *     |0     |16  |DB3.7..DB3.0|Not Used                                                           |
 *     +--------------------------------------------------------------------------------------------+
 *     |8     |8   |DB2.7..DB2.0|Humidity   |  HUM   |Rel. Humidity(linear)|  0..250  |0..100 |  %  |
 *     +--------------------------------------------------------------------------------------------+
 *     |16    |8   |DB1.7..DB1.0|Temperature|  TMP   |Temperature(linear)  |  0..250  |0..+40 | °C  |
 *     +--------------------------------------------------------------------------------------------+
 *     |24    |4   |DB0.7..DB0.4|Not Used                                                           |
 *     +--------------------------------------------------------------------------------------------+
 *     |28    |1   |DB0.3       |Learn Bit  |  LRNB  |Learn bit            |Enum:                   |
 *     |      |    |            |           |        |                     +------------------------+
 *     |      |    |            |           |        |                     |0: Teach-in telegram    |
 *     |      |    |            |           |        |                     +------------------------+
 *     |      |    |            |           |        |                     |1: Data telegram        |
 *     +--------------------------------------------------------------------------------------------+
 *     |29    |1   |DB0.2       |Not Used                                                           |
 *     +--------------------------------------------------------------------------------------------+
 *     |30    |1   |DB0.1       |T-Sensor   |  TSN   |Availability of the  |Enum:                   |
 *     |      |    |            |           |        |Temperature Sensor   +------------------------+
 *     |      |    |            |           |        |                     |0: not available        |
 *     |      |    |            |           |        |                     +------------------------+
 *     |      |    |            |           |        |                     |1: available            |
 *     +--------------------------------------------------------------------------------------------+
 *     |31    |1   |DB0.0       |Not Used                                                           |
 *     +--------------------------------------------------------------------------------------------+
 *
 * </pre>
 *
 * @see org.openremote.controller.protocol.enocean.packet.radio.Esp34BSTelegram
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA50401 implements EepReceive
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Start bit of temperature data field.
   */
  static final int EEP_A50401_TMP_OFFSET = 16;

  /**
   * Bit size of temperature data field.
   */
  static final int EEP_A50401_TMP_SIZE = 8;

  /**
   * Begin of raw temperature value range.
   */
  static final int EEP_A50401_TMP_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw temperature value range.
   */
  static final int EEP_A50401_TMP_RAW_DATA_RANGE_MAX = 250;

  /**
   * Begin of scaled temperature value range.
   */
  static final double EEP_A50401_TMP_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled temperature value range.
   */
  static final double EEP_A50401_TMP_UNITS_DATA_RANGE_MAX = 40;

  /**
   * Start bit of humidity data field.
   */
  static final int EEP_A50401_HUM_OFFSET = 8;

  /**
   * Bit size of humidity data field.
   */
  static final int EEP_A50401_HUM_SIZE = 8;

  /**
   * Begin of raw humidity value range.
   */
  static final int EEP_A50401_HUM_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw humidity value range.
   */
  static final int EEP_A50401_HUM_RAW_DATA_RANGE_MAX = 250;

  /**
   * Begin of scaled humidity value range.
   */
  static final double EEP_A50401_HUM_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled humidity value range.
   */
  static final double EEP_A50401_HUM_UNITS_DATA_RANGE_MAX = 100;

  /**
   * EnOcean equipment profile (EEP) temperature sensor availability field name.
   */
  static final String EEP_A50401_TSN_DATA_FIELD_NAME = "TSN";

  /**
   * Bit offset of EnOcean equipment profile (EEP) temperature sensor availability data field.
   */
  public static final int EEP_A50401_TSN_OFFSET = 30;

  /**
   * Bit size of EnOcean equipment profile (EEP) temperature sensor availability data field.
   */
  public static final int EEP_A50401_TSN_SIZE = 1;

  /**
   * Description for the temperature sensor available state indicated by the
   * {@link #EEP_A50401_TSN_AVAILABLE_VALUE} value.
   */
  public static final String EEP_A50401_TSN_AVAILABLE_DESC = "available";

  /**
   * EnOcean equipment profile (EEP) temperature sensor availability data field value
   * which indicates that the temperature sensor is available.
   */
  public static final int EEP_A50401_TSN_AVAILABLE_VALUE = 1;

  /**
   * Description for the temperature sensor not available state indicated by the
   * {@link #EEP_A50401_TSN_NOT_AVAILABLE_VALUE} value.
   */
  public static final String EEP_A50401_TSN_NOT_AVAILABLE_DESC = "not available";

  /**
   * EnOcean equipment profile (EEP) temperature sensor availability data field value
   * which indicates that the temperature sensor is not available.
   */
  public static final int EEP_A50401_TSN_NOT_AVAILABLE_VALUE = 0;

  /**
   * Number of fractional digits to be used for temperature and
   * humidity values.
   */
  public static final int EEP_A50401_FRACTIONAL_DIGITS = 1;

  // Enums ----------------------------------------------------------------------------------------

  private enum Command
  {
    /**
     * Receive temperature sensor value.
     */
    TEMPERATURE(TEMPERATURE_STATUS_COMMAND),

    /**
     * Receive humidity sensor value.
     */
    HUMIDITY(HUMIDITY_STATUS_COMMAND);


    // Members ------------------------------------------------------------------------------------

    public static Command toCommand(String value) throws ConfigurationException
    {
      if(value.equalsIgnoreCase(TEMPERATURE.toString()))
      {
        return TEMPERATURE;
      }

      else if(value.equalsIgnoreCase(HUMIDITY.toString()))
      {
        return HUMIDITY;
      }

      else
      {
        throw new ConfigurationException(
            "Invalid command ''{0}'' in combination with " +
            "EnOcean equipment profile (EEP) ''A5-04-01''.", value
        );
      }
    }

    private String commandString;

    private Command(String command)
    {
      this.commandString = command;
    }

    @Override public String toString()
    {
      return commandString;
    }
  }


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
   * Temperature sensor value.
   */
  private Range temperature;

  /**
   * Humidity sensor value.
   */
  private Range humidity;

  /**
   * Indicates if a teach in telegram has been received.
   */
  private Bool teachInFlag;

  /**
   * Indicates if the temperature sensor is available.
   */
  private Bool temperatureFlag;

  /**
   * Contains the EnOcean equipment profile (EEP) data for
   * extracting the temperature and humidity sensor values.
   *
   * @see #temperature
   * @see #humidity
   */
  private EepData sensorData;

  /**
   * Contains the EnOcean equipment profile (EEP) data for
   * extracting the teach in and temperature availability
   * control flags.
   *
   * @see #teachInFlag
   * @see #temperatureFlag
   */
  private EepData controlData;

  /**
   * Type safe command from command configuration.
   */
  private Command command;

  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a 'A5-04-01' EnOcean equipment profile (EEP) instance with given
   * EnOcean device ID and command string.
   *
   * @param  deviceID       EnOcean device ID for filtering received radio telegrams
   *
   * @param  commandString  command string from command configuration
   *
   * @throws ConfigurationException
   *           if the command string cannot be used in combination with this profile
   */
  public EepA50401(DeviceID deviceID, String commandString) throws ConfigurationException
  {
    if(deviceID == null)
    {
      throw new IllegalArgumentException("null device ID");
    }

    if(commandString == null)
    {
      throw new IllegalArgumentException("null command string");
    }

    this.command = Command.toCommand(commandString);

    this.eepType = EepType.EEP_TYPE_A50401;
    this.deviceID = deviceID;

    this.teachInFlag = Bool.createTeachInFlag4BS();
    this.temperatureFlag = createTemperatureAvailabilityFlag();
    this.controlData = new EepData(this.eepType, 4, this.teachInFlag, this.temperatureFlag);

    this.temperature = createTemperatureRange();
    this.humidity = createHumidityRange();
    this.sensorData = new EepData(this.eepType, 4, this.temperature, this.humidity);
  }

  // Object Overrides -----------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public String toString()
  {
    return "EEP ('" + eepType + "' : FUNC = 'Temperature and Humidity Sensor', " +
           "TYPE = 'Range 0°C to 40°C and 0% to 100%')";
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

    boolean isUpdate = false;

    if(command == Command.TEMPERATURE)
    {
      isUpdate = updateTemperature(telegram);
    }

    else if(command == Command.HUMIDITY)
    {
      isUpdate = updateHumidity(telegram);
    }

    return isUpdate;
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

    if(command == Command.TEMPERATURE)
    {
      if(isTemperatureAvailable())
      {
        temperature.updateSensor(sensor);
      }
    }

    else if(command == Command.HUMIDITY)
    {
      humidity.updateSensor(sensor);
    }
  }


  // Package Private Methods ----------------------------------------------------------------------

  /**
   * Returns the temperature value.
   *
   * @return the temperature value, <tt>null</tt> if no temperature sensor value
   *         has been received
   */
  Double getTemperature()
  {
    return temperature.rangeValue();
  }

  /**
   * Returns the humidity value.
   *
   * @return the humidity value, <tt>null</tt> if no humidity sensor value
   *         has been received
   */
  Double getHumidity()
  {
    return humidity.rangeValue();
  }



  // Private Instance Methods ---------------------------------------------------------------------

  /**
   * Updates the {@link #temperature} value if the EnOcean equipment profile (EEP)
   * data contains the temperature sensor value.
   *
   * @param  telegram  EnOcean radio telegram
   *
   * @return <tt>true</tt> if the temperature value has been updated, <tt>false</tt>
   *         otherwise
   */
  private boolean updateTemperature(EspRadioTelegram telegram)
  {
    boolean isUpdate = false;
    Double oldTempValue = temperature.rangeValue();

    if(isTemperatureAvailable())
    {
      this.sensorData.update(telegram.getPayload());

      Double newTempValue = temperature.rangeValue();

      isUpdate = ((oldTempValue == null && newTempValue != null) ||
                  (oldTempValue != null && newTempValue != null &&
                   newTempValue.doubleValue() != oldTempValue.doubleValue()));
    }

    return isUpdate;
  }

  /**
   * Updates the {@link #humidity} value.
   *
   * @param telegram  EnOcean radio telegram
   *
   * @return <tt>true</tt> if the humidity value has been updated, <tt>false</tt>
   *         otherwise
   */
  private boolean updateHumidity(EspRadioTelegram telegram)
  {
    boolean isUpdate = false;
    Double oldHumValue = humidity.rangeValue();

    this.sensorData.update(telegram.getPayload());

    Double newHumValue = humidity.rangeValue();

    isUpdate = ((oldHumValue == null && newHumValue != null) ||
                (oldHumValue != null && newHumValue != null &&
                 newHumValue.doubleValue() != oldHumValue.doubleValue()));

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
   * Checks if the last received radio telegram contained a temperature sensor
   * value.
   *
   * @return <tt>true</tt> if the last received radio telegram contained a
   *         temperature value, <tt>false</tt> otherwise
   */
  private boolean isTemperatureAvailable()
  {
    return (temperatureFlag.boolValue() != null && temperatureFlag.boolValue());
  }

  /**
   * Creates a bool data type which represents the {@link #EEP_A50401_TSN_DATA_FIELD_NAME}
   * data field.
   *
   * @return new bool data type instance
   */
  private Bool createTemperatureAvailabilityFlag()
  {
     return Bool.createBool(
         EEP_A50401_TSN_DATA_FIELD_NAME, EEP_A50401_TSN_OFFSET, EEP_A50401_TSN_SIZE,
         EEP_A50401_TSN_AVAILABLE_DESC, EEP_A50401_TSN_AVAILABLE_VALUE,
         EEP_A50401_TSN_NOT_AVAILABLE_DESC, EEP_A50401_TSN_NOT_AVAILABLE_VALUE
     );
  }

  /**
   * Creates a range data type which represents the temperature data field.
   *
   * @return new range data type instance
   */
  private Range createTemperatureRange()
  {
    return Range.createRange(
        EEP_TEMPERATURE_DATA_FIELD_NAME, EEP_A50401_TMP_OFFSET, EEP_A50401_TMP_SIZE,
        EEP_A50401_TMP_RAW_DATA_RANGE_MIN, EEP_A50401_TMP_RAW_DATA_RANGE_MAX,
        EEP_A50401_TMP_UNITS_DATA_RANGE_MIN, EEP_A50401_TMP_UNITS_DATA_RANGE_MAX,
        EEP_A50401_FRACTIONAL_DIGITS
    );
  }

  /**
   * Creates a range data type which represents the humidity data field.
   *
   * @return new range data type instance
   */
  private Range createHumidityRange()
  {
    return Range.createRange(
        EEP_HUMIDITY_DATA_FIELD_NAME, EEP_A50401_HUM_OFFSET, EEP_A50401_HUM_SIZE,
        EEP_A50401_HUM_RAW_DATA_RANGE_MIN, EEP_A50401_HUM_RAW_DATA_RANGE_MAX,
        EEP_A50401_HUM_UNITS_DATA_RANGE_MIN, EEP_A50401_HUM_UNITS_DATA_RANGE_MAX,
        EEP_A50401_FRACTIONAL_DIGITS
    );
  }
}
