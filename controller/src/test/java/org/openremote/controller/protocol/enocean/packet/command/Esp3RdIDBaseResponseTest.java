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
package org.openremote.controller.protocol.enocean.packet.command;

import org.junit.Assert;
import org.junit.Test;
import org.openremote.controller.protocol.enocean.DeviceID;
import org.openremote.controller.protocol.enocean.EspException;
import org.openremote.controller.protocol.enocean.packet.Esp3ResponsePacket;

/**
 * Unit tests for {@link Esp3RdIDBaseResponse} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp3RdIDBaseResponseTest
{

  @Test public void testBasicConstruction() throws Exception
  {
    Esp3ResponsePacket.ReturnCode returnCode = Esp3ResponsePacket.ReturnCode.RET_OK;
    DeviceID deviceID = DeviceID.fromString("0xFF800001");

    byte[] dataBytes = createResponseDataBytes(returnCode.getValue(), deviceID);

    Esp3RdIDBaseResponse response = new Esp3RdIDBaseResponse(dataBytes);

    Assert.assertEquals(returnCode, response.getReturnCode());
    Assert.assertEquals(deviceID, response.getBaseID());
  }

  @Test public void testBaseID() throws Exception
  {
    Esp3ResponsePacket.ReturnCode returnCode = Esp3ResponsePacket.ReturnCode.RET_OK;

    DeviceID deviceID = DeviceID.fromString("0x12345678");

    byte[] dataBytes = createResponseDataBytes(returnCode.getValue(), deviceID);
    Esp3RdIDBaseResponse response = new Esp3RdIDBaseResponse(dataBytes);

    Assert.assertEquals(deviceID, response.getBaseID());


    deviceID = DeviceID.fromString("0xFFFFFFFF");

    dataBytes = createResponseDataBytes(returnCode.getValue(), deviceID);
    response = new Esp3RdIDBaseResponse(dataBytes);

    Assert.assertEquals(deviceID, response.getBaseID());


    deviceID = DeviceID.fromString("0x00000000");

    dataBytes = createResponseDataBytes(returnCode.getValue(), deviceID);
    response = new Esp3RdIDBaseResponse(dataBytes);

    Assert.assertEquals(deviceID, response.getBaseID());
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNullArg() throws Exception
  {
    byte[] data = null;
    Esp3RdIDBaseResponse response = new Esp3RdIDBaseResponse(data);
  }

  @Test (expected = EspException.class)
  public void testUnknownReturnCode() throws Exception
  {
    byte unknownReturnCode = (byte)0x81;

    DeviceID deviceID = DeviceID.fromString("0xFF800001");

    byte[] dataBytes = createResponseDataBytes(unknownReturnCode, deviceID);

    Esp3RdIDBaseResponse response = new Esp3RdIDBaseResponse(dataBytes);
  }

  @Test (expected = EspException.class)
  public void testInvalidDataLength1() throws Exception
  {
    Esp3ResponsePacket.ReturnCode returnCode = Esp3ResponsePacket.ReturnCode.RET_OK;
    DeviceID deviceID = DeviceID.fromString("0xFF800001");

    byte[] dataBytes = createResponseDataBytes(returnCode.getValue(), deviceID);
    byte[] dataBytesTooLong = new byte[dataBytes.length + 1];

    System.arraycopy(
        dataBytes, 0, dataBytesTooLong, 0, dataBytes.length
    );

    Esp3RdIDBaseResponse response = new Esp3RdIDBaseResponse(dataBytesTooLong);
  }

  @Test (expected = EspException.class)
  public void testInvalidDataLength2() throws Exception
  {
    Esp3ResponsePacket.ReturnCode returnCode = Esp3ResponsePacket.ReturnCode.RET_OK;
    DeviceID deviceID = DeviceID.fromString("0xFF800001");

    byte[] dataBytesTooShort = new byte[0];

    Esp3RdIDBaseResponse response = new Esp3RdIDBaseResponse(dataBytesTooShort);
  }


  // Helpers --------------------------------------------------------------------------------------

  private byte [] createResponseDataBytes(byte returnCode, DeviceID baseID)
  {
    byte[] dataBytes = new byte[DeviceID.ENOCEAN_ESP_ID_LENGTH + 1];

    dataBytes[Esp3ResponsePacket.ESP3_RESPONSE_RETURN_CODE_INDEX] = returnCode;

    byte[] baseIDBytes = baseID.asByteArray();

    System.arraycopy(
        baseIDBytes, 0, dataBytes,
        Esp3RdIDBaseResponse.ESP3_RESPONSE_RD_IDBASE_ID_INDEX,
        DeviceID.ENOCEAN_ESP_ID_LENGTH
    );

    return dataBytes;
  }
}
