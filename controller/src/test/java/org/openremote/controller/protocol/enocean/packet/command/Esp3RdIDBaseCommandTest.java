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

/**
 * Unit tests for {@link Esp3RdIDBaseCommand} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp3RdIDBaseCommandTest
{
  // Instance Fields ------------------------------------------------------------------------------

  /**
   * ESP3 read base ID command (see EnOcean Serial Protocol 3.0 V1.17 Specification
   * chapter 2.2.4: CO_RD_IDBASE).
   */
  private byte[] rdBaseIDCmdBytes;

  private Esp3RdIDBaseResponse response;

  private DeviceID baseID;

  private Esp3ResponsePacket.ReturnCode returnCode;

  // Test Lifecycle -------------------------------------------------------------------------------

  @Before public void setUp() throws Exception
  {
    rdBaseIDCmdBytes = new byte[]
    {
        (byte)0x55, (byte)0x00, (byte)0x01, (byte)0x00,
        (byte)0x05, (byte)0x70, (byte)0x08, (byte)0x38
    };


    baseID = DeviceID.fromString("0xFF800001");

    returnCode = Esp3ResponsePacket.ReturnCode.RET_OK;

    response = new Esp3RdIDBaseResponse(
        createResponseDataBytes(returnCode, baseID)
    );
  }


  // Tests ----------------------------------------------------------------------------------------

  @Test public void testBasicConstruction() throws Exception
  {
    Esp3RdIDBaseCommand command = new Esp3RdIDBaseCommand();

    byte[] packetBytes = command.asByteArray();

    Assert.assertArrayEquals(packetBytes, rdBaseIDCmdBytes);
  }

  @Test public void testSend() throws Exception
  {
    TestProcessor processor = new TestProcessor(response);

    Esp3RdIDBaseCommand command = new Esp3RdIDBaseCommand();

    command.send(processor);

    Assert.assertEquals(returnCode, command.getReturnCode());
    Assert.assertEquals(baseID, command.getBaseID());
  }

  @Test public void testRepeatedSend() throws Exception
  {
    TestProcessor processor = new TestProcessor(response);
    Esp3RdIDBaseCommand command = new Esp3RdIDBaseCommand();
    command.send(processor);


    processor = new TestProcessor(null);
    command.send(processor);

    Assert.assertEquals(Esp3ResponsePacket.ReturnCode.RET_CODE_NOT_SET, command.getReturnCode());
    Assert.assertNull(command.getBaseID());


    processor = new TestProcessor(response);
    command.send(processor);

    Assert.assertEquals(returnCode, command.getReturnCode());
    Assert.assertEquals(baseID, command.getBaseID());
  }

  @Test public void testReturnCode() throws Exception
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

    Esp3RdIDBaseCommand command = new Esp3RdIDBaseCommand();

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
    Esp3RdIDBaseCommand command = new Esp3RdIDBaseCommand();

    command.send(null);
  }


  // Helpers --------------------------------------------------------------------------------------

  private byte[] createResponseDataBytes(Esp3ResponsePacket.ReturnCode returnCode, DeviceID baseID)
  {
    byte[] dataBytes = new byte[DeviceID.ENOCEAN_ESP_ID_LENGTH + 1];

    dataBytes[Esp3ResponsePacket.ESP3_RESPONSE_RETURN_CODE_INDEX] = returnCode.getValue();

    byte[] baseIDBytes = baseID.asByteArray();

    System.arraycopy(
        baseIDBytes, 0, dataBytes,
        Esp3RdIDBaseResponse.ESP3_RESPONSE_RD_IDBASE_ID_INDEX,
        DeviceID.ENOCEAN_ESP_ID_LENGTH
    );

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
