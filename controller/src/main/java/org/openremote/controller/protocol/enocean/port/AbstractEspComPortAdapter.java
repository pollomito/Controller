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
package org.openremote.controller.protocol.enocean.port;

import org.openremote.controller.protocol.enocean.ConfigurationException;
import org.openremote.controller.protocol.enocean.ConnectionException;
import org.openremote.controller.protocol.enocean.EnOceanCommandBuilder;
import org.openremote.controller.protocol.enocean.EspVersion;
import org.openremote.controller.protocol.port.Message;
import org.openremote.controller.protocol.port.Port;
import org.openremote.controller.protocol.port.PortException;
import org.openremote.controller.protocol.port.pad.AbstractPort;
import org.openremote.controller.utils.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * A common superclass for EnOcean Serial Protocol (ESP) COM port adapter implementations to
 * reuse code. <p>
 *
 * The class acts as an adapter to the {@link org.openremote.controller.protocol.port.Port}
 * interface. The adapter translates exceptions and automates serial port configuration. <p>
 *
 * Subclasses should provide an implementation of {@link #createComPortConfiguration(EspPortConfiguration)}.
 * The createComPortConfiguration() method should create a configuration which is a valid parameter
 * for {@link org.openremote.controller.protocol.port.Port#configure(java.util.Map)}.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public abstract class AbstractEspComPortAdapter implements EspPort
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);

  /**
   * Map contains all already used PAD communication layer ports using the
   * serial port name as the key.
   */
  private final static HashMap<String, AbstractPort> padPorts = new HashMap<String, AbstractPort>(1);

  /**
   * Creates and returns a PAD communication layer port or returns an
   * already used port for the given serial port name.
   *
   * @return  PAD communication layer port
   */
  protected static AbstractPort getPADPort(EspPortConfiguration config)
  {
    AbstractPort port = null;
    String comPort = config.getComPort();

    if(comPort != null)
    {
      if(padPorts.containsKey(comPort))
      {
        port = padPorts.get(comPort);
      }

      else
      {
        port = new AbstractPort();
        padPorts.put(comPort, port);
      }
    }

    else
    {
      port = new AbstractPort();
    }

    return port;
  }


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Wrapped serial port.
   */
  private Port port;

  /**
   * Configuration which is used to configure the wrapped serial port.
   */
  private EspPortConfiguration configuration;

  /**
   * Indicates if wrapped serial port has already been configured.
   */
  private boolean configured;

  /**
   * Indicated if wrapped serial port has already been started.
   */
  private boolean started;


  // Constructors -------------------------------------------------------------------------------

  /**
   * Constructs a new EnOcean Serial Protocol (ESP) port adapter with a given serial port and
   * an EnOcean Serial Protocol port configuration used to configure the wrapped serial port.
   *
   * @param port           serial port
   * @param configuration  EnOcean Serial Protocol (ESP) port configuration
   */
  public AbstractEspComPortAdapter(Port port, EspPortConfiguration configuration)
  {
    if(port == null)
    {
      throw new IllegalArgumentException("Must include a reference to a port.");
    }

    if(configuration == null)
    {
      throw new IllegalArgumentException("Must include a reference to a configuration.");
    }

    this.started = false;
    this.configured = false;

    this.port = port;
    this.configuration = configuration;

  }


  // Implements EspPort ---------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public void start() throws ConnectionException, ConfigurationException
  {
    if(started)
      return;

    configurePort();

    try
    {
      port.start();
    }

    catch(PortException e)
    {
      throw new ConnectionException(
          "Starting {0} failed : {1}", e, this, e.getMessage()
      );
    }

    catch(IOException e)
    {
      throw new ConnectionException(
          "Starting {0} failed : {1}", e, this, e.getMessage()
      );
    }

    started = true;
  }

  /**
   * {@inheritDoc}
   */
  @Override public void stop() throws ConnectionException
  {
    if(!started)
      return;

    try
    {
      port.stop();
    }

    catch(PortException e)
    {
      throw new ConnectionException(
          "Stopping {0} failed : {1}", e, this, e.getMessage()
      );
    }

    catch(IOException e)
    {
      throw new ConnectionException(
          "Stopping {0} failed : {1}", e, this, e.getMessage()
      );
    }

    started = false;
  }

  /**
   * {@inheritDoc}
   */
  @Override public boolean isStarted()
  {
    return started;
  }


  /**
   * {@inheritDoc}
   */
  @Override public void send(byte[] data) throws ConnectionException
  {
    if(!started)
    {
      throw new ConnectionException(
          "Failed to send data because {0} has not been started.", this
      );
    }

    Message msg = new Message(data);

    try
    {
      port.send(msg);
    }

    catch(PortException e)
    {
      throw new ConnectionException(
          "Failed to send data to {0} : {1}", e, e.getMessage()
      );
    }

    catch(IOException e)
    {
      throw new ConnectionException(
          "Failed to send data to {0} : {1}", e, this, e.getMessage()
      );
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override public byte[] receive() throws ConnectionException
  {
    if(!started)
    {
      throw new ConnectionException(
          "Failed to receive data from {0} because port has not been started.", this
      );
    }

    Message msg = null;

    try
    {
      msg = port.receive();
    }

    catch(IOException e)
    {
      throw new ConnectionException(
          "Failed to receive data from {0} : {1}", e, this, e.getMessage()
      );
    }

    if(msg == null || msg.getContent() == null)
    {
      return new byte[] {};
    }

    logReceivedData(msg.getContent());

    return msg.getContent();
  }


  // Object Overrides -----------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public String toString()
  {
    StringBuilder builder = new StringBuilder();

    String comPort = configuration.getComPort();
    if(comPort == null)
    {
      comPort = "--";
    }

    EspPortConfiguration.CommLayer commLayerType = configuration.getCommLayer();
    String commLayer = commLayerType == null ? "--" : commLayerType.toString();

    builder
        .append("[COM port: ")
        .append(comPort)
        .append(", Protocol: ")
        .append(getEspVersion())
        .append(", Communication Layer: ")
        .append(commLayer)
        .append("]");

    return builder.toString();
  }


  // Protected Instance Methods -------------------------------------------------------------------

  /**
   * Subclasses should implement this to provide the COM port configuration which is used inside
   * the {@link #configurePort()} method to configure the wrapped serial port.
   *
   * @param   configuration  EnOcean Serial Protocol (ESP) port configuration
   *
   * @return  serial port configuration. The returned configuration should be a valid parameter
   *          for {@link org.openremote.controller.protocol.port.Port#configure(java.util.Map)}
   *
   * @throws  ConfigurationException
   *            if the configuration provided by the input parameter is invalid
   */
  protected abstract Map<String, Object> createComPortConfiguration(EspPortConfiguration configuration)
      throws ConfigurationException;


  /**
   * Returns the EnOcean serial protocol version.
   *
   * @return ESP version
   */
  protected abstract EspVersion getEspVersion();


  // Private Instance Methods ---------------------------------------------------------------------

  private void logReceivedData(byte[] dataBytes)
  {
    if(dataBytes == null)
    {
      return;
    }

    String dataAsString = "";

    for(byte dataByte : dataBytes)
    {
      dataAsString = dataAsString + String.format("0x%02X ", dataByte);
    }

    log.trace("Received serial data from {0} : {1}", this, dataAsString);
  }

  /**
   * Configures the wrapped {@link #port} after translating the configuration in the
   * {@link #configuration} field to a configuration which can be passed as parameter to
   * {@link org.openremote.controller.protocol.port.Port#configure(java.util.Map)}. <p>
   *
   * Subclasses have to provide an implementation of
   * {@link #createComPortConfiguration(EspPortConfiguration)} which creates a configuration format
   * suitable for {@link org.openremote.controller.protocol.port.Port#configure(java.util.Map)}.
   *
   * @throws ConnectionException
   *           if port cannot be configured because of port connection errors
   *
   * @throws ConfigurationException
   *           if port cannot be configured because of an invalid configuration
   */
  private void configurePort() throws ConnectionException, ConfigurationException
  {
    if(configured)
      return;

    Map<String, Object> comPortConfig = createComPortConfiguration(configuration);

    try
    {
      port.configure(comPortConfig);
    }

    catch(PortException e)
    {
      if(e.getCode() == PortException.INVALID_CONFIGURATION)
      {
        throw new ConfigurationException(
            "Configuring {0} failed : {1}",e , this, e.getMessage()
        );
      }

      throw new ConnectionException(
          "Configuring {0} failed : {1}", e, this, e.getMessage()
      );
    }

    catch(IOException e)
    {
      throw new ConnectionException(
          "Configuring {0} failed : {1}", e, this, e.getMessage()
      );
    }

    configured = true;
  }
}
