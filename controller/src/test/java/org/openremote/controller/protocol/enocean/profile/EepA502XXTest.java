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

import org.junit.Before;
import org.openremote.controller.protocol.enocean.DeviceID;
import org.openremote.controller.protocol.enocean.packet.radio.Esp24BSTelegram;
import org.openremote.controller.protocol.enocean.packet.radio.Esp34BSTelegram;

/**
 * Unit test base class for temperature sensor EnOcean equipment profile (EEP)
 * implementations ('A5-02-01', 'A5-02-02', 'A5-02-03'...).
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA502XXTest
{

  // Protected Instance Fields ----------------------------------------------------------------------

  protected DeviceID deviceID;


  // Test Lifecycle -------------------------------------------------------------------------------

  @Before public void setUp() throws Exception
  {
    deviceID = DeviceID.fromString("0xFF800001");
  }


  // Helpers --------------------------------------------------------------------------------------

  protected Esp34BSTelegram createRadioTelegramESP3(DeviceID deviceID, int rawTempValue)
  {
    byte[] payload = new byte[4];
    payload[1] = (byte)((rawTempValue & 0xFF00) >> 8);
    payload[2] = (byte)rawTempValue;
    payload[3] = 0x08; // Regular telegram

    Esp34BSTelegram telegram = new Esp34BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }

  protected Esp24BSTelegram createRadioTelegramESP2(DeviceID deviceID, int rawTempValue)
  {
    byte[] payload = new byte[4];
    payload[1] = (byte)((rawTempValue & 0xFF00) >> 8);
    payload[2] = (byte)rawTempValue;
    payload[3] = 0x08; // Regular telegram

    Esp24BSTelegram telegram = new Esp24BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }

  protected Esp34BSTelegram createTeachInTelegram(DeviceID deviceID, int rawTempValue)
  {
    byte[] payload = new byte[4];
    payload[1] = (byte)((rawTempValue & 0xFF00) >> 8);
    payload[2] = (byte)rawTempValue;
    payload[3] = 0x00; // Teach-in telegram

    Esp34BSTelegram telegram = new Esp34BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }
}
