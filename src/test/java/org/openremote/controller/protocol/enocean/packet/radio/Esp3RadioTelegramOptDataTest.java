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
package org.openremote.controller.protocol.enocean.packet.radio;

import org.junit.Assert;
import org.junit.Test;
import org.openremote.controller.protocol.enocean.DeviceID;

/**
 * Unit tests for {@link Esp3RadioTelegramOptData} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp3RadioTelegramOptDataTest
{

  // Tests ----------------------------------------------------------------------------------------

  @Test public void testBasicConstruction() throws Exception
  {
    int subTelNum = 3;
    DeviceID deviceID = DeviceID.fromString("0xFF800001");
    int dBm = 50;
    int securityLevel = 1;

    assertOptDataParams(subTelNum, deviceID, dBm, securityLevel);

    subTelNum = 0xFF;
    deviceID = DeviceID.fromString("0xFFFFFFFF");
    dBm = 0xFF;
    securityLevel = 0xFF;

    assertOptDataParams(subTelNum, deviceID, dBm, securityLevel);

    subTelNum = 0;
    deviceID = DeviceID.fromString("0x00000000");
    dBm = 0;
    securityLevel = 0;

    assertOptDataParams(subTelNum, deviceID, dBm, securityLevel);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNullArg() throws Exception
  {
    Esp3RadioTelegramOptData optData = new Esp3RadioTelegramOptData(null);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testInvalidOptDataLength() throws Exception
  {
    int subTelNum = 3;
    DeviceID deviceID = DeviceID.fromString("0xFF800001");
    int dBm = 50;
    int securityLevel = 1;

    byte[] optDataBytes = createOptDataAsByteArray(
        subTelNum, deviceID, dBm, securityLevel
    );

    byte[] optDataBytesTooLong = new byte[optDataBytes.length + 1];

    Esp3RadioTelegramOptData optData = new Esp3RadioTelegramOptData(
        optDataBytesTooLong
    );
  }

// Helpers --------------------------------------------------------------------------------------

  private void assertOptDataParams(int subTelNum, DeviceID deviceID, int dBm, int secLevel)
  {
    byte[] optDataBytes = createOptDataAsByteArray(subTelNum, deviceID, dBm, secLevel);

    Esp3RadioTelegramOptData optData = new Esp3RadioTelegramOptData(optDataBytes);

    Assert.assertEquals(subTelNum, optData.getSubTelNum());
    Assert.assertEquals(deviceID, optData.getDestinationID());
    Assert.assertEquals(dBm, optData.getdBm());
    Assert.assertEquals(secLevel, optData.getSecurityLevel());
    Assert.assertArrayEquals(optDataBytes, optData.asByteArray());
  }

  private byte[] createOptDataAsByteArray(int subTelNum, DeviceID deviceID, int dBm, int secLevel)
  {
    byte[] optDataBytes = new byte[7];

    optDataBytes[0] = (byte)subTelNum;
    optDataBytes[5] = (byte)dBm;
    optDataBytes[6] = (byte)secLevel;

    System.arraycopy(
        deviceID.asByteArray(), 0, optDataBytes,
        1, DeviceID.ENOCEAN_ESP_ID_LENGTH
    );

    return optDataBytes;
  }
}
