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
package org.openremote.controller.protocol.enocean;

import static org.openremote.controller.protocol.enocean.profile.EepConstants.*;

/**
 * Constant class for global constants.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public final class Constants
{
  /**
   * Command string for configuring a command which receives temperature sensor values.
   */
  public static final String TEMPERATURE_STATUS_COMMAND = EEP_TEMPERATURE_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives humidity sensor values.
   */
  public static final String HUMIDITY_STATUS_COMMAND = EEP_HUMIDITY_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives humidity sensor values.
   */
  public static final String ILLUMINATION_STATUS_COMMAND = EEP_ILLUMINATION_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives gas sensor values.
   */
  public static final String CONCENTRATION_STATUS_COMMAND = EEP_CONCENTRATION_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives supply voltage sensor values.
   */
  public static final String SUPPLY_VOLTAGE_STATUS_COMMAND = EEP_SUPPLY_VOLTAGE_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives supply voltage sensor values
   * (Room Operating Panel).
   */
  public static final String SUPPLY_VOLTAGE_ROP_STATUS_COMMAND = EEP_SUPPLY_VOLTAGE_ROP_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives the state (open/closed) of
   * an input contact.
   */
  public static final String CONTACT_STATUS_COMMAND = EEP_CONTACT_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives the state (open/closed) of
   * an input contact (room operation panel).
   */
  public static final String CONTACT_STATE_STATUS_COMMAND = EEP_CONTACT_STATE_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives the state (on/off) of
   * a PIR sensor (occupancy).
   */
  public static final String PIR_STATUS_COMMAND = EEP_PIR_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives the state (pressed/released) of
   * an occupancy button.
   */
  public static final String OCCUPANCY_STATUS_COMMAND = EEP_OCCUPANCY_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives the occupancy
   * enabled/disabled state.
   */
  public static final String OCCUPANCY_ENABLE_STATUS_COMMAND = EEP_OCCUPANCY_ENABLE_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives the occupancy
   * button pressed/released state.
   */
  public static final String OCCUPANCY_BUTTON_STATUS_COMMAND = EEP_OCCUPANCY_BUTTON_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives the fan speed setting of
   * a turn switch.
   */
  public static final String FAN_SPEED_STATUS_COMMAND = EEP_FAN_SPEED_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives the set point value.
   */
  public static final String SET_POINT_STATUS_COMMAND = EEP_SET_POINT_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives the temperature set point value.
   */
  public static final String TEMP_SET_POINT_STATUS_COMMAND = EEP_TEMP_SET_POINT_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives the temperature set point value.
   */
  public static final String ILL_SET_POINT_STATUS_COMMAND = EEP_ILL_SET_POINT_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives the humidity set point value.
   */
  public static final String HUM_SET_POINT_STATUS_COMMAND = EEP_HUM_SET_POINT_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives the slide switch status value.
   */
  public static final String SLIDE_SWITCH_STATUS_COMMAND = EEP_SLIDE_SWITCH_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives a meter reading value
   * (automated meter reading (AMR) profiles).
   */
  public static final String AMR_METER_READING_STATUS_COMMAND = EEP_AMR_METER_READING_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives a measurement channel value
   * (automated meter reading (AMR) profiles).
   */
  public static final String AMR_MEASUREMENT_CHANNEL_STATUS_COMMAND = EEP_AMR_CHANNEL_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives a tariff info value
   * (automated meter reading (AMR) profiles).
   */
  public static final String AMR_TARIFF_STATUS_COMMAND = EEP_AMR_TARIFF_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives a 'data type (unit)' value
   * (automated meter reading (AMR) profiles).
   */
  public static final String AMR_DATA_TYPE_STATUS_COMMAND = EEP_AMR_DATA_TYPE_DATA_FIELD_NAME;

  /**
   * Command string for configuring a command which receives a divisor value
   * (automated meter reading (AMR) profiles).
   */
  public static final String AMR_DIVISOR_STATUS_COMMAND = EEP_AMR_DIVISOR_DATA_FIELD_NAME;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Private constructor to prevent instantiation of constant class.
   */
  private Constants()
  {

  }
}
