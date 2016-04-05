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

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openremote.controller.protocol.enocean.ConfigurationException;
import org.openremote.controller.protocol.enocean.Constants;
import org.openremote.controller.protocol.enocean.DeviceID;
import org.openremote.controller.protocol.enocean.packet.radio.*;

/**
 * Unit tests for {@link EepA5101B} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA5101BTest
{

  // Private Instance Fields ----------------------------------------------------------------------

  private DeviceID deviceID;


  // Test Lifecycle -------------------------------------------------------------------------------

  @Before public void setUp() throws Exception
  {
    deviceID = DeviceID.fromString("0xFF800001");
  }


  // Tests ----------------------------------------------------------------------------------------

  @Test public void testBasicConstruction() throws Exception
  {

    // New EEP number ...

    Eep eep = EepType.lookup("A5-10-1B").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND
    );

    Assert.assertTrue(eep instanceof EepA5101B);
    Assert.assertEquals(EepType.EEP_TYPE_A5101B, eep.getType());

    // Old EEP number ...

    eep = EepType.lookup("07-10-1B").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND
    );

    Assert.assertTrue(eep instanceof EepA5101B);
    Assert.assertEquals(EepType.EEP_TYPE_A5101B, eep.getType());

  }

  @Test public void testUpdateSupplyVoltage() throws Exception
  {
    EepA5101B eep = (EepA5101B)EepType.lookup("A5-10-1B").createEep(
        deviceID, Constants.SUPPLY_VOLTAGE_ROP_STATUS_COMMAND
    );

    Assert.assertNull(eep.getSupplyVoltage());

    int rawSupplyVoltageValue = 0;
    int rawIlluminationValue = 0;
    int rawTempValue = 0;
    int rawFanSpeedValue = 0;
    boolean isOccupancyEnabled = true;
    boolean isOccupied = false;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getSupplyVoltage());


    rawSupplyVoltageValue = 0;

    telegram = createRadioTelegramESP3(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getSupplyVoltage());


    rawSupplyVoltageValue = 250;

    telegram = createRadioTelegramESP2(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(5), eep.getSupplyVoltage());


    rawSupplyVoltageValue = 0;
    isTeachIn = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(5), eep.getSupplyVoltage());

  }

  @Test public void testUpdateIllumination() throws Exception
  {

    EepA5101B eep = (EepA5101B)EepType.lookup("A5-10-1B").createEep(
        deviceID, Constants.ILLUMINATION_STATUS_COMMAND
    );

    Assert.assertNull(eep.getIllumination());

    int rawSupplyVoltageValue = 0;
    int rawIlluminationValue = 0;
    int rawTempValue = 0;
    int rawFanSpeedValue = 0;
    boolean isOccupancyEnabled = true;
    boolean isOccupied = false;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getIllumination());


    rawIlluminationValue = 0;

    telegram = createRadioTelegramESP3(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getIllumination());


    rawIlluminationValue = 250;

    telegram = createRadioTelegramESP2(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(1000), eep.getIllumination());


    isTeachIn = true;
    rawIlluminationValue = 0;

    telegram = createRadioTelegramESP2(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(1000), eep.getIllumination());

  }

  @Test public void testUpdateTemperature() throws Exception
  {
    EepA5101B eep = (EepA5101B)EepType.lookup("A5-10-1B").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND
    );

    Assert.assertNull(eep.getTemperature());

    int rawSupplyVoltageValue = 0;
    int rawIlluminationValue = 0;
    int rawTempValue = 250;
    int rawFanSpeedValue = 0;
    boolean isOccupancyEnabled = true;
    boolean isOccupied = false;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getTemperature());


    rawTempValue = 250;

    telegram = createRadioTelegramESP3(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getTemperature());


    rawTempValue = 0;

    telegram = createRadioTelegramESP2(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(40), eep.getTemperature());


    rawTempValue = 250;
    isTeachIn = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(40), eep.getTemperature());

  }

  @Test public void testUpdateFanSpeed() throws Exception
  {
    EepA5101B eep = (EepA5101B)EepType.lookup("A5-10-1B").createEep(
        deviceID, Constants.FAN_SPEED_STATUS_COMMAND
    );

    Assert.assertNull(eep.getFanSpeed());

    int rawSupplyVoltageValue = 0;
    int rawIlluminationValue = 0;
    int rawTempValue = 0;
    int rawFanSpeedValue = 0;
    boolean isOccupancyEnabled = true;
    boolean isOccupied = false;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(0), eep.getFanSpeed());


    rawFanSpeedValue = 0;

    telegram = createRadioTelegramESP3(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Integer.valueOf(0), eep.getFanSpeed());


    rawFanSpeedValue = 1;

    telegram = createRadioTelegramESP3(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(1), eep.getFanSpeed());


    rawFanSpeedValue = 2;

    telegram = createRadioTelegramESP3(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(2), eep.getFanSpeed());


    rawFanSpeedValue = 3;

    telegram = createRadioTelegramESP3(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(3), eep.getFanSpeed());


    rawFanSpeedValue = 4;

    telegram = createRadioTelegramESP3(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(4), eep.getFanSpeed());


    rawFanSpeedValue = 5;

    telegram = createRadioTelegramESP2(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(5), eep.getFanSpeed());


    rawFanSpeedValue = 6;

    telegram = createRadioTelegramESP2(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(6), eep.getFanSpeed());


    rawFanSpeedValue = 7;

    telegram = createRadioTelegramESP2(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(7), eep.getFanSpeed());
  }

  @Test public void testUpdateOccupancyEnable() throws Exception
  {
    EepA5101B eep = (EepA5101B)EepType.lookup("A5-10-1B").createEep(
        deviceID, Constants.OCCUPANCY_ENABLE_STATUS_COMMAND
    );

    Assert.assertNull(eep.isOccupancyEnabled());

    int rawSupplyVoltageValue = 0;
    int rawIlluminationValue = 0;
    int rawTempValue = 0;
    int rawFanSpeedValue = 0;
    boolean isOccupancyEnabled = false;
    boolean isOccupied = false;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertFalse(eep.isOccupancyEnabled());


    isOccupancyEnabled = false;

    telegram = createRadioTelegramESP3(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertFalse(eep.isOccupancyEnabled());


    isOccupancyEnabled = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertTrue(eep.isOccupancyEnabled());


    isOccupancyEnabled = false;
    isTeachIn = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertTrue(eep.isOccupancyEnabled());

  }

  @Test public void testUpdateOccupancy() throws Exception
  {
    EepA5101B eep = (EepA5101B)EepType.lookup("A5-10-1B").createEep(
        deviceID, Constants.OCCUPANCY_BUTTON_STATUS_COMMAND
    );

    Assert.assertNull(eep.isOccupied());

    int rawSupplyVoltageValue = 0;
    int rawIlluminationValue = 0;
    int rawTempValue = 0;
    int rawFanSpeedValue = 0;
    boolean isOccupancyEnabled = true;
    boolean isOccupied = false;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertFalse(eep.isOccupied());


    isOccupied = false;

    telegram = createRadioTelegramESP3(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertFalse(eep.isOccupied());


    isOccupied = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertTrue(eep.isOccupied());


    isOccupied = false;
    isTeachIn = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertTrue(eep.isOccupied());

  }

  @Test (expected = ConfigurationException.class)
  public void testUnknownCommand() throws Exception
  {
    Eep eep = EepType.lookup("A5-10-1B").createEep(
        deviceID, "UNKONWN_COMMAND"
    );
  }

  @Test public void testInvalidRadioTelegramType() throws Exception
  {
    EepA5101B eep = (EepA5101B)EepType.lookup("A5-10-1B").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND
    );

    EspRadioTelegram invalidTelegram = new Esp31BSTelegram(deviceID, (byte)0x00, (byte)0x00);

    boolean isUpdate = eep.update(invalidTelegram);

    Assert.assertFalse(isUpdate);


    invalidTelegram = new Esp21BSTelegram(deviceID, (byte)0x00, (byte)0x00);

    isUpdate = eep.update(invalidTelegram);

    Assert.assertFalse(isUpdate);
  }

  @Test public void testInvalidDeviceID() throws Exception
  {
    EepA5101B eep = (EepA5101B)EepType.lookup("A5-10-1B").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND
    );

    int rawSupplyVoltageValue = 0;
    int rawIlluminationValue = 0;
    int rawTempValue = 0;
    int rawFanSpeedValue = 0;
    boolean isOccupancyEnabled = true;
    boolean isOccupied = false;
    boolean isTeachIn = false;
    DeviceID invalidDeviceID = DeviceID.fromString("0xFF800002");

    EspRadioTelegram telegram = createRadioTelegramESP3(
        invalidDeviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);


    telegram = createRadioTelegramESP2(
        invalidDeviceID, rawSupplyVoltageValue, rawIlluminationValue, rawTempValue,
        rawFanSpeedValue, isOccupancyEnabled, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
  }


  // Helpers --------------------------------------------------------------------------------------

  private Esp34BSTelegram createRadioTelegramESP3(DeviceID deviceID, int rawSupplyVoltageValue, int rawIlluminationValue,
                                                  int rawTempValue, int rawFanSpeedValue, boolean isOccupancyEnabled,
                                                  boolean isOccupied, boolean isTeachIn)
  {
    byte[] payload = new byte[4];
    payload[0] = (byte)rawSupplyVoltageValue;
    payload[1] = (byte)rawIlluminationValue;
    payload[2] = (byte)rawTempValue;
    payload[3] = (byte)(rawFanSpeedValue << 4);
    payload[3] |= (byte)(isTeachIn ? 0x00 : 0x08);
    payload[3] |= (byte)(isOccupancyEnabled ? 0x00 : 0x02);
    payload[3] |= (byte)(isOccupied ? 0x00 : 0x01);

    Esp34BSTelegram telegram = new Esp34BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }

  private Esp24BSTelegram createRadioTelegramESP2(DeviceID deviceID, int rawSupplyVoltageValue, int rawIlluminationValue,
                                                  int rawTempValue, int rawFanSpeedValue, boolean isOccupancyEnabled,
                                                  boolean isOccupied, boolean isTeachIn)
  {
    byte[] payload = new byte[4];
    payload[0] = (byte)rawSupplyVoltageValue;
    payload[1] = (byte)rawIlluminationValue;
    payload[2] = (byte)rawTempValue;
    payload[3] = (byte)(rawFanSpeedValue << 4);
    payload[3] |= (byte)(isTeachIn ? 0x00 : 0x08);
    payload[3] |= (byte)(isOccupancyEnabled ? 0x00 : 0x02);
    payload[3] |= (byte)(isOccupied ? 0x00 : 0x01);

    Esp24BSTelegram telegram = new Esp24BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }
}
