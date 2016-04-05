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
import org.openremote.controller.protocol.enocean.Constants;
import org.openremote.controller.protocol.enocean.DeviceID;

/**
 * Unit tests for {@link EepA50218} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA50218Test extends EepA502XXTest
{

  // Tests ----------------------------------------------------------------------------------------

  @Test public void testBasicConstruction() throws Exception
  {

    // New EEP number ...

    Eep eep = EepType.lookup("A5-02-18").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND
    );

    Assert.assertTrue(eep instanceof EepA50218);
    Assert.assertEquals(EepType.EEP_TYPE_A50218, eep.getType());

    // Old EEP number ...

    eep = EepType.lookup("07-02-18").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND
    );

    Assert.assertTrue(eep instanceof EepA50218);
    Assert.assertEquals(EepType.EEP_TYPE_A50218, eep.getType());
  }

  @Test public void testUpdateESP3() throws Exception
  {
    EepA50218 eep = (EepA50218)EepType.lookup("A5-02-18").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND
    );

    int rawTemperatureValue = 255;
    Boolean isUpdate = eep.update(createRadioTelegramESP3(deviceID, rawTemperatureValue));

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(20), eep.getTemperature(), 0.0);


    rawTemperatureValue = 255;
    isUpdate = eep.update(createRadioTelegramESP3(deviceID, rawTemperatureValue));
    Assert.assertFalse(isUpdate);


    rawTemperatureValue = 0;
    isUpdate = eep.update(createRadioTelegramESP3(deviceID, rawTemperatureValue));

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(100), eep.getTemperature(), 0.0);
  }

  @Test public void testUpdateESP2() throws Exception
  {
    EepA50218 eep = (EepA50218)EepType.lookup("A5-02-18").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND
    );

    int rawTemperatureValue = 255;
    Boolean isUpdate = eep.update(createRadioTelegramESP2(deviceID, rawTemperatureValue));

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(20), eep.getTemperature(), 0.0);


    rawTemperatureValue = 255;
    isUpdate = eep.update(createRadioTelegramESP2(deviceID, rawTemperatureValue));
    Assert.assertFalse(isUpdate);


    rawTemperatureValue = 0;
    isUpdate = eep.update(createRadioTelegramESP2(deviceID, rawTemperatureValue));

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(100), eep.getTemperature(), 0.0);
  }

  @Test public void testUpdateWithTeachInTelegram() throws Exception
  {
    EepA50218 eep = (EepA50218)EepType.lookup("A5-02-18").createEep(
        deviceID, Constants.TEMPERATURE_STATUS_COMMAND
    );

    // Regular update...

    int rawTemperatureValue = 255;
    Boolean isUpdate = eep.update(createRadioTelegramESP3(deviceID, rawTemperatureValue));

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(20), eep.getTemperature(), 0.0);


    // Update with teach-in telegram...

    rawTemperatureValue = 100;
    isUpdate = eep.update(createTeachInTelegram(deviceID, rawTemperatureValue));

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Double.valueOf(20), eep.getTemperature(), 0.0);


    // Regular update...

    rawTemperatureValue = 0;
    isUpdate = eep.update(createRadioTelegramESP3(deviceID, rawTemperatureValue));

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Double.valueOf(100), eep.getTemperature(), 0.0);
  }
}




