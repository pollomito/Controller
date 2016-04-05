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
import org.openremote.controller.protocol.enocean.datatype.*;
import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram;
import org.openremote.controller.utils.Logger;

import static org.openremote.controller.protocol.enocean.Constants.*;

/**
 * A common superclass for EnOcean equipment profile (RORG = A5,
 * FUNC = 12 : Automated meter reading (AMR)) implementations to reuse code.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA512XX implements EepReceive
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * EnOcean equipment profile (EEP) meter reading data field name.
   */
  static final String EEP_A512XX_MR_DATA_FIELD_NAME = "MR";

  /**
   * Bit size of meter reading data field.
   */
  static final int EEP_A512XX_MR_SIZE = 24;

  /**
   * Bit offset of meter reading data field.
   */
  static final int EEP_A512XX_MR_OFFSET = 0;

  /**
   * Begin of raw meter reading value range.
   */
  static final int EEP_A512XX_MR_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw meter reading value range.
   */
  static final int EEP_A512XX_MR_RAW_DATA_RANGE_MAX = 16777215;

  /**
   * EnOcean equipment profile (EEP) measurement channel data field name.
   */
  static final String EEP_A512XX_CH_DATA_FIELD_NAME = "CH";

  /**
   * Bit size of measurement channel data field.
   */
  static final int EEP_A512XX_CH_SIZE = 4;

  /**
   * Bit offset of channel data field.
   */
  static final int EEP_A512XX_CH_OFFSET = 24;

  /**
   * Begin of raw measurement channel value range.
   */
  static final int EEP_A512XX_CH_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw measurement channel value range.
   */
  static final int EEP_A512XX_CH_RAW_DATA_RANGE_MAX = 15;

  /**
   * Begin of scaled measurement channel value range.
   */
  static final int EEP_A512XX_CH_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled measurement channel value range.
   */
  static final int EEP_A512XX_CH_UNITS_DATA_RANGE_MAX = 15;

  /**
   * EnOcean equipment profile (EEP) tariff info data field name.
   */
  static final String EEP_A512XX_TI_DATA_FIELD_NAME = "TI";

  /**
   * Bit size of tariff info data field.
   */
  static final int EEP_A512XX_TI_SIZE = 4;

  /**
   * Bit offset of tariff info data field.
   */
  static final int EEP_A512XX_TI_OFFSET = 24;

  /**
   * Begin of raw tariff info value range.
   */
  static final int EEP_A512XX_TI_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw tariff info value range.
   */
  static final int EEP_A512XX_TI_RAW_DATA_RANGE_MAX = 15;

  /**
   * Begin of scaled tariff info value range.
   */
  static final int EEP_A512XX_TI_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled tariff info value range.
   */
  static final int EEP_A512XX_TI_UNITS_DATA_RANGE_MAX = 15;

  /**
   * EnOcean equipment profile (EEP) 'data type (unit)' data field name.
   */
  public static final String EEP_A512XX_DT_DATA_FIELD_NAME = "DT";

  /**
   * Bit offset of 'data type (unit)' data field.
   */
  static final int EEP_A512XX_DT_OFFSET = 29;


  /**
   * Bit size of 'data type (unit)' data field.
   */
  static final int EEP_A512XX_DT_SIZE = 1;

  /**
   * Description for an EnOcean telegram which contains a cumulative value.
   */
  public static final String EEP_A512XX_DT_CUMULATIVE_DESC = "Cumulative value";

  /**
   * EnOcean equipment profile (EEP) 'data type (unit)' data field value for
   * indicating that the telegram contains a cumulative value.
   */
  public static final int EEP_A512XX_DT_CUMULATIVE_VALUE = 0;

  /**
   * Description for an EnOcean telegram which contains the current value.
   */
  public static final String EEP_A512XX_DT_CURRENT_DESC = "Current value";

  /**
   * EnOcean equipment profile (EEP) 'data type (unit)' data field value for
   * indicating that the telegram contains the current value.
   */
  public static final int EEP_A512XX_DT_CURRENT_VALUE = 1;

  /**
   * EnOcean equipment profile (EEP) 'divisor (scale)' data field name.
   */
  static final String EEP_A512XX_DIV_DATA_FIELD_NAME = "DIV";

  /**
   * Bit offset of 'divisor (scale)' data field.
   */
  static final int EEP_A512XX_DIV_OFFSET = 30;

  /**
   * Bit size of 'divisor (scale)' data field name.
   */
  static final int EEP_A512XX_DIV_SIZE = 2;

  /**
   * Begin of raw divisor value range which represents the divisor value 1.
   */
  static final int EEP_A512XX_DIV_1_RAW_VALUE_RANGE_MIN = 0;

  /**
   * End of raw divisor value range which represents the divisor value 1.
   */
  static final int EEP_A512XX_DIV_1_RAW_VALUE_RANGE_MAX = 0;

  /**
   * Description for divisor {@link #EEP_A512XX_DIV_1_VALUE}.
   */
  static final String EEP_A512XX_DIV_1_DESC = "x/1";

  /**
   * Divisor 1 value.
   */
  static final int EEP_A512XX_DIV_1_VALUE = 1;

  /**
   * Begin of raw divisor value range which represents the divisor value 10.
   */
  static final int EEP_A512XX_DIV_10_RAW_VALUE_RANGE_MIN = 1;

  /**
   * End of raw divisor value range which represents the divisor value 10.
   */
  static final int EEP_A512XX_DIV_10_RAW_VALUE_RANGE_MAX = 1;

  /**
   * Description for divisor {@link #EEP_A512XX_DIV_10_VALUE}.
   */
  static final String EEP_A512XX_DIV_10_DESC = "x/10";

  /**
   * Divisor 10 value.
   */
  static final int EEP_A512XX_DIV_10_VALUE = 10;

  /**
   * Begin of raw divisor value range which represents the divisor value 100.
   */
  static final int EEP_A512XX_DIV_100_RAW_VALUE_RANGE_MIN = 2;

  /**
   * End of raw divisor value range which represents the divisor value 100.
   */
  static final int EEP_A512XX_DIV_100_RAW_VALUE_RANGE_MAX = 2;

  /**
   * Description for divisor {@link #EEP_A512XX_DIV_100_VALUE}.
   */
  static final String EEP_A512XX_DIV_100_DESC = "x/100";

  /**
   * Divisor 100 value.
   */
  static final int EEP_A512XX_DIV_100_VALUE = 100;

  /**
   * Begin of raw divisor value range which represents the divisor value 1000.
   */
  static final int EEP_A512XX_DIV_1000_RAW_VALUE_RANGE_MIN = 3;

  /**
   * End of raw divisor value range which represents the divisor value 1000.
   */
  static final int EEP_A512XX_DIV_1000_RAW_VALUE_RANGE_MAX = 3;

  /**
   * Description for divisor {@link #EEP_A512XX_DIV_1000_VALUE}.
   */
  static final String EEP_A512XX_DIV_1000_DESC = "x/1000";

  /**
   * Divisor 1000 value.
   */
  static final int EEP_A512XX_DIV_1000_VALUE = 1000;

  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  // Enums ----------------------------------------------------------------------------------------

  protected enum Command
  {
    /**
     * Receive meter reading value.
     */
    METER_READING(AMR_METER_READING_STATUS_COMMAND),

    /**
     * Receive channel value.
     */
    CHANNEL(AMR_MEASUREMENT_CHANNEL_STATUS_COMMAND),

    /**
     * Receive tariff info value.
     */
    TARIFF(AMR_TARIFF_STATUS_COMMAND),

    /**
     * Receive 'data type (unit)' value (cumulative/current).
     */
    DATA_TYPE(AMR_DATA_TYPE_STATUS_COMMAND),

    /**
     * Receive divisor value.
     */
    DIVISOR(AMR_DIVISOR_STATUS_COMMAND);


    // Members ------------------------------------------------------------------------------------

    public static Command toCommand(String value, EepType eepType) throws ConfigurationException
    {

      if(value.equalsIgnoreCase(METER_READING.toString()))
      {
        return METER_READING;
      }

      else if(value.equalsIgnoreCase(CHANNEL.toString()))
      {
        return CHANNEL;
      }

      else if(value.equalsIgnoreCase(TARIFF.toString()))
      {
        return TARIFF;
      }

      else if(value.equalsIgnoreCase(DATA_TYPE.toString()))
      {
        return DATA_TYPE;
      }

      else if(value.equalsIgnoreCase(DIVISOR.toString()))
      {
        return DIVISOR;
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


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Indicates if a tech in telegram has been received.
   */
  private Bool teachIn;

  /**
   * Contains the EnOcean equipment profile (EEP) data for
   * extracting the teach in control flag.
   *
   * @see #teachIn
   */
  private EepData controlData;

  /**
   * Contains the EnOcean equipment profile (EEP) data for
   * extracting sensor value.
   */
  private EepData sensorData1;

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

  /**
   * Meter reading value.
   */
  private Range meterReadingValue;

  /**
   * Tariff info value.
   */
  private Range tariff;

  /**
   * Measurement channel value.
   */
  private Range channel;

  /**
   * Indicates if the {@link #meterReadingValue} contains the current value as
   * opposed to a cumulative value.
   */
  private Bool isCurrentValue;

  /**
   * Divisor for {@link #meterReadingValue}.
   */
  private Ordinal divisor;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * TODO
   *
   * @param  eepType
   *
   * @param  deviceID
   *
   * @param  commandString
   *
   * @throws ConfigurationException
   */
  public EepA512XX(EepType eepType, DeviceID deviceID, String commandString) throws ConfigurationException
  {
    if(eepType == null)
    {
      throw new IllegalArgumentException("null EEP data type");
    }

    if(deviceID == null)
    {
      throw new IllegalArgumentException("null device ID");
    }

    this.command = Command.toCommand(commandString, eepType);

    this.eepType = eepType;
    this.deviceID = deviceID;

    this.teachIn = Bool.createTeachInFlag4BS();
    this.controlData = new EepData(eepType, 4, teachIn);

    this.meterReadingValue = createMeterReadingRange(1);
    this.channel = createChannelRange();
    this.tariff = createTariffInfoRange();
    this.isCurrentValue = createDataTypeFlag();
    this.divisor = createDivisor();

    this.sensorData1 = new EepData(
        eepType, 4, channel, tariff, isCurrentValue, divisor
    );
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

    if(command == Command.METER_READING)
    {
      isUpdate = updateMeterReadingVariable(telegram);
    }

    else if(command == Command.CHANNEL)
    {
      isUpdate = updateRangeVariable(channel, telegram);
    }

    else if(command == Command.TARIFF)
    {
      isUpdate = updateRangeVariable(tariff, telegram);
    }

    else if(command == Command.DATA_TYPE)
    {
      isUpdate = updateBoolVariable(isCurrentValue, telegram);
    }

    else if(command == Command.DIVISOR)
    {
      isUpdate = updateOrdinalVariable(divisor, telegram);
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

    if(command == Command.METER_READING)
    {
      meterReadingValue.updateSensor(sensor);
    }

    else if(command == Command.CHANNEL)
    {
      channel.updateSensor(sensor);
    }

    else if(command == Command.TARIFF)
    {
      tariff.updateSensor(sensor);
    }

    else if(command == Command.DATA_TYPE)
    {
      isCurrentValue.updateSensor(sensor);
    }

    else if(command == Command.DIVISOR)
    {
      divisor.updateSensor(sensor);
    }
  }


  // Package Private Instance Methods -------------------------------------------------------------

  /**
   * Returns the meter reading value.
   *
   * @return meter reading value, <tt>null</tt> if no meter reading value
   *         has been received
   */
  Double getMeterReading()
  {
    return meterReadingValue.rangeValue();
  }

  /**
   * Returns the tariff info value.
   *
   * @return tariff info value, <tt>null</tt> if no tariff info value
   *         has been received
   */
  Integer getTariff()
  {
    if(tariff.rangeValue() == null)
    {
      return null;
    }

    else
    {
      return tariff.rangeValue().intValue();
    }
  }

  /**
   * Returns the channel value.
   *
   * @return channel value, <tt>null</tt> if no channel value
   *         has been received
   */
  Integer getChannel()
  {
    if(channel.rangeValue() == null)
    {
      return null;
    }

    else
    {
      return channel.rangeValue().intValue();
    }
  }

  /**
   * Returns the data type (unit).
   *
   * @return  <tt>true</tt> meter reading value is the current value,
   *          <tt>false</tt> meter reading value is a cumulative value,
   *          <tt>null</tt> if no data type has been received
   */
  Boolean isCurrentValue()
  {
    return isCurrentValue.boolValue();
  }

  /**
   * Returns the divisor.
   *
   * @return the divisor value, <tt>null</tt> if no divisor value
   *         has been received
   */
  Integer getDivisor()
  {
    return divisor.ordinalValue();
  }


  // Private Instance Methods ---------------------------------------------------------------------

  private boolean updateMeterReadingVariable(EspRadioTelegram telegram)
  {
    boolean isUpdate = false;
    Double oldValue = meterReadingValue.rangeValue();

    sensorData1.update(telegram.getPayload());

    Integer divisorValue = divisor.ordinalValue();
    if(divisorValue == null)
    {
      divisorValue = 1;
    }

    meterReadingValue = createMeterReadingRange(divisorValue);

    EepData data = new EepData(
        eepType, 4, meterReadingValue
    );

    data.update(telegram.getPayload());

    Double newValue = meterReadingValue.rangeValue();

    isUpdate = ((oldValue == null && newValue != null) ||
                (oldValue != null && newValue != null &&
                 newValue.doubleValue() != oldValue.doubleValue()));

    return isUpdate;
  }

  /**
   * Updates the given range variable with EnOcean equipment profile data from
   * the received radio telegram.
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

    sensorData1.update(telegram.getPayload());

    Double newValue = range.rangeValue();

    isUpdate = ((oldValue == null && newValue != null) ||
                (oldValue != null && newValue != null &&
                 newValue.doubleValue() != oldValue.doubleValue()));

    return isUpdate;
  }

  /**
   * Updates the given ordinal variable with EnOcean equipment profile data from
   * the received radio telegram.
   *
   * @param  ordinal      the ordinal variable to be updated
   *
   * @param  telegram  received radio telegram
   *
   * @return <tt>true</tt> if the sensor variable has been updated with a new value,
   *         <tt>false</tt> otherwise
   */
  private boolean updateOrdinalVariable(Ordinal ordinal, EspRadioTelegram telegram)
  {
    boolean isUpdate = false;
    Integer oldValue = ordinal.ordinalValue();

    sensorData1.update(telegram.getPayload());

    Integer newValue = ordinal.ordinalValue();

    isUpdate = ((oldValue == null && newValue != null) ||
                (oldValue != null && newValue != null &&
                 newValue.intValue() != oldValue.intValue()));

    return isUpdate;
  }

  /**
   * Updates the given bool variable with EnOcean equipment profile data from
   * the received radio telegram.
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

    sensorData1.update(telegram.getPayload());

    Boolean newValue = bool.boolValue();

    isUpdate = ((oldValue == null && newValue != null) ||
                (oldValue != null && newValue != null &&
                 newValue.booleanValue() != oldValue.booleanValue()));

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
    return (teachIn.boolValue() != null && teachIn.boolValue());
  }

  private Ordinal createDivisor()
  {
    ScaleCategory div1 = new ScaleCategory(
        EEP_A512XX_DIV_1_DESC,
        EEP_A512XX_DIV_1_RAW_VALUE_RANGE_MIN, EEP_A512XX_DIV_1_RAW_VALUE_RANGE_MAX,
        EEP_A512XX_DIV_1_DESC, EEP_A512XX_DIV_1_VALUE
    );

    ScaleCategory div10 = new ScaleCategory(
        EEP_A512XX_DIV_10_DESC,
        EEP_A512XX_DIV_10_RAW_VALUE_RANGE_MIN, EEP_A512XX_DIV_10_RAW_VALUE_RANGE_MAX,
        EEP_A512XX_DIV_10_DESC, EEP_A512XX_DIV_10_VALUE
    );

    ScaleCategory div100 = new ScaleCategory(
        EEP_A512XX_DIV_100_DESC,
        EEP_A512XX_DIV_100_RAW_VALUE_RANGE_MIN, EEP_A512XX_DIV_100_RAW_VALUE_RANGE_MAX,
        EEP_A512XX_DIV_100_DESC, EEP_A512XX_DIV_100_VALUE
    );

    ScaleCategory div1000 = new ScaleCategory(
        EEP_A512XX_DIV_1000_DESC,
        EEP_A512XX_DIV_1000_RAW_VALUE_RANGE_MIN, EEP_A512XX_DIV_1000_RAW_VALUE_RANGE_MAX,
        EEP_A512XX_DIV_1000_DESC, EEP_A512XX_DIV_1000_VALUE
    );

    CategoricalScale scale = new CategoricalScale(div1, div10, div100, div1000);
    EepDataField dataField = new EepDataField(EEP_A512XX_DIV_DATA_FIELD_NAME, EEP_A512XX_DIV_OFFSET, EEP_A512XX_DIV_SIZE);
    Ordinal div = new Ordinal(dataField, scale);

    return div;
  }

  private Bool createDataTypeFlag()
  {
    return Bool.createBool(
        EEP_A512XX_DT_DATA_FIELD_NAME, EEP_A512XX_DT_OFFSET, EEP_A512XX_DT_SIZE,
        EEP_A512XX_DT_CURRENT_DESC, EEP_A512XX_DT_CURRENT_VALUE,
        EEP_A512XX_DT_CUMULATIVE_DESC, EEP_A512XX_DT_CUMULATIVE_VALUE
    );
  }

  private Range createChannelRange()
  {
    return Range.createRange(
        EEP_A512XX_CH_DATA_FIELD_NAME, EEP_A512XX_CH_OFFSET, EEP_A512XX_CH_SIZE,
        EEP_A512XX_CH_RAW_DATA_RANGE_MIN, EEP_A512XX_CH_RAW_DATA_RANGE_MAX,
        EEP_A512XX_CH_UNITS_DATA_RANGE_MIN, EEP_A512XX_CH_UNITS_DATA_RANGE_MAX,
        0
    );
  }

  private Range createTariffInfoRange()
  {
    return Range.createRange(
        EEP_A512XX_TI_DATA_FIELD_NAME, EEP_A512XX_TI_OFFSET, EEP_A512XX_TI_SIZE,
        EEP_A512XX_TI_RAW_DATA_RANGE_MIN, EEP_A512XX_TI_RAW_DATA_RANGE_MAX,
        EEP_A512XX_TI_UNITS_DATA_RANGE_MIN, EEP_A512XX_TI_UNITS_DATA_RANGE_MAX,
        0
    );
  }

  private Range createMeterReadingRange(int divisor)
  {
    return Range.createRange(
        EEP_A512XX_MR_DATA_FIELD_NAME, EEP_A512XX_MR_OFFSET, EEP_A512XX_MR_SIZE,
        EEP_A512XX_MR_RAW_DATA_RANGE_MIN, EEP_A512XX_MR_RAW_DATA_RANGE_MAX,
        EEP_A512XX_MR_RAW_DATA_RANGE_MIN, EEP_A512XX_MR_RAW_DATA_RANGE_MAX/(double)divisor,
        (int)Math.log10(divisor)
    );
  }
}
