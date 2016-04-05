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

import static org.openremote.controller.protocol.enocean.Constants.CONCENTRATION_STATUS_COMMAND;
import static org.openremote.controller.protocol.enocean.Constants.HUMIDITY_STATUS_COMMAND;
import static org.openremote.controller.protocol.enocean.Constants.TEMPERATURE_STATUS_COMMAND;
import static org.openremote.controller.protocol.enocean.profile.EepConstants.EEP_CONCENTRATION_DATA_FIELD_NAME;
import static org.openremote.controller.protocol.enocean.profile.EepConstants.EEP_HUMIDITY_DATA_FIELD_NAME;
import static org.openremote.controller.protocol.enocean.profile.EepConstants.EEP_TEMPERATURE_DATA_FIELD_NAME;

/**
 * Represents the EnOcean equipment profile (EEP) 'A5-09-04'. <p>
 *
 * <pre>
 *
 *     +------+------+--------------+
 *     | RORG |  A5  | 4BS Telegram |
 *     +------+------+--------------+
 *     | FUNC |  09  |  Gas Sensor  |
 *     +------+------+--------------+
 *     | TYPE |  04  |  CO2 Sensor  |
 *     +------+------+--------------+
 *
 * </pre>
 *
 * The 'A5-09-04' profile data is transmitted by means of 4BS radio telegrams.
 * The EnOcean Equipment Profiles (EEP) 2.1 specification defines the profile
 * structure as follows:
 *
 * <pre>
 *                                                                      H-Sensor
 *                                                                      HSN
 *                                                             Learn Bit| |T-Sensor
 *                                                                  LRNB| |TSN
 *                                                                    | | | |
 *            +-------------------------------------------------------+ + + --+
 *            |   Humidity    | Concentration |  Temperature  |       | | | | |
 *            |     HUM       |     CONC      |     TMP       |       | | | | |
 *            +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     bits   |7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|
 *            +---------------------------------------------------------------+
 *     offset |0              |8              |16             |24             |
 *            +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     byte   |      DB3      |      DB2      |      DB1      |      DB0      |
 *            +-------------------------------+-------------------------------+
 *
 *
 *     +-----------------------------------------------------------------------------------------------+
 *     |Offset|Size|  Bitrange  |    Data     |ShortCut|   Description       |Valid Range| Scale |Unit |
 *     +-----------------------------------------------------------------------------------------------+
 *     |0     |8   |DB3.7..DB3.0|Humidity     |  HUM   |Rel. Humidity(linear)|  0..200   |0..100 |  %  |
 *     |      |    |            |             |        |0.5 % = 1 bit        |           |       |     |
 *     +-----------------------------------------------------------------------------------------------+
 *     |8     |8   |DB2.7..DB2.0|Concentration|  CONC  |Concentration(linear)|  0..255   |0..2550| ppm |
 *     |      |    |            |             |        |10 ppm = 1 bit       |           |       |     |
 *     +-----------------------------------------------------------------------------------------------+
 *     |16    |8   |DB1.7..DB1.0|Temperature  |  TMP   |Temperature(linear)  |  0..255   |0..+51 | °C  |
 *     |      |    |            |             |        |0.2 °C = 1 bit       |           |       |     |
 *     +-----------------------------------------------------------------------------------------------+
 *     |24    |4   |DB0.7..DB0.4|Not Used                                                              |
 *     +-----------------------------------------------------------------------------------------------+
 *     |28    |1   |DB0.3       |Learn Bit    |  LRNB  |Learn bit            |Enum:                    |
 *     |      |    |            |             |        |                     +-------------------------+
 *     |      |    |            |             |        |                     |0: Teach-in telegram     |
 *     |      |    |            |             |        |                     +-------------------------+
 *     |      |    |            |             |        |                     |1: Data telegram         |
 *     +-----------------------------------------------------------------------------------------------+
 *     |29    |1   |DB0.2       |H-Sensor     |  HSN   |Availability of the  |Enum:                    |
 *     |      |    |            |             |        |Humidity Sensor      +-------------------------+
 *     |      |    |            |             |        |                     |0: Humidity Sensor not   |
 *     |      |    |            |             |        |                     |   available             |
 *     |      |    |            |             |        |                     +-------------------------+
 *     |      |    |            |             |        |                     |1: Humidity Sensor       |
 *     |      |    |            |             |        |                     |   available             |
 *     +-----------------------------------------------------------------------------------------------+
 *     |30    |1   |DB0.1       |T-Sensor     |  TSN   |Availability of the  |Enum:                    |
 *     |      |    |            |             |        |Temperature Sensor   +-------------------------+
 *     |      |    |            |             |        |                     |0: Temperature Sensor not|
 *     |      |    |            |             |        |                     |   available             |
 *     |      |    |            |             |        |                     +-------------------------+
 *     |      |    |            |             |        |                     |1: Temperature Sensor    |
 *     |      |    |            |             |        |                     |   available             |
 *     +-----------------------------------------------------------------------------------------------+
 *     |31    |1   |DB0.0       |Not Used                                                              |
 *     +-----------------------------------------------------------------------------------------------+
 *
 * </pre>
 *
 * @see org.openremote.controller.protocol.enocean.packet.radio.Esp34BSTelegram
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA50904 implements EepReceive
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Start bit of humidity data field.
   */
  static final int EEP_A50904_HUM_OFFSET = 0;

  /**
   * Bit size of humidity data field.
   */
  static final int EEP_A50904_HUM_SIZE = 8;

  /**
   * Begin of raw humidity value range.
   */
  static final int EEP_A50904_HUM_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw humidity value range.
   */
  static final int EEP_A50904_HUM_RAW_DATA_RANGE_MAX = 200;

  /**
   * Begin of scaled humidity value range.
   */
  static final double EEP_A50904_HUM_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled humidity value range.
   */
  static final double EEP_A50904_HUM_UNITS_DATA_RANGE_MAX = 100;

  /**
   * Start bit of concentration data field.
   */
  static final int EEP_A50904_CONC_OFFSET = 8;

  /**
   * Bit size of concentration data field.
   */
  static final int EEP_A50904_CONC_SIZE = 8;

  /**
   * Begin of raw concentration value range.
   */
  static final int EEP_A50904_CONC_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw concentration value range.
   */
  static final int EEP_A50904_CONC_RAW_DATA_RANGE_MAX = 255;

  /**
   * Begin of scaled concentration value range.
   */
  static final double EEP_A50904_CONC_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled concentration value range.
   */
  static final double EEP_A50904_CONC_UNITS_DATA_RANGE_MAX = 2550;

  /**
   * Start bit of temperature data field.
   */
  static final int EEP_A50904_TMP_OFFSET = 16;

  /**
   * Bit size of temperature data field.
   */
  static final int EEP_A50904_TMP_SIZE = 8;

  /**
   * Begin of raw temperature value range.
   */
  static final int EEP_A50904_TMP_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw temperature value range.
   */
  static final int EEP_A50904_TMP_RAW_DATA_RANGE_MAX = 255;

  /**
   * Begin of scaled temperature value range.
   */
  static final double EEP_A50904_TMP_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled temperature value range.
   */
  static final double EEP_A50904_TMP_UNITS_DATA_RANGE_MAX = 51;

  /**
   * EnOcean equipment profile (EEP) humidity sensor availability field name.
   */
  static final String EEP_A50904_HSN_DATA_FIELD_NAME = "HSN";

  /**
   * Bit offset of EnOcean equipment profile (EEP) humidity sensor availability data field.
   */
  public static final int EEP_A50904_HSN_OFFSET = 29;

  /**
   * Bit size of EnOcean equipment profile (EEP) humidity sensor availability data field.
   */
  public static final int EEP_A50904_HSN_SIZE = 1;

  /**
   * Description for the humidity sensor available state indicated by the
   * {@link #EEP_A50904_HSN_AVAILABLE_VALUE} value.
   */
  public static final String EEP_A50904_HSN_AVAILABLE_DESC = "Humidity sensor available";

  /**
   * EnOcean equipment profile (EEP) humidity sensor availability data field value
   * which indicates that the temperature sensor is available.
   */
  public static final int EEP_A50904_HSN_AVAILABLE_VALUE = 1;

  /**
   * Description for the humidity sensor not available state indicated by the
   * {@link #EEP_A50904_HSN_NOT_AVAILABLE_VALUE} value.
   */
  public static final String EEP_A50904_HSN_NOT_AVAILABLE_DESC = "Humidity sensor not available";

  /**
   * EnOcean equipment profile (EEP) humidity sensor availability data field value
   * which indicates that the temperature sensor is not available.
   */
  public static final int EEP_A50904_HSN_NOT_AVAILABLE_VALUE = 0;

  /**
   * EnOcean equipment profile (EEP) temperature sensor availability field name.
   */
  static final String EEP_A50904_TSN_DATA_FIELD_NAME = "TSN";

  /**
   * Bit offset of EnOcean equipment profile (EEP) temperature sensor availability data field.
   */
  public static final int EEP_A50904_TSN_OFFSET = 30;

  /**
   * Bit size of EnOcean equipment profile (EEP) temperature sensor availability data field.
   */
  public static final int EEP_A50904_TSN_SIZE = 1;

  /**
   * Description for the temperature sensor available state indicated by the
   * {@link #EEP_A50904_TSN_AVAILABLE_VALUE} value.
   */
  public static final String EEP_A50904_TSN_AVAILABLE_DESC = "Temperature Sensor available";

  /**
   * EnOcean equipment profile (EEP) temperature sensor availability data field value
   * which indicates that the temperature sensor is available.
   */
  public static final int EEP_A50904_TSN_AVAILABLE_VALUE = 1;

  /**
   * Description for the temperature sensor not available state indicated by the
   * {@link #EEP_A50904_TSN_NOT_AVAILABLE_VALUE} value.
   */
  public static final String EEP_A50904_TSN_NOT_AVAILABLE_DESC = "Temperature Sensor not available";

  /**
   * EnOcean equipment profile (EEP) temperature sensor availability data field value
   * which indicates that the temperature sensor is not available.
   */
  public static final int EEP_A50904_TSN_NOT_AVAILABLE_VALUE = 0;

  /**
   * Number of fractional digits to be used for temperature values.
   */
  public static final int EEP_A50904_TMP_FRACTIONAL_DIGITS = 1;

  /**
   * Number of fractional digits to be used for humidity values.
   */
  public static final int EEP_A50904_HUM_FRACTIONAL_DIGITS = 1;

  /**
   * Number of fractional digits to be used for concentration values.
   */
  public static final int EEP_A50904_CONC_FRACTIONAL_DIGITS = 1;

  // Enums ----------------------------------------------------------------------------------------

  private enum Command
  {

    /**
     * Receive gas concentration sensor value.
     */
    CONCENTRATION(CONCENTRATION_STATUS_COMMAND),

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
      if(value.equalsIgnoreCase(CONCENTRATION.toString()))
      {
        return CONCENTRATION;
      }

      else if(value.equalsIgnoreCase(TEMPERATURE.toString()))
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
            "EnOcean equipment profile (EEP) ''A5-09-04''.", value
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
   * Gas concentration sensor value.
   */
  private Range concentration;

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
   * Indicates if the humidity sensor is available.
   */
  private Bool humidityFlag;

  /**
   * Contains the EnOcean equipment profile (EEP) data for
   * extracting the gas concentration, temperature and
   * humidity sensor values.
   *
   * @see #concentration
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
   * @see #humidityFlag
   */
  private EepData controlData;

  /**
   * Type safe command from command configuration.
   */
  private Command command;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a 'A5-09-04' EnOcean equipment profile (EEP) instance with given
   * EnOcean device ID and command string.
   *
   * @param  deviceID       EnOcean device ID for filtering received radio telegrams
   *
   * @param  commandString  command string from command configuration
   *
   * @throws ConfigurationException
   *           if the command string cannot be used in combination with this profile
   */
  public EepA50904(DeviceID deviceID, String commandString) throws ConfigurationException
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

    this.eepType = EepType.EEP_TYPE_A50904;
    this.deviceID = deviceID;

    this.teachInFlag = Bool.createTeachInFlag4BS();
    this.temperatureFlag = createTemperatureAvailabilityFlag();
    this.humidityFlag = createHumidityAvailabilityFlag();
    this.controlData = new EepData(
        this.eepType, 4, this.teachInFlag, this.temperatureFlag, this.humidityFlag
    );

    this.temperature = createTemperatureRange();
    this.humidity = createHumidityRange();
    this.concentration = createConcentrationRange();
    this.sensorData = new EepData(
        this.eepType, 4, this.temperature, this.humidity, this.concentration
    );
  }


  // Object Overrides -----------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public String toString()
  {
    return "EEP ('" + eepType + "' : FUNC = 'Gas Sensor', TYPE = 'CO2 Sensor')";
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

    if(command == Command.CONCENTRATION)
    {
      isUpdate = updateRangeVariable(concentration, telegram);
    }

    else if(command == Command.TEMPERATURE)
    {
      if(isTemperatureAvailable())
      {
        isUpdate = updateRangeVariable(temperature, telegram);
      }
      else
      {
        isUpdate = false;
      }
    }

    else if(command == Command.HUMIDITY)
    {
      if(isHumidityAvailable())
      {
        isUpdate = updateRangeVariable(humidity, telegram);
      }
      else
      {
        isUpdate = false;
      }
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

    if(command == Command.CONCENTRATION)
    {
      concentration.updateSensor(sensor);
    }

    else if(command == Command.TEMPERATURE)
    {
      if(isTemperatureAvailable())
      {
        temperature.updateSensor(sensor);
      }
    }

    else if(command == Command.HUMIDITY)
    {
      if(isHumidityAvailable())
      {
        humidity.updateSensor(sensor);
      }
    }
  }

  // Package Private Methods ----------------------------------------------------------------------

  /**
   * Returns the concentration value.
   *
   * @return the concentration value, <tt>null</tt> if no concentration sensor value
   *         has been received
   */
  Double getConcentration()
  {
    return concentration.rangeValue();
  }

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
   * Checks if the last received radio telegram contained a humidity sensor
   * value.
   *
   * @return <tt>true</tt> if the last received radio telegram contained a
   *         humidity value, <tt>false</tt> otherwise
   */
  private boolean isHumidityAvailable()
  {
    return (humidityFlag.boolValue() != null && humidityFlag.boolValue());
  }

  /**
   * Updates the given range variable with EnOcean equipment profile data from
   * a received radio telegram.
   *
   * @param range           the range variable to be updated
   *
   * @param telegram        received radio telegram
   *
   * @return                <tt>true</tt> if the sensor variable has been updated with a new value,
   *                        <tt>false</tt> otherwise
   */
  private boolean updateRangeVariable(Range range, EspRadioTelegram telegram)
  {
    boolean isUpdate = false;
    Double oldValue = range.rangeValue();

    sensorData.update(telegram.getPayload());

    Double newValue = range.rangeValue();

    isUpdate = ((oldValue == null && newValue != null) ||
        (oldValue != null && newValue != null &&
            newValue.doubleValue() != oldValue.doubleValue()));

    return isUpdate;
  }

  /**
   * Creates a range data type which represents the humidity data field.
   *
   * @return new range data type instance
   */
  private Range createHumidityRange()
  {
    return Range.createRange(
        EEP_HUMIDITY_DATA_FIELD_NAME, EEP_A50904_HUM_OFFSET, EEP_A50904_HUM_SIZE,
        EEP_A50904_HUM_RAW_DATA_RANGE_MIN, EEP_A50904_HUM_RAW_DATA_RANGE_MAX,
        EEP_A50904_HUM_UNITS_DATA_RANGE_MIN, EEP_A50904_HUM_UNITS_DATA_RANGE_MAX,
        EEP_A50904_HUM_FRACTIONAL_DIGITS
    );
  }

  /**
   * Creates a range data type which represents the concentration data field.
   *
   * @return new range data type instance
   */
  private Range createConcentrationRange()
  {
    return Range.createRange(
        EEP_CONCENTRATION_DATA_FIELD_NAME, EEP_A50904_CONC_OFFSET, EEP_A50904_CONC_SIZE,
        EEP_A50904_CONC_RAW_DATA_RANGE_MIN, EEP_A50904_CONC_RAW_DATA_RANGE_MAX,
        EEP_A50904_CONC_UNITS_DATA_RANGE_MIN, EEP_A50904_CONC_UNITS_DATA_RANGE_MAX,
        EEP_A50904_CONC_FRACTIONAL_DIGITS
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
        EEP_TEMPERATURE_DATA_FIELD_NAME, EEP_A50904_TMP_OFFSET, EEP_A50904_TMP_SIZE,
        EEP_A50904_TMP_RAW_DATA_RANGE_MIN, EEP_A50904_TMP_RAW_DATA_RANGE_MAX,
        EEP_A50904_TMP_UNITS_DATA_RANGE_MIN, EEP_A50904_TMP_UNITS_DATA_RANGE_MAX,
        EEP_A50904_TMP_FRACTIONAL_DIGITS
    );
  }

  /**
   * Creates a bool data type which represents the {@link #EEP_A50904_TSN_DATA_FIELD_NAME}
   * data field.
   *
   * @return new bool data type instance
   */
  private Bool createTemperatureAvailabilityFlag()
  {
    return Bool.createBool(
        EEP_A50904_TSN_DATA_FIELD_NAME, EEP_A50904_TSN_OFFSET, EEP_A50904_TSN_SIZE,
        EEP_A50904_TSN_AVAILABLE_DESC, EEP_A50904_TSN_AVAILABLE_VALUE,
        EEP_A50904_TSN_NOT_AVAILABLE_DESC, EEP_A50904_TSN_NOT_AVAILABLE_VALUE
    );
  }

  /**
   * Creates a bool data type which represents the {@link #EEP_A50904_HSN_DATA_FIELD_NAME}
   * data field.
   *
   * @return new bool data type instance
   */
  private Bool createHumidityAvailabilityFlag()
  {
    return Bool.createBool(
        EEP_A50904_HSN_DATA_FIELD_NAME, EEP_A50904_HSN_OFFSET, EEP_A50904_HSN_SIZE,
        EEP_A50904_HSN_AVAILABLE_DESC, EEP_A50904_HSN_AVAILABLE_VALUE,
        EEP_A50904_HSN_NOT_AVAILABLE_DESC, EEP_A50904_HSN_NOT_AVAILABLE_VALUE
    );
  }
}
