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

import org.openremote.controller.protocol.enocean.EnOceanCommandBuilder;
import org.openremote.controller.protocol.enocean.packet.radio.*;
import org.openremote.controller.protocol.enocean.port.Esp2ComPortAdapter;
import org.openremote.controller.utils.Logger;

import java.util.concurrent.TimeUnit;

/**
 * EnOcean Serial Protocol 2.0 processor implementation.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp2Processor extends AbstractEspProcessor<Esp2Packet>
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * ESP 2.0 response timeout value [milliseconds].
   *
   * TODO : try to find timing parameter in the EnOcean documents
   */
  public static final long ESP2_RESPONSE_TIMEOUT = 500;


  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Processor listener.
   */
  private Esp2ProcessorListener listener = null;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new processor instance with a given serial COM port adapter which is
   * used to send and receive data.
   *
   * @param port  ESP 2.0 COM port adapter
   */
  public Esp2Processor(Esp2ComPortAdapter port)
  {
    super(port);
  }


  // Implements AbstractEspProcessor --------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override protected long getResponseTimeout()
  {
    return ESP2_RESPONSE_TIMEOUT;
  }

  /**
   * {@inheritDoc}
   */
  @Override protected Esp2PortReaderBuffer createPortReaderBuffer()
  {
    return new Esp2PortReaderBuffer();
  }

  /**
   * {@inheritDoc}
   */
  @Override protected void dispatchPacket(Esp2Packet packet) throws InterruptedException
  {
    Esp2PacketHeader.PacketType type = packet.getPacketType();

    if(type == Esp2PacketHeader.PacketType.RCT)
    {
      dispatchResponse(packet);
    }

    else if(type == Esp2PacketHeader.PacketType.RRT_UNKNOWN ||
            type == Esp2PacketHeader.PacketType.RRT_KNOWN )
    {
      dispatchRadio(packet);
    }
  }


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Sets processor listener.
   */
  public void setProcessorListener(Esp2ProcessorListener listener)
  {
    this.listener = listener;
  }


  // Private Instance Methods ---------------------------------------------------------------------

  /**
   * Adds response packet to the response queue.
   *
   * @param packet ESP 2.0 response packet
   */
  private void dispatchResponse(Esp2Packet packet) throws InterruptedException
  {
    // Do not dispatch INF_INIT packets because they are not related to a request.
    // INF_INIT packets are initially sent after power-on, a hardware reset or a
    // RESET command.
    if(isInfInitPacket(packet))
    {
      return;
    }

    responseQueue.offer(
        packet, Esp2Processor.ESP2_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS
    );
  }

  /**
   * Checks if the given packet is a INF_INIT receive command telegram (RCT).
   *
   * @param packet  receive command telegram (RCT)
   *
   * @return true if IN_INIT telegram, false otherwise
   */
  private boolean isInfInitPacket(Esp2Packet packet)
  {
    boolean isInfInit = false;
    byte[] data = packet.getData();

    if(data != null && data.length > Esp2ResponsePacket.ESP2_RCT_RETURN_CODE_INDEX &&
       data[Esp2ResponsePacket.ESP2_RCT_RETURN_CODE_INDEX] == Esp2ResponsePacket.ReturnCode.INF_INIT.getValue())
    {
      isInfInit = true;
    }

    return isInfInit;
  }

  /**
   * Notifies listener that a radio telegram has been received.
   *
   * @param radioPacket radio telegram
   */
  private void dispatchRadio(final Esp2Packet radioPacket)
  {
    if((radioPacket instanceof EspRadioTelegram) && listener != null)
    {
      executor.execute(
          new Runnable()
          {
            @Override public void run()
            {
              listener.radioTelegramReceived((EspRadioTelegram)radioPacket);
            }
          }
      );
    }
  }


  // Inner Classes --------------------------------------------------------------------------------

  /**
   * ESP 2.0 port reader buffer implementation.
   */
  private class Esp2PortReaderBuffer extends AbstractPortReaderBuffer<Esp2Packet>
  {

    // Private Instance Fields --------------------------------------------------------------------

    /**
     * ESP 2.0 packet header.
     */
    private Esp2PacketHeader header = null;


    // Implements AbstractPortReaderBuffer --------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override public Esp2Packet getEspPacket()
    {
      if(header == null)
      {
        header = readHeader();
      }

      if(header == null)
      {
        return null;
      }

      if(header.getLength() < 1)
      {
        log.error(
            "Discarded received packet header {0} because of " +
            "invalid length value.", header);

        header = null;
        return null;
      }

      if(buffer.size() < header.getLength())
      {
        return null;
      }

      byte[] data = readDataGroup(header.getLength() - Esp2Packet.ESP2_PACKET_CHECKSUM_LENGTH);

      byte checksum = buffer.removeFirst();

      Esp2Packet packet = null;

      if(verifyChecksum(checksum, header, data))
      {
        packet = createPacket(header, data);
      }
      else
      {
        log.error(
            "Discarded received packet {0} because of " +
            "invalid checksum.", header
        );
      }

      header = null;

      return packet;
    }

    /**
     * Reads an ESP 2.0 packet header from the buffer and returns it. <p>
     *
     * The implementation tries to find the beginning of the header which is indicated
     * by two synchronizations bytes.
     *
     * @return returns a packet header if available or null otherwise
     */
    private Esp2PacketHeader readHeader()
    {
      Esp2PacketHeader retHeader = null;

      while(retHeader == null && buffer.size() >= Esp2PacketHeader.ESP2_HEADER_SIZE)
      {
        int discardedBytes = 0;
        while(buffer.size() > 0 && buffer.getFirst() != Esp2PacketHeader.ESP2_SYNC_BYTE_1)
        {
          buffer.removeFirst();
          discardedBytes++;
        }

        if(discardedBytes > 0)
        {
          log.warn(
              "Discarded {0} received bytes while searching " +
              "for the beginning of the next packet.", discardedBytes
          );
        }

        if(buffer.size() >= Esp2PacketHeader.ESP2_HEADER_SIZE)
        {
          byte[] headerBytes = new byte[Esp2PacketHeader.ESP2_HEADER_SIZE];

          // Get first sync. byte
          headerBytes[0] = buffer.removeFirst();

          // Check for second sync. byte
          if(buffer.getFirst() != Esp2PacketHeader.ESP2_SYNC_BYTE_2)
          {
            log.warn(
                "Discarded 1 received byte while searching " +
                "for the beginning of the next packet."
            );

            continue;
          }

          for(int i = 1; i < Esp2PacketHeader.ESP2_HEADER_SIZE; i++)
          {
            headerBytes[i] = buffer.removeFirst();
          }

          try
          {
            retHeader = new Esp2PacketHeader(headerBytes);
          }

          catch (Esp2PacketHeader.UnknownPacketTypeException e)
          {
            log.warn("Discarded received packet : {0}", e, e.getMessage());
          }
        }
      }

      return retHeader;
    }

    /**
     * Reads a data group of given size from the buffer and returns it.
     *
     * @param  size  data group size
     * @return data group as byte array
     */
    private byte[] readDataGroup(int size)
    {
      byte[] dataGroup = new byte[size];

      for(int i = 0; i < size; i++)
      {
        dataGroup[i] = buffer.removeFirst();
      }

      return dataGroup;
    }

    private boolean verifyChecksum(byte expectedChecksum, Esp2PacketHeader header, byte[] data)
    {
      byte[] headerBytes = header.asByteArray();

      int calculatedChecksum = headerBytes[Esp2PacketHeader.ESP2_HEADER_H_SEQ_LENGTH_INDEX] & 0xFF;

      for(int i = 0; i < data.length; i++)
      {
        calculatedChecksum += (data[i] & 0xFF);
      }

      return expectedChecksum == (byte)calculatedChecksum;
    }

    /**
     * Creates a ESP 2.0 packet with given header and data group. <p>
     *
     * Packet creation is based on the header packet type attribute.
     *
     * @return ESP 2.0 packet
     */
    private Esp2Packet createPacket(Esp2PacketHeader header, byte[] data)
    {
      Esp2Packet packet = null;

      Esp2PacketHeader.PacketType type = header.getPacketType();

      if(type == Esp2PacketHeader.PacketType.RCT)
      {
        packet = createResponsePacket(data);
      }

      else if(type == Esp2PacketHeader.PacketType.RRT_UNKNOWN ||
              type == Esp2PacketHeader.PacketType.RRT_KNOWN)
      {
        packet = createRadioTelegram(header, data);
      }

      else if(type == Esp2PacketHeader.PacketType.LEARN)
      {
        // TODO
      }

      else if(type == Esp2PacketHeader.PacketType.DELETE)
      {
        // TODO
      }

      return packet;
    }

    /**
     * Creates a ESP 2.0 response packet with given data group.
     *
     * @return ESP 2.0 response packet
     */
    private Esp2Packet createResponsePacket(byte[] data)
    {
      Esp2Packet responsePacket = null;

      responsePacket = new Esp2Packet(Esp2PacketHeader.PacketType.RCT, data);

      return responsePacket;
    }

    /**
     * Creates a radio telegram instance with given header and data group.
     *
     * @param  header  radio telegram header
     *
     * @param  data    data group
     *
     * @return new radio telegram instance or <tt>null</tt> if radio telegram type (RORG) is not
     *         implemented or if there's something wrong with the telegram data
     *
     */
    private Esp2Packet createRadioTelegram(Esp2PacketHeader header, byte[] data)
    {
      Esp2Packet radioTelegram = null;

      if(data.length == AbstractEsp2RadioTelegram.ESP2_RADIO_DATA_LENGTH)
      {
        EspRadioTelegram.RORG org;

        try
        {
          org = AbstractEsp2RadioTelegram.getORGFromDataGroup(data);
        }

        catch (EspRadioTelegram.UnknownRorgException e)
        {
          log.warn("Discarded received radio telegram : {0}", e, e.getMessage());

          return null;
        }

        if(org == EspRadioTelegram.RORG.RPS_ESP2)
        {
          radioTelegram = createRPSRadioTelegram(header, data);
        }

        else if(org == EspRadioTelegram.RORG.BS1_ESP2)
        {
          radioTelegram = create1BSRadioTelegram(header, data);
        }

        else if(org == EspRadioTelegram.RORG.BS4_ESP2)
        {
          radioTelegram = create4BSRadioTelegram(header, data);
        }

        else
        {
          log.debug(
              "Discarded received radio telegram (RORG={0}) " +
              "because processing of this type is not implemented yet.", org
          );
        }
      }

      else
      {
        log.error(
            "Discarded received radio telegram {0} because of invalid data length.",
            header
        );
      }

      if(radioTelegram != null)
      {
        log.debug("Received radio telegram : " + radioTelegram);
      }

      return radioTelegram;
    }

    /**
     * Creates a new RPS radio telegram instance with given data group.
     *
     * @param  header  radio telegram header
     *
     * @param  data    data group
     *
     * @return RPS radio telegram instance or <tt>null</tt> if the data group does not
     *         have the expected size
     */
    private Esp2RPSTelegram createRPSRadioTelegram(Esp2PacketHeader header, byte[] data)
    {
      Esp2RPSTelegram telegram = null;

      if(data.length == AbstractEsp2RadioTelegram.ESP2_PACKET_DATA_LENGTH)
      {
        telegram = new Esp2RPSTelegram(header.getPacketType(), data);
      }

      else
      {
        log.error("Discarded received RPS radio telegram {0} " +
                  "because of invalid data length.", header);
      }

      return telegram;
    }

    /**
     * Creates a new 1BS radio telegram instance with given data group.
     *
     * @param  header  radio telegram header
     *
     * @param  data    data group
     *
     * @return 1BS radio telegram instance or <tt>null</tt> if the data group does not
     *         have the expected size
     */
    private Esp21BSTelegram create1BSRadioTelegram(Esp2PacketHeader header, byte[] data)
    {
      Esp21BSTelegram telegram = null;

      if(data.length == AbstractEsp2RadioTelegram.ESP2_PACKET_DATA_LENGTH)
      {
        telegram = new Esp21BSTelegram(header.getPacketType(), data);
      }

      else
      {
        log.error("Discarded received 1BS radio telegram {0} " +
                  "because of invalid data length.", header);
      }

      return telegram;
    }

    /**
     * Creates a new 4BS radio telegram instance with given data group.
     *
     * @param  header  radio telegram header
     *
     * @param  data    data group
     *
     * @return 4BS radio telegram instance or <tt>null</tt> if the data group does not
     *         have the expected size
     */
    private Esp24BSTelegram create4BSRadioTelegram(Esp2PacketHeader header, byte[] data)
    {
      Esp24BSTelegram telegram = null;

      if(data.length == AbstractEsp2RadioTelegram.ESP2_PACKET_DATA_LENGTH)
      {
        telegram = new Esp24BSTelegram(header.getPacketType(), data);
      }

      else
      {
        log.error("Discarded received 4BS radio telegram {0} " +
                  "because of invalid data length.", header);
      }

      return telegram;
    }
  }
}