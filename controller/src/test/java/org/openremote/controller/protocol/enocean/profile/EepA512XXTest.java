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
 * Unit test base class for automated meter reading (AMR) EnOcean equipment profile (EEP)
 * implementations ('A5-12-00', 'A5-12-01', 'A5-12-02' and 'A5-12-03').
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA512XXTest
{

  // Protected Instance Fields ----------------------------------------------------------------------

  protected DeviceID deviceID;


  // Test Lifecycle -------------------------------------------------------------------------------

  @Before public void setUp() throws Exception
  {
    deviceID = DeviceID.fromString("0xFF800001");
  }


  // Helpers --------------------------------------------------------------------------------------

  protected Esp34BSTelegram createRadioTelegramESP3(DeviceID deviceID, int rawMeterValue, int tariffInfo,
                                                    boolean isTeachin, boolean isCurrentValue, int rawDivisorValue)
  {

    byte[] payload = getPayload(
        rawMeterValue, tariffInfo, isTeachin, isCurrentValue, rawDivisorValue
    );

    Esp34BSTelegram telegram = new Esp34BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }

  protected Esp24BSTelegram createRadioTelegramESP2(DeviceID deviceID, int rawMeterValue, int tariffInfo,
                                                    boolean isTeachin, boolean isCurrentValue, int rawDivisorValue)
  {
    byte[] payload = getPayload(
        rawMeterValue, tariffInfo, isTeachin, isCurrentValue, rawDivisorValue
    );

    Esp24BSTelegram telegram = new Esp24BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;
  }

  private byte[] getPayload(int rawMeterValue, int tariffInfo, boolean isTeachin,
                            boolean isCurrentValue, int rawDivisorValue)
  {
    byte[] payload = new byte[4];
    payload[0] = (byte)((rawMeterValue & 0xFF0000) >> 16);
    payload[1] = (byte)((rawMeterValue & 0xFF00) >> 8);
    payload[2] = (byte)rawMeterValue;
    payload[3] |= (byte)(tariffInfo << 4);
    payload[3] |= (byte)(isTeachin ? 0x00 : 0x08);
    payload[3] |= (byte)(isCurrentValue ? 0x04 : 0x00);
    payload[3] |= (rawDivisorValue & 0x03);

    return payload;
  }

}
