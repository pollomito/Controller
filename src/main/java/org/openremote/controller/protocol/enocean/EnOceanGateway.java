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

import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram;
import org.openremote.controller.protocol.enocean.port.EspPortConfiguration;
import org.openremote.controller.utils.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * EnOcean gateway for sending and receiving radio telegrams.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EnOceanGateway implements RadioInterface, RadioTelegramListener
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * EnOcean connection manager for establishing a connection to the EnOcean module.
   */
  private EnOceanConnectionManager connectionManager;

  /**
   * EnOcean serial protocol (ESP) port configuration.
   */
  private EspPortConfiguration configuration;

  /**
   * Map containing all radio telegram listeners using the EnOcean device ID
   * as the key.
   */
  private ConcurrentHashMap<DeviceID, CopyOnWriteArraySet<RadioTelegramListener>> radioListeners =
      new ConcurrentHashMap<DeviceID, CopyOnWriteArraySet<RadioTelegramListener>>();


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs an EnOcean gateway instance with given EnOcean connection manager
   * and serial port configuration.
   *
   * @param mgr     EnOcean connection manager for establishing a connection to the EnOcean module
   *
   * @param config  EnOcean serial protocol (ESP) port configuration
   */
  public EnOceanGateway(EnOceanConnectionManager mgr, EspPortConfiguration config)
  {
    if(mgr == null)
    {
      throw new IllegalArgumentException("null connection manager");
    }

    if(config == null)
    {
      throw new IllegalArgumentException("null configuration");
    }

    this.connectionManager = mgr;
    this.configuration = config;
  }


  // Implements RadioInterface --------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  public void sendRadio(EspRadioTelegram.RORG rorg, DeviceID deviceID, byte[] payload, byte statusByte)
      throws ConfigurationException, ConnectionException
  {
    EnOceanConnection connection = internalConnect();

    if(connection != null)
    {
      connection.sendRadio(rorg, deviceID, payload, statusByte);
    }
  }

  /**
   * {@inheritDoc}
   */
  public void addRadioListener(DeviceID deviceID, RadioTelegramListener listener)
  {
    CopyOnWriteArraySet<RadioTelegramListener> listenerList = null;

    listenerList = radioListeners.get(deviceID);

    if(listenerList == null)
    {
      radioListeners.putIfAbsent(deviceID, new CopyOnWriteArraySet<RadioTelegramListener>());
      listenerList = radioListeners.get(deviceID);
    }

    if(listenerList != null)
    {
      listenerList.add(listener);
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override public void removeRadioListener(DeviceID deviceID, RadioTelegramListener listener)
  {
    CopyOnWriteArraySet<RadioTelegramListener> listenerList = null;

    listenerList = radioListeners.get(deviceID);

    if(listenerList != null)
    {
      listenerList.remove(listener);
    }
  }


  // Implements RadioTelegramListener -------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public void radioTelegramReceived(EspRadioTelegram telegram)
  {
    Set<RadioTelegramListener> listeners = radioListeners.get(telegram.getSenderID());

    if(listeners != null)
    {
      for(RadioTelegramListener curListener : listeners)
      {
        curListener.radioTelegramReceived(telegram);
      }
    }
  }

  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Establishes a connection for receiving radio telegrams from the EnOcean module.
   *
   * @throws ConfigurationException
   *           if connection failed because of a configuration error
   *
   * @throws ConnectionException
   *           if connection failed because of a connection error
   */
  public void connect() throws ConfigurationException, ConnectionException
  {
    internalConnect();
  }

  /**
   * Disconnects from EnOcean interface.
   */
  public void disconnect() throws ConnectionException
  {
    connectionManager.disconnect();
  }

  // Private Instance Methods ---------------------------------------------------------------------

  /**
   * Retrieves an established connection from the connection manager and returns it.
   *
   * @return  established connection
   *
   * @throws ConfigurationException
   *           if connection failed because of a configuration error
   *
   * @throws ConnectionException
   *           if connection failed because of a connection error
   */
  private EnOceanConnection internalConnect() throws ConfigurationException, ConnectionException
  {
    return connectionManager.getConnection(configuration, this);
  }
}
