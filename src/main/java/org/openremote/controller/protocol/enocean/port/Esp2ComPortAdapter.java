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
import org.openremote.controller.protocol.enocean.EnOceanCommandBuilder;
import org.openremote.controller.protocol.enocean.EspVersion;
import org.openremote.controller.protocol.port.Port;
import org.openremote.controller.protocol.port.pad.AbstractPort;
import org.openremote.controller.utils.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * COM port adapter implementation for EnOcean Serial Protocol version 2 (ESP2).
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp2ComPortAdapter extends AbstractEspComPortAdapter
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  // Constructors -------------------------------------------------------------------------------

  /**
   * Constructs a new EnOcean Serial Protocol version 2 (ESP2) COM port adapter with a given
   * port configuration which is used to configure the internally created serial port instance.
   *
   * Note that the PAD communication layer is used.
   *
   * @param configuration  EnOcean Serial Protocol (ESP) port configuration
   */
  public Esp2ComPortAdapter(EspPortConfiguration configuration)
  {
    this(getPADPort(configuration), configuration);
  }

  /**
   * Constructs a new EnOcean Serial Protocol version 2 (ESP2) COM port adapter with a given
   * serial port and an EnOcean Serial Protocol (ESP) port configuration used to configure
   * the wrapped serial port.
   *
   * @param port           serial port
   * @param configuration  EnOcean Serial Protocol (ESP) port configuration
   */
  public Esp2ComPortAdapter(Port port, EspPortConfiguration configuration)
  {
    super(port, configuration);
  }


  // Implements AbstractEspComPortAdapter -------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override protected Map<String, Object> createComPortConfiguration(EspPortConfiguration configuration)
      throws ConfigurationException
  {
    String comPort = configuration.getComPort();

    if(comPort == null || comPort.trim().length() == 0)
    {
      throw new ConfigurationException(
          "Missing COM port configuration."
      );
    }

    Map<String, Object> portConfig = new HashMap<String, Object>();

    portConfig.put(AbstractPort.PORT_ID, comPort);
    portConfig.put(AbstractPort.PORT_TYPE, "serial");
    portConfig.put(AbstractPort.PORT_SPEED, "9600");
    portConfig.put(AbstractPort.PORT_NB_BITS, "8");
    portConfig.put(AbstractPort.PORT_PARITY, "no");

    return portConfig;
  }

  /**
   * {@inheritDoc}
   */
  @Override protected EspVersion getEspVersion()
  {
    return EspVersion.ESP2;
  }
}
