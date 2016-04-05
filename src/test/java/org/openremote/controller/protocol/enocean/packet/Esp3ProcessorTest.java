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
package org.openremote.controller.protocol.enocean.packet;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openremote.controller.protocol.enocean.DeviceID;
import org.openremote.controller.protocol.enocean.EspException;
import org.openremote.controller.protocol.enocean.packet.command.Esp3RdIDBaseCommand;
import org.openremote.controller.protocol.enocean.packet.command.Esp3RdIDBaseResponse;
import org.openremote.controller.protocol.enocean.packet.radio.*;
import org.openremote.controller.protocol.enocean.port.Esp3ComPortAdapter;
import org.openremote.controller.protocol.enocean.port.EspPortConfiguration;
import org.openremote.controller.protocol.enocean.port.MockPort;
import org.openremote.controller.protocol.enocean.port.RXTXPort;

import java.util.*;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link Esp3Processor} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp3ProcessorTest
{

  // Class Members --------------------------------------------------------------------------------

  private static final String COM_PORT = "/dev/cu.usbserial-FTUOKF2Q";


  // Instance Fields ------------------------------------------------------------------------------

  private EspPortConfiguration portConfig;
  private MockPort mockPort;
  private Esp3ComPortAdapter portAdapter;
  private Esp3Processor processor;

  private DeviceID senderID;
  private DeviceID baseID;

  private Esp3RdIDBaseCommand rdIDBaseCommand;
  private Esp3RdIDBaseResponse rdIDBaseResponse;

  private Esp3RPSTelegram radioTelegram;
  private Esp3ResponsePacket radioTelegramRespose;


  // Test Lifecycle -------------------------------------------------------------------------------

  @Before public void setUp() throws Exception
  {
    portConfig = new EspPortConfiguration();
    portConfig.setComPort(COM_PORT);
    portConfig.setSerialProtocol(EspPortConfiguration.SerialProtocol.ESP3);
    portConfig.setCommLayer(EspPortConfiguration.CommLayer.RXTX);

    mockPort = new MockPort();

    portAdapter = new Esp3ComPortAdapter(mockPort, portConfig);
    processor = new Esp3Processor(portAdapter);

    baseID = DeviceID.fromString("0xFF800000");
    senderID = DeviceID.fromString("0xFF800001");

    rdIDBaseCommand = new Esp3RdIDBaseCommand();
    rdIDBaseResponse = createRdIDBaseResponse(Esp3ResponsePacket.ReturnCode.RET_OK);

    radioTelegram = new Esp3RPSTelegram(senderID, (byte)0x00, (byte)0x00);
    radioTelegramRespose = createRadioTelegramResponse(Esp3ResponsePacket.ReturnCode.RET_OK);
  }

  @After public void tearDown() throws Exception
  {
    processor.stop();
  }


  // Tests ----------------------------------------------------------------------------------------

  @Test public void testSendCommand() throws Exception
  {
    mockPort.setRequestResponseMode();
    mockPort.addDataToReturn(rdIDBaseResponse.asByteArray());

    processor.start();

    rdIDBaseCommand.send(processor);

    Assert.assertEquals(Esp3ResponsePacket.ReturnCode.RET_OK, rdIDBaseCommand.getReturnCode());
    Assert.assertEquals(baseID, rdIDBaseCommand.getBaseID());
  }

  @Test public void testSendRadioTelegram() throws Exception
  {
    mockPort.setRequestResponseMode();
    mockPort.addDataToReturn(radioTelegramRespose.asByteArray());

    processor.start();

    radioTelegram.send(processor);

    Assert.assertEquals(Esp3ResponsePacket.ReturnCode.RET_OK, radioTelegram.getReturnCode());
  }

  @Test public void testRepeatedSend() throws Exception
  {
    mockPort.setRequestResponseMode();

    int repeatCount = 10;

    for(int i = 0; i < repeatCount; i++)
    {
      mockPort.addDataToReturn(rdIDBaseResponse.asByteArray());
    }

    processor.start();

    for(int i = 0; i < repeatCount; i++)
    {
      Esp3RdIDBaseCommand command = new Esp3RdIDBaseCommand();

      command.send(processor);

      Assert.assertEquals(Esp3ResponsePacket.ReturnCode.RET_OK, command.getReturnCode());
      Assert.assertEquals(baseID, command.getBaseID());
    }
  }

  @Test public void testResponseTimeout() throws Exception
  {
    mockPort.setRequestResponseMode();
    processor.start();

    try
    {
      radioTelegram.send(processor);

      Assert.fail();
    }
    catch (EspException e)
    {
      // expected
    }
  }

  @Test public void testHeaderCRC8() throws Exception
  {
    byte[] invalidResponse = rdIDBaseResponse.asByteArray();
    invalidResponse[Esp3PacketHeader.ESP3_HEADER_CRC8_INDEX] =
        (byte)(invalidResponse[Esp3PacketHeader.ESP3_HEADER_CRC8_INDEX] + 1);

    mockPort.setRequestResponseMode();
    mockPort.addDataToReturn(invalidResponse);

    processor.start();

    try
    {
      rdIDBaseCommand.send(processor);

      Assert.fail();
    }
    catch (EspException e)
    {
      // expected
    }

    List<byte[]> returnData = new ArrayList<byte[]>();
    returnData.add(invalidResponse);
    returnData.add(rdIDBaseResponse.asByteArray());

    mockPort.addDataToReturn(returnData);

    rdIDBaseCommand.send(processor);

    Assert.assertEquals(Esp3ResponsePacket.ReturnCode.RET_OK, rdIDBaseCommand.getReturnCode());
    Assert.assertEquals(baseID, rdIDBaseCommand.getBaseID());
  }

  @Test public void testDataCRC8() throws Exception
  {
    byte[] invalidResponse = rdIDBaseResponse.asByteArray();
    // Invalidate data CRC8
    invalidResponse[invalidResponse.length - 1] =
        (byte)(invalidResponse[invalidResponse.length - 1] + 1);

    mockPort.setRequestResponseMode();
    mockPort.addDataToReturn(invalidResponse);

    processor.start();

    try
    {
      rdIDBaseCommand.send(processor);

      Assert.fail();
    }
    catch (EspException e)
    {
      // expected
    }

    List<byte[]> returnData = new ArrayList<byte[]>();
    returnData.add(invalidResponse);
    returnData.add(rdIDBaseResponse.asByteArray());

    mockPort.addDataToReturn(returnData);

    rdIDBaseCommand.send(processor);

    Assert.assertEquals(Esp3ResponsePacket.ReturnCode.RET_OK, rdIDBaseCommand.getReturnCode());
    Assert.assertEquals(baseID, rdIDBaseCommand.getBaseID());
  }

  @Test public void testFragmentedResponse1() throws Exception
  {
    List<Integer> boundaries = new ArrayList<Integer>();

    boundaries.add(Esp3PacketHeader.ESP3_HEADER_SYNC_BYTE_INDEX);
    boundaries.add(Esp3PacketHeader.ESP3_HEADER_SIZE / 2);
    boundaries.add(Esp3PacketHeader.ESP3_HEADER_CRC8_INDEX);
    boundaries.add(
        Esp3PacketHeader.ESP3_HEADER_CRC8_INDEX +
            (Esp3RdIDBaseResponse.ESP3_RESPONSE_RD_IDBASE_DATA_LENGTH / 2)
    );

    List<byte[]> fragments = fragmentPacket(rdIDBaseResponse, boundaries);

    mockPort.setRequestResponseMode();
    mockPort.addDataToReturn(fragments);

    processor.start();

    rdIDBaseCommand.send(processor);

    Assert.assertEquals(Esp3ResponsePacket.ReturnCode.RET_OK, rdIDBaseCommand.getReturnCode());
    Assert.assertEquals(baseID, rdIDBaseCommand.getBaseID());
  }

  @Test public void testFragmentedResponse2() throws Exception
  {
    int numOfTelegrams = 30000;
    Deque<Byte> buffer = new LinkedList<Byte>();
    Random rand = new Random(47);
    int payload = 0;


    for(int i = 0; i < numOfTelegrams; i++)
    {
      Esp3RPSTelegram telegram = new Esp3RPSTelegram(senderID, (byte)payload, (byte)0x00);
      ++payload;

      byte[] packetBytes = telegram.asByteArray();

      for(byte dataByte : packetBytes)
      {
        buffer.add(dataByte);
      }
    }

    while(buffer.size() > 0)
    {
      int size = Math.min(rand.nextInt(radioTelegram.asByteArray().length / 2), buffer.size());

      byte[] data = new byte[size];

      for(int i = 0; i < size; i++)
      {
        data[i] = buffer.removeFirst();
      }

      mockPort.addDataToReturn(data);
    }


    RadioListener listener = new RadioListener(120, numOfTelegrams);

    processor.setProcessorListener(listener);
    processor.start();

    List<EspRadioTelegram> receivedTelegrams = listener.waitForRadioTelegrams();


    Assert.assertNotNull(receivedTelegrams);
    Assert.assertEquals(numOfTelegrams, receivedTelegrams.size());

    int expectedPayload = 0;

    for(EspRadioTelegram telegram : receivedTelegrams)
    {
      byte receivedPayload = telegram.getPayload()[0];

      Assert.assertTrue((byte)expectedPayload == receivedPayload);
      ++expectedPayload;
    }
  }

  @Test public void testReceiveRadioTelegrams() throws Exception
  {
    List<EspRadioTelegram> expectedTelegrams = new ArrayList<EspRadioTelegram>();

    expectedTelegrams.add(new Esp3RPSTelegram(senderID, (byte)0x11, (byte)0x22));
    expectedTelegrams.add(new Esp31BSTelegram(senderID, (byte)0x33, (byte)0x44));
    expectedTelegrams.add(new Esp34BSTelegram(
        senderID, new byte[] {(byte)0xFF, 0x01, 0x02, 0x03}, (byte)0x55)
    );

    for(EspRadioTelegram telegram : expectedTelegrams)
    {
      mockPort.addDataToReturn(((EspPacket) telegram).asByteArray());
    }

    RadioListener listener = new RadioListener(2, expectedTelegrams.size());

    processor.setProcessorListener(listener);
    processor.start();

    List<EspRadioTelegram> receivedTelegrams = listener.waitForRadioTelegrams();

    Assert.assertNotNull(receivedTelegrams);
    Assert.assertEquals(expectedTelegrams.size(), receivedTelegrams.size());

    for(int index = 0; index < expectedTelegrams.size(); index++)
    {
      EspRadioTelegram expTele = expectedTelegrams.get(index);
      EspRadioTelegram rcvTele = receivedTelegrams.get(index);

      Assert.assertEquals(expTele.getRORG(), rcvTele.getRORG());
      Assert.assertEquals(expTele.getSenderID(), rcvTele.getSenderID());
      Assert.assertArrayEquals(expTele.getPayload(), rcvTele.getPayload());
      Assert.assertEquals(expTele.getStatusByte(), rcvTele.getStatusByte());
    }

    Assert.assertTrue(receivedTelegrams.get(0) instanceof Esp3RPSTelegram);
    Assert.assertTrue(receivedTelegrams.get(1) instanceof Esp31BSTelegram);
    Assert.assertTrue(receivedTelegrams.get(2) instanceof Esp34BSTelegram);
  }

  @Test public void testReceiveAfterRestart() throws Exception
  {
    List<EspRadioTelegram> expectedTelegrams = new ArrayList<EspRadioTelegram>();

    expectedTelegrams.add(new Esp3RPSTelegram(senderID, (byte)0x11, (byte)0x22));
    expectedTelegrams.add(new Esp31BSTelegram(senderID, (byte)0x33, (byte)0x44));
    expectedTelegrams.add(new Esp34BSTelegram(
        senderID, new byte[] {(byte)0xFF, 0x01, 0x02, 0x03}, (byte)0x55)
    );

    for(EspRadioTelegram telegram : expectedTelegrams)
    {
      mockPort.addDataToReturn(((EspPacket) telegram).asByteArray());
    }

    RadioListener listener = new RadioListener(2, expectedTelegrams.size());

    processor.setProcessorListener(listener);
    processor.start();

    List<EspRadioTelegram> receivedTelegrams = listener.waitForRadioTelegrams();

    processor.stop();

    Assert.assertNotNull(receivedTelegrams);
    Assert.assertEquals(expectedTelegrams.size(), receivedTelegrams.size());



    for(EspRadioTelegram telegram : expectedTelegrams)
    {
      mockPort.addDataToReturn(((EspPacket) telegram).asByteArray());
    }

    listener = new RadioListener(2, expectedTelegrams.size());

    processor.setProcessorListener(listener);
    processor.start();

    receivedTelegrams = listener.waitForRadioTelegrams();

    processor.stop();

    Assert.assertNotNull(receivedTelegrams);
    Assert.assertEquals(expectedTelegrams.size(), receivedTelegrams.size());
  }

  /*
  @Test public void testSerialPort() throws Exception
  {
    RadioListener listener = new RadioListener(20, 2);

    Esp3ComPortAdapter comPortPortAdapter = new Esp3ComPortAdapter(new RXTXPort(), portConfig);
    processor = new Esp3Processor(comPortPortAdapter);

    processor.setProcessorListener(listener);
    processor.start();

    List<EspRadioTelegram> receivedTelegrams = listener.waitForRadioTelegrams();

    processor.stop();



    listener = new RadioListener(20, 2);

    processor.setProcessorListener(listener);
    processor.start();

    receivedTelegrams = listener.waitForRadioTelegrams();

    processor.stop();

  }
  */

  // Helpers --------------------------------------------------------------------------------------

  private Esp3RdIDBaseResponse createRdIDBaseResponse(Esp3ResponsePacket.ReturnCode returnCode)
      throws EspException
  {
    byte[] dataBytes = new byte[Esp3RdIDBaseResponse.ESP3_RESPONSE_RD_IDBASE_DATA_LENGTH];

    dataBytes[Esp3ResponsePacket.ESP3_RESPONSE_RETURN_CODE_INDEX] = returnCode.getValue();

    System.arraycopy(
        baseID.asByteArray(), 0, dataBytes,
        Esp3ResponsePacket.ESP3_RESPONSE_RETURN_CODE_INDEX + 1,
        DeviceID.ENOCEAN_ESP_ID_LENGTH
    );

    return new Esp3RdIDBaseResponse(dataBytes);
  }

  private Esp3ResponsePacket createRadioTelegramResponse(Esp3ResponsePacket.ReturnCode returnCode)
      throws EspException
  {
    return new Esp3ResponsePacket(
        new byte[] {returnCode.getValue()},
        Esp3RadioTelegramOptData.ESP3_RADIO_OPT_DATA_TX.asByteArray()
    );
  }

  private List<byte[]> fragmentPacket(Esp3Packet packet, List<Integer> boundaries)
  {

    List<byte[]> fragments = new LinkedList<byte[]>();
    byte[] packetBytes = packet.asByteArray();

    int startIndex = 0;
    int stopIndex = 0;

    for(int index = 0; index < (boundaries.size() + 1); index++)
    {
      if(index == 0)
      {
        startIndex = 0;
      }
      else
      {
        startIndex = stopIndex + 1;
      }

      if(index < boundaries.size())
      {
        stopIndex = boundaries.get(index);
      }
      else
      {
        stopIndex = packetBytes.length - 1;
      }

      int length = stopIndex - startIndex + 1;
      byte[] fragmentBytes = new byte[length];
      System.arraycopy(
          packetBytes, startIndex, fragmentBytes, 0, length
      );

      fragments.add(fragmentBytes);
    }

    return fragments;
  }


  // Inner Classes --------------------------------------------------------------------------------

  private static class RadioListener implements Esp3ProcessorListener
  {

    // Private Instance Fields --------------------------------------------------------------------

    private List<EspRadioTelegram> radioTelegrams;
    private SynchronousQueue<List<EspRadioTelegram>> queue;
    private int numOfExpectedTelegrams;
    private int timeout;

    // Constructors -------------------------------------------------------------------------------

    public RadioListener(int timeout, int numOfExpectedTelegrams)
    {
      this.timeout = timeout;
      this.numOfExpectedTelegrams = numOfExpectedTelegrams;

      radioTelegrams = new ArrayList<EspRadioTelegram>();
      queue = new SynchronousQueue<List<EspRadioTelegram>>();
    }

    // Implements Esp3ProcessorListener -----------------------------------------------------------

    @Override public void radioTelegramReceived(EspRadioTelegram telegram)
    {
      radioTelegrams.add(telegram);

      if(radioTelegrams.size() == numOfExpectedTelegrams)
      {
        try
        {
          queue.offer(radioTelegrams, 1, TimeUnit.SECONDS);
        }
        catch (InterruptedException e)
        {
          Thread.currentThread().interrupt();
        }
      }
    }

    // Public Instance Methods --------------------------------------------------------------------

    public List<EspRadioTelegram> waitForRadioTelegrams() throws InterruptedException
    {
      return queue.poll(timeout, TimeUnit.SECONDS);
    }
  };
}
