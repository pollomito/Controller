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
 * Unit tests for {@link EepA51001} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA51001Test
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

    Eep eep = EepType.lookup("A5-10-01").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND
    );

    Assert.assertTrue(eep instanceof EepA51001);
    Assert.assertEquals(EepType.EEP_TYPE_A51001, eep.getType());

    // Old EEP number ...

    eep = EepType.lookup("07-10-01").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND
    );

    Assert.assertTrue(eep instanceof EepA51001);
    Assert.assertEquals(EepType.EEP_TYPE_A51001, eep.getType());
  }

  @Test public void testUpdateFanSpeed() throws Exception
  {
    EepA51001 eep = (EepA51001)EepType.lookup("A5-10-01").createEep(
        deviceID, Constants.FAN_SPEED_STATUS_COMMAND
    );

    Assert.assertNull(eep.getFanSpeed());


    int rawFanSpeedValue = 0;
    int rawSetPointValue = 0;
    int rawTempValue = 0;
    boolean isOccupied = false;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(4), eep.getFanSpeed());


    rawFanSpeedValue = 144;

    telegram = createRadioTelegramESP3(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Integer.valueOf(4), eep.getFanSpeed());


    rawFanSpeedValue = 145;

    telegram = createRadioTelegramESP3(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(3), eep.getFanSpeed());


    rawFanSpeedValue = 164;

    telegram = createRadioTelegramESP3(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Integer.valueOf(3), eep.getFanSpeed());


    rawFanSpeedValue = 165;

    telegram = createRadioTelegramESP3(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(2), eep.getFanSpeed());


    rawFanSpeedValue = 189;

    telegram = createRadioTelegramESP3(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Integer.valueOf(2), eep.getFanSpeed());


    rawFanSpeedValue = 190;

    telegram = createRadioTelegramESP2(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(1), eep.getFanSpeed());


    rawFanSpeedValue = 209;

    telegram = createRadioTelegramESP2(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Integer.valueOf(1), eep.getFanSpeed());


    rawFanSpeedValue = 210;

    telegram = createRadioTelegramESP2(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(0), eep.getFanSpeed());


    rawFanSpeedValue = 255;

    telegram = createRadioTelegramESP2(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Integer.valueOf(0), eep.getFanSpeed());

  }


  @Test public void testUpdateSetPoint() throws Exception
  {

    EepA51001 eep = (EepA51001)EepType.lookup("A5-10-01").createEep(
        deviceID, Constants.SET_POINT_STATUS_COMMAND
    );

    Assert.assertNull(eep.getSetPoint());


    int rawFanSpeedValue = 0;
    int rawSetPointValue = 0;
    int rawTempValue = 0;
    boolean isOccupied = false;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getSetPoint());


    rawSetPointValue = 0;

    telegram = createRadioTelegramESP3(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getSetPoint());


    rawSetPointValue = 255;

    telegram = createRadioTelegramESP2(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(255), eep.getSetPoint());


    isTeachIn = true;
    rawSetPointValue = 0;

    telegram = createRadioTelegramESP2(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(255), eep.getSetPoint());

  }

  @Test public void testUpdateTemperature() throws Exception
  {
    EepA51001 eep = (EepA51001)EepType.lookup("A5-10-01").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND
    );

    Assert.assertNull(eep.getTemperature());


    int rawFanSpeedValue = 0;
    int rawSetPointValue = 0;
    int rawTempValue = 255;
    boolean isOccupied = false;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getTemperature());


    rawTempValue = 255;

    telegram = createRadioTelegramESP3(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getTemperature());


    rawTempValue = 0;

    telegram = createRadioTelegramESP2(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(40), eep.getTemperature());


    rawTempValue = 0;
    isTeachIn = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(40), eep.getTemperature());

  }

  @Test public void testUpdateOccupancy() throws Exception
  {
    EepA51001 eep = (EepA51001)EepType.lookup("A5-10-01").createEep(
        deviceID, Constants.OCCUPANCY_STATUS_COMMAND
    );

    Assert.assertNull(eep.isOccupied());


    int rawFanSpeedValue = 0;
    int rawSetPointValue = 0;
    int rawTempValue = 0;
    boolean isOccupied = false;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertFalse(eep.isOccupied());


    isOccupied = false;

    telegram = createRadioTelegramESP3(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertFalse(eep.isOccupied());


    isOccupied = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertTrue(eep.isOccupied());


    isOccupied = false;
    isTeachIn = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertTrue(eep.isOccupied());

  }

  @Test (expected = ConfigurationException.class)
  public void testUnknownCommand() throws Exception
  {
    Eep eep = EepType.lookup("A5-10-01").createEep(
        deviceID, "UNKONWN_COMMAND"
    );
  }

  @Test public void testInvalidRadioTelegramType() throws Exception
  {
    EepA51001 eep = (EepA51001)EepType.lookup("A5-10-01").createEep(
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
    EepA51001 eep = (EepA51001)EepType.lookup("A5-10-01").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND
    );

    int rawFanSpeedValue = 0;
    int rawSetPointValue = 0;
    int rawTempValue = 0;
    boolean isOccupied = false;
    boolean isTeachIn = false;
    DeviceID invalidDeviceID = DeviceID.fromString("0xFF800002");

    EspRadioTelegram telegram = createRadioTelegramESP3(
        invalidDeviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);


    telegram = createRadioTelegramESP2(
        invalidDeviceID, rawFanSpeedValue, rawSetPointValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
  }


  // Helpers --------------------------------------------------------------------------------------

  private Esp34BSTelegram createRadioTelegramESP3(DeviceID deviceID, int rawFanSpeedValue, int rawSetPointValue,
                                                  int rawTempValue, boolean isOccupancy, boolean isTeachIn)
  {
    byte[] payload = new byte[4];
    payload[0] = (byte)rawFanSpeedValue;
    payload[1] = (byte)rawSetPointValue;
    payload[2] = (byte)rawTempValue;
    payload[3] |= (byte)(isTeachIn ? 0x00 : 0x08);
    payload[3] |= (byte)(isOccupancy ? 0x00 : 0x01);

    Esp34BSTelegram telegram = new Esp34BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }

  private Esp24BSTelegram createRadioTelegramESP2(DeviceID deviceID, int rawFanSpeedValue, int rawSetPointValue,
                                                  int rawTempValue, boolean isOccupancy, boolean isTeachIn)
  {
    byte[] payload = new byte[4];
    payload[0] = (byte)rawFanSpeedValue;
    payload[1] = (byte)rawSetPointValue;
    payload[2] = (byte)rawTempValue;
    payload[3] |= (byte)(isTeachIn ? 0x00 : 0x08);
    payload[3] |= (byte)(isOccupancy ? 0x00 : 0x01);

    Esp24BSTelegram telegram = new Esp24BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }
}
