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
 * Unit tests for {@link EepA50904} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA50904Test
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

    Eep eep = EepType.lookup("A5-09-04").createEep(
        deviceID, Constants.CONCENTRATION_STATUS_COMMAND
    );

    Assert.assertTrue(eep instanceof EepA50904);
    Assert.assertEquals(EepType.EEP_TYPE_A50904, eep.getType());

    // Old EEP number ...

    eep = EepType.lookup("07-09-04").createEep(
        deviceID, Constants.CONCENTRATION_STATUS_COMMAND
    );

    Assert.assertTrue(eep instanceof EepA50904);
    Assert.assertEquals(EepType.EEP_TYPE_A50904, eep.getType());
  }

  @Test public void testUpdateHumidity() throws Exception
  {
    EepA50904 eep = (EepA50904)EepType.lookup("A5-09-04").createEep(
        deviceID, Constants.HUMIDITY_STATUS_COMMAND
    );

    Assert.assertNull(eep.getHumidity());


    int rawConcValue = 0;
    int rawHumValue = 0;
    int rawTempValue = 0;
    boolean isHumidity = true;
    boolean isTemperature = true;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawConcValue, rawTempValue, rawHumValue,
        isHumidity, isTemperature, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getHumidity());


    rawHumValue = 0;

    telegram = createRadioTelegramESP3(
        deviceID, rawConcValue, rawTempValue, rawHumValue,
        isHumidity, isTemperature, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getHumidity());


    rawHumValue = 200;

    telegram = createRadioTelegramESP3(
        deviceID, rawConcValue, rawTempValue, rawHumValue,
        isHumidity, isTemperature, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(100), eep.getHumidity());


    rawHumValue = 201;

    telegram = createRadioTelegramESP2(
        deviceID, rawConcValue, rawTempValue, rawHumValue,
        isHumidity, isTemperature, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(100), eep.getHumidity());


    rawHumValue = 0;
    isTeachIn = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawConcValue, rawTempValue, rawHumValue,
        isHumidity, isTemperature, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(100), eep.getHumidity());


    rawHumValue = 0;
    isHumidity = false;
    isTeachIn = false;


    telegram = createRadioTelegramESP2(
        deviceID, rawConcValue, rawTempValue, rawHumValue,
        isHumidity, isTemperature, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(100), eep.getHumidity());

  }

  @Test public void testUpdateTemperature() throws Exception
  {
    EepA50904 eep = (EepA50904)EepType.lookup("A5-09-04").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND
    );

    Assert.assertNull(eep.getTemperature());


    int rawConcValue = 0;
    int rawHumValue = 0;
    int rawTempValue = 0;
    boolean isHumidity = true;
    boolean isTemperature = true;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawConcValue, rawTempValue, rawHumValue,
        isHumidity, isTemperature, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getTemperature());


    rawTempValue = 0;

    telegram = createRadioTelegramESP3(
        deviceID, rawConcValue, rawTempValue, rawHumValue,
        isHumidity, isTemperature, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getTemperature());


    rawTempValue = 255;

    telegram = createRadioTelegramESP2(
        deviceID, rawConcValue, rawTempValue, rawHumValue,
        isHumidity, isTemperature, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(51), eep.getTemperature());


    rawTempValue = 0;
    isTeachIn = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawConcValue, rawTempValue, rawHumValue,
        isHumidity, isTemperature, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(51), eep.getTemperature());


    rawTempValue = 0;
    isTemperature = false;
    isTeachIn = false;


    telegram = createRadioTelegramESP2(
        deviceID, rawConcValue, rawTempValue, rawHumValue,
        isHumidity, isTemperature, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(51), eep.getTemperature());
  }

  @Test public void testUpdateConcentration() throws Exception
  {
    EepA50904 eep = (EepA50904)EepType.lookup("A5-09-04").createEep(
        deviceID, Constants.CONCENTRATION_STATUS_COMMAND
    );

    Assert.assertNull(eep.getConcentration());


    int rawConcValue = 0;
    int rawHumValue = 0;
    int rawTempValue = 0;
    boolean isHumidity = true;
    boolean isTemperature = true;
    boolean isTeachIn = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawConcValue, rawTempValue, rawHumValue,
        isHumidity, isTemperature, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getConcentration());


    rawConcValue = 0;

    telegram = createRadioTelegramESP3(
        deviceID, rawConcValue, rawTempValue, rawHumValue,
        isHumidity, isTemperature, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getConcentration());


    rawConcValue = 255;

    telegram = createRadioTelegramESP2(
        deviceID, rawConcValue, rawTempValue, rawHumValue,
        isHumidity, isTemperature, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(2550), eep.getConcentration());


    rawConcValue = 0;
    isTeachIn = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawConcValue, rawTempValue, rawHumValue,
        isHumidity, isTemperature, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(2550), eep.getConcentration());


    rawConcValue = 0;
    isTemperature = false;
    isHumidity = false;
    isTeachIn = false;


    telegram = createRadioTelegramESP2(
        deviceID, rawConcValue, rawTempValue, rawHumValue,
        isHumidity, isTemperature, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getConcentration());
  }

  @Test (expected = ConfigurationException.class)
  public void testUnknownCommand() throws Exception
  {
    Eep eep = EepType.lookup("A5-09-04").createEep(
        deviceID, "UNKONWN_COMMAND"
    );
  }

  @Test public void testInvalidRadioTelegramType() throws Exception
  {
    EepA50904 eep = (EepA50904)EepType.lookup("A5-09-04").createEep(
        deviceID, Constants.CONCENTRATION_STATUS_COMMAND
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
    EepA50904 eep = (EepA50904)EepType.lookup("A5-09-04").createEep(
        deviceID, Constants.CONCENTRATION_STATUS_COMMAND
    );

    int rawConcValue = 0;
    int rawHumValue = 0;
    int rawTempValue = 0;
    boolean isHumidity = true;
    boolean isTemperature = true;
    boolean isTeachIn = false;
    DeviceID invalidDeviceID = DeviceID.fromString("0xFF800002");

    EspRadioTelegram telegram = createRadioTelegramESP3(
        invalidDeviceID, rawConcValue, rawTempValue, rawHumValue,
        isHumidity, isTemperature, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);


    telegram = createRadioTelegramESP2(
        invalidDeviceID, rawConcValue, rawTempValue, rawHumValue,
        isHumidity, isTemperature, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
  }


  // Helpers --------------------------------------------------------------------------------------

  private Esp34BSTelegram createRadioTelegramESP3(DeviceID deviceID, int rawConcValue, int rawTempValue,
                                                  int rawHumValue, boolean isHumidity, boolean isTemperature,
                                                  boolean isTeachIn)
  {
    byte[] payload = new byte[4];
    payload[0] = (byte)rawHumValue;
    payload[1] = (byte)rawConcValue;
    payload[2] = (byte)rawTempValue;
    payload[3] |= (byte)(isTeachIn ? 0x00 : 0x08);
    payload[3] |= (byte)(isHumidity ? 0x04 : 0x00);
    payload[3] |= (byte)(isTemperature ? 0x02 : 0x01);

    Esp34BSTelegram telegram = new Esp34BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }

  private Esp24BSTelegram createRadioTelegramESP2(DeviceID deviceID, int rawConcValue, int rawTempValue,
                                                  int rawHumValue, boolean isHumidity, boolean isTemperature,
                                                  boolean isTeachIn)
  {
    byte[] payload = new byte[4];
    payload[0] = (byte)rawHumValue;
    payload[1] = (byte)rawConcValue;
    payload[2] = (byte)rawTempValue;
    payload[3] |= (byte)(isTeachIn ? 0x00 : 0x08);
    payload[3] |= (byte)(isHumidity ? 0x04 : 0x00);
    payload[3] |= (byte)(isTemperature ? 0x02 : 0x01);

    Esp24BSTelegram telegram = new Esp24BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }
}
