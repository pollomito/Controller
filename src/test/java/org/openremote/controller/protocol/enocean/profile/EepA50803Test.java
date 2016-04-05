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
 * Unit tests for {@link EepA50803} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA50803Test
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

    Eep eep = EepType.lookup("A5-08-03").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND
    );

    Assert.assertTrue(eep instanceof EepA50803);
    Assert.assertEquals(EepType.EEP_TYPE_A50803, eep.getType());

    // Old EEP number ...

    eep = EepType.lookup("07-08-03").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND
    );

    Assert.assertTrue(eep instanceof EepA50803);
    Assert.assertEquals(EepType.EEP_TYPE_A50803, eep.getType());
  }

  @Test public void testUpdateSupplyVoltage() throws Exception
  {
    EepA50803 eep = (EepA50803)EepType.lookup("A5-08-03").createEep(
        deviceID, Constants.SUPPLY_VOLTAGE_STATUS_COMMAND
    );

    Assert.assertNull(eep.getSupplyVoltage());


    int rawVoltageValue = 0;
    int rawTempValue = 0;
    int rawIlluValue = 0;
    boolean isPirOn = false;
    boolean isOccBtnPressed = false;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getSupplyVoltage());


    rawVoltageValue = 0;

    telegram = createRadioTelegramESP3(
        deviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getSupplyVoltage());


    rawVoltageValue = 255;

    telegram = createRadioTelegramESP2(
        deviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(5.1), eep.getSupplyVoltage());


    rawVoltageValue = 0;
    isTeachIn = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(5.1), eep.getSupplyVoltage());
  }

  @Test public void testUpdateIllumination() throws Exception
  {
    EepA50803 eep = (EepA50803)EepType.lookup("A5-08-03").createEep(
        deviceID, Constants.ILLUMINATION_STATUS_COMMAND
    );

    Assert.assertNull(eep.getIllumination());


    int rawVoltageValue = 0;
    int rawTempValue = 0;
    int rawIlluValue = 0;
    boolean isPirOn = false;
    boolean isOccBtnPressed = false;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getIllumination());


    rawIlluValue = 0;

    telegram = createRadioTelegramESP3(
        deviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getIllumination());


    rawIlluValue = 255;

    telegram = createRadioTelegramESP2(
        deviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(1530), eep.getIllumination());


    rawIlluValue = 0;
    isTeachIn = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(1530), eep.getIllumination());
  }

  @Test public void testUpdateTemperature() throws Exception
  {
    EepA50803 eep = (EepA50803)EepType.lookup("A5-08-03").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND
    );

    Assert.assertNull(eep.getTemperature());


    int rawVoltageValue = 0;
    int rawTempValue = 0;
    int rawIlluValue = 0;
    boolean isPirOn = false;
    boolean isOccBtnPressed = false;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(-30), eep.getTemperature());


    rawTempValue = 0;

    telegram = createRadioTelegramESP3(
        deviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(-30), eep.getTemperature());


    rawTempValue = 255;

    telegram = createRadioTelegramESP2(
        deviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(50), eep.getTemperature());


    rawTempValue = 0;
    isTeachIn = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(50), eep.getTemperature());
  }

  @Test public void testUpdatePirStatus() throws Exception
  {
    EepA50803 eep = (EepA50803)EepType.lookup("A5-08-03").createEep(
        deviceID, Constants.PIR_STATUS_COMMAND
    );

    Assert.assertNull(eep.isPirOn());


    int rawVoltageValue = 0;
    int rawTempValue = 0;
    int rawIlluValue = 0;
    boolean isPirOn = false;
    boolean isOccBtnPressed = false;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertFalse(eep.isPirOn());


    isPirOn = false;

    telegram = createRadioTelegramESP3(
        deviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertFalse(eep.isPirOn());


    isPirOn = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertTrue(eep.isPirOn());


    isPirOn = false;
    isTeachIn = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertTrue(eep.isPirOn());
  }

  @Test public void testUpdateOccupancy() throws Exception
  {
    EepA50803 eep = (EepA50803)EepType.lookup("A5-08-03").createEep(
        deviceID, Constants.OCCUPANCY_STATUS_COMMAND
    );

    Assert.assertNull(eep.isOccupancy());


    int rawVoltageValue = 0;
    int rawTempValue = 0;
    int rawIlluValue = 0;
    boolean isPirOn = false;
    boolean isOccBtnPressed = false;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertFalse(eep.isOccupancy());


    isOccBtnPressed = false;

    telegram = createRadioTelegramESP3(
        deviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertFalse(eep.isOccupancy());


    isOccBtnPressed = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertTrue(eep.isOccupancy());


    isOccBtnPressed = false;
    isTeachIn = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertTrue(eep.isOccupancy());
  }

  @Test (expected = ConfigurationException.class)
  public void testUnknownCommand() throws Exception
  {
    Eep eep = EepType.lookup("A5-08-03").createEep(
        deviceID, "UNKONWN_COMMAND"
    );
  }

  @Test public void testInvalidRadioTelegramType() throws Exception
  {
    EepA50803 eep = (EepA50803)EepType.lookup("A5-08-03").createEep(
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
    EepA50803 eep = (EepA50803)EepType.lookup("A5-08-03").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND
    );

    int rawVoltageValue = 0;
    int rawTempValue = 0;
    int rawIlluValue = 0;
    boolean isPirOn = false;
    boolean isOccBtnPressed = false;
    boolean isTeachIn = false;
    DeviceID invalidDeviceID = DeviceID.fromString("0xFF800002");

    EspRadioTelegram telegram = createRadioTelegramESP3(
        invalidDeviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);


    telegram = createRadioTelegramESP2(
        invalidDeviceID, rawVoltageValue, rawIlluValue, rawTempValue, isPirOn, isOccBtnPressed, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
  }


  // Helpers --------------------------------------------------------------------------------------

  protected Esp34BSTelegram createRadioTelegramESP3(DeviceID deviceID, int rawVoltageValue, int rawIlluValue,
                                                    int rawTempValue, boolean isPirOn, boolean isOccBtnPressed,
                                                    boolean isTeachIn)
  {
    byte[] payload = new byte[4];
    payload[0] = (byte)rawVoltageValue;
    payload[1] = (byte)rawIlluValue;
    payload[2] = (byte)rawTempValue;
    payload[3] |= (byte)(isTeachIn ? 0x00 : 0x08);
    payload[3] |= (byte)(isPirOn ? 0x00 : 0x02);
    payload[3] |= (byte)(isOccBtnPressed ? 0x00 : 0x01);

    Esp34BSTelegram telegram = new Esp34BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }

  protected Esp24BSTelegram createRadioTelegramESP2(DeviceID deviceID, int rawVoltageValue, int rawIlluValue,
                                                    int rawTempValue, boolean isPirOn, boolean isOccBtnPressed,
                                                    boolean isTeachIn)
  {
    byte[] payload = new byte[4];
    payload[0] = (byte)rawVoltageValue;
    payload[1] = (byte)rawIlluValue;
    payload[2] = (byte)rawTempValue;
    payload[3] |= (byte)(isTeachIn ? 0x00 : 0x08);
    payload[3] |= (byte)(isPirOn ? 0x00 : 0x02);
    payload[3] |= (byte)(isOccBtnPressed ? 0x00 : 0x01);

    Esp24BSTelegram telegram = new Esp24BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }
}
