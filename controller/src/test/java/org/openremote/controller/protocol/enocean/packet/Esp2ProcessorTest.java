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
import org.openremote.controller.protocol.enocean.packet.command.Esp2RdIDBaseCommand;
import org.openremote.controller.protocol.enocean.packet.command.Esp2RdIDBaseResponse;
import org.openremote.controller.protocol.enocean.packet.radio.*;
import org.openremote.controller.protocol.enocean.port.Esp2ComPortAdapter;
import org.openremote.controller.protocol.enocean.port.EspPortConfiguration;
import org.openremote.controller.protocol.enocean.port.MockPort;
import org.openremote.controller.protocol.enocean.port.RXTXPort;

import java.util.*;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

/**
 * Unit tests for {@link Esp2Processor} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp2ProcessorTest
{

  // Class Members --------------------------------------------------------------------------------

  private static final String COM_PORT = "/dev/cu.usbserial-EOU9ERRYB";


  // Instance Fields ------------------------------------------------------------------------------

  private EspPortConfiguration portConfig;
  private MockPort mockPort;
  private Esp2ComPortAdapter portAdapter;
  private Esp2Processor processor;

  private DeviceID senderID;
  private DeviceID baseID;

  private Esp2RdIDBaseCommand rdIDBaseCommand;
  private Esp2RdIDBaseResponse rdIDBaseResponse;

  private Esp2RPSTelegram radioTelegram;
  private Esp2ResponsePacket radioTelegramRespose;


  // Test Lifecycle -------------------------------------------------------------------------------

  @Before public void setUp() throws Exception
  {
    portConfig = new EspPortConfiguration();
    portConfig.setComPort(COM_PORT);
    portConfig.setSerialProtocol(EspPortConfiguration.SerialProtocol.ESP2);
    portConfig.setCommLayer(EspPortConfiguration.CommLayer.RXTX);

    mockPort = new MockPort();

    portAdapter = new Esp2ComPortAdapter(mockPort, portConfig);
    processor = new Esp2Processor(portAdapter);

    baseID = DeviceID.fromString("0xFF800000");
    senderID = DeviceID.fromString("0xFF800001");

    rdIDBaseCommand = new Esp2RdIDBaseCommand();
    rdIDBaseResponse = createRdIDBaseResponse(Esp2ResponsePacket.ReturnCode.INF_BASEID);

    radioTelegram = new Esp2RPSTelegram(senderID, (byte)0x01, (byte)0x02);
    radioTelegramRespose = createRadioTelegramResponse(Esp2ResponsePacket.ReturnCode.OK);
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

    Assert.assertEquals(Esp2ResponsePacket.ReturnCode.INF_BASEID, rdIDBaseCommand.getReturnCode());
    Assert.assertEquals(baseID, rdIDBaseCommand.getBaseID());
  }

  @Test public void testSendRadioTelegram() throws Exception
  {
    mockPort.setRequestResponseMode();
    mockPort.addDataToReturn(radioTelegramRespose.asByteArray());

    processor.start();

    radioTelegram.send(processor);

    Assert.assertEquals(Esp2ResponsePacket.ReturnCode.OK, radioTelegram.getReturnCode());
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
      Esp2RdIDBaseCommand command = new Esp2RdIDBaseCommand();

      command.send(processor);

      Assert.assertEquals(Esp2ResponsePacket.ReturnCode.INF_BASEID, command.getReturnCode());
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

  @Test public void testChecksum() throws Exception
  {
    byte[] invalidResponse = rdIDBaseResponse.asByteArray();
    // Invalidate checksum
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

    Assert.assertEquals(Esp2ResponsePacket.ReturnCode.INF_BASEID, rdIDBaseCommand.getReturnCode());
    Assert.assertEquals(baseID, rdIDBaseCommand.getBaseID());
  }

  @Test public void testFragmentedResponse1() throws Exception
  {
    List<Integer> boundaries = new ArrayList<Integer>();

    boundaries.add(Esp2PacketHeader.ESP2_HEADER_SYNC_BYTE_1_INDEX);
    boundaries.add(Esp2PacketHeader.ESP2_HEADER_SYNC_BYTE_2_INDEX);
    boundaries.add(Esp2PacketHeader.ESP2_HEADER_H_SEQ_LENGTH_INDEX);
    boundaries.add(
        Esp2PacketHeader.ESP2_HEADER_H_SEQ_LENGTH_INDEX +
        (Esp2RdIDBaseResponse.ESP2_PACKET_DATA_LENGTH / 2)
    );

    List<byte[]> fragments = fragmentPacket(rdIDBaseResponse, boundaries);

    mockPort.setRequestResponseMode();
    mockPort.addDataToReturn(fragments);

    processor.start();

    rdIDBaseCommand.send(processor);

    Assert.assertEquals(Esp2ResponsePacket.ReturnCode.INF_BASEID, rdIDBaseCommand.getReturnCode());
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
      Esp2RPSTelegram telegram = new Esp2RPSTelegram(senderID, (byte)payload, (byte)0x00);
      ++payload;

      byte[] packetBytes = setPacketType(
          Esp2PacketHeader.PacketType.RRT_UNKNOWN, telegram.asByteArray()
      );

      for(byte dataByte : packetBytes)
      {
        buffer.add(dataByte);
      }
    }

    while(buffer.size() > 0)
    {
      int size = Math.min(rand.nextInt(AbstractEsp2RadioTelegram.ESP2_RADIO_PACKET_LENGTH / 2), buffer.size());

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

    expectedTelegrams.add(new Esp2RPSTelegram(senderID, (byte)0x11, (byte)0x22));
    expectedTelegrams.add(new Esp21BSTelegram(senderID, (byte)0x33, (byte)0x44));
    expectedTelegrams.add(new Esp24BSTelegram(
        senderID, new byte[] {(byte)0xFF, 0x01, 0x02, 0x03}, (byte)0x55)
    );

    for(EspRadioTelegram telegram : expectedTelegrams)
    {
      byte[] telegramBytes = ((EspPacket) telegram).asByteArray();

      mockPort.addDataToReturn(
          setPacketType(Esp2PacketHeader.PacketType.RRT_UNKNOWN, telegramBytes)
      );
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

    Assert.assertTrue(receivedTelegrams.get(0) instanceof Esp2RPSTelegram);
    Assert.assertTrue(receivedTelegrams.get(1) instanceof Esp21BSTelegram);
    Assert.assertTrue(receivedTelegrams.get(2) instanceof Esp24BSTelegram);
  }

  @Test public void testReceiveAfterRestart() throws Exception
  {
    List<EspRadioTelegram> expectedTelegrams = new ArrayList<EspRadioTelegram>();

    expectedTelegrams.add(new Esp2RPSTelegram(senderID, (byte)0x11, (byte)0x22));
    expectedTelegrams.add(new Esp21BSTelegram(senderID, (byte)0x33, (byte)0x44));
    expectedTelegrams.add(new Esp24BSTelegram(
        senderID, new byte[] {(byte)0xFF, 0x01, 0x02, 0x03}, (byte)0x55)
    );

    for(EspRadioTelegram telegram : expectedTelegrams)
    {
      byte[] telegramBytes = ((EspPacket) telegram).asByteArray();

      mockPort.addDataToReturn(
          setPacketType(Esp2PacketHeader.PacketType.RRT_UNKNOWN, telegramBytes)
      );
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
      byte[] telegramBytes = ((EspPacket) telegram).asByteArray();

      mockPort.addDataToReturn(
          setPacketType(Esp2PacketHeader.PacketType.RRT_UNKNOWN, telegramBytes)
      );
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

    //Esp2ComPortAdapter comPortAdapter = new Esp2ComPortAdapter(portConfig);
    Esp2ComPortAdapter comPortAdapter = new Esp2ComPortAdapter(new RXTXPort(), portConfig);
    processor = new Esp2Processor(comPortAdapter);

    processor.setProcessorListener(listener);
    processor.start();

    List<EspRadioTelegram> receivedTelegrams = listener.waitForRadioTelegrams();


    processor.stop();
  }

  @Test public void testCommand() throws Exception
  {
    //Esp2ComPortAdapter comPortAdapter = new Esp2ComPortAdapter(portConfig);
    Esp2ComPortAdapter comPortAdapter = new Esp2ComPortAdapter(new RXTXPort(), portConfig);
    processor = new Esp2Processor(comPortAdapter);

    processor.start();

    rdIDBaseCommand.send(processor);

    Assert.assertEquals(Esp2ResponsePacket.ReturnCode.INF_BASEID, rdIDBaseCommand.getReturnCode());

    DeviceID id = rdIDBaseCommand.getBaseID();
    System.out.println("Device Base ID: " + id);

    processor.stop();
  }
  */

  // Helpers --------------------------------------------------------------------------------------

  private Esp2RdIDBaseResponse createRdIDBaseResponse(Esp2ResponsePacket.ReturnCode returnCode)
      throws EspException
  {
    byte[] dataBytes = new byte[Esp2Packet.ESP2_PACKET_DATA_LENGTH];

    dataBytes[Esp2ResponsePacket.ESP2_RCT_RETURN_CODE_INDEX] = returnCode.getValue();

    System.arraycopy(
        baseID.asByteArray(), 0, dataBytes,
        Esp2ResponsePacket.ESP2_RCT_RETURN_CODE_INDEX + 1,
        DeviceID.ENOCEAN_ESP_ID_LENGTH
    );

    return new Esp2RdIDBaseResponse(dataBytes);
  }

  private Esp2ResponsePacket createRadioTelegramResponse(Esp2ResponsePacket.ReturnCode returnCode)
      throws EspException
  {
    byte[] dataBytes = new byte[Esp2Packet.ESP2_PACKET_DATA_LENGTH];
    dataBytes[Esp2ResponsePacket.ESP2_RCT_RETURN_CODE_INDEX] = returnCode.getValue();

    return new Esp2ResponsePacket(dataBytes);
  }

  private List<byte[]> fragmentPacket(Esp2Packet packet, List<Integer> boundaries)
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

  private byte[] setPacketType(Esp2PacketHeader.PacketType type, byte[] packetBytes)
  {
    packetBytes[Esp2PacketHeader.ESP2_HEADER_H_SEQ_LENGTH_INDEX] =
        (byte)((packetBytes[Esp2PacketHeader.ESP2_HEADER_H_SEQ_LENGTH_INDEX] & 0x1F) +
        type.getValue());

    int checksum = 0;
    for(int i = 2; i < (packetBytes.length - 1); i++)
    {
      checksum += packetBytes[i];
    }

    packetBytes[packetBytes.length - 1] = (byte)checksum;

    return packetBytes;
  }


  // Inner Classes --------------------------------------------------------------------------------

  private static class RadioListener implements Esp2ProcessorListener
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
