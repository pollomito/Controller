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
import org.openremote.controller.protocol.enocean.packet.radio.Esp21BSTelegram;
import org.openremote.controller.protocol.enocean.packet.radio.Esp31BSTelegram;

/**
 * Unit tests for {@link EepD50001} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepD50001Test
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

    Eep eep = EepType.lookup("D5-00-01").createEep(
        deviceID, Constants.CONTACT_STATUS_COMMAND
    );

    Assert.assertTrue(eep instanceof EepD50001);
    Assert.assertEquals(EepType.EEP_TYPE_D50001, eep.getType());

    // Old EEP number ...

    eep = EepType.lookup("06-00-01").createEep(
        deviceID, Constants.CONTACT_STATUS_COMMAND
    );

    Assert.assertTrue(eep instanceof EepD50001);
    Assert.assertEquals(EepType.EEP_TYPE_D50001, eep.getType());
  }

  @Test public void testUpdate() throws Exception
  {
    EepD50001 eep = (EepD50001)EepType.lookup("D5-00-01").createEep(
        deviceID, Constants.CONTACT_STATUS_COMMAND
    );

    Assert.assertNull(eep.isClosed());

    boolean isLearn = false;
    boolean isContactClosed = true;
    Boolean isUpdate = eep.update(createRadioTelegramESP3(deviceID, isContactClosed, isLearn));

    Assert.assertTrue(isUpdate);
    Assert.assertTrue(eep.isClosed());


    isUpdate = eep.update(createRadioTelegramESP3(deviceID, isContactClosed, isLearn));

    Assert.assertFalse(isUpdate);
    Assert.assertTrue(eep.isClosed());


    isContactClosed = false;
    isUpdate = eep.update(createRadioTelegramESP2(deviceID, isContactClosed, isLearn));

    Assert.assertTrue(isUpdate);
    Assert.assertFalse(eep.isClosed());
  }

  @Test public void testUpdateWithLearnTelegram() throws Exception
  {
    EepD50001 eep = (EepD50001)EepType.lookup("D5-00-01").createEep(
        deviceID, Constants.CONTACT_STATUS_COMMAND
    );


    // Regular update...

    boolean isLearn = false;
    boolean isContactClosed = true;
    Boolean isUpdate = eep.update(createRadioTelegramESP3(deviceID, isContactClosed, isLearn));

    Assert.assertTrue(isUpdate);
    Assert.assertTrue(eep.isClosed());


    // Update with learn telegram...

    isLearn = true;
    isContactClosed = false;
    isUpdate = eep.update(createRadioTelegramESP3(deviceID, isContactClosed, isLearn));

    Assert.assertFalse(isUpdate);
    Assert.assertTrue(eep.isClosed());


    // Regular update...

    isLearn = false;
    isContactClosed = false;
    isUpdate = eep.update(createRadioTelegramESP2(deviceID, isContactClosed, isLearn));

    Assert.assertTrue(isUpdate);
    Assert.assertFalse(eep.isClosed());
  }

  // Helpers --------------------------------------------------------------------------------------

  private Esp31BSTelegram createRadioTelegramESP3(DeviceID deviceID, boolean isContactClosed, boolean isLearn)
  {
    byte payload;

    payload = (byte)(isLearn ? 0x00 : 0x08);

    if(isContactClosed)
    {
      payload |= 0x01;
    }

    Esp31BSTelegram telegram = new Esp31BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }

  private Esp21BSTelegram createRadioTelegramESP2(DeviceID deviceID, boolean isContactClosed, boolean isLearn)
  {
    byte payload;

    payload = (byte)(isLearn ? 0x00 : 0x08);

    if(isContactClosed)
    {
      payload |= 0x01;
    }

    Esp21BSTelegram telegram = new Esp21BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }

}
