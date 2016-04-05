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

import static org.openremote.controller.protocol.enocean.Constants.ILLUMINATION_STATUS_COMMAND;
import static org.openremote.controller.protocol.enocean.Constants.SUPPLY_VOLTAGE_STATUS_COMMAND;

/**
 * A common superclass for EnOcean equipment light sensor profile implementations
 * to reuse code.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public abstract class EepA506XX implements EepReceive
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * EnOcean equipment profile (EEP) supply voltage data field name.
   */
  static final String EEP_A506XX_SVC_DATA_FIELD_NAME = "SVC";

  /**
   * Start bit of supply voltage data field.
   */
  static final int EEP_A506XX_SVC_OFFSET = 0;

  /**
   * Bit size of supply voltage data field.
   */
  static final int EEP_A506XX_SVC_SIZE = 8;

  /**
   * Begin of raw supply voltage value range.
   */
  static final int EEP_A506XX_SVC_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw supply voltage value range.
   */
  static final int EEP_A506XX_SVC_RAW_DATA_RANGE_MAX = 255;

  /**
   * Begin of scaled supply voltage value range.
   */
  static final double EEP_A506XX_SVC_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled supply voltage value range.
   */
  static final double EEP_A506XX_SVC_UNITS_DATA_RANGE_MAX = 5.1;

  /**
   * EnOcean equipment profile (EEP) 'range select' data field name.
   */
  static final String EEP_A506XX_RS_DATA_FIELD_NAME = "RS";

  /**
   * Start bit of 'range select' data field.
   */
  static final int EEP_A506XX_RS_OFFSET = 31;

  /**
   * Bit size of 'range select' data field.
   */
  static final int EEP_A506XX_RS_SIZE = 1;

  /**
   * Description for the first illumination range (ILL1) indicated
   * by the {@link #EEP_A506XX_RS_ILL1_VALUE} value.
   */
  static final String EEP_A506XX_RS_ILL1_DESC = "Range select DB1 (ILL1)";

  /**
   * EnOcean equipment profile (EEP) range select data field value
   * which indicates that the first illumination (ILL1) range is active.
   */
  static final int EEP_A506XX_RS_ILL1_VALUE = 0;

  /**
   * Description for the second illumination range (ILL2) indicated
   * by the {@link #EEP_A506XX_RS_ILL2_VALUE} value.
   */
  static final String EEP_A506XX_RS_ILL2_DESC = "Range select DB2 (ILL2)";

  /**
   * EnOcean equipment profile (EEP) range select data field value
   * which indicates that the second illumination (ILL2) range is active.
   */
  static final int EEP_A506XX_RS_ILL2_VALUE = 1;

  /**
   * Number of fractional digits to be used for illumination values.
   */
  static final int EEP_A506XX_ILL_FRACTIONAL_DIGITS = 1;

  /**
   * Number of fractional digits to be used for supply voltage values.
   */
  static final int EEP_A506XX_SVC_FRACTIONAL_DIGITS = 1;


  // Enums ----------------------------------------------------------------------------------------

  private enum Command
  {
    /**
     * Receive illumination sensor value.
     */
    ILLUMINATION(ILLUMINATION_STATUS_COMMAND),

    /**
     * Receive supply voltage sensor value.
     */
    SUPPLY_VOLTAGE(SUPPLY_VOLTAGE_STATUS_COMMAND);

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
   * Illumination value related to first range (ILL1).
   */
  private Range illumination1;

  /**
   * Illumination value related to second range (ILL2).
   */
  private Range illumination2;

  /**
   * Supply voltage value.
   */
  private Range supplyVoltage;

  /**
   * Indicates if the second illumination range is active.
   *
   * @see #illumination1
   * @see #illumination2
   */
  private Bool isIll2Selected;

  /**
   * Indicates if a teach in telegram has been received.
   */
  private Bool teachInFlag;


  /**
   * Contains the EnOcean equipment profile (EEP) data for
   * extracting the illumination and supply voltage sensor values.
   *
   * @see #illumination1
   * @see #illumination2
   * @see #supplyVoltage
   */
  private EepData sensorData;

  /**
   * Contains the EnOcean equipment profile (EEP) data for
   * extracting the teach in and range selector control flags.
   *
   * @see #teachInFlag
   * @see #isIll2Selected
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

  /**
   * Constructs a new EnOcean illumination sensor profile instance.
   *
   * @param eepType        EnOcean equipment profile type
   *
   * @param deviceID       EnOcean device ID for filtering received radio telegrams
   *
   * @param commandString  command string from command configuration
   *
   * @param ill1          first illumination range for retrieving raw illumination values from
   *                       EnOcean equipment profile (EEP) data and scaling to engineering units
   *
   * @param ill2          second illumination range for retrieving raw illumination values from
   *                       EnOcean equipment profile (EEP) data and scaling to engineering units
   *
   * @throws ConfigurationException
   *           if command string is invalid
   */
  public EepA506XX(EepType eepType, DeviceID deviceID, String commandString,
                   Range ill1, Range ill2) throws ConfigurationException
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

    if(ill1 == null)
    {
      throw new IllegalArgumentException("null illumination 1 range");
    }

    if(ill2 == null)
    {
      throw new IllegalArgumentException("null illumination 2 range");
    }

    this.command = Command.toCommand(commandString, eepType);

    this.eepType = eepType;
    this.deviceID = deviceID;

    this.illumination1 = ill1;
    this.illumination2 = ill2;
    this.supplyVoltage = createSupplyVoltageRange();
    this.teachInFlag = Bool.createTeachInFlag4BS();
    this.isIll2Selected = createRangeSelectBool();

    this.sensorData = new EepData(eepType, 4, illumination1, illumination2, supplyVoltage);
    this.controlData = new EepData(eepType, 4, teachInFlag, isIll2Selected);
  }


  // Implements Eep -------------------------------------------------------------------------------

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
      Range selectedIlluRange = isIllu2Selected() ? illumination2 : illumination1;

      isUpdate = updateSensorVariable(selectedIlluRange, telegram);
    }

    else if(command == Command.SUPPLY_VOLTAGE)
    {
      isUpdate = updateSensorVariable(supplyVoltage, telegram);
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
      if(isIllu2Selected())
      {
        illumination2.updateSensor(sensor);
      }
      else
      {
        illumination1.updateSensor(sensor);
      }
    }

    else if(command == Command.SUPPLY_VOLTAGE)
    {
      supplyVoltage.updateSensor(sensor);
    }
  }


  // Package Private Methods ----------------------------------------------------------------------

  /**
   * Returns the first illumination range value (ILL1).
   *
   * @return the illumination value, <tt>null</tt> if no illumination sensor value
   *         has been received
   */
  Double getIllumination1()
  {
    return illumination1.rangeValue();
  }

  /**
   * Returns the second illumination range value (ILL2).
   *
   * @return the illumination value, <tt>null</tt> if no illumination sensor value
   *         has been received
   */
  Double getIllumination2()
  {
    return illumination2.rangeValue();
  }

  /**
   * Returns the supply voltage value (SVC).
   *
   * @return the supply voltage value, <tt>null</tt> if not supply voltage value
   *         has been received
   */
  Double getSupplyVoltage()
  {
    return supplyVoltage.rangeValue();
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
   * Checks if the second illumination range (ILL2) has been selected.
   *
   * @return <tt>true</tt> if the second illumination range is selected,
   *        <tt>false</tt> otherwise
   */
  private boolean isIllu2Selected()
  {
    return (isIll2Selected.boolValue() != null && isIll2Selected.boolValue());
  }

  /**
   * Updates the given sensor variable with EnOcean equipment profile data from
   * a received radio telegram.
   *
   * @param sensorVariable  the sensor variable to be updated
   *
   * @param telegram        received radio telegram
   *
   * @return                <tt>true</tt> if the sensor variable has been updated with a new value,
   *                        <tt>false</tt> otherwise
   */
  private boolean updateSensorVariable(Range sensorVariable, EspRadioTelegram telegram)
  {
    boolean isUpdate = false;
    Double oldValue = sensorVariable.rangeValue();

    sensorData.update(telegram.getPayload());

    Double newValue = sensorVariable.rangeValue();

    isUpdate = ((oldValue == null && newValue != null) ||
                (oldValue != null && newValue != null &&
                 newValue.doubleValue() != oldValue.doubleValue()));

    return isUpdate;
  }

  /**
   * Creates a bool data type which represents the 'range select' (RS) data field.
   * The true value indicates that the second illumination range (ILL2) has been
   * selected.
   *
   * @return new bool data type instance
   */
  private Bool createRangeSelectBool()
  {
    return Bool.createBool(
        EEP_A506XX_RS_DATA_FIELD_NAME, EEP_A506XX_RS_OFFSET, EEP_A506XX_RS_SIZE,
        EEP_A506XX_RS_ILL2_DESC, EEP_A506XX_RS_ILL2_VALUE,
        EEP_A506XX_RS_ILL1_DESC, EEP_A506XX_RS_ILL1_VALUE
    );
  }

  /**
   * Creates a range data type which represents the 'supply voltage' (SVC) data field.
   *
   * @return new range data type instance
   */
  private Range createSupplyVoltageRange()
  {
    return Range.createRange(
        EEP_A506XX_SVC_DATA_FIELD_NAME, EEP_A506XX_SVC_OFFSET, EEP_A506XX_SVC_SIZE,
        EEP_A506XX_SVC_RAW_DATA_RANGE_MIN, EEP_A506XX_SVC_RAW_DATA_RANGE_MAX,
        EEP_A506XX_SVC_UNITS_DATA_RANGE_MIN, EEP_A506XX_SVC_UNITS_DATA_RANGE_MAX,
        EEP_A506XX_SVC_FRACTIONAL_DIGITS
    );
  }
}


