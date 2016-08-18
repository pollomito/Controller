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
 * Unit tests for {@link EepA51013} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA51013Test
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

    Eep eep = EepType.lookup("A5-10-13").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND, null
    );

    Assert.assertTrue(eep instanceof EepA51013);
    Assert.assertEquals(EepType.EEP_TYPE_A51013, eep.getType());

    // Old EEP number ...

    eep = EepType.lookup("07-10-13").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND, null
    );

    Assert.assertTrue(eep instanceof EepA51013);
    Assert.assertEquals(EepType.EEP_TYPE_A51013, eep.getType());
  }

  @Test public void testUpdateTemperature() throws Exception
  {
    EepA51013 eep = (EepA51013)EepType.lookup("A5-10-13").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND, null
    );

    Assert.assertNull(eep.getTemperature());


    int rawHumidityValue = 0;
    int rawTempValue = 0;
    boolean isOccupied = false;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawHumidityValue, rawTempValue, isOccupied, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getTemperature());


    rawTempValue = 0;

    telegram = createRadioTelegramESP3(
        deviceID, rawHumidityValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getTemperature());


    rawTempValue = 250;

    telegram = createRadioTelegramESP2(
        deviceID, rawHumidityValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(40), eep.getTemperature());


    rawTempValue = 0;
    isTeachIn = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawHumidityValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(40), eep.getTemperature());

  }

  @Test public void testUpdateHumidity() throws Exception
  {
    EepA51013 eep = (EepA51013)EepType.lookup("A5-10-13").createEep(
        deviceID, Constants.HUMIDITY_STATUS_COMMAND, null
    );

    Assert.assertNull(eep.getHumidity());


    int rawHumidityValue = 0;
    int rawTempValue = 0;
    boolean isOccupied = false;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawHumidityValue, rawTempValue, isOccupied, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getHumidity());


    rawHumidityValue = 0;

    telegram = createRadioTelegramESP3(
        deviceID, rawHumidityValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getHumidity());


    rawHumidityValue = 250;

    telegram = createRadioTelegramESP2(
        deviceID, rawHumidityValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(100), eep.getHumidity());


    rawHumidityValue = 0;
    isTeachIn = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawHumidityValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(100), eep.getHumidity());

  }

  @Test public void testUpdateOccupancy() throws Exception
  {
    EepA51013 eep = (EepA51013)EepType.lookup("A5-10-13").createEep(
        deviceID, Constants.OCCUPANCY_STATUS_COMMAND, null
    );

    Assert.assertNull(eep.isOccupied());


    int rawHumidityValue = 0;
    int rawTempValue = 0;
    boolean isOccupied = false;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawHumidityValue, rawTempValue, isOccupied, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertFalse(eep.isOccupied());


    isOccupied = false;

    telegram = createRadioTelegramESP3(
        deviceID, rawHumidityValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertFalse(eep.isOccupied());


    isOccupied = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawHumidityValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertTrue(eep.isOccupied());


    isOccupied = false;
    isTeachIn = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawHumidityValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertTrue(eep.isOccupied());

  }

  @Test (expected = ConfigurationException.class)
  public void testUnknownCommand() throws Exception
  {
    Eep eep = EepType.lookup("A5-10-13").createEep(
        deviceID, "UNKONWN_COMMAND", null
    );
  }

  @Test public void testInvalidRadioTelegramType() throws Exception
  {
    EepA51013 eep = (EepA51013)EepType.lookup("A5-10-13").createEep(
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
    EepA51013 eep = (EepA51013)EepType.lookup("A5-10-13").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND, null
    );

    int rawHumidityValue = 0;
    int rawTempValue = 0;
    boolean isOccupied = false;
    boolean isTeachIn = false;
    DeviceID invalidDeviceID = DeviceID.fromString("0xFF800002");

    EspRadioTelegram telegram = createRadioTelegramESP3(
        invalidDeviceID, rawHumidityValue, rawTempValue, isOccupied, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);


    telegram = createRadioTelegramESP2(
        invalidDeviceID, rawHumidityValue, rawTempValue, isOccupied, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
  }


  // Helpers --------------------------------------------------------------------------------------

  private Esp34BSTelegram createRadioTelegramESP3(DeviceID deviceID, int rawHumidityValue,
                                                  int rawTempValue, boolean isOccupancy, boolean isTeachIn)
  {
    byte[] payload = new byte[4];
    payload[1] = (byte)rawHumidityValue;
    payload[2] = (byte)rawTempValue;
    payload[3] |= (byte)(isTeachIn ? 0x00 : 0x08);
    payload[3] |= (byte)(isOccupancy ? 0x00 : 0x01);

    Esp34BSTelegram telegram = new Esp34BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }

  private Esp24BSTelegram createRadioTelegramESP2(DeviceID deviceID, int rawHumidityValue,
                                                  int rawTempValue, boolean isOccupancy, boolean isTeachIn)
  {
    byte[] payload = new byte[4];
    payload[1] = (byte)rawHumidityValue;
    payload[2] = (byte)rawTempValue;
    payload[3] |= (byte)(isTeachIn ? 0x00 : 0x08);
    payload[3] |= (byte)(isOccupancy ? 0x00 : 0x01);

    Esp24BSTelegram telegram = new Esp24BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }
}
