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
import org.openremote.controller.protocol.enocean.port.Esp3ComPortAdapter;
import org.openremote.controller.utils.Logger;

import java.util.concurrent.TimeUnit;

/**
 * EnOcean Serial Protocol 3.0 processor implementation.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp3Processor extends AbstractEspProcessor<Esp3Packet>
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * ESP 3.0 response timeout value [milliseconds].
   */
  public static final long ESP3_RESPONSE_TIMEOUT = 500;


  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  // Private Instance Fields

  /**
   * Processor listener.
   */
  private Esp3ProcessorListener listener = null;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new processor instance with a given serial COM port adapter which is
   * used to send and receive data.
   *
   * @param port  ESP 3.0 COM port adapter
   */
  public Esp3Processor(Esp3ComPortAdapter port)
  {
    super(port);
  }


  // Implements AbstractEspProcessor --------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override protected long getResponseTimeout()
  {
    return ESP3_RESPONSE_TIMEOUT;
  }

  /**
   * {@inheritDoc}
   */
  @Override protected Esp3PortReaderBuffer createPortReaderBuffer()
  {
    return new Esp3PortReaderBuffer();
  }

  /**
   * {@inheritDoc}
   */
  @Override protected void dispatchPacket(Esp3Packet packet) throws InterruptedException
  {
    Esp3PacketHeader.PacketType type = packet.getPacketType();

    if(type == Esp3PacketHeader.PacketType.RESPONSE)
    {
      dispatchResponse(packet);
    }

    else if(type == Esp3PacketHeader.PacketType.RADIO)
    {
      dispatchRadio(packet);
    }

    else if(type == Esp3PacketHeader.PacketType.EVENT)
    {
      // TODO
    }
  }

  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Sets processor listener.
   */
  public void setProcessorListener(Esp3ProcessorListener listener)
  {
    this.listener = listener;
  }


  // Private Instance Methods ---------------------------------------------------------------------

  /**
   * Adds response packet to the response queue.
   *
   * @param packet ESP 3.0 response packet
   */
  private void dispatchResponse(Esp3Packet packet) throws InterruptedException
  {
    responseQueue.offer(
        packet, Esp3Processor.ESP3_RESPONSE_TIMEOUT, TimeUnit.MILLISECONDS
    );
  }

  /**
   * Notifies listener that a radio telegram has been received.
   *
   * @param radioPacket radio telegram
   */
  private void dispatchRadio(final Esp3Packet radioPacket)
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
   * ESP 3.0 port reader buffer implementation.
   */
  private class Esp3PortReaderBuffer extends AbstractPortReaderBuffer<Esp3Packet>
  {

    // Private Instance Fields --------------------------------------------------------------------

    /**
     * ESP 3.0 packet header.
     */
    private Esp3PacketHeader header = null;


    // Implements AbstractPortReaderBuffer --------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    @Override public Esp3Packet getEspPacket()
    {
      if(header == null)
      {
        header = readHeader();
      }

      if(header == null)
      {
        return null;
      }

      int packetSizeWithoutHeader = header.getPacketSize() - Esp3PacketHeader.ESP3_HEADER_SIZE;

      if(buffer.size() < packetSizeWithoutHeader)
      {
        return null;
      }

      byte[] data = readDataGroup(header.getDataLength());

      byte[] optionalData = readDataGroup(header.getOptionalDataLength());

      byte crc8 = buffer.removeFirst();

      Esp3Packet packet = null;

      if(verifyCRC8DataValue(crc8, data, optionalData))
      {
        packet = createPacket(header, data, optionalData);
      }
      else
      {
        log.error(
            "Discarded received packet {0} because of " +
            "invalid CRC8 data checksum.", header
        );
      }

      header = null;

      return packet;
    }


    /**
     * Reads a ESP 3.0 packet header from the buffer and returns it. <p>
     *
     * The implementation tries to find the beginning of the header which is indicated
     * by the {@link Esp3PacketHeader#ESP3_SYNC_BYTE} value. If the CRC-8 header
     * verification fails it's assumed that the sync. byte was not the beginning of
     * a valid header and the data is discarded.
     *
     * @return returns a packet header if available or null otherwise
     */
    private Esp3PacketHeader readHeader()
    {
      Esp3PacketHeader retHeader = null;

      while(retHeader == null && buffer.size() >= Esp3PacketHeader.ESP3_HEADER_SIZE)
      {
        while(buffer.size() > 0 && buffer.getFirst() != Esp3PacketHeader.ESP3_SYNC_BYTE)
        {
          buffer.removeFirst();
        }

        if(buffer.size() >= Esp3PacketHeader.ESP3_HEADER_SIZE)
        {
          byte[] headerBytes = new byte[Esp3PacketHeader.ESP3_HEADER_SIZE];

          for(int i = 0; i < Esp3PacketHeader.ESP3_HEADER_SIZE; i++)
          {
            headerBytes[i] = buffer.removeFirst();
          }

          try
          {
            retHeader = new Esp3PacketHeader(headerBytes);
          }

          catch (Esp3PacketHeader.UnknownPacketTypeException e)
          {
            log.warn("Discarded received packet : {0}", e, e.getMessage());
          }

          catch (Esp3PacketHeader.CRC8Exception e)
          {
            log.error("Received invalid packet header : {0}", e, e.getMessage());
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

    private boolean verifyCRC8DataValue(byte expectedCRC8, byte[] data, byte[] optionalData)
    {
      int allDataSize = data.length + optionalData.length;

      byte[] allData = new byte[allDataSize];

      // Concatenate data blocks

      System.arraycopy(
          data, 0, allData, 0, data.length
      );

      System.arraycopy(
          optionalData, 0, allData, data.length, optionalData.length
      );

      byte calculatedCRC8 = Esp3Packet.CRC8.calculate(allData);

      return expectedCRC8 == calculatedCRC8;
    }

    /**
     * Creates a ESP 3.0 packet with given header, data and optional data groups. <p>
     *
     * Packet creation is based on the header packet type attribute.
     *
     * @return ESP 3.0 packet
     */
    private Esp3Packet createPacket(Esp3PacketHeader header, byte[] data, byte[] optionalData)
    {
      Esp3Packet packet = null;

      Esp3PacketHeader.PacketType type = header.getPacketType();

      if(type == Esp3PacketHeader.PacketType.RESPONSE)
      {
        packet = createResponsePacket(data, optionalData);
      }

      else if(type == Esp3PacketHeader.PacketType.RADIO)
      {
        packet = createRadioTelegram(header, data, optionalData);
      }

      else if(type == Esp3PacketHeader.PacketType.EVENT)
      {
        // TODO
      }

      return packet;
    }

    /**
     * Creates a ESP 3.0 response packet with given data and optional data groups.
     *
     * @return ESP 3.0 response packet
     */
    private Esp3Packet createResponsePacket(byte[] data, byte[] optionalData)
    {
      Esp3Packet responsePacket = null;

      responsePacket = new Esp3Packet(Esp3PacketHeader.PacketType.RESPONSE, data, optionalData);

      return responsePacket;
    }

    /**
     * Creates a radio telegram instance with given data groups.
     *
     * @param  header        radio telegram header
     *
     * @param  data          data group
     *
     * @param  optionalData  optional data group
     *
     * @return new radio telegram instance or <tt>null</tt> if radio telegram type (RORG) is not
     *         implemented or if there's something wrong with the telegram data
     *
     */
    private Esp3Packet createRadioTelegram(Esp3PacketHeader header, byte[] data, byte[] optionalData)
    {
      Esp3Packet radioTelegram = null;

      if(data.length >= AbstractEsp3RadioTelegram.ESP3_RADIO_MIN_DATA_LENGTH &&
         optionalData.length == Esp3RadioTelegramOptData.ESP3_RADIO_OPT_DATA_LENGTH)
      {
        EspRadioTelegram.RORG rorg;

        try
        {
          rorg = AbstractEsp3RadioTelegram.getRORGFromDataGroup(data);
        }

        catch (EspRadioTelegram.UnknownRorgException e)
        {
          log.warn("Discarded received radio telegram : {0}", e, e.getMessage());

          return null;
        }

        if(rorg == EspRadioTelegram.RORG.RPS)
        {
          radioTelegram = createRPSRadioTelegram(header, data, optionalData);
        }

        else if(rorg == EspRadioTelegram.RORG.BS1)
        {
          radioTelegram = create1BSRadioTelegram(header, data, optionalData);
        }

        else if(rorg == EspRadioTelegram.RORG.BS4)
        {
          radioTelegram = create4BSRadioTelegram(header, data, optionalData);
        }

        else
        {
          log.debug(
              "Discarded received radio telegram (RORG={0}) " +
              "because processing of this type is not implemented yet.", rorg
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
     * Creates a new RPS radio telegram instance with given data groups.
     *
     * @param  header        radio telegram header
     *
     * @param  data          data group
     *
     * @param  optionalData  optional data group
     *
     * @return RPS radio telegram instance or <tt>null</tt> if the data group does not
     *         have the expected size
     */
    private Esp3RPSTelegram createRPSRadioTelegram(Esp3PacketHeader header, byte[] data, byte[] optionalData)
    {
      Esp3RPSTelegram telegram = null;

      if(data.length == Esp3RPSTelegram.ESP3_RADIO_RPS_DATA_LENGTH)
      {
        telegram = new Esp3RPSTelegram(data, optionalData);
      }

      else
      {
        log.error("Discarded received RPS radio telegram {0} " +
                  "because of invalid data length.", header);
      }

      return telegram;
    }

    /**
     * Creates a new 1BS radio telegram instance with given data groups.
     *
     * @param  header        radio telegram header
     *
     * @param  data          data group
     *
     * @param  optionalData  optional data group
     *
     * @return 1BS radio telegram instance or <tt>null</tt> if the data group does not
     *         have the expected size
     */
    private Esp31BSTelegram create1BSRadioTelegram(Esp3PacketHeader header, byte[] data, byte[] optionalData)
    {
      Esp31BSTelegram telegram = null;

      if(data.length == Esp31BSTelegram.ESP3_RADIO_1BS_DATA_LENGTH)
      {
        telegram = new Esp31BSTelegram(data, optionalData);
      }

      else
      {
        log.error("Discarded received 1BS radio telegram {0} " +
                  "because of invalid data length.", header);
      }

      return telegram;
    }

    /**
     * Creates a new 4BS radio telegram instance with given data groups.
     *
     * @param  header        radio telegram header
     *
     * @param  data          data group
     *
     * @param  optionalData  optional data group
     *
     * @return 4BS radio telegram instance or <tt>null</tt> if the data group does not
     *         have the expected size
     */
    private Esp34BSTelegram create4BSRadioTelegram(Esp3PacketHeader header, byte[] data, byte[] optionalData)
    {
      Esp34BSTelegram telegram = null;

      if(data.length == Esp34BSTelegram.ESP3_RADIO_4BS_DATA_LENGTH)
      {
        telegram = new Esp34BSTelegram(data, optionalData);
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
