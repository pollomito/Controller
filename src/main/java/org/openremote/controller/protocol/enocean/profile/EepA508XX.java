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

import static org.openremote.controller.protocol.enocean.Constants.*;

/**
 * A common superclass for EnOcean equipment profile (RORG = A5, FUNC = 08)
 * implementations to reuse code.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public abstract class EepA508XX implements EepReceive
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * EnOcean equipment profile (EEP) supply voltage data field name.
   */
  static final String EEP_A508XX_SVC_DATA_FIELD_NAME = "SVC";

  /**
   * Start bit of supply voltage data field.
   */
  static final int EEP_A508XX_SVC_OFFSET = 0;

  /**
   * Bit size of supply voltage data field.
   */
  static final int EEP_A508XX_SVC_SIZE = 8;

  /**
   * Begin of raw supply voltage value range.
   */
  static final int EEP_A508XX_SVC_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw supply voltage value range.
   */
  static final int EEP_A508XX_SVC_RAW_DATA_RANGE_MAX = 255;

  /**
   * Begin of scaled supply voltage value range.
   */
  static final double EEP_A508XX_SVC_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled supply voltage value range.
   */
  static final double EEP_A508XX_SVC_UNITS_DATA_RANGE_MAX = 5.1;

  /**
   * EnOcean equipment profile (EEP) illumination data field name.
   */
  static final String EEP_A508XX_ILL_DATA_FIELD_NAME = "ILL";

  /**
   * Start bit of illumination data field.
   */
  static final int EEP_A508XX_ILL_OFFSET = 8;

  /**
   * Bit size of illumination data field.
   */
  static final int EEP_A508XX_ILL_SIZE = 8;

  /**
   * Begin of raw illumination value range.
   */
  static final int EEP_A508XX_ILL_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw illumination value range.
   */
  static final int EEP_A508XX_ILL_RAW_DATA_RANGE_MAX = 255;

  /**
   * EnOcean equipment profile (EEP) temperature data field name.
   */
  static final String EEP_A508XX_TMP_DATA_FIELD_NAME = "TMP";

  /**
   * Start bit of temperature data field.
   */
  static final int EEP_A508XX_TMP_OFFSET = 16;

  /**
   * Bit size of temperature data field.
   */
  static final int EEP_A508XX_TMP_SIZE = 8;

  /**
   * Begin of raw temperature value range.
   */
  static final int EEP_A508XX_TMP_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw temperature value range.
   */
  static final int EEP_A508XX_TMP_RAW_DATA_RANGE_MAX = 255;

  /**
   * EnOcean equipment profile (EEP) 'PIR status' data field name.
   */
  public static final String EEP_A508XX_PIRS_DATA_FIELD_NAME = "PIRS";

  /**
   * Bit offset of EnOcean equipment profile (EEP) 'PIR status' data field.
   */
  public static final int EEP_A508XX_PIRS_OFFSET = 30;

  /**
   * Bit size of EnOcean equipment profile (EEP) 'PIR status' data field.
   */
  public static final int EEP_A508XX_PIRS_SIZE = 1;

  /**
   * Description for the OFF state of the 'PIR status' data field.
   */
  public static final String EEP_A508XX_PIRS_OFF_DESC = "PIR off";

  /**
   * Begin of the value range which represents the OFF state of the
   * 'PIR status' data field.
   */
  public static final int EEP_A508XX_PIRS_OFF_VALUE = 1;

  /**
   * Description for the ON state of the 'PIR status' data field.
   */
  public static final String EEP_A508XX_PIRS_ON_DESC = "PIR on";

  /**
   * Begin of the value range which represents the ON state of the
   * 'PIR status' data field.
   */
  public static final int EEP_A508XX_PIRS_ON_VALUE = 0;

  /**
   * EnOcean equipment profile (EEP) occupancy data field name.
   */
  public static final String EEP_A508XX_OCC_DATA_FIELD_NAME = "OCC";

  /**
   * Bit offset of EnOcean equipment profile (EEP) occupancy data field.
   */
  public static final int EEP_A508XX_OCC_OFFSET = 31;

  /**
   * Bit size of EnOcean equipment profile (EEP) occupancy data field.
   */
  public static final int EEP_A508XX_OCC_SIZE = 1;

  /**
   * Description for the 'Button pressed' state of the occupancy data field.
   */
  public static final String EEP_A508XX_OCC_BTN_PRESS_DESC = "Button pressed";

  /**
   * Occupancy data field value which represents the button pressed state.
   */
  public static final int EEP_A508XX_OCC_BTN_PRESS_VALUE = 0;

  /**
   * Description for the 'Button released' state of the occupancy data field.
   */
  public static final String EEP_A508XX_OCC_BTN_RELEASE_DESC = "Button released";

  /**
   * Occupancy data field value which represents the button released state.
   */
  public static final int EEP_A508XX_OCC_BTN_RELEASE_VALUE = 1;

  /**
   * Number of fractional digits to be used for illumination values.
   */
  static final int EEP_A508XX_ILL_FRACTIONAL_DIGITS = 1;

  /**
   * Number of fractional digits to be used for temperature values.
   */
  static final int EEP_A508XX_TMP_FRACTIONAL_DIGITS = 1;

  /**
   * Number of fractional digits to be used for supply voltage values.
   */
  static final int EEP_A508XX_SVC_FRACTIONAL_DIGITS = 1;


  // Enums ----------------------------------------------------------------------------------------

  private enum Command
  {
    /**
     * Receive supply voltage sensor value.
     */
    SUPPLY_VOLTAGE(SUPPLY_VOLTAGE_STATUS_COMMAND),

    /**
     * Receive illumination sensor value.
     */
    ILLUMINATION(ILLUMINATION_STATUS_COMMAND),

    /**
     * Receive temperature sensor value.
     */
    TEMPERATURE(TEMPERATURE_STATUS_COMMAND),

    /**
     * Receive PIR sensor value.
     */
    PIR(PIR_STATUS_COMMAND),

    /**
     * Receive occupancy button state.
     */
    OCCUPANCY(OCCUPANCY_STATUS_COMMAND);

    // Members ------------------------------------------------------------------------------------

    public static Command toCommand(String value, EepType eepType) throws ConfigurationException
    {
      if(value.equalsIgnoreCase(ILLUMINATION.toString()))
      {
        return ILLUMINATION;
      }

      else if(value.equalsIgnoreCase(SUPPLY_VOLTAGE.toString()))
      {
        return SUPPLY_VOLTAGE;
      }

      else if(value.equalsIgnoreCase(TEMPERATURE.toString()))
      {
        return TEMPERATURE;
      }

      else if(value.equalsIgnoreCase(PIR.toString()))
      {
        return PIR;
      }

      else if(value.equalsIgnoreCase(OCCUPANCY.toString()))
      {
        return OCCUPANCY;
      }

      else
      {
        throw new ConfigurationException(
            "Invalid command ''{0}'' in combination with " +
            "EnOcean equipment profile (EEP) ''{1}''.", value, eepType
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
   * Supply voltage value.
   */
  private Range supplyVoltage;

  /**
   * Illumination value.
   */
  private Range illumination;

  /**
   * Temperature value.
   */
  private Range temperature;

  /**
   * PIR status.
   */
  private Bool pirStatus;

  /**
   * Indicates if a occupancy button has been pressed.
   */
  private Bool occupancy;

  /**
   * Indicates if a tech in telegram has been received.
   */
  private Bool teachIn;

  /**
   * Contains the EnOcean equipment profile (EEP) data for
   * extracting sensor value.
   *
   * @see #illumination
   * @see #temperature
   * @see #supplyVoltage
   * @see #pirStatus
   * @see #occupancy
   */
  private EepData sensorData;

  /**
   * Contains the EnOcean equipment profile (EEP) data for
   * extracting the teach in control flag.
   *
   * @see #teachIn
   */
  private EepData controlData;

  /**
   * EnOcean equipment profile (EEP) type.
   */
  private EepType eepType;

  /**
   * EnOcean device ID for filtering received radio telegrams.
   */
  private DeviceID deviceID;

  /**
   * Type safe command from command configuration.
   */
  private Command command;


  // Constructors ---------------------------------------------------------------------------------

  public EepA508XX(EepType eepType, DeviceID deviceID, String commandString,
                   Range illumination, Range temperature) throws ConfigurationException
  {
    if(eepType == null)
    {
      throw new IllegalArgumentException("null EEP data type");
    }

    if(deviceID == null)
    {
      throw new IllegalArgumentException("null device ID");
    }

    if(commandString == null)
    {
      throw new IllegalArgumentException("null command string");
    }

    if(illumination == null)
    {
      throw new IllegalArgumentException("null illumination range");
    }

    if(temperature == null)
    {
      throw new IllegalArgumentException("null temperature range");
    }

    this.command = Command.toCommand(commandString, eepType);

    this.eepType = eepType;
    this.deviceID = deviceID;

    this.illumination = illumination;
    this.temperature = temperature;
    this.supplyVoltage = createSupplyVoltageRange();
    this.pirStatus = createPirStatusBool();
    this.occupancy = createOccupancyBool();
    this.teachIn = Bool.createTeachInFlag4BS();

    this.sensorData = new EepData(
        eepType, 4, illumination, temperature, supplyVoltage, pirStatus, occupancy
    );
    this.controlData = new EepData(eepType, 4, teachIn);
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

    if(command == Command.ILLUMINATION)
    {
      isUpdate = updateRangeVariable(illumination, telegram);
    }

    else if(command == Command.TEMPERATURE)
    {
      isUpdate = updateRangeVariable(temperature, telegram);
    }

    else if(command == Command.SUPPLY_VOLTAGE)
    {
      isUpdate = updateRangeVariable(supplyVoltage, telegram);
    }

    else if(command == Command.PIR)
    {
      isUpdate = updateBoolVariable(pirStatus, telegram);
    }

    else if(command == Command.OCCUPANCY)
    {
      isUpdate = updateBoolVariable(occupancy, telegram);
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

    if(command == Command.ILLUMINATION)
    {
      illumination.updateSensor(sensor);
    }

    else if(command == Command.TEMPERATURE)
    {
      temperature.updateSensor(sensor);
    }

    else if(command == Command.SUPPLY_VOLTAGE)
    {
      supplyVoltage.updateSensor(sensor);
    }

    else if(command == Command.PIR)
    {
      pirStatus.updateSensor(sensor);
    }

    else if(command == Command.OCCUPANCY)
    {
      occupancy.updateSensor(sensor);
    }
  }


  // Package Private Methods ----------------------------------------------------------------------

  /**
   * Returns the supply voltage value.
   *
   * @return the supply voltage value, <tt>null</tt> if no supply voltage sensor value
   *         has been received
   */
  Double getSupplyVoltage()
  {
    return supplyVoltage.rangeValue();
  }


  /**
   * Returns the illumination value.
   *
   * @return the illumination value, <tt>null</tt> if no illumination sensor value
   *         has been received
   */
  Double getIllumination()
  {
    return illumination.rangeValue();
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
   * Returns the PIR state.
   *
   * @return  <tt>true</tt> if PIR is ON, <tt>false</tt> if PIR is off,
   *          <tt>null</tt> if no PIR status has been received
   */
  Boolean isPirOn()
  {
    return pirStatus.boolValue();
  }

  /**
   * Returns the occupancy state.
   *
   * @return  <tt>true</tt> if the occupancy button has been pressed,
   *          <tt>false</tt> if the occupancy button is released,
   *          <tt>null</tt> if no occupancy status has been received
   */
  Boolean isOccupancy()
  {
    return occupancy.boolValue();
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
    return (teachIn.boolValue() != null && teachIn.boolValue());
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
   * Updates the given bool variable with EnOcean equipment profile data from
   * a received radio telegram.
   *
   * @param  bool      the bool variable to be updated
   *
   * @param  telegram  received radio telegram
   *
   * @return <tt>true</tt> if the sensor variable has been updated with a new value,
   *         <tt>false</tt> otherwise
   */
  private boolean updateBoolVariable(Bool bool, EspRadioTelegram telegram)
  {
    boolean isUpdate = false;
    Boolean oldValue = bool.boolValue();

    sensorData.update(telegram.getPayload());

    Boolean newValue = bool.boolValue();

    isUpdate = ((oldValue == null && newValue != null) ||
                (oldValue != null && newValue != null &&
                 newValue.booleanValue() != oldValue.booleanValue()));

    return isUpdate;
  }

  /**
   * Creates a range data type which represents the {@link #EEP_A508XX_SVC_DATA_FIELD_NAME}
   * data field.
   *
   * @return new range data type instance
   */
  private Range createSupplyVoltageRange()
  {
    return Range.createRange(
        EEP_A508XX_SVC_DATA_FIELD_NAME, EEP_A508XX_SVC_OFFSET, EEP_A508XX_SVC_SIZE,
        EEP_A508XX_SVC_RAW_DATA_RANGE_MIN, EEP_A508XX_SVC_RAW_DATA_RANGE_MAX,
        EEP_A508XX_SVC_UNITS_DATA_RANGE_MIN, EEP_A508XX_SVC_UNITS_DATA_RANGE_MAX,
        EEP_A508XX_SVC_FRACTIONAL_DIGITS
    );
  }

  /**
   * Creates a bool data type which represents the {@link #EEP_A508XX_PIRS_DATA_FIELD_NAME}
   * data field.
   *
   * @return new bool data type instance
   */
  private Bool createPirStatusBool()
  {
    return Bool.createBool(
        EEP_A508XX_PIRS_DATA_FIELD_NAME, EEP_A508XX_PIRS_OFFSET, EEP_A508XX_PIRS_SIZE,
        EEP_A508XX_PIRS_ON_DESC, EEP_A508XX_PIRS_ON_VALUE,
        EEP_A508XX_PIRS_OFF_DESC, EEP_A508XX_PIRS_OFF_VALUE
    );
  }

  /**
   * Creates a bool data type which represents the {@link #EEP_A508XX_OCC_DATA_FIELD_NAME}
   * data field.
   *
   * @return new bool data type instance
   */
  private Bool createOccupancyBool()
  {
    return Bool.createBool(
        EEP_A508XX_OCC_DATA_FIELD_NAME, EEP_A508XX_OCC_OFFSET, EEP_A508XX_OCC_SIZE,
        EEP_A508XX_OCC_BTN_PRESS_DESC, EEP_A508XX_OCC_BTN_PRESS_VALUE,
        EEP_A508XX_OCC_BTN_RELEASE_DESC, EEP_A508XX_OCC_BTN_RELEASE_VALUE
    );
  }
}
