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
 * Unit tests for {@link EepA51003} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA51003Test
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

    Eep eep = EepType.lookup("A5-10-03").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND, null
    );

    Assert.assertTrue(eep instanceof EepA51003);
    Assert.assertEquals(EepType.EEP_TYPE_A51003, eep.getType());

    // Old EEP number ...

    eep = EepType.lookup("07-10-03").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND, null
    );

    Assert.assertTrue(eep instanceof EepA51003);
    Assert.assertEquals(EepType.EEP_TYPE_A51003, eep.getType());
  }

  @Test public void testUpdateSetPoint() throws Exception
  {

    EepA51003 eep = (EepA51003)EepType.lookup("A5-10-03").createEep(
        deviceID, Constants.SET_POINT_STATUS_COMMAND, null
    );

    Assert.assertNull(eep.getSetPoint());


    int rawSetPointValue = 0;
    int rawTempValue = 0;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawSetPointValue, rawTempValue, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getSetPoint());


    rawSetPointValue = 0;

    telegram = createRadioTelegramESP3(
        deviceID, rawSetPointValue, rawTempValue, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getSetPoint());


    rawSetPointValue = 255;

    telegram = createRadioTelegramESP2(
        deviceID, rawSetPointValue, rawTempValue, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(255), eep.getSetPoint());


    isTeachIn = true;
    rawSetPointValue = 0;

    telegram = createRadioTelegramESP2(
        deviceID, rawSetPointValue, rawTempValue, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(255), eep.getSetPoint());

  }

  @Test public void testUpdateTemperature() throws Exception
  {
    EepA51003 eep = (EepA51003)EepType.lookup("A5-10-03").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND, null
    );

    Assert.assertNull(eep.getTemperature());


    int rawSetPointValue = 0;
    int rawTempValue = 255;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawSetPointValue, rawTempValue, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getTemperature());


    rawTempValue = 255;

    telegram = createRadioTelegramESP3(
        deviceID, rawSetPointValue, rawTempValue, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getTemperature());


    rawTempValue = 0;

    telegram = createRadioTelegramESP2(
        deviceID, rawSetPointValue, rawTempValue, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(40), eep.getTemperature());


    rawTempValue = 0;
    isTeachIn = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawSetPointValue, rawTempValue, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(40), eep.getTemperature());

  }

  @Test (expected = ConfigurationException.class)
  public void testUnknownCommand() throws Exception
  {
    Eep eep = EepType.lookup("A5-10-03").createEep(
        deviceID, "UNKONWN_COMMAND", null
    );
  }

  @Test public void testInvalidRadioTelegramType() throws Exception
  {
    EepA51003 eep = (EepA51003)EepType.lookup("A5-10-03").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND, null
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
    EepA51003 eep = (EepA51003)EepType.lookup("A5-10-03").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND, null
    );

    int rawSetPointValue = 0;
    int rawTempValue = 0;
    boolean isTeachIn = false;
    DeviceID invalidDeviceID = DeviceID.fromString("0xFF800002");

    EspRadioTelegram telegram = createRadioTelegramESP3(
        invalidDeviceID, rawSetPointValue, rawTempValue, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);


    telegram = createRadioTelegramESP2(
        invalidDeviceID, rawSetPointValue, rawTempValue, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
  }


  // Helpers --------------------------------------------------------------------------------------

  private Esp34BSTelegram createRadioTelegramESP3(DeviceID deviceID, int rawSetPointValue,
                                                  int rawTempValue, boolean isTeachIn)
  {
    byte[] payload = new byte[4];
    payload[1] = (byte)rawSetPointValue;
    payload[2] = (byte)rawTempValue;
    payload[3] |= (byte)(isTeachIn ? 0x00 : 0x08);

    Esp34BSTelegram telegram = new Esp34BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }

  private Esp24BSTelegram createRadioTelegramESP2(DeviceID deviceID, int rawSetPointValue,
                                                  int rawTempValue, boolean isTeachIn)
  {
    byte[] payload = new byte[4];
    payload[1] = (byte)rawSetPointValue;
    payload[2] = (byte)rawTempValue;
    payload[3] |= (byte)(isTeachIn ? 0x00 : 0x08);

    Esp24BSTelegram telegram = new Esp24BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }
}
