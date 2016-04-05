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

import static org.openremote.controller.protocol.enocean.Constants.TEMPERATURE_STATUS_COMMAND;

/**
 * A common superclass for EnOcean temperature sensor profile implementations
 * to reuse code.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public abstract class EepA502XX implements EepReceive
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Number of fractional digits for temperature values.
   */
  static final int EEP_A502XX_TMP_FRACTIONAL_DIGITS = 1;

  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Temperature value.
   */
  private Range temperature;

  /**
   * Data instance for extracting the temperature value.
   *
   * @see #temperature
   */
  private EepData temperatureData;

  /**
   * Indicates if a teach in telegram has been received.
   */
  private Bool teachInFlag;

  /**
   * Data instance for extracting the teach in flag.
   *
   * @see #teachInFlag
   */
  private EepData teachInData;

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
   * Constructs a new EnOcean temperature sensor profile instance.
   *
   * @param eepType         EnOcean equipment profile type
   *
   * @param deviceID        EnOcean device ID for filtering received radio telegrams
   *
   * @param commandString   command string from command configuration
   *
   * @param temperature     temperature range for retrieving raw temperature values from  EnOcean
   *                        equipment profile (EEP) data and scaling to engineering units
   *
   * @throws ConfigurationException
   *           if command string is invalid
   */
  public EepA502XX(EepType eepType, DeviceID deviceID, String commandString, Range temperature) throws ConfigurationException
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

    if(temperature == null)
    {
      throw new IllegalArgumentException("null temperature range");
    }

    if(!TEMPERATURE_STATUS_COMMAND.equalsIgnoreCase(commandString))
    {
      throw new ConfigurationException(
          "Invalid command ''{0}'' in combination with EnOcean equipment profile (EEP) ''{1}''.",
          commandString, eepType
      );
    }

    this.eepType = eepType;
    this.deviceID = deviceID;
    this.temperature = temperature;
    this.teachInFlag = Bool.createTeachInFlag4BS();

    this.temperatureData = new EepData(EepType.EEP_TYPE_A50205, 4, this.temperature);
    this.teachInData = new EepData(EepType.EEP_TYPE_A50205, 4, this.teachInFlag);
  }

  // Object Overrides -----------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public String toString()
  {
    return "EEP (Type = '" + eepType + "', Data = '" + temperatureData.dataAsString() + "')";
  }


  // Implements Eep -------------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  public EepType getType()
  {
    return eepType;
  }


  // Implements EepReceive ------------------------------------------------------------------------

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


    this.teachInData.update(telegram.getPayload());

    if(isTeachInTelegram())
    {
      return false;
    }


    Double oldTempValue = temperature.rangeValue();

    this.temperatureData.update(telegram.getPayload());

    Double newTempValue = temperature.rangeValue();

    return ((oldTempValue == null && newTempValue != null) ||
            (oldTempValue != null && newTempValue != null &&
             newTempValue.doubleValue() != oldTempValue.doubleValue()));
  }

  /**
   * {@inheritDoc}
   */
  @Override public void updateSensor(Sensor sensor)
  {
    if(isTeachInTelegram())
    {
      return;
    }

    temperature.updateSensor(sensor);
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

}
