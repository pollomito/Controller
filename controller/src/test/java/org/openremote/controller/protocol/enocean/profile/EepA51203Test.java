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
import org.openremote.controller.protocol.enocean.packet.radio.Esp21BSTelegram;
import org.openremote.controller.protocol.enocean.packet.radio.Esp31BSTelegram;
import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram;

import java.math.BigDecimal;

/**
 * Unit tests for {@link EepA51203} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA51203Test extends EepA512XXTest
{

  // Tests ----------------------------------------------------------------------------------------

  @Test public void testBasicConstruction() throws Exception
  {

    // New EEP number ...

    Eep eep = EepType.lookup("A5-12-03").createEep(
        deviceID, Constants.AMR_METER_READING_STATUS_COMMAND, null
    );

    Assert.assertTrue(eep instanceof EepA51203);
    Assert.assertEquals(EepType.EEP_TYPE_A51203, eep.getType());

    // Old EEP number ...

    eep = EepType.lookup("07-12-03").createEep(
        deviceID, Constants.AMR_METER_READING_STATUS_COMMAND, null
    );

    Assert.assertTrue(eep instanceof EepA51203);
    Assert.assertEquals(EepType.EEP_TYPE_A51203, eep.getType());
  }

  @Test public void testUpdateMeterReadingESP3() throws Exception
  {
    EepA51203 eep = (EepA51203)EepType.lookup("A5-12-03").createEep(
        deviceID, Constants.AMR_METER_READING_STATUS_COMMAND, null
    );

    Assert.assertNull(eep.getMeterReading());

    int rawMeterValue = 0;
    int rawChannelValue = 0;
    boolean isTeachin = false;
    boolean isCurrentValue = false;
    int rawDivisorValue = 0; // divisor = 1

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawMeterValue, rawChannelValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getMeterReading());


    rawMeterValue = 0;

    telegram = createRadioTelegramESP3(
        deviceID, rawMeterValue, rawChannelValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getMeterReading());


    rawMeterValue = 16777215;

    telegram = createRadioTelegramESP3(
        deviceID, rawMeterValue, rawChannelValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(16777215), eep.getMeterReading());


    rawMeterValue = 0;
    isTeachin = true;

    telegram = createRadioTelegramESP3(
        deviceID, rawMeterValue, rawChannelValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(16777215), eep.getMeterReading());
  }

  @Test public void testUpdateMeterReadingESP2() throws Exception
  {
    EepA51203 eep = (EepA51203)EepType.lookup("A5-12-03").createEep(
        deviceID, Constants.AMR_METER_READING_STATUS_COMMAND, null
    );

    Assert.assertNull(eep.getMeterReading());

    int rawMeterValue = 0;
    int rawChannelValue = 0;
    boolean isTeachin = false;
    boolean isCurrentValue = false;
    int rawDivisorValue = 0; // divisor = 1

    EspRadioTelegram telegram = createRadioTelegramESP2(
        deviceID, rawMeterValue, rawChannelValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getMeterReading());


    rawMeterValue = 0;

    telegram = createRadioTelegramESP2(
        deviceID, rawMeterValue, rawChannelValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(0), eep.getMeterReading());


    rawMeterValue = 16777215;

    telegram = createRadioTelegramESP2(
        deviceID, rawMeterValue, rawChannelValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(16777215), eep.getMeterReading());


    rawMeterValue = 0;
    isTeachin = true;

    telegram = createRadioTelegramESP2(
        deviceID, rawMeterValue, rawChannelValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(16777215), eep.getMeterReading());
  }

  @Test public void testUpdateMeterReadingWithDivisor() throws Exception
  {
    int[] rawDivisorValues = {0, 1, 2, 3};
    int[] divisors = {1, 10, 100, 1000};
    double [] scaledValues = {1, 0.1, 0.01, 0.001};

    EepA51203 eep = (EepA51203)EepType.lookup("A5-12-03").createEep(
        deviceID, Constants.AMR_METER_READING_STATUS_COMMAND, null
    );

    int tariffInfo = 0;
    boolean isTeachin = false;
    boolean isCurrentValue = false;


    for(int index = 0; index < divisors.length; index++)
    {
      int rawMeterValue = 1;
      int rawDivisorValue = rawDivisorValues[index];

      EspRadioTelegram telegram = createRadioTelegramESP2(
          deviceID, rawMeterValue, tariffInfo, isTeachin, isCurrentValue, rawDivisorValue
      );

      eep.update(telegram);

      BigDecimal expectedValue = BigDecimal.valueOf(scaledValues[index]);
      BigDecimal actualValue = BigDecimal.valueOf(eep.getMeterReading());

      Assert.assertTrue(expectedValue.compareTo(actualValue) == 0);
    }
  }

  @Test public void testTariff() throws Exception
  {
    EepA51203 eep = (EepA51203)EepType.lookup("A5-12-03").createEep(
        deviceID, Constants.AMR_TARIFF_STATUS_COMMAND, null
    );

    Assert.assertNull(eep.getTariff());

    int rawMeterValue = 0;
    int rawTariffValue = 0;
    boolean isTeachin = false;
    boolean isCurrentValue = false;
    int rawDivisorValue = 0; // divisor = 1

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawMeterValue, rawTariffValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(0), eep.getTariff());


    rawTariffValue = 0;

    telegram = createRadioTelegramESP3(
        deviceID, rawMeterValue, rawTariffValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Integer.valueOf(0), eep.getTariff());


    rawTariffValue = 15;

    telegram = createRadioTelegramESP3(
        deviceID, rawMeterValue, rawTariffValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(15), eep.getTariff());


    rawTariffValue = 0;
    isTeachin = true;

    telegram = createRadioTelegramESP3(
        deviceID, rawMeterValue, rawTariffValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Integer.valueOf(15), eep.getTariff());


    isTeachin = false;

    for(rawTariffValue = 0; rawTariffValue <= 15; rawTariffValue++)
    {
      telegram = createRadioTelegramESP3(
          deviceID, rawMeterValue, rawTariffValue, isTeachin, isCurrentValue, rawDivisorValue
      );

      isUpdate = eep.update(telegram);

      Assert.assertTrue(isUpdate);
      Assert.assertEquals(Integer.valueOf(rawTariffValue), eep.getTariff());
    }
  }

  @Test public void testDataType() throws Exception
  {
    EepA51203 eep = (EepA51203)EepType.lookup("A5-12-03").createEep(
        deviceID, Constants.AMR_DATA_TYPE_STATUS_COMMAND, null
    );

    Assert.assertNull(eep.isCurrentValue());

    int rawMeterValue = 0;
    int rawChannelValue = 0;
    boolean isTeachin = false;
    boolean isCurrentValue = true;
    int rawDivisorValue = 0; // divisor = 1

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawMeterValue, rawChannelValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertTrue(eep.isCurrentValue());


    isCurrentValue = true;

    telegram = createRadioTelegramESP3(
        deviceID, rawMeterValue, rawChannelValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertTrue(eep.isCurrentValue());


    isCurrentValue = false;

    telegram = createRadioTelegramESP3(
        deviceID, rawMeterValue, rawChannelValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertFalse(eep.isCurrentValue());


    isCurrentValue = true;
    isTeachin = true;

    telegram = createRadioTelegramESP3(
        deviceID, rawMeterValue, rawChannelValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertFalse(eep.isCurrentValue());
  }

  @Test public void testDivisor() throws Exception
  {
    EepA51203 eep = (EepA51203)EepType.lookup("A5-12-03").createEep(
        deviceID, Constants.AMR_DIVISOR_STATUS_COMMAND, null
    );

    Assert.assertNull(eep.getDivisor());

    int rawMeterValue = 0;
    int rawChannelValue = 0;
    boolean isTeachin = false;
    boolean isCurrentValue = false;
    int rawDivisorValue = 0; // divisor = 1

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, rawMeterValue, rawChannelValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(1), eep.getDivisor());


    rawDivisorValue = 0; // divisor = 1

    telegram = createRadioTelegramESP3(
        deviceID, rawMeterValue, rawChannelValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Integer.valueOf(1), eep.getDivisor());


    rawDivisorValue = 3; // divisor = 1000

    telegram = createRadioTelegramESP3(
        deviceID, rawMeterValue, rawChannelValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(1000), eep.getDivisor());


    rawDivisorValue = 0;
    isTeachin = true;

    telegram = createRadioTelegramESP3(
        deviceID, rawMeterValue, rawChannelValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Integer.valueOf(1000), eep.getDivisor());


    isTeachin = false;
    int[] expectedDivisors = new int[]{1, 10, 100, 1000};

    for(rawDivisorValue = 0; rawDivisorValue <= 3; rawDivisorValue++)
    {
      telegram = createRadioTelegramESP3(
          deviceID, rawMeterValue, rawChannelValue, isTeachin, isCurrentValue, rawDivisorValue
      );

      isUpdate = eep.update(telegram);

      Assert.assertTrue(isUpdate);
      Assert.assertEquals(Integer.valueOf(expectedDivisors[rawDivisorValue]), eep.getDivisor());
    }
  }

  @Test (expected = ConfigurationException.class)
  public void testUnknownCommand() throws Exception
  {
    Eep eep = EepType.lookup("A5-12-03").createEep(
        deviceID, "UNKONWN_COMMAND", null
    );
  }

  @Test public void testInvalidRadioTelegramType() throws Exception
  {

    EepA51203 eep = (EepA51203)EepType.lookup("A5-12-03").createEep(
        deviceID, Constants.AMR_METER_READING_STATUS_COMMAND, null
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
    EepA51203 eep = (EepA51203)EepType.lookup("A5-12-03").createEep(
        deviceID, Constants.AMR_METER_READING_STATUS_COMMAND, null
    );

    int rawMeterValue = 0;
    int rawChannelValue = 0;
    boolean isTeachin = false;
    boolean isCurrentValue = false;
    int rawDivisorValue = 0;

    DeviceID invalidDeviceID = DeviceID.fromString("0xFF800002");

    EspRadioTelegram telegram = createRadioTelegramESP3(
        invalidDeviceID, rawMeterValue, rawChannelValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);


    telegram = createRadioTelegramESP3(
        invalidDeviceID, rawMeterValue, rawChannelValue, isTeachin, isCurrentValue, rawDivisorValue
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
  }
}
