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
 * A common superclass for EnOcean equipment profile (RORG = A5, FUNC = 10 : Room Operating Panel)
 * implementations to reuse code.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public abstract class EepA510XX implements EepReceive
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * EnOcean equipment profile (EEP) temperature data field name.
   */
  static final String EEP_A510XX_TMP_DATA_FIELD_NAME = "TMP";

  /**
   * Bit size of temperature data field.
   */
  static final int EEP_A510XX_TMP_8BIT_SIZE = 8;

  /**
   * Begin of raw temperature value range (8 bit data field length).
   */
  static final int EEP_A510XX_TMP_8BIT_1_RAW_DATA_RANGE_MIN = 255;

  /**
   * End of raw temperature value range (8 bit data field length).
   */
  static final int EEP_A510XX_TMP_8BIT_1_RAW_DATA_RANGE_MAX = 0;

  /**
   * Begin of raw temperature value range (8 bit data field length).
   */
  static final int EEP_A510XX_TMP_8BIT_2_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw temperature value range (8 bit data field length).
   */
  static final int EEP_A510XX_TMP_8BIT_2_RAW_DATA_RANGE_MAX = 250;

  /**
   * Begin of raw temperature value range (8 bit data field length).
   */
  static final int EEP_A510XX_TMP_8BIT_3_RAW_DATA_RANGE_MIN = 250;

  /**
   * End of raw temperature value range (8 bit data field length).
   */
  static final int EEP_A510XX_TMP_8BIT_3_RAW_DATA_RANGE_MAX = 0;

  /**
   * Begin of scaled temperature value range (8 bit data field length).
   */
  static final double EEP_A510XX_TMP_8BIT_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled temperature value range (8 bit data field length).
   */
  static final double EEP_A510XX_TMP_8BIT_UNITS_DATA_RANGE_MAX = 40;

  /**
   * Bit size of temperature data field.
   */
  static final int EEP_A510XX_TMP_10BIT_SIZE = 10;

  /**
   * Begin of raw temperature value range (10 bit data field length).
   */
  static final int EEP_A510XX_TMP_10BIT_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw temperature value range (10 bit data field length).
   */
  static final int EEP_A510XX_TMP_10BIT_RAW_DATA_RANGE_MAX = 1023;

  /**
   * Begin of scaled temperature value range (10 bit data field length).
   */
  static final double EEP_A510XX_TMP_10BIT_UNITS_DATA_RANGE_MIN = -10;

  /**
   * End of scaled temperature value range (10 bit data field length).
   */
  static final double EEP_A510XX_TMP_10BIT_UNITS_DATA_RANGE_MAX = 41.2;

  /**
   * EnOcean equipment profile (EEP) humidity data field name.
   */
  static final String EEP_A510XX_HUM_DATA_FIELD_NAME = "HUM";

  /**
   * Bit size of humidity data field.
   */
  static final int EEP_A510XX_HUM_SIZE = 8;

  /**
   * Begin of raw humidity value range.
   */
  static final int EEP_A510XX_HUM_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw humidity value range.
   */
  static final int EEP_A510XX_HUM_RAW_DATA_RANGE_MAX = 250;

  /**
   * Begin of scaled humidity value range.
   */
  static final double EEP_A510XX_HUM_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled humidity value range.
   */
  static final double EEP_A510XX_HUM_UNITS_DATA_RANGE_MAX = 100;

  /**
   * EnOcean equipment profile (EEP) illumination data field name.
   */
  static final String EEP_A510XX_ILL_DATA_FIELD_NAME = "ILL";

  /**
   * Bit size of humidity data field.
   */
  static final int EEP_A510XX_ILL_SIZE = 8;

  /**
   * Begin of raw humidity value range.
   */
  static final int EEP_A510XX_ILL_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw humidity value range.
   */
  static final int EEP_A510XX_ILL_RAW_DATA_RANGE_MAX = 250;

  /**
   * Begin of scaled humidity value range.
   */
  static final double EEP_A510XX_ILL_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled humidity value range.
   */
  static final double EEP_A510XX_ILL_UNITS_DATA_RANGE_MAX = 1000;

  /**
   * EnOcean equipment profile (EEP) set point data field name.
   */
  static final String EEP_A510XX_SP_DATA_FIELD_NAME = "SP";

  /**
   * Bit size of set point data field.
   */
  static final int EEP_A510XX_SP_8BIT_SIZE = 8;

  /**
   * Begin of raw set point value range (8 bit data field length).
   */
  static final int EEP_A510XX_SP_8BIT_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw set point value range (8 bit data field length).
   */
  static final int EEP_A510XX_SP_8BIT_RAW_DATA_RANGE_MAX = 255;

  /**
   * Begin of scaled set point value range (8 bit data field length).
   */
  static final double EEP_A510XX_SP_8BIT_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled set point value range (8 bit data field length).
   */
  static final double EEP_A510XX_SP_8BIT_UNITS_DATA_RANGE_MAX = 255;

  /**
   * Bit size of set point data field.
   */
  static final int EEP_A510XX_SP_6BIT_SIZE = 6;

  /**
   * Begin of raw set point value range (6 bit data field length).
   */
  static final int EEP_A510XX_SP_6BIT_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw set point value range (6 bit data field length).
   */
  static final int EEP_A510XX_SP_6BIT_RAW_DATA_RANGE_MAX = 63;

  /**
   * Begin of scaled set point value range (6 bit data field length).
   */
  static final double EEP_A510XX_SP_6BIT_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled set point value range (6 bit data field length).
   */
  static final double EEP_A510XX_SP_6BIT_UNITS_DATA_RANGE_MAX = 63;

  /**
   * EnOcean equipment profile (EEP) set point data field name.
   */
  static final String EEP_A510XX_TMPSP_DATA_FIELD_NAME = "TMPSP";

  /**
   * Bit size of 'temperature set point' data field.
   */
  static final int EEP_A510XX_TMPSP_SIZE = 8;

  /**
   * Begin of raw 'temperature set point' value range.
   */
  static final int EEP_A510XX_TMPSP_RAW_DATA_RANGE_MIN = 250;

  /**
   * End of raw 'temperature set point' value range.
   */
  static final int EEP_A510XX_TMPSP_RAW_DATA_RANGE_MAX = 0;

  /**
   * Begin of scaled 'temperature set point' value range.
   */
  static final double EEP_A510XX_TMPSP_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled 'temperature set point' value range.
   */
  static final double EEP_A510XX_TMPSP_UNITS_DATA_RANGE_MAX = 40;

  /**
   * EnOcean equipment profile (EEP) illumination set point data field name.
   */
  static final String EEP_A510XX_ILLSP_DATA_FIELD_NAME = "ILLSP";

  /**
   * Bit size of 'illumination set point' data field.
   */
  static final int EEP_A510XX_ILLSP_SIZE = 8;

  /**
   * Begin of raw 'illumination set point' value range.
   */
  static final int EEP_A510XX_ILLSP_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw 'illumination set point' value range.
   */
  static final int EEP_A510XX_ILLSP_RAW_DATA_RANGE_MAX = 250;

  /**
   * Begin of scaled 'illumination set point' value range.
   */
  static final double EEP_A510XX_ILLSP_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled 'illumination set point' value range.
   */
  static final double EEP_A510XX_ILLSP_UNITS_DATA_RANGE_MAX = 1000;

  /**
   * EnOcean equipment profile (EEP) humidity set point data field name.
   */
  static final String EEP_A510XX_HUMSP_DATA_FIELD_NAME = "HUMSP";

  /**
   * Bit size of 'humidity set point' data field.
   */
  static final int EEP_A510XX_HUMSP_SIZE = 8;

  /**
   * Begin of raw 'humidity set point' value range.
   */
  static final int EEP_A510XX_HUMSP_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw 'humidity set point' value range.
   */
  static final int EEP_A510XX_HUMSP_RAW_DATA_RANGE_MAX = 250;

  /**
   * Begin of scaled 'humidity set point' value range.
   */
  static final double EEP_A510XX_HUMSP_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled 'humidity set point' value range.
   */
  static final double EEP_A510XX_HUMSP_UNITS_DATA_RANGE_MAX = 100;

  /**
   * EnOcean equipment profile (EEP) fan speed data field name.
   */
  static final String EEP_A510XX_FAN_DATA_FIELD_NAME = "FAN";

  /**
   * Bit size of temperature data field.
   */
  static final int EEP_A510XX_FAN_SIZE = 8;

  /**
   * Begin of raw fan speed value range which represents 'Stage Auto'
   */
  static final int EEP_A510XX_FAN_STAGE_AUTO_RAW_VALUE_RANGE_MIN = 210;

  /**
   * End of raw fan speed value range which represents 'Stage Auto'.
   */
  static final int EEP_A510XX_FAN_STAGE_AUTO_RAW_VALUE_RANGE_MAX = 255;

  /**
   * Fan speed integer value which represents 'Stage Auto'.
   */
  static final int EEP_A510XX_FAN_STAGE_AUTO_VALUE = 0;

  /**
   * 'Stage Auto' fan speed name.
   */
  static final String EEP_A510XX_FAN_STAGE_AUTO_NAME = "Stage Auto";

  /**
   * Begin of raw fan speed value range which represents 'Stage 0'
   */
  static final int EEP_A510XX_FAN_STAGE_0_RAW_VALUE_RANGE_MIN = 190;

  /**
   * End of raw fan speed value range which represents 'Stage 0'.
   */
  static final int EEP_A510XX_FAN_STAGE_0_RAW_VALUE_RANGE_MAX = 209;

  /**
   * Fan speed integer value which represents 'Stage 0'.
   */
  static final int EEP_A510XX_FAN_STAGE_0_VALUE = 1;

  /**
   * 'Stage 0' fan speed name.
   */
  static final String EEP_A510XX_FAN_STAGE_0_NAME = "Stage 0";

  /**
   * Begin of raw fan speed value range which represents 'Stage 1'
   */
  static final int EEP_A510XX_FAN_STAGE_1_RAW_VALUE_RANGE_MIN = 165;

  /**
   * End of raw fan speed value range which represents 'Stage 1'.
   */
  static final int EEP_A510XX_FAN_STAGE_1_RAW_VALUE_RANGE_MAX = 189;

  /**
   * Fan speed integer value which represents 'Stage 1'.
   */
  static final int EEP_A510XX_FAN_STAGE_1_VALUE = 2;

  /**
   * 'Stage 1' fan speed name.
   */
  static final String EEP_A510XX_FAN_STAGE_1_NAME = "Stage 1";

  /**
   * Begin of raw fan speed value range which represents 'Stage 2'
   */
  static final int EEP_A510XX_FAN_STAGE_2_RAW_VALUE_RANGE_MIN = 145;

  /**
   * End of raw fan speed value range which represents 'Stage 2'.
   */
  static final int EEP_A510XX_FAN_STAGE_2_RAW_VALUE_RANGE_MAX = 164;

  /**
   * Fan speed integer value which represents 'Stage 2'.
   */
  static final int EEP_A510XX_FAN_STAGE_2_VALUE = 3;

  /**
   * 'Stage 2' fan speed name.
   */
  static final String EEP_A510XX_FAN_STAGE_2_NAME = "Stage 2";

  /**
   * Begin of raw fan speed value range which represents 'Stage 3'
   */
  static final int EEP_A510XX_FAN_STAGE_3_RAW_VALUE_RANGE_MIN = 0;

  /**
   * End of raw fan speed value range which represents 'Stage 3'.
   */
  static final int EEP_A510XX_FAN_STAGE_3_RAW_VALUE_RANGE_MAX = 144;

  /**
   * Fan speed integer value which represents 'Stage 3'.
   */
  static final int EEP_A510XX_FAN_STAGE_3_VALUE = 4;

  /**
   * 'Stage 3' fan speed name.
   */
  static final String EEP_A510XX_FAN_STAGE_3_NAME = "Stage 3";

  /**
   * Bit size of fan data field.
   */
  static final int EEP_A510XX_FAN_2_SIZE = 3;

  /**
   * Begin of raw fan speed value range which represents the 'Auto' setting
   */
  static final int EEP_A510XX_FAN_2_AUTO_RAW_VALUE_RANGE_MIN = 0;

  /**
   * End of raw fan speed value range which represents the 'Auto' setting.
   */
  static final int EEP_A510XX_FAN_2_AUTO_RAW_VALUE_RANGE_MAX = 0;

  /**
   * Fan speed integer value which represents the 'Auto' setting.
   */
  static final int EEP_A510XX_FAN_2_AUTO_VALUE = 0;

  /**
   * Name for the fan speed 'Auto' setting.
   */
  static final String EEP_A510XX_FAN_2_AUTO_NAME = "Auto";

  /**
   * Begin of raw fan speed value range which represents the 'Speed 0' setting
   */
  static final int EEP_A510XX_FAN_2_SPEED_0_RAW_VALUE_RANGE_MIN = 1;

  /**
   * End of raw fan speed value range which represents the 'Speed 0' setting.
   */
  static final int EEP_A510XX_FAN_2_SPEED_0_RAW_VALUE_RANGE_MAX = 1;

  /**
   * Fan speed integer value which represents the 'Speed 0' setting.
   */
  static final int EEP_A510XX_FAN_2_SPEED_0_VALUE = 1;

  /**
   * Name for the fan 'Speed 0' setting.
   */
  static final String EEP_A510XX_FAN_2_SPEED_0_NAME = "Speed 0";

  /**
   * Begin of raw fan speed value range which represents the 'Speed 1' setting
   */
  static final int EEP_A510XX_FAN_2_SPEED_1_RAW_VALUE_RANGE_MIN = 2;

  /**
   * End of raw fan speed value range which represents the 'Speed 1' setting.
   */
  static final int EEP_A510XX_FAN_2_SPEED_1_RAW_VALUE_RANGE_MAX = 2;

  /**
   * Fan speed integer value which represents the 'Speed 1' setting.
   */
  static final int EEP_A510XX_FAN_2_SPEED_1_VALUE = 2;

  /**
   * Name for the fan 'Speed 1' setting.
   */
  static final String EEP_A510XX_FAN_2_SPEED_1_NAME = "Speed 1";

  /**
   * Begin of raw fan speed value range which represents the 'Speed 2' setting
   */
  static final int EEP_A510XX_FAN_2_SPEED_2_RAW_VALUE_RANGE_MIN = 3;

  /**
   * End of raw fan speed value range which represents the 'Speed 2' setting.
   */
  static final int EEP_A510XX_FAN_2_SPEED_2_RAW_VALUE_RANGE_MAX = 3;

  /**
   * Fan speed integer value which represents the 'Speed 2' setting.
   */
  static final int EEP_A510XX_FAN_2_SPEED_2_VALUE = 3;

  /**
   * Name for the fan 'Speed 2' setting.
   */
  static final String EEP_A510XX_FAN_2_SPEED_2_NAME = "Speed 2";

  /**
   * Begin of raw fan speed value range which represents the 'Speed 3' setting
   */
  static final int EEP_A510XX_FAN_2_SPEED_3_RAW_VALUE_RANGE_MIN = 4;

  /**
   * End of raw fan speed value range which represents the 'Speed 3' setting.
   */
  static final int EEP_A510XX_FAN_2_SPEED_3_RAW_VALUE_RANGE_MAX = 4;

  /**
   * Fan speed integer value which represents the 'Speed 3' setting.
   */
  static final int EEP_A510XX_FAN_2_SPEED_3_VALUE = 4;

  /**
   * Name for the fan 'Speed 3' setting.
   */
  static final String EEP_A510XX_FAN_2_SPEED_3_NAME = "Speed 3";

  /**
   * Begin of raw fan speed value range which represents the 'Speed 4' setting
   */
  static final int EEP_A510XX_FAN_2_SPEED_4_RAW_VALUE_RANGE_MIN = 5;

  /**
   * End of raw fan speed value range which represents the 'Speed 4' setting.
   */
  static final int EEP_A510XX_FAN_2_SPEED_4_RAW_VALUE_RANGE_MAX = 5;

  /**
   * Fan speed integer value which represents the 'Speed 4' setting.
   */
  static final int EEP_A510XX_FAN_2_SPEED_4_VALUE = 5;

  /**
   * Name for the fan 'Speed 4' setting.
   */
  static final String EEP_A510XX_FAN_2_SPEED_4_NAME = "Speed 4";

  /**
   * Begin of raw fan speed value range which represents the 'Speed 5' setting
   */
  static final int EEP_A510XX_FAN_2_SPEED_5_RAW_VALUE_RANGE_MIN = 6;

  /**
   * End of raw fan speed value range which represents the 'Speed 5' setting.
   */
  static final int EEP_A510XX_FAN_2_SPEED_5_RAW_VALUE_RANGE_MAX = 6;

  /**
   * Fan speed integer value which represents the 'Speed 5' setting.
   */
  static final int EEP_A510XX_FAN_2_SPEED_5_VALUE = 6;

  /**
   * Name for the fan 'Speed 5' setting.
   */
  static final String EEP_A510XX_FAN_2_SPEED_5_NAME = "Speed 5";

  /**
   * Begin of raw fan speed value range which represents the 'Off' setting
   */
  static final int EEP_A510XX_FAN_2_OFF_RAW_VALUE_RANGE_MIN = 7;

  /**
   * End of raw fan speed value range which represents the 'Off' setting.
   */
  static final int EEP_A510XX_FAN_2_OFF_RAW_VALUE_RANGE_MAX = 7;

  /**
   * Fan speed integer value which represents the 'Off' setting.
   */
  static final int EEP_A510XX_FAN_2_OFF_VALUE = 7;

  /**
   * Name for the fan 'Off' setting.
   */
  static final String EEP_A510XX_FAN_2_OFF_NAME = "Off";


  /**
   * Fractional digits to be used for temperature values.
   */
  static final int EEP_A510XX_TMP_FRACTIONAL_DIGITS = 1;

  /**
   * Fractional digits to be used for humidity values.
   */
  static final int EEP_A510XX_HUM_FRACTIONAL_DIGITS = 1;

  /**
   * Fractional digits to be used for illumination values.
   */
  static final int EEP_A510XX_ILL_FRACTIONAL_DIGITS = 1;

  /**
   * Fractional digits to be used for set point values.
   */
  static final int EEP_A510XX_SP_FRACTIONAL_DIGITS = 0;

  /**
   * Fractional digits to be used for 'temperature set point' values.
   */
  static final int EEP_A510XX_TMPSP_FRACTIONAL_DIGITS = 1;

  /**
   * Fractional digits to be used for 'illumination set point' values.
   */
  static final int EEP_A510XX_ILLSP_FRACTIONAL_DIGITS = 1;

  /**
   * Fractional digits to be used for 'humidity set point' values.
   */
  static final int EEP_A510XX_HUMSP_FRACTIONAL_DIGITS = 1;

  /**
   * Fractional digits to be used for 'supply voltage' values.
   */
  static final int EEP_A510XX_SV_FRACTIONAL_DIGITS = 1;

  /**
   * EnOcean equipment profile (EEP) occupancy data field name.
   */
  public static final String EEP_A510XX_OCC_DATA_FIELD_NAME = "OCC";

  /**
   * Bit size of EnOcean equipment profile (EEP) occupancy data field.
   */
  public static final int EEP_A510XX_OCC_SIZE = 1;

  /**
   * Description for the 'Button pressed' state of the occupancy data field.
   */
  public static final String EEP_A510XX_OCC_BTN_PRESS_DESC = "Button pressed";

  /**
   * Occupancy data field value which represents the button pressed state.
   */
  public static final int EEP_A510XX_OCC_BTN_PRESS_VALUE = 0;

  /**
   * Description for the 'Button released' state of the occupancy data field.
   */
  public static final String EEP_A510XX_OCC_BTN_RELEASE_DESC = "Button released";

  /**
   * Occupancy data field value which represents the button released state.
   */
  public static final int EEP_A510XX_OCC_BTN_RELEASE_VALUE = 1;

  /**
   * EnOcean equipment profile (EEP) occupancy button data field name.
   */
  public static final String EEP_A510XX_OB_DATA_FIELD_NAME = "OB";

  /**
   * EnOcean equipment profile (EEP) 'occupancy enable/disable' data field name.
   */
  public static final String EEP_A510XX_OED_DATA_FIELD_NAME = "OED";

  /**
   * Bit size of EnOcean equipment profile (EEP) 'occupancy enable/disable' data field.
   */
  public static final int EEP_A510XX_OED_SIZE = 1;

  /**
   * Description for the 'enabled' state of the 'occupancy enable/disable' data field.
   */
  public static final String EEP_A510XX_OED_ENABLED_DESC = "Occupancy enabled";

  /**
   * 'occupancy enable/disable' data field value which represents the 'enabled' state.
   */
  public static final int EEP_A510XX_OED_ENABLED_VALUE = 0;

  /**
   * Description for the 'disabled' state of the 'occupancy enable/disable' data field.
   */
  public static final String EEP_A510XX_OED_DISABLED_DESC = "Occupancy disabled";

  /**
   * Occupancy data field value which represents the button released state.
   */
  public static final int EEP_A510XX_OED_DISABLED_VALUE = 1;


  /**
   * EnOcean equipment profile (EEP) slide switch data field name.
   */
  public static final String EEP_A510XX_SLSW_DATA_FIELD_NAME = "SLSW";

  /**
   * Bit size of EnOcean equipment profile (EEP) slide switch data field.
   */
  public static final int EEP_A510XX_SLSW_SIZE = 1;

  /**
   * Description for the 'Night/Off' state of the slide switch data field.
   */
  public static final String EEP_A510XX_SLSW_OFF_DESC = "Position I / Night / Off";

  /**
   * Slide switch data field value which represents the 'Night/Off' state.
   */
  public static final int EEP_A510XX_SLSW_OFF_VALUE = 0;

  /**
   * Description for the 'Day/On' state of the slide switch data field.
   */
  public static final String EEP_A510XX_SLSW_ON_DESC = "Position O / Day / On";

  /**
   * Slide switch data field value which represents the 'Day/On' state.
   */
  public static final int EEP_A510XX_SLSW_ON_VALUE = 1;

  /**
   * EnOcean equipment profile (EEP) contact state data field name.
   */
  public static final String EEP_A510XX_CTST_DATA_FIELD_NAME = "CTST";

  /**
   * Bit size of EnOcean equipment profile (EEP) contact state data field.
   */
  public static final int EEP_A510XX_CTST_SIZE = 1;

  /**
   * Description for the 'closed' state of the contact state data field.
   */
  public static final String EEP_A510XX_CTST_CLOSED_DESC = "closed";

  /**
   * Contact data field value which represents the 'closed' state.
   */
  public static final int EEP_A510XX_CTST_CLOSED_VALUE = 0;

  /**
   * Description for the 'open' state of the contact state data field.
   */
  public static final String EEP_A510XX_CTST_OPEN_DESC = "open";

  /**
   * Contact state data field value which represents the 'open' state.
   */
  public static final int EEP_A510XX_CTST_OPEN_VALUE = 1;

  /**
   * EnOcean equipment profile (EEP) 'supply voltage' data field name.
   */
  static final String EEP_A510XX_SV_DATA_FIELD_NAME = "SV";

  /**
   * Bit size of 'supply voltage' data field.
   */
  static final int EEP_A510XX_SV_SIZE = 8;

  /**
   * Begin of raw 'supply voltage' value range.
   */
  static final int EEP_A510XX_SV_RAW_DATA_RANGE_MIN = 0;

  /**
   * End of raw 'supply voltage' value range.
   */
  static final int EEP_A510XX_SV_RAW_DATA_RANGE_MAX = 250;

  /**
   * Begin of scaled 'supply voltage' value range.
   */
  static final double EEP_A510XX_SV_UNITS_DATA_RANGE_MIN = 0;

  /**
   * End of scaled 'supply voltage' value range.
   */
  static final double EEP_A510XX_SV_UNITS_DATA_RANGE_MAX = 5;


  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  // Enums ----------------------------------------------------------------------------------------

  protected enum Command
  {
    /**
     * Receive temperature sensor value.
     */
    TEMPERATURE(TEMPERATURE_STATUS_COMMAND),

    /**
     * Receive humidity sensor value.
     */
    HUMIDITY(HUMIDITY_STATUS_COMMAND),

    /**
     * Receive illumination value.
     */
    ILLUMINATION(ILLUMINATION_STATUS_COMMAND),

    /**
     * Receive occupancy button state (pressed/released).
     */
    OCCUPANCY(OCCUPANCY_STATUS_COMMAND),

    /**
     * Receive occupancy button pressed/released state.
     */
    OCCUPANCY_BUTTON(OCCUPANCY_BUTTON_STATUS_COMMAND),

    /**
     * Receive occupancy enable/disable state.
     */
    OCCUPANCY_ENABLE(OCCUPANCY_ENABLE_STATUS_COMMAND),

    /**
     * Receive contact state (closed/open)
     */
    CONTACT(CONTACT_STATE_STATUS_COMMAND),

    /**
     * Receive the fan speed value.
     */
    FAN_SPEED(FAN_SPEED_STATUS_COMMAND),

    /**
     * Receive set point value.
     */
    SET_POINT(SET_POINT_STATUS_COMMAND),

    /**
     * Receive temperature set point value.
     */
    TEMPERATURE_SET_POINT(TEMP_SET_POINT_STATUS_COMMAND),

    /**
     * Receive illumination set point value.
     */
    ILLUMINATION_SET_POINT(ILL_SET_POINT_STATUS_COMMAND),

    /**
     * Receive humidity set point value.
     */
    HUMIDITY_SET_POINT(HUM_SET_POINT_STATUS_COMMAND),

    /**
     * Receive slide switch status value.
     */
    SLIDE_SWITCH(SLIDE_SWITCH_STATUS_COMMAND),

    /**
     * Receive supply voltage value.
     */
    SUPPLY_VOLTAGE(SUPPLY_VOLTAGE_ROP_STATUS_COMMAND);


    // Members ------------------------------------------------------------------------------------

    public static Command toCommand(String value, EepType eepType) throws ConfigurationException
    {

      if(value.equalsIgnoreCase(TEMPERATURE.toString()))
      {
        return TEMPERATURE;
      }

      else if(value.equalsIgnoreCase(HUMIDITY.toString()))
      {
        return HUMIDITY;
      }

      else if(value.equalsIgnoreCase(ILLUMINATION.toString()))
      {
        return ILLUMINATION;
      }

      else if(value.equalsIgnoreCase(OCCUPANCY.toString()))
      {
        return OCCUPANCY;
      }

      else if(value.equalsIgnoreCase(OCCUPANCY_ENABLE.toString()))
      {
        return OCCUPANCY_ENABLE;
      }

      else if(value.equalsIgnoreCase(OCCUPANCY_BUTTON.toString()))
      {
        return OCCUPANCY_BUTTON;
      }

      else if(value.equalsIgnoreCase(SLIDE_SWITCH.toString()))
      {
        return SLIDE_SWITCH;
      }

      else if(value.equalsIgnoreCase(CONTACT.toString()))
      {
        return CONTACT;
      }

      else if(value.equalsIgnoreCase(FAN_SPEED.toString()))
      {
        return FAN_SPEED;
      }

      else if(value.equalsIgnoreCase(SET_POINT.toString()))
      {
        return SET_POINT;
      }

      else if(value.equalsIgnoreCase(TEMPERATURE_SET_POINT.toString()))
      {
        return TEMPERATURE_SET_POINT;
      }

      else if(value.equalsIgnoreCase(ILLUMINATION_SET_POINT.toString()))
      {
        return ILLUMINATION_SET_POINT;
      }

      else if(value.equalsIgnoreCase(HUMIDITY_SET_POINT.toString()))
      {
        return HUMIDITY_SET_POINT;
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


  // Protected Instance Fields --------------------------------------------------------------------

  /**
   * Temperature value.
   */
  protected Range temperature;

  /**
   * Humidity value.
   */
  protected Range humidity;

  /**
   * Illumination value.
   */
  protected Range illumination;

  /**
   * Supply voltage value.
   */
  protected Range supplyVoltage;

  /**
   * Set point value.
   */
  protected Range setPoint;

  /**
   * Fan speed value.
   */
  protected Ordinal fanSpeed;

  /**
   * Indicates if occupancy button has been pressed.
   */
  protected Bool occupancy;

  /**
   * Indicated if occupancy has been enabled/disabled.
   */
  protected Bool occupancyEnabled;

  /**
   * Indicates if contact is closed.
   */
  protected Bool contact;

  /**
   * Indicated if slide switch has been turned on/off.
   */
  protected Bool slideSwitch;

  /**
   * Contains the EnOcean equipment profile (EEP) data for
   * extracting sensor value.
   */
  protected EepData sensorData;

  /**
   * Type safe command from command configuration.
   */
  protected Command command;


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
   * EnOcean equipment profile (EEP) type.
   */
  private EepType eepType;

  /**
   * EnOcean device ID for filtering received radio telegrams.
   */
  private DeviceID deviceID;

  // Constructors ---------------------------------------------------------------------------------

  public EepA510XX(EepType eepType, DeviceID deviceID) throws ConfigurationException
  {
    if(eepType == null)
    {
      throw new IllegalArgumentException("null EEP data type");
    }

    if(deviceID == null)
    {
      throw new IllegalArgumentException("null device ID");
    }

    this.eepType = eepType;
    this.deviceID = deviceID;

    this.teachIn = Bool.createTeachInFlag4BS();

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
       log.debug("Teach in telegram for device ID {0} with EEP {1}", deviceID, getType());
      return false;
    }


    boolean isUpdate = false;

    if(command == Command.TEMPERATURE)
    {
      isUpdate = updateRangeVariable(temperature, telegram);
    }

    else if(command == Command.HUMIDITY)
    {
      isUpdate = updateRangeVariable(humidity, telegram);
    }

    else if(command == Command.ILLUMINATION)
    {
      isUpdate = updateRangeVariable(illumination, telegram);
    }

    else if(command == Command.SUPPLY_VOLTAGE)
    {
      isUpdate = updateRangeVariable(supplyVoltage, telegram);
    }

    else if(command == Command.FAN_SPEED)
    {
      isUpdate = updateOrdinalVariable(fanSpeed, telegram);
    }

    else if(command == Command.OCCUPANCY)
    {
      isUpdate = updateBoolVariable(occupancy, telegram);
    }

    else if(command == Command.OCCUPANCY_BUTTON)
    {
      isUpdate = updateBoolVariable(occupancy, telegram);
    }

    else if(command == Command.OCCUPANCY_ENABLE)
    {
      isUpdate = updateBoolVariable(occupancyEnabled, telegram);
    }

    else if(command == Command.CONTACT)
    {
      isUpdate = updateBoolVariable(contact, telegram);
    }

    else if(command == Command.SLIDE_SWITCH)
    {
      isUpdate = updateBoolVariable(slideSwitch, telegram);
    }

    else if(command == Command.SET_POINT)
    {
      isUpdate = updateRangeVariable(setPoint, telegram);
    }

    else if(command == Command.TEMPERATURE_SET_POINT)
    {
      isUpdate = updateRangeVariable(setPoint, telegram);
    }

    else if(command == Command.ILLUMINATION_SET_POINT)
    {
      isUpdate = updateRangeVariable(setPoint, telegram);
    }

    else if(command == Command.HUMIDITY_SET_POINT)
    {
      isUpdate = updateRangeVariable(setPoint, telegram);
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
      temperature.updateSensor(sensor);
    }

    else if(command == Command.HUMIDITY)
    {
      humidity.updateSensor(sensor);
    }

    else if(command == Command.ILLUMINATION)
    {
      illumination.updateSensor(sensor);
    }

    else if(command == Command.SUPPLY_VOLTAGE)
    {
      supplyVoltage.updateSensor(sensor);
    }

    else if(command == Command.FAN_SPEED)
    {
      fanSpeed.updateSensor(sensor);
    }

    else if(command == Command.OCCUPANCY)
    {
      occupancy.updateSensor(sensor);
    }

    else if(command == Command.OCCUPANCY_BUTTON)
    {
      occupancy.updateSensor(sensor);
    }

    else if(command == Command.OCCUPANCY_ENABLE)
    {
      occupancyEnabled.updateSensor(sensor);
    }

    else if(command == Command.CONTACT)
    {
      contact.updateSensor(sensor);
    }

    else if(command == Command.SLIDE_SWITCH)
    {
      slideSwitch.updateSensor(sensor);
    }

    else if(command == Command.SET_POINT)
    {
      setPoint.updateSensor(sensor);
    }

    else if(command == Command.TEMPERATURE_SET_POINT)
    {
      setPoint.updateSensor(sensor);
    }

    else if(command == Command.ILLUMINATION_SET_POINT)
    {
      setPoint.updateSensor(sensor);
    }

    else if(command == Command.HUMIDITY_SET_POINT)
    {
      setPoint.updateSensor(sensor);
    }
  }

  // Package Private Instance Methods -------------------------------------------------------------

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
   * Returns the occupancy state.
   *
   * @return  <tt>true</tt> if the occupancy button has been pressed,
   *          <tt>false</tt> if the occupancy button is released,
   *          <tt>null</tt> if no occupancy status has been received
   */
  Boolean isOccupied()
  {
    return occupancy.boolValue();
  }

  /**
   * Returns the occupancy enabled/disabled state.
   *
   * @return  <tt>true</tt> if occupancy has been enabled,
   *          <tt>false</tt> if occupancy has been disabled,
   *          <tt>null</tt> if no occupancy disabled/enabled status has been received
   */
  Boolean isOccupancyEnabled()
  {
    return occupancyEnabled.boolValue();
  }

  /**
   * Returns the slide switch state.
   *
   * @return  <tt>true</tt> if the slide switch is on,
   *          <tt>false</tt> if the slide switch is off,
   *          <tt>null</tt> if no slide switch state has been received
   */
  Boolean isSlideSwitchOn()
  {
    return slideSwitch.boolValue();
  }

  /**
   * Returns the contact state.
   *
   * @return  <tt>true</tt> if the contact is closed,
   *          <tt>false</tt> if the contact is open,
   *          <tt>null</tt> if no contact state has been received
   */
  Boolean isContactClosed()
  {
    return contact.boolValue();
  }

  Integer getFanSpeed()
  {
    return fanSpeed.ordinalValue();
  }

  Double getSetPoint()
  {
    return setPoint.rangeValue();
  }

  // Protected Instance Methods -------------------------------------------------------------------

  protected Range createTemperatureRange8Bit(int offset)
  {
    return Range.createRange(
        EEP_A510XX_TMP_DATA_FIELD_NAME, offset, EEP_A510XX_TMP_8BIT_SIZE,
        EEP_A510XX_TMP_8BIT_1_RAW_DATA_RANGE_MIN, EEP_A510XX_TMP_8BIT_1_RAW_DATA_RANGE_MAX,
        EEP_A510XX_TMP_8BIT_UNITS_DATA_RANGE_MIN, EEP_A510XX_TMP_8BIT_UNITS_DATA_RANGE_MAX,
        EEP_A510XX_TMP_FRACTIONAL_DIGITS
    );
  }

  protected Range createTemperatureRange8Bit2(int offset)
  {
    return Range.createRange(
        EEP_A510XX_TMP_DATA_FIELD_NAME, offset, EEP_A510XX_TMP_8BIT_SIZE,
        EEP_A510XX_TMP_8BIT_2_RAW_DATA_RANGE_MIN, EEP_A510XX_TMP_8BIT_2_RAW_DATA_RANGE_MAX,
        EEP_A510XX_TMP_8BIT_UNITS_DATA_RANGE_MIN, EEP_A510XX_TMP_8BIT_UNITS_DATA_RANGE_MAX,
        EEP_A510XX_TMP_FRACTIONAL_DIGITS
    );
  }

  protected Range createTemperatureRange8Bit3(int offset)
  {
    return Range.createRange(
        EEP_A510XX_TMP_DATA_FIELD_NAME, offset, EEP_A510XX_TMP_8BIT_SIZE,
        EEP_A510XX_TMP_8BIT_3_RAW_DATA_RANGE_MIN, EEP_A510XX_TMP_8BIT_3_RAW_DATA_RANGE_MAX,
        EEP_A510XX_TMP_8BIT_UNITS_DATA_RANGE_MIN, EEP_A510XX_TMP_8BIT_UNITS_DATA_RANGE_MAX,
        EEP_A510XX_TMP_FRACTIONAL_DIGITS
    );
  }

  protected Range createTemperatureRange10Bit(int offset)
  {
    return Range.createRange(
        EEP_A510XX_TMP_DATA_FIELD_NAME, offset, EEP_A510XX_TMP_10BIT_SIZE,
        EEP_A510XX_TMP_10BIT_RAW_DATA_RANGE_MIN, EEP_A510XX_TMP_10BIT_RAW_DATA_RANGE_MAX,
        EEP_A510XX_TMP_10BIT_UNITS_DATA_RANGE_MIN, EEP_A510XX_TMP_10BIT_UNITS_DATA_RANGE_MAX,
        EEP_A510XX_TMP_FRACTIONAL_DIGITS
    );
  }

  protected Range createHumidityRange(int offset)
  {
    return Range.createRange(
        EEP_A510XX_HUM_DATA_FIELD_NAME, offset, EEP_A510XX_HUM_SIZE,
        EEP_A510XX_HUM_RAW_DATA_RANGE_MIN, EEP_A510XX_HUM_RAW_DATA_RANGE_MAX,
        EEP_A510XX_HUM_UNITS_DATA_RANGE_MIN, EEP_A510XX_HUM_UNITS_DATA_RANGE_MAX,
        EEP_A510XX_HUM_FRACTIONAL_DIGITS
    );
  }

  protected Range createSupplyVoltageRange(int offset)
  {
    return Range.createRange(
        EEP_A510XX_SV_DATA_FIELD_NAME, offset, EEP_A510XX_SV_SIZE,
        EEP_A510XX_SV_RAW_DATA_RANGE_MIN, EEP_A510XX_SV_RAW_DATA_RANGE_MAX,
        EEP_A510XX_SV_UNITS_DATA_RANGE_MIN, EEP_A510XX_SV_UNITS_DATA_RANGE_MAX,
        EEP_A510XX_SV_FRACTIONAL_DIGITS
    );
  }

  protected Range createIlluminationRange(int offset)
  {
    return Range.createRange(
        EEP_A510XX_ILL_DATA_FIELD_NAME, offset, EEP_A510XX_ILL_SIZE,
        EEP_A510XX_ILL_RAW_DATA_RANGE_MIN, EEP_A510XX_ILL_RAW_DATA_RANGE_MAX,
        EEP_A510XX_ILL_UNITS_DATA_RANGE_MIN, EEP_A510XX_ILL_UNITS_DATA_RANGE_MAX,
        EEP_A510XX_ILL_FRACTIONAL_DIGITS
    );
  }

  protected Range createSetPointRange8Bit(int offset)
  {
    return Range.createRange(
        EEP_A510XX_SP_DATA_FIELD_NAME, offset, EEP_A510XX_SP_8BIT_SIZE,
        EEP_A510XX_SP_8BIT_RAW_DATA_RANGE_MIN, EEP_A510XX_SP_8BIT_RAW_DATA_RANGE_MAX,
        EEP_A510XX_SP_8BIT_UNITS_DATA_RANGE_MIN, EEP_A510XX_SP_8BIT_UNITS_DATA_RANGE_MAX,
        EEP_A510XX_SP_FRACTIONAL_DIGITS
    );
  }

  protected Range createSetPointRange6Bit(int offset)
  {
    return Range.createRange(
        EEP_A510XX_SP_DATA_FIELD_NAME, offset, EEP_A510XX_SP_6BIT_SIZE,
        EEP_A510XX_SP_6BIT_RAW_DATA_RANGE_MIN, EEP_A510XX_SP_6BIT_RAW_DATA_RANGE_MAX,
        EEP_A510XX_SP_6BIT_UNITS_DATA_RANGE_MIN, EEP_A510XX_SP_6BIT_UNITS_DATA_RANGE_MAX,
        EEP_A510XX_SP_FRACTIONAL_DIGITS
    );
  }

  protected Range createTempSetPointRange(int offset)
  {
    return Range.createRange(
        EEP_A510XX_TMPSP_DATA_FIELD_NAME, offset, EEP_A510XX_TMPSP_SIZE,
        EEP_A510XX_TMPSP_RAW_DATA_RANGE_MIN, EEP_A510XX_TMPSP_RAW_DATA_RANGE_MAX,
        EEP_A510XX_TMPSP_UNITS_DATA_RANGE_MIN, EEP_A510XX_TMPSP_UNITS_DATA_RANGE_MAX,
        EEP_A510XX_TMPSP_FRACTIONAL_DIGITS
    );
  }

  protected Range createIllSetPointRange(int offset)
  {
    return Range.createRange(
        EEP_A510XX_ILLSP_DATA_FIELD_NAME, offset, EEP_A510XX_ILLSP_SIZE,
        EEP_A510XX_ILLSP_RAW_DATA_RANGE_MIN, EEP_A510XX_ILLSP_RAW_DATA_RANGE_MAX,
        EEP_A510XX_ILLSP_UNITS_DATA_RANGE_MIN, EEP_A510XX_ILLSP_UNITS_DATA_RANGE_MAX,
        EEP_A510XX_ILLSP_FRACTIONAL_DIGITS
    );
  }

  protected Range createHumSetPointRange(int offset)
  {
    return Range.createRange(
        EEP_A510XX_HUMSP_DATA_FIELD_NAME, offset, EEP_A510XX_HUMSP_SIZE,
        EEP_A510XX_HUMSP_RAW_DATA_RANGE_MIN, EEP_A510XX_HUMSP_RAW_DATA_RANGE_MAX,
        EEP_A510XX_HUMSP_UNITS_DATA_RANGE_MIN, EEP_A510XX_HUMSP_UNITS_DATA_RANGE_MAX,
        EEP_A510XX_HUMSP_FRACTIONAL_DIGITS
    );
  }

  protected Ordinal createFanSpeedRange2(int offset)
  {
    ScaleCategory auto = new ScaleCategory(
        EEP_A510XX_FAN_2_AUTO_NAME,
        EEP_A510XX_FAN_2_AUTO_RAW_VALUE_RANGE_MIN, EEP_A510XX_FAN_2_AUTO_RAW_VALUE_RANGE_MAX,
        EEP_A510XX_FAN_2_AUTO_NAME, EEP_A510XX_FAN_2_AUTO_VALUE
    );

    ScaleCategory speed0 = new ScaleCategory(
        EEP_A510XX_FAN_2_SPEED_0_NAME,
        EEP_A510XX_FAN_2_SPEED_0_RAW_VALUE_RANGE_MIN, EEP_A510XX_FAN_2_SPEED_0_RAW_VALUE_RANGE_MAX,
        EEP_A510XX_FAN_2_SPEED_0_NAME, EEP_A510XX_FAN_2_SPEED_0_VALUE
    );

    ScaleCategory speed1 = new ScaleCategory(
        EEP_A510XX_FAN_2_SPEED_1_NAME,
        EEP_A510XX_FAN_2_SPEED_1_RAW_VALUE_RANGE_MIN, EEP_A510XX_FAN_2_SPEED_1_RAW_VALUE_RANGE_MAX,
        EEP_A510XX_FAN_2_SPEED_1_NAME, EEP_A510XX_FAN_2_SPEED_1_VALUE
    );

    ScaleCategory speed2 = new ScaleCategory(
        EEP_A510XX_FAN_2_SPEED_2_NAME,
        EEP_A510XX_FAN_2_SPEED_2_RAW_VALUE_RANGE_MIN, EEP_A510XX_FAN_2_SPEED_2_RAW_VALUE_RANGE_MAX,
        EEP_A510XX_FAN_2_SPEED_2_NAME, EEP_A510XX_FAN_2_SPEED_2_VALUE
    );

    ScaleCategory speed3 = new ScaleCategory(
        EEP_A510XX_FAN_2_SPEED_3_NAME,
        EEP_A510XX_FAN_2_SPEED_3_RAW_VALUE_RANGE_MIN, EEP_A510XX_FAN_2_SPEED_3_RAW_VALUE_RANGE_MAX,
        EEP_A510XX_FAN_2_SPEED_3_NAME, EEP_A510XX_FAN_2_SPEED_3_VALUE
    );

    ScaleCategory speed4 = new ScaleCategory(
        EEP_A510XX_FAN_2_SPEED_4_NAME,
        EEP_A510XX_FAN_2_SPEED_4_RAW_VALUE_RANGE_MIN, EEP_A510XX_FAN_2_SPEED_4_RAW_VALUE_RANGE_MAX,
        EEP_A510XX_FAN_2_SPEED_4_NAME, EEP_A510XX_FAN_2_SPEED_4_VALUE
    );

    ScaleCategory speed5 = new ScaleCategory(
        EEP_A510XX_FAN_2_SPEED_5_NAME,
        EEP_A510XX_FAN_2_SPEED_5_RAW_VALUE_RANGE_MIN, EEP_A510XX_FAN_2_SPEED_5_RAW_VALUE_RANGE_MAX,
        EEP_A510XX_FAN_2_SPEED_5_NAME, EEP_A510XX_FAN_2_SPEED_5_VALUE
    );

    ScaleCategory off = new ScaleCategory(
        EEP_A510XX_FAN_2_OFF_NAME,
        EEP_A510XX_FAN_2_OFF_RAW_VALUE_RANGE_MIN, EEP_A510XX_FAN_2_OFF_RAW_VALUE_RANGE_MAX,
        EEP_A510XX_FAN_2_OFF_NAME, EEP_A510XX_FAN_2_OFF_VALUE
    );

    CategoricalScale scale = new CategoricalScale(auto, speed0, speed1, speed2, speed3, speed4, speed5, off);
    EepDataField dataField = new EepDataField(EEP_A510XX_FAN_DATA_FIELD_NAME, offset, EEP_A510XX_FAN_2_SIZE);
    Ordinal fanSpeed = new Ordinal(dataField, scale);

    return fanSpeed;
  }

  protected Ordinal createFanSpeedRange(int offset)
  {
    ScaleCategory stageAuto = new ScaleCategory(
        EEP_A510XX_FAN_STAGE_AUTO_NAME,
        EEP_A510XX_FAN_STAGE_AUTO_RAW_VALUE_RANGE_MIN, EEP_A510XX_FAN_STAGE_AUTO_RAW_VALUE_RANGE_MAX,
        EEP_A510XX_FAN_STAGE_AUTO_NAME, EEP_A510XX_FAN_STAGE_AUTO_VALUE
    );

    ScaleCategory stage0 = new ScaleCategory(
        EEP_A510XX_FAN_STAGE_0_NAME,
        EEP_A510XX_FAN_STAGE_0_RAW_VALUE_RANGE_MIN, EEP_A510XX_FAN_STAGE_0_RAW_VALUE_RANGE_MAX,
        EEP_A510XX_FAN_STAGE_0_NAME, EEP_A510XX_FAN_STAGE_0_VALUE
    );

    ScaleCategory stage1 = new ScaleCategory(
        EEP_A510XX_FAN_STAGE_1_NAME,
        EEP_A510XX_FAN_STAGE_1_RAW_VALUE_RANGE_MIN, EEP_A510XX_FAN_STAGE_1_RAW_VALUE_RANGE_MAX,
        EEP_A510XX_FAN_STAGE_1_NAME, EEP_A510XX_FAN_STAGE_1_VALUE
    );

    ScaleCategory stage2 = new ScaleCategory(
        EEP_A510XX_FAN_STAGE_2_NAME,
        EEP_A510XX_FAN_STAGE_2_RAW_VALUE_RANGE_MIN, EEP_A510XX_FAN_STAGE_2_RAW_VALUE_RANGE_MAX,
        EEP_A510XX_FAN_STAGE_2_NAME, EEP_A510XX_FAN_STAGE_2_VALUE
    );

    ScaleCategory stage3 = new ScaleCategory(
        EEP_A510XX_FAN_STAGE_3_NAME,
        EEP_A510XX_FAN_STAGE_3_RAW_VALUE_RANGE_MIN, EEP_A510XX_FAN_STAGE_3_RAW_VALUE_RANGE_MAX,
        EEP_A510XX_FAN_STAGE_3_NAME, EEP_A510XX_FAN_STAGE_3_VALUE
    );

    CategoricalScale scale = new CategoricalScale(stageAuto, stage0, stage1, stage2, stage3);
    EepDataField dataField = new EepDataField(EEP_A510XX_FAN_DATA_FIELD_NAME, offset, EEP_A510XX_FAN_SIZE);
    Ordinal fanSpeed = new Ordinal(dataField, scale);

    return fanSpeed;
  }

  protected Bool createOccupancyBool(int offset)
  {
    return Bool.createBool(
        EEP_A510XX_OCC_DATA_FIELD_NAME, offset, EEP_A510XX_OCC_SIZE,
        EEP_A510XX_OCC_BTN_PRESS_DESC, EEP_A510XX_OCC_BTN_PRESS_VALUE,
        EEP_A510XX_OCC_BTN_RELEASE_DESC, EEP_A510XX_OCC_BTN_RELEASE_VALUE
    );
  }

  protected Bool createOccupancyButtonBool(int offset)
  {
    return Bool.createBool(
        EEP_A510XX_OB_DATA_FIELD_NAME, offset, EEP_A510XX_OCC_SIZE,
        EEP_A510XX_OCC_BTN_PRESS_DESC, EEP_A510XX_OCC_BTN_PRESS_VALUE,
        EEP_A510XX_OCC_BTN_RELEASE_DESC, EEP_A510XX_OCC_BTN_RELEASE_VALUE
    );
  }

  protected Bool createOccupancyButtonEnableDisableBool(int offset)
  {
    return Bool.createBool(
        EEP_A510XX_OED_DATA_FIELD_NAME, offset, EEP_A510XX_OED_SIZE,
        EEP_A510XX_OED_ENABLED_DESC, EEP_A510XX_OED_ENABLED_VALUE,
        EEP_A510XX_OED_DISABLED_DESC, EEP_A510XX_OED_DISABLED_VALUE
    );
  }

  protected Bool createSlideSwitchBool(int offset)
  {
    return Bool.createBool(
        EEP_A510XX_SLSW_DATA_FIELD_NAME, offset, EEP_A510XX_SLSW_SIZE,
        EEP_A510XX_SLSW_ON_DESC, EEP_A510XX_SLSW_ON_VALUE,
        EEP_A510XX_SLSW_OFF_DESC, EEP_A510XX_SLSW_OFF_VALUE
    );
  }

  protected Bool createContactStateBool(int offset)
  {
    return Bool.createBool(
        EEP_A510XX_CTST_DATA_FIELD_NAME, offset, EEP_A510XX_CTST_SIZE,
        EEP_A510XX_CTST_CLOSED_DESC, EEP_A510XX_CTST_CLOSED_VALUE,
        EEP_A510XX_CTST_OPEN_DESC, EEP_A510XX_CTST_OPEN_VALUE
    );
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

    sensorData.update(telegram.getPayload());

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

    sensorData.update(telegram.getPayload());

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

    sensorData.update(telegram.getPayload());

    Boolean newValue = bool.boolValue();

    isUpdate = ((oldValue == null && newValue != null) ||
                (oldValue != null && newValue != null &&
                 newValue.booleanValue() != oldValue.booleanValue()));

    return isUpdate;
  }
}

