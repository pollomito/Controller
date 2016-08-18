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
 * Unit tests for {@link EepA50602} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA50602Test
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

    Eep eep = EepType.lookup("A5-06-02").createEep(
        deviceID, Constants.ILLUMINATION_STATUS_COMMAND, null
    );

    Assert.assertTrue(eep instanceof EepA50602);
    Assert.assertEquals(EepType.EEP_TYPE_A50602, eep.getType());

    // Old EEP number ...

    eep = EepType.lookup("07-06-02").createEep(
        deviceID, Constants.ILLUMINATION_STATUS_COMMAND, null
    );

    Assert.assertTrue(eep instanceof EepA50602);
    Assert.assertEquals(EepType.EEP_TYPE_A50602, eep.getType());
  }

  @Test public void testUpdateIllumination1() throws Exception
  {
    EepA50602 eep = (EepA50602)EepType.lookup("A5-06-02").createEep(
        deviceID, Constants.ILLUMINATION_STATUS_COMMAND, null
    );

    Assert.assertNull(eep.getIllumination1());

    int rawIllu1Value = 0;
    int rawIllu2Value = 0;
    int rawVoltageValue = 255;
    boolean isTeachIn = false;
    boolean isIllu2 = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawIllu1Value, rawIllu2Value, rawVoltageValue, isIllu2, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getIllumination1());


    rawIllu1Value = 0;
    telegram = createRadioTelegramESP3(
        deviceID, rawIllu1Value, rawIllu2Value, rawVoltageValue, isIllu2, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getIllumination1());


    rawIllu1Value = 255;
    telegram = createRadioTelegramESP2(
        deviceID, rawIllu1Value, rawIllu2Value, rawVoltageValue, isIllu2, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(1020), eep.getIllumination1());


    rawIllu1Value = 0;
    isTeachIn = true;
    telegram = createRadioTelegramESP2(
        deviceID, rawIllu1Value, rawIllu2Value, rawVoltageValue, isIllu2, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(1020), eep.getIllumination1());
  }

  @Test public void testUpdateIllumination2() throws Exception
  {
    EepA50602 eep = (EepA50602)EepType.lookup("A5-06-02").createEep(
        deviceID, Constants.ILLUMINATION_STATUS_COMMAND, null
    );

    Assert.assertNull(eep.getIllumination2());

    int rawIllu1Value = 0;
    int rawIllu2Value = 0;
    int rawVoltageValue = 255;
    boolean isTeachIn = false;
    boolean isIllu2 = true;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawIllu1Value, rawIllu2Value, rawVoltageValue, isIllu2, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getIllumination2());


    rawIllu2Value = 0;
    telegram = createRadioTelegramESP3(
        deviceID, rawIllu1Value, rawIllu2Value, rawVoltageValue, isIllu2, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getIllumination2());


    rawIllu2Value = 255;
    telegram = createRadioTelegramESP2(
        deviceID, rawIllu1Value, rawIllu2Value, rawVoltageValue, isIllu2, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(510), eep.getIllumination2());


    rawIllu2Value = 0;
    isTeachIn = true;
    telegram = createRadioTelegramESP2(
        deviceID, rawIllu1Value, rawIllu2Value, rawVoltageValue, isIllu2, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(510), eep.getIllumination2());
  }

  @Test public void testUpdateSupplyVoltage() throws Exception
  {
    EepA50602 eep = (EepA50602)EepType.lookup("A5-06-02").createEep(
        deviceID, Constants.SUPPLY_VOLTAGE_STATUS_COMMAND, null
    );

    Assert.assertNull(eep.getSupplyVoltage());

    int rawIllu1Value = 0;
    int rawIllu2Value = 0;
    int rawVoltageValue = 0;
    boolean isTeachIn = false;
    boolean isIllu2 = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawIllu1Value, rawIllu2Value, rawVoltageValue, isIllu2, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getSupplyVoltage());


    rawVoltageValue = 0;
    telegram = createRadioTelegramESP3(
        deviceID, rawIllu1Value, rawIllu2Value, rawVoltageValue, isIllu2, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getSupplyVoltage());


    rawVoltageValue = 255;
    telegram = createRadioTelegramESP2(
        deviceID, rawIllu1Value, rawIllu2Value, rawVoltageValue, isIllu2, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(5.1), eep.getSupplyVoltage());


    rawVoltageValue = 0;
    isTeachIn = true;
    telegram = createRadioTelegramESP2(
        deviceID, rawIllu1Value, rawIllu2Value, rawVoltageValue, isIllu2, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(5.1), eep.getSupplyVoltage());
  }

  @Test (expected = ConfigurationException.class)
  public void testUnknownCommand() throws Exception
  {
    Eep eep = EepType.lookup("A5-06-02").createEep(
        deviceID, "UNKONWN_COMMAND", null
    );
  }

  @Test public void testInvalidRadioTelegramType() throws Exception
  {
    EepA50602 eep = (EepA50602)EepType.lookup("A5-06-02").createEep(
        deviceID, Constants.ILLUMINATION_STATUS_COMMAND, null
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
    EepA50602 eep = (EepA50602)EepType.lookup("A5-06-02").createEep(
        deviceID, Constants.ILLUMINATION_STATUS_COMMAND, null
    );

    int rawIllu1Value = 0;
    int rawIllu2Value = 0;
    int rawVoltageValue = 255;
    boolean isTeachIn = false;
    boolean isIllu2 = false;
    DeviceID invalidDeviceID = DeviceID.fromString("0xFF800002");


    EspRadioTelegram telegram = createRadioTelegramESP3(
        invalidDeviceID, rawIllu1Value, rawIllu2Value, rawVoltageValue, isIllu2, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);


    telegram = createRadioTelegramESP2(
        invalidDeviceID, rawIllu1Value, rawIllu2Value, rawVoltageValue, isIllu2, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
  }


  // Helpers --------------------------------------------------------------------------------------

  private Esp34BSTelegram createRadioTelegramESP3(DeviceID deviceID, int rawIllu1Value, int rawIllu2Value,
                                                  int rawVoltageValue, boolean isIllu2, boolean isTeachIn)
  {
    byte[] payload = new byte[4];
    payload[0] = (byte)rawVoltageValue;
    payload[1] = (byte)rawIllu2Value;
    payload[2] = (byte)rawIllu1Value;
    payload[3] |= (byte)(isTeachIn ? 0x00 : 0x08);
    payload[3] |= (byte)(isIllu2 ? 0x01 : 0x00);

    Esp34BSTelegram telegram = new Esp34BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }

  private Esp24BSTelegram createRadioTelegramESP2(DeviceID deviceID, int rawIllu1Value, int rawIllu2Value,
                                                  int rawVoltageValue, boolean isIllu2, boolean isTeachIn)
  {
    byte[] payload = new byte[4];
    payload[0] = (byte)rawVoltageValue;
    payload[1] = (byte)rawIllu2Value;
    payload[2] = (byte)rawIllu1Value;
    payload[3] |= (byte)(isTeachIn ? 0x00 : 0x08);
    payload[3] |= (byte)(isIllu2 ? 0x01 : 0x00);

    Esp24BSTelegram telegram = new Esp24BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }
}
