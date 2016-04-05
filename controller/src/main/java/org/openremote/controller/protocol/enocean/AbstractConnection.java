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
package org.openremote.controller.protocol.enocean;

import org.openremote.controller.protocol.enocean.packet.EspPacket;
import org.openremote.controller.protocol.enocean.packet.EspProcessor;
import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram;
import org.openremote.controller.utils.Logger;

/**
 * A common superclass for EnOcean connection implementations to reuse code.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public abstract class AbstractConnection<P extends EspPacket, R> implements EnOceanConnection
{

  // Enums ----------------------------------------------------------------------------------------

  public enum ConnectionState
  {
    CONNECTED,
    DISCONNECTED
  }


  // Constants ------------------------------------------------------------------------------------

  /**
   *  Number of times to retry reading the base ID of the EnOcean module : {@value}
   */
  private static final int ESP_CONNECTION_READ_BASE_ID_RETRY_COUNT_MAX = 3;


  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Connection state.
   */
  private ConnectionState state = ConnectionState.DISCONNECTED;

  /**
   * Object for handling the EnOcean serial protocol.
   */
  private EspProcessor<P> processor;

  /**
   * Listener which will be notified of received radio telegrams.
   */
  protected RadioTelegramListener radioListener;

  /**
   * EnOcean module base device ID.
   *
   * @see DeviceID
   */
  private DeviceID baseID;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs an EnOcean connection instance with given serial protocol processor
   * and radio telegram listener.
   *
   * @param processor   processor for handling the EnOcean serial protocol
   *
   * @param listener    listener which will be notified of received radio telegrams
   */
  public AbstractConnection(EspProcessor<P> processor, RadioTelegramListener listener)
  {
    if(processor == null)
    {
      throw new IllegalArgumentException("null processor");
    }

    this.processor = processor;
    this.radioListener = listener;
  }


  // Implements EnOceanConnection -----------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public synchronized void connect() throws ConnectionException, ConfigurationException
  {
    if(state == ConnectionState.CONNECTED)
    {
      return;
    }

    processor.start();

    try
    {
      /*
       *  Immediately after the start of the serial connection, it may happen that
       *  a truncated radio telegram is received. This error is reproducible (Mac)
       *  if the EnOcean interface (USB 300) receives several radio telegrams before
       *  the start of the serial connection. This seems to be an error of the EnOcean
       *  firmware (USB 300). If this happens, the base ID of the EnOcean interface
       *  has to be read more than once, in order to synchronize the serial protocol.
       */
      this.baseID = readBaseIDRepeatedly();

      log.info("Received EnOcean gateway base ID : {0}", this.baseID);
    }

    catch (InterruptedException e)
    {
      Thread.currentThread().interrupt();

      return;
    }

    catch(ConnectionException e)
    {
      try
      {
        processor.stop();
      }
      catch(ConnectionException stopExc)
      {
        log.error("Failed to close port: {0}", stopExc.getMessage());
      }

      throw e;
    }

    state = ConnectionState.CONNECTED;
  }

  /**
   * {@inheritDoc}
   */
  @Override public synchronized void disconnect() throws ConnectionException
  {
    if(state == ConnectionState.DISCONNECTED)
    {
      return;
    }

    processor.stop();

    state = ConnectionState.DISCONNECTED;
  }

  /**
   * {@inheritDoc}
   */
  @Override public synchronized void sendRadio(EspRadioTelegram.RORG rorg, DeviceID deviceID, byte[] payload, byte statusByte)
      throws ConnectionException, ConfigurationException
  {
    if(state == ConnectionState.DISCONNECTED)
    {
      throw new ConnectionException("Missing EnOcean interface connection.");
    }

    try
    {
      deviceID = deviceID.resolve(baseID);
    }
    catch (InvalidDeviceIDException e)
    {
      throw new ConfigurationException(e.getMessage(), e);
    }

    R radioTelegram = createRadioTelegram(rorg, deviceID, payload, statusByte);

    if(radioTelegram != null)
    {
      try
      {
        sendRadioTelegram(radioTelegram, processor);
      }

      catch (InterruptedException e)
      {
        Thread.currentThread().interrupt();
      }
    }
  }


  // Protected Instance Methods -------------------------------------------------------------------

  /**
   * Reads base ID from EnOcean gateway interface and returns it.
   *
   * @param   processor  processor for executing read base ID command
   *
   * @return  EnOcean gateway base ID
   *
   * @throws  InterruptedException
   *            if read base ID command has been interrupted
   *
   * @throws  ConnectionException
   *            if read base ID command failed because of an connection error
   */
  protected abstract DeviceID readBaseID(EspProcessor<P> processor) throws InterruptedException, ConnectionException;

  /**
   * Creates radio telegram instance and returns it.
   *
   * @param rorg        radio telegram type
   *
   * @param deviceID    absolute sender device ID
   *
   * @param payload     radio telegram payload data
   *
   * @param statusByte  radio telegram status byte
   *
   * @return  radio telegram instance
   */
  protected abstract R createRadioTelegram(EspRadioTelegram.RORG rorg, DeviceID deviceID, byte[] payload, byte statusByte);

  /**
   * Sends radio telegram.
   *
   * @param  radioTelegram  radio telegram
   *
   * @param  processor      processor for sending the radio telegram
   *
   * @throws InterruptedException
   *           if sending the radio telegram has been interrupted
   *
   * @throws ConnectionException
   *           if sending the radio telegram failed because of a connection error
   */
  protected abstract void sendRadioTelegram(R radioTelegram, EspProcessor<P> processor) throws InterruptedException, ConnectionException;


  // Private Instance Methods ---------------------------------------------------------------------

  private DeviceID readBaseIDRepeatedly() throws InterruptedException, ConnectionException
  {
    int retryCount = 0;

    while(true)
    {
      ++retryCount;

      log.debug("Try to read EnOcean module base ID (retry count = {0})", retryCount);

      try
      {
        return readBaseID(processor);
      }

      catch (ConnectionException e)
      {
        if(ESP_CONNECTION_READ_BASE_ID_RETRY_COUNT_MAX == retryCount)
        {
          throw e;
        }
      }
    }
  }
}
