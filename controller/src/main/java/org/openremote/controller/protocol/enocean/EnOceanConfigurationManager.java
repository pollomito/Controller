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

import org.openremote.controller.*;
import org.openremote.controller.protocol.enocean.port.EspPortConfiguration;
import org.openremote.controller.utils.Logger;

/**
 * EnOcean configuration manager.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EnOceanConfigurationManager implements ConfigurationManager
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  // Private Instance Fields ----------------------------------------------------------------------

  private EspPortConfiguration oldConfiguration;


  // Implements ConfigurationManager --------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public EspPortConfiguration getPortConfig()
  {
    EspPortConfiguration newConfiguration = createEspPortConfiguration();

    oldConfiguration = newConfiguration;

    return newConfiguration;
  }

  /**
   * {@inheritDoc}
   */
  @Override public boolean hasPortConfigChanged()
  {
    if(oldConfiguration == null)
    {
      return true;
    }

    EspPortConfiguration newConfiguration = createEspPortConfiguration();

    return !oldConfiguration.equals(newConfiguration);
  }


  // Private Instance Methods ---------------------------------------------------------------------

  /**
   * Creates an EnOcean serial port configuration with configuration settings from
   * the EnOcean configuration.
   *
   * @return  new EnOcean serial port configuration instance
   */
  private EspPortConfiguration createEspPortConfiguration()
  {
    EnOceanConfiguration enoceanConfig = EnOceanConfiguration.readXML();

    EspPortConfiguration portConfig = new EspPortConfiguration();

    portConfig.setComPort(enoceanConfig.getComPort());

    try
    {
      portConfig.setCommLayer(
          EspPortConfiguration.CommLayer.valueOf(enoceanConfig.getCommLayer())
      );
    }

    catch(RuntimeException exc)
    {
      log.error("Invalid communication layer configuration.");
    }

    try
    {
      portConfig.setSerialProtocol(
          EspPortConfiguration.SerialProtocol.valueOf(enoceanConfig.getSerialProtocol())
      );
    }

    catch(RuntimeException exc)
    {
      log.error("Invalid EnOcean serial protocol configuration.");
    }

    return portConfig;
  }
}
