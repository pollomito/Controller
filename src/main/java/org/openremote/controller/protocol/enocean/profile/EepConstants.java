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

/**
 * Constant class for EnOcean equipment profile (EEP) related constants.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public final class EepConstants
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * EnOcean equipment profile (EEP) contact input data field name.
   */
  public static final String EEP_CONTACT_DATA_FIELD_NAME = "CO";

  /**
   * EnOcean equipment profile (EEP) contact state data field name (room operation panel).
   */
  public static final String EEP_CONTACT_STATE_DATA_FIELD_NAME = "CTST";

  /**
   * EnOcean equipment profile (EEP) temperature data field name.
   */
  public static final String EEP_TEMPERATURE_DATA_FIELD_NAME = "TMP";

  /**
   * EnOcean equipment profile (EEP) humidity data field name.
   */
  public static final String EEP_HUMIDITY_DATA_FIELD_NAME = "HUM";

  /**
   * EnOcean equipment profile (EEP) concentration data field name.
   */
  public static final String EEP_CONCENTRATION_DATA_FIELD_NAME = "CONC";

  /**
   * EnOcean equipment profile (EEP) illumination data field name.
   */
  public static final String EEP_ILLUMINATION_DATA_FIELD_NAME = "ILL";

  /**
   * EnOcean equipment profile (EEP) supply voltage data field name.
   */
  public static final String EEP_SUPPLY_VOLTAGE_DATA_FIELD_NAME = "SVC";

  /**
   * EnOcean equipment profile (EEP) supply voltage data field name (Room Operating Panel).
   */
  public static final String EEP_SUPPLY_VOLTAGE_ROP_DATA_FIELD_NAME = "SV";

  /**
   * EnOcean equipment profile (EEP) 'PIR status' data field name.
   */
  public static final String EEP_PIR_DATA_FIELD_NAME = "PIRS";

  /**
   * EnOcean equipment profile (EEP) occupancy data field name.
   */
  public static final String EEP_OCCUPANCY_DATA_FIELD_NAME = "OCC";

  /**
   * EnOcean equipment profile (EEP) occupancy enable/disable data field name.
   */
  public static final String EEP_OCCUPANCY_ENABLE_DATA_FIELD_NAME = "OED";

  /**
   * EnOcean equipment profile (EEP) occupancy button data field name.
   */
  public static final String EEP_OCCUPANCY_BUTTON_DATA_FIELD_NAME = "OB";

  /**
   * EnOcean equipment profile (EEP) fan speed data field name.
   */
  public static final String EEP_FAN_SPEED_DATA_FIELD_NAME = "FAN";

  /**
   * EnOcean equipment profile (EEP) set point data field name.
   */
  public static final String EEP_SET_POINT_DATA_FIELD_NAME = "SP";

  /**
   * EnOcean equipment profile (EEP) temperature set point data field name.
   */
  public static final String EEP_TEMP_SET_POINT_DATA_FIELD_NAME = "TMPSP";

  /**
   * EnOcean equipment profile (EEP) illumination set point data field name.
   */
  public static final String EEP_ILL_SET_POINT_DATA_FIELD_NAME = "ILLSP";

  /**
   * EnOcean equipment profile (EEP) humidity set point data field name.
   */
  public static final String EEP_HUM_SET_POINT_DATA_FIELD_NAME = "HUMSP";

  /**
   * EnOcean equipment profile (EEP) slide switch data field name.
   */
  public static final String EEP_SLIDE_SWITCH_DATA_FIELD_NAME = "SLSW";

  /**
   * EnOcean equipment profile (EEP) learn bit data field name.
   */
  public static final String EEP_LEARN_BIT_DATA_FIELD_NAME = "LRNB";

  /**
   * EnOcean equipment profile (EEP) meter reading data field name
   * (automated meter reading (AMR) profiles).
   */
  public static final String EEP_AMR_METER_READING_DATA_FIELD_NAME = "MR";

  /**
   * EnOcean equipment profile (EEP) measurement channel data field name
   * (automated meter reading (AMR) profiles).
   */
  public static final String EEP_AMR_CHANNEL_DATA_FIELD_NAME = "CH";

  /**
   * EnOcean equipment profile (EEP) tariff data field name
   * (automated meter reading (AMR) profiles).
   */
  public static final String EEP_AMR_TARIFF_DATA_FIELD_NAME = "TI";

  /**
   * EnOcean equipment profile (EEP) 'data type (unit)' data field name
   * (automated meter reading (AMR) profiles).
   */
  public static final String EEP_AMR_DATA_TYPE_DATA_FIELD_NAME = "DT";

  /**
   * EnOcean equipment profile (EEP) divisor data field name
   * (automated meter reading (AMR) profiles).
   */
  public static final String EEP_AMR_DIVISOR_DATA_FIELD_NAME = "DIV";


  /**
   * Bit offset of EnOcean equipment profile (EEP) learn bit data field for
   * {@link org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram.RORG#BS4 4BS}
   * radio telegrams.
   */
  public static final int EEP_LEARN_BIT_DATA_FIELD_OFFSET_4BS = 28;

  /**
   * Bit offset of EnOcean equipment profile (EEP) learn bit data field for
   * {@link org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram.RORG#BS1 1BS}
   * radio telegrams.
   */
  public static final int EEP_LEARN_BIT_DATA_FIELD_OFFSET_1BS = 4;

  /**
   * Bit size of EnOcean equipment profile (EEP) learn bit data field.
   */
  public static final int EEP_LEARN_BIT_DATA_FIELD_SIZE = 1;

  /**
   * Enocean equipment profile (EEP) learn bit data field value for indicating
   * a teach in telegram.
   *
   * @see #EEP_LEARN_BIT_DATA_FIELD_NAME
   * @see #EEP_LEARN_BIT_DATA_FIELD_TEACH_IN_TELEGRAM_DESC
   */
  public static final int EEP_LEARN_BIT_DATA_FIELD_TEACH_IN_TELEGRAM_VALUE = 0;

  /**
   * Enocean equipment profile (EEP) learn bit data field value for indicating
   * a regular data telegram.
   *
   * @see #EEP_LEARN_BIT_DATA_FIELD_NAME
   * @see #EEP_LEARN_BIT_DATA_FIELD_DATA_TELEGRAM_VALUE
   */
  public static final int EEP_LEARN_BIT_DATA_FIELD_DATA_TELEGRAM_VALUE = 1;

  /**
   * Description for an EnOcean teach in telegram indicated by the
   * {@link #EEP_LEARN_BIT_DATA_FIELD_TEACH_IN_TELEGRAM_VALUE}.
   *
   * @see #EEP_LEARN_BIT_DATA_FIELD_NAME
   */
  public static final String EEP_LEARN_BIT_DATA_FIELD_TEACH_IN_TELEGRAM_DESC = "Teach-in telegram";

  /**
   * Description for an EnOcean data telegram indicated by the
   * {@link #EEP_LEARN_BIT_DATA_FIELD_DATA_TELEGRAM_VALUE}.
   *
   * @see #EEP_LEARN_BIT_DATA_FIELD_NAME
   */
  public static final String EEP_LEARN_BIT_DATA_FIELD_DATA_TELEGRAM_DESC = "Data telegram";


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Private constructor to prevent instantiation of constant class.
   */
  private EepConstants()
  {

  }
}
