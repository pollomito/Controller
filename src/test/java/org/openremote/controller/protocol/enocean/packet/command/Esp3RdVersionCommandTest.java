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
import org.openremote.controller.protocol.enocean.ConfigurationException;
import org.openremote.controller.protocol.enocean.ConnectionException;
import org.openremote.controller.protocol.enocean.DeviceID;
import org.openremote.controller.protocol.enocean.EspException;
import org.openremote.controller.protocol.enocean.packet.Esp3Packet;
import org.openremote.controller.protocol.enocean.packet.Esp3ResponsePacket;
import org.openremote.controller.protocol.enocean.packet.EspProcessor;

import java.io.UnsupportedEncodingException;

/**
 * Unit tests for {@link Esp3RdVersionCommand} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp3RdVersionCommandTest
{

  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * ESP3 read version command (see EnOcean Serial Protocol 3.0 V1.17 Specification
   * chapter 1.11.5: CO_RD_VERSION).
   */
  private byte[] rdVersionCmdBytes;

  private Esp3RdVersionResponse response;

  private DeviceID chipID;

  private Esp3ResponsePacket.ReturnCode returnCode;

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

  @Before public void setUp() throws Exception
  {

    rdVersionCmdBytes = new byte[]
    {
        (byte)0x55, (byte)0x00, (byte)0x01, (byte)0x00,
        (byte)0x05, (byte)0x70, (byte)0x03, (byte)0x09
    };

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

    returnCode = Esp3ResponsePacket.ReturnCode.RET_OK;

    response = new Esp3RdVersionResponse(
        createResponseDataBytes()
    );
  }


  // Tests ----------------------------------------------------------------------------------------

  @Test public void testBasicConstruction() throws Exception
  {
    Esp3RdVersionCommand command = new Esp3RdVersionCommand();

    byte[] packetBytes = command.asByteArray();

    Assert.assertArrayEquals(packetBytes, rdVersionCmdBytes);
  }

  @Test public void testSend() throws Exception
  {
    TestProcessor processor = new TestProcessor(response);

    Esp3RdVersionCommand command = new Esp3RdVersionCommand();

    command.send(processor);

    Assert.assertEquals(returnCode, command.getReturnCode());
    Assert.assertEquals(chipID, command.getChipID());
    Assert.assertEquals(appDescription, command.getAppDescription());
    Assert.assertEquals(appVersionBeta, command.getAppVersion().getBetaVersion());
    Assert.assertEquals(appVersionMain, command.getAppVersion().getMainVersion());
    Assert.assertEquals(appVersionAlpha, command.getAppVersion().getAlphaVersion());
    Assert.assertEquals(appVersionBuild, command.getAppVersion().getBuild());
    Assert.assertEquals(apiVersionBeta, command.getApiVersion().getBetaVersion());
    Assert.assertEquals(apiVersionMain, command.getApiVersion().getMainVersion());
    Assert.assertEquals(apiVersionAlpha, command.getApiVersion().getAlphaVersion());
    Assert.assertEquals(apiVersionBuild, command.getApiVersion().getBuild());
  }

  @Test public void testRepeatedSend() throws Exception
  {
    TestProcessor processor = new TestProcessor(response);
    Esp3RdVersionCommand command = new Esp3RdVersionCommand();
    command.send(processor);


    processor = new TestProcessor(null);
    command.send(processor);

    Assert.assertEquals(Esp3ResponsePacket.ReturnCode.RET_CODE_NOT_SET, command.getReturnCode());
    Assert.assertNull(command.getChipID());


    processor = new TestProcessor(response);
    command.send(processor);

    Assert.assertEquals(returnCode, command.getReturnCode());
    Assert.assertEquals(chipID, command.getChipID());
  }

  @Test public void testReturnCode1() throws Exception
  {
    Esp3RdVersionCommand command = new Esp3RdVersionCommand();

    Assert.assertEquals(Esp3ResponsePacket.ReturnCode.RET_CODE_NOT_SET, command.getReturnCode());


    returnCode = Esp3ResponsePacket.ReturnCode.RET_OK;

    Esp3RdVersionResponse resp = new Esp3RdVersionResponse(createResponseDataBytes());
    TestProcessor processor = new TestProcessor(resp);

    command = new Esp3RdVersionCommand();
    command.send(processor);

    Assert.assertEquals(returnCode, command.getReturnCode());
  }

  @Test public void testReturnCode2() throws Exception
  {
    Esp3ResponsePacket.ReturnCode[] returnCodes = new Esp3ResponsePacket.ReturnCode[]
    {
        Esp3ResponsePacket.ReturnCode.RET_ERROR,
        Esp3ResponsePacket.ReturnCode.RET_NOT_SUPPORTED,
        Esp3ResponsePacket.ReturnCode.RET_WRONG_PARAM,
        Esp3ResponsePacket.ReturnCode.RET_OPERATION_DENIED
    };

    EspException.ErrorCode[] errorCodes = new EspException.ErrorCode[]
    {
        EspException.ErrorCode.RESP_ERROR,
        EspException.ErrorCode.RESP_NOT_SUPPORTED,
        EspException.ErrorCode.RESP_WRONG_PARAM,
        EspException.ErrorCode.RESP_OPERATION_DENIED
    };

    Esp3RdVersionCommand command = new Esp3RdVersionCommand();

    for(int index = 0; index < returnCodes.length; index++)
    {
      Esp3ResponsePacket response = new Esp3ResponsePacket(
          new byte[] {returnCodes[index].getValue()}, null
      );

      TestProcessor processor = new TestProcessor(response);

      try
      {
        command.send(processor);

        Assert.fail();
      }
      catch (EspException e)
      {
        Assert.assertEquals(errorCodes[index], e.getErrorCode());
        Assert.assertEquals(returnCodes[index], command.getReturnCode());
      }
      catch (Throwable e)
      {
        Assert.fail();
      }
    }
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSendNullArg() throws Exception
  {
    Esp3RdVersionCommand command = new Esp3RdVersionCommand();

    command.send(null);
  }


// Helpers --------------------------------------------------------------------------------------

  private byte[] createResponseDataBytes() throws UnsupportedEncodingException
  {
    byte[] dataBytes = new byte[Esp3RdVersionResponse.ESP3_RESPONSE_RD_VERSION_DATA_LENGTH];

    dataBytes[Esp3ResponsePacket.ESP3_RESPONSE_RETURN_CODE_INDEX] = returnCode.getValue();

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

    dataBytes[nullIndex] = '\0';

    return dataBytes;
  }

  // Inner Classes --------------------------------------------------------------------------------

  private static class TestProcessor implements EspProcessor<Esp3Packet>
  {
    private Esp3Packet responsePacket;

    public TestProcessor(Esp3Packet responsePacket)
    {
      this.responsePacket = responsePacket;
    }

    @Override public void start() throws ConfigurationException, ConnectionException
    {

    }

    @Override public void stop() throws ConnectionException
    {

    }

    @Override public Esp3Packet sendRequest(Esp3Packet packet) throws ConnectionException, InterruptedException
    {
      return responsePacket;
    }

    @Override public void sendResponse(Esp3Packet packet) throws ConnectionException
    {

    }
  }
}
