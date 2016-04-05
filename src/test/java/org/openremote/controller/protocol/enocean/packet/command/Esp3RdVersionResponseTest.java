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
import org.junit.Before;
import org.junit.Test;
import org.openremote.controller.protocol.enocean.DeviceID;
import org.openremote.controller.protocol.enocean.EspException;
import org.openremote.controller.protocol.enocean.packet.Esp3ResponsePacket;

import java.io.UnsupportedEncodingException;

/**
 * Unit tests for {@link Esp3RdVersionResponse} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp3RdVersionResponseTest
{

  // Private Instance Fields ----------------------------------------------------------------------

  private DeviceID chipID;

  private byte returnCode;

  private byte appVersionMain;
  private byte appVersionBeta;
  private byte appVersionAlpha;
  private byte appVersionBuild;

  private byte apiVersionMain;
  private byte apiVersionBeta;
  private byte apiVersionAlpha;
  private byte apiVersionBuild;

  private String appDescription;


  // Test Lifecycle -------------------------------------------------------------------------------

  @Before
  public void setUp() throws Exception
  {
    appVersionMain = 0x01;
    appVersionBeta = 0x02;
    appVersionAlpha = 0x03;
    appVersionBuild = 0x04;

    apiVersionMain = 0x05;
    apiVersionBeta = 0x06;
    apiVersionAlpha = 0x07;
    apiVersionBuild = 0x08;

    chipID = DeviceID.fromString("0xFF800001");

    appDescription = "Test";

    returnCode = Esp3ResponsePacket.ReturnCode.RET_OK.getValue();
  }


  // Tests ----------------------------------------------------------------------------------------

  @Test public void testBasicConstruction() throws Exception
  {
    byte[] dataBytes = createResponseDataBytes();

    Esp3RdVersionResponse response = new Esp3RdVersionResponse(dataBytes);

    Assert.assertEquals(Esp3ResponsePacket.ReturnCode.resolve(returnCode), response.getReturnCode());
    Assert.assertEquals(chipID, response.getChipID());
    Assert.assertEquals(appDescription, response.getAppDescription());
    Assert.assertEquals(appVersionBeta, response.getAppVersion().getBetaVersion());
    Assert.assertEquals(appVersionMain, response.getAppVersion().getMainVersion());
    Assert.assertEquals(appVersionAlpha, response.getAppVersion().getAlphaVersion());
    Assert.assertEquals(appVersionBuild, response.getAppVersion().getBuild());
    Assert.assertEquals(apiVersionBeta, response.getApiVersion().getBetaVersion());
    Assert.assertEquals(apiVersionMain, response.getApiVersion().getMainVersion());
    Assert.assertEquals(apiVersionAlpha, response.getApiVersion().getAlphaVersion());
    Assert.assertEquals(apiVersionBuild, response.getApiVersion().getBuild());
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNullArg() throws Exception
  {
    Esp3RdVersionResponse response = new Esp3RdVersionResponse(null);
  }

  @Test (expected = EspException.class)
  public void testUnknownReturnCode() throws Exception
  {
    returnCode = (byte)0x81;

    byte[] dataBytes = createResponseDataBytes();

    Esp3RdVersionResponse response = new Esp3RdVersionResponse(dataBytes);
  }

  @Test (expected = EspException.class)
  public void testInvalidDataLength1() throws Exception
  {
    byte[] dataBytes = createResponseDataBytes();
    byte[] dataBytesTooLong = new byte[dataBytes.length + 1];

    System.arraycopy(
        dataBytes, 0, dataBytesTooLong, 0, dataBytes.length
    );

    Esp3RdVersionResponse response = new Esp3RdVersionResponse(dataBytesTooLong);
  }

  @Test (expected = EspException.class)
  public void testInvalidDataLength2() throws Exception
  {
    byte[] dataBytesTooShort = new byte[0];

    Esp3RdVersionResponse response = new Esp3RdVersionResponse(dataBytesTooShort);
  }

  @Test public void testAppVersion() throws Exception
  {
    appVersionMain = 0x01;
    appVersionBeta = 0x02;
    appVersionAlpha = 0x03;
    appVersionBuild = (byte)0xFF;

    byte[] dataBytes = createResponseDataBytes();

    Esp3RdVersionResponse response = new Esp3RdVersionResponse(dataBytes);

    Assert.assertEquals(appVersionMain, response.getAppVersion().getMainVersion());
    Assert.assertEquals(appVersionBeta, response.getAppVersion().getBetaVersion());
    Assert.assertEquals(appVersionAlpha, response.getAppVersion().getAlphaVersion());
    Assert.assertEquals(appVersionBuild, response.getAppVersion().getBuild());
  }

  @Test public void testApiVersion() throws Exception
  {
    apiVersionMain = (byte)0xFF;
    apiVersionBeta = 0x03;
    apiVersionAlpha = 0x02;
    apiVersionBuild = 0x01;

    byte[] dataBytes = createResponseDataBytes();

    Esp3RdVersionResponse response = new Esp3RdVersionResponse(dataBytes);

    Assert.assertEquals(apiVersionMain, response.getApiVersion().getMainVersion());
    Assert.assertEquals(apiVersionBeta, response.getApiVersion().getBetaVersion());
    Assert.assertEquals(apiVersionAlpha, response.getApiVersion().getAlphaVersion());
    Assert.assertEquals(apiVersionBuild, response.getApiVersion().getBuild());
  }

  @Test public void testAppDescription() throws Exception
  {
    appDescription = "123456";

    byte[] dataBytes = createResponseDataBytes();
    Esp3RdVersionResponse response = new Esp3RdVersionResponse(dataBytes);

    Assert.assertEquals(appDescription, response.getAppDescription());


    appDescription = "";

    dataBytes = createResponseDataBytes();
    response = new Esp3RdVersionResponse(dataBytes);

    Assert.assertEquals(appDescription, response.getAppDescription());


    StringBuffer buf = new StringBuffer();
    for(int i = 0; i < Esp3RdVersionResponse.ESP3_RESPONSE_RD_VERSION_APP_DESC_LENGTH; i++)
    {
      buf.append((char)('A' + i));
    }

    appDescription = new String(buf);

    dataBytes = createResponseDataBytes();
    response = new Esp3RdVersionResponse(dataBytes);

    Assert.assertEquals(appDescription, response.getAppDescription());
  }

  @Test public void testChipID() throws Exception
  {
    chipID = DeviceID.fromString("0x12345678");

    byte[] dataBytes = createResponseDataBytes();
    Esp3RdVersionResponse response = new Esp3RdVersionResponse(dataBytes);

    Assert.assertEquals(chipID, response.getChipID());


    chipID = DeviceID.fromString("0x00000000");

    dataBytes = createResponseDataBytes();
    response = new Esp3RdVersionResponse(dataBytes);

    Assert.assertEquals(chipID, response.getChipID());


    chipID = DeviceID.fromString("0xFFFFFFFF");

    dataBytes = createResponseDataBytes();
    response = new Esp3RdVersionResponse(dataBytes);

    Assert.assertEquals(chipID, response.getChipID());
  }

  @Test public void testReturnCode() throws Exception
  {
    returnCode = Esp3ResponsePacket.ReturnCode.RET_ERROR.getValue();

    byte[] dataBytes = createResponseDataBytes();
    Esp3RdVersionResponse response = new Esp3RdVersionResponse(dataBytes);

    Assert.assertEquals(Esp3ResponsePacket.ReturnCode.resolve(returnCode), response.getReturnCode());
  }

  // Helpers --------------------------------------------------------------------------------------

  private byte[] createResponseDataBytes() throws UnsupportedEncodingException
  {
    byte[] dataBytes = new byte[Esp3RdVersionResponse.ESP3_RESPONSE_RD_VERSION_DATA_LENGTH];

    dataBytes[Esp3ResponsePacket.ESP3_RESPONSE_RETURN_CODE_INDEX] = returnCode;

    dataBytes[Esp3RdVersionResponse.ESP3_RESPONSE_RD_VERSION_APP_VERSION_INDEX] = appVersionMain;
    dataBytes[Esp3RdVersionResponse.ESP3_RESPONSE_RD_VERSION_APP_VERSION_INDEX + 1] = appVersionBeta;
    dataBytes[Esp3RdVersionResponse.ESP3_RESPONSE_RD_VERSION_APP_VERSION_INDEX + 2] = appVersionAlpha;
    dataBytes[Esp3RdVersionResponse.ESP3_RESPONSE_RD_VERSION_APP_VERSION_INDEX + 3] = appVersionBuild;

    dataBytes[Esp3RdVersionResponse.ESP3_RESPONSE_RD_VERSION_API_VERSION_INDEX] = apiVersionMain;
    dataBytes[Esp3RdVersionResponse.ESP3_RESPONSE_RD_VERSION_API_VERSION_INDEX + 1] = apiVersionBeta;
    dataBytes[Esp3RdVersionResponse.ESP3_RESPONSE_RD_VERSION_API_VERSION_INDEX + 2] = apiVersionAlpha;
    dataBytes[Esp3RdVersionResponse.ESP3_RESPONSE_RD_VERSION_API_VERSION_INDEX + 3] = apiVersionBuild;

    byte[] chipIDBytes = chipID.asByteArray();

    for(int i = 0; i < DeviceID.ENOCEAN_ESP_ID_LENGTH; i++)
    {
      dataBytes[Esp3RdVersionResponse.ESP3_RESPONSE_RD_VERSION_CHIP_ID_INDEX + i] = chipIDBytes[i];
    }

    byte[] appDescBytes = appDescription.getBytes("US-ASCII");

    for(int i = 0; i < appDescBytes.length; i++)
    {
      dataBytes[Esp3RdVersionResponse.ESP3_RESPONSE_RD_VERSION_APP_DESC_INDEX + i] = appDescBytes[i];
    }

    int nullIndex = Esp3RdVersionResponse.ESP3_RESPONSE_RD_VERSION_APP_DESC_INDEX +
                    appDescBytes.length;

    if(nullIndex < dataBytes.length)
    {
      dataBytes[nullIndex] = '\0';
    }

    return dataBytes;
  }
}
