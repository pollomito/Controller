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

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import org.openremote.controller.protocol.enocean.ConfigurationException;
import org.openremote.controller.protocol.enocean.ConnectionException;
import org.openremote.controller.protocol.enocean.DeviceID;
import org.openremote.controller.protocol.enocean.EspException;
import org.openremote.controller.protocol.enocean.packet.Esp2Packet;
import org.openremote.controller.protocol.enocean.packet.Esp2ResponsePacket;
import org.openremote.controller.protocol.enocean.packet.Esp3ResponsePacket;
import org.openremote.controller.protocol.enocean.packet.EspProcessor;

/**
 * Unit tests for {@link Esp2RdIDBaseCommand} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp2RdIDBaseCommandTest
{
  // Instance Fields ------------------------------------------------------------------------------

  private byte[] rdBaseIDCmdBytes;

  private Esp2RdIDBaseResponse response;

  private DeviceID baseID;

  private Esp2ResponsePacket.ReturnCode returnCode;


  // Test Lifecycle -------------------------------------------------------------------------------

  @Before public void setUp() throws Exception
  {
    rdBaseIDCmdBytes = new byte[]
    {
        (byte)0xA5, (byte)0x5A, (byte)0xAB, (byte)0x58,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00, (byte)0x00, (byte)0x00,
        (byte)0x00, (byte)0x00
    };

    int checksum = 0;

    for(int index = 2; index < (rdBaseIDCmdBytes.length - 1); index++)
    {
      checksum += rdBaseIDCmdBytes[index];
    }

    rdBaseIDCmdBytes[rdBaseIDCmdBytes.length - 1] = (byte)checksum;

    baseID = DeviceID.fromString("0xFF800001");

    returnCode = Esp2ResponsePacket.ReturnCode.INF_BASEID;

    response = new Esp2RdIDBaseResponse(
        createResponseDataBytes(returnCode, baseID)
    );
  }


  // Tests ----------------------------------------------------------------------------------------

  @Test public void testBasicConstruction() throws Exception
  {
    Esp2RdIDBaseCommand command = new Esp2RdIDBaseCommand();

    byte[] packetBytes = command.asByteArray();

    Assert.assertArrayEquals(packetBytes, rdBaseIDCmdBytes);
  }

  @Test public void testSend() throws Exception
  {
    TestProcessor processor = new TestProcessor(response);

    Esp2RdIDBaseCommand command = new Esp2RdIDBaseCommand();

    command.send(processor);

    Assert.assertEquals(returnCode, command.getReturnCode());
    Assert.assertEquals(baseID, command.getBaseID());
  }

  @Test public void testRepeatedSend() throws Exception
  {
    TestProcessor processor = new TestProcessor(response);
    Esp2RdIDBaseCommand command = new Esp2RdIDBaseCommand();
    command.send(processor);


    processor = new TestProcessor(null);
    command.send(processor);

    Assert.assertEquals(Esp2ResponsePacket.ReturnCode.RET_CODE_NOT_SET, command.getReturnCode());
    Assert.assertNull(command.getBaseID());


    processor = new TestProcessor(response);
    command.send(processor);

    Assert.assertEquals(returnCode, command.getReturnCode());
    Assert.assertEquals(baseID, command.getBaseID());
  }

  @Test (expected = IllegalArgumentException.class)
  public void testSendNullArg() throws Exception
  {
    Esp2RdIDBaseCommand command = new Esp2RdIDBaseCommand();

    command.send(null);
  }


  // Helpers --------------------------------------------------------------------------------------

  private byte [] createResponseDataBytes(Esp2ResponsePacket.ReturnCode returnCode, DeviceID baseID)
  {
    byte[] dataBytes = new byte[Esp2Packet.ESP2_PACKET_DATA_LENGTH];

    dataBytes[Esp2ResponsePacket.ESP2_RCT_RETURN_CODE_INDEX] = returnCode.getValue();

    byte[] baseIDBytes = baseID.asByteArray();

    System.arraycopy(
        baseIDBytes, 0, dataBytes,
        Esp2RdIDBaseResponse.ESP2_RESPONSE_RD_IDBASE_ID_INDEX,
        DeviceID.ENOCEAN_ESP_ID_LENGTH
    );

    return dataBytes;
  }


  // Inner Classes --------------------------------------------------------------------------------

  private static class TestProcessor implements EspProcessor<Esp2Packet>
  {
    private Esp2Packet responsePacket;

    public TestProcessor(Esp2Packet responsePacket)
    {
      this.responsePacket = responsePacket;
    }

    @Override public void start() throws ConfigurationException, ConnectionException
    {

    }

    @Override public void stop() throws ConnectionException
    {

    }

    @Override public Esp2Packet sendRequest(Esp2Packet packet) throws ConnectionException, InterruptedException
    {
      return responsePacket;
    }

    @Override public void sendResponse(Esp2Packet packet) throws ConnectionException
    {

    }
  }
}
