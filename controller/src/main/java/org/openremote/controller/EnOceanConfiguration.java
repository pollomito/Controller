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
package org.openremote.controller;

import org.openremote.controller.service.ServiceContext;

/**
 * Represents the EnOcean configuration.
 *
 * @author Rainer Hitz
 */
public class EnOceanConfiguration extends Configuration
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Property name of serial port configuration item.
   */
  public static final String ENOCEAN_COM_PORT = "enocean.comPort";

  /**
   * Property name of communication layer configuration item.
   */
  public static final String ENOCEAN_COMM_LAYER = "enocean.commLayer";

  /**
   * Property name of EnOcean serial protocol configuration item.
   */
  public static final String ENOCEAN_SERIAL_PROTOCOL = "enocean.serialProtocol";


  // Class Members --------------------------------------------------------------------------------

  /**
   * Updates the EnOcean configuration with EnOcean configuration settings from
   * the designer (controller.xml) and returns the updated EnOcean configuration.
   *
   * @return  updated EnOcean configuration
   */
  public static EnOceanConfiguration readXML()
  {
    EnOceanConfiguration config = ServiceContext.getEnOceanConfiguration();

    return (EnOceanConfiguration)Configuration.updateWithControllerXMLConfiguration(config);
  }


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Serial port.
   */
  private String comPort;

  /**
   * Communication layer [PAD, RXTX].
   */
  private String commLayer;

  /**
   * EnOcean serial protocol [ESP2, ESP3].
   */
  private String serialProtocol;


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Returns the serial port configuration setting.
   *
   * @return  serial port
   */
  public String getComPort()
  {
    return preferAttrCustomValue(ENOCEAN_COM_PORT, comPort);
  }

  /**
   * Sets the serial port.
   *
   * @param comPort  serial port
   */
  public void setComPort(String comPort)
  {
    this.comPort = comPort;
  }

  /**
   * Returns the communication layer configuration setting (PAD or RXTX).
   *
   * @return  communication layer
   */
  public String getCommLayer()
  {
    return preferAttrCustomValue(ENOCEAN_COMM_LAYER, commLayer);
  }

  /**
   * Sets the communication layer (PAD or RXTX).
   *
   * @param commLayer  communication layer
   */
  public void setCommLayer(String commLayer)
  {
    this.commLayer = commLayer;
  }

  /**
   * Returns the EnOcean serial protocol configuration setting (ESP2 or ESP3).
   *
   * @return  EnOcean serial protocol
   */
  public String getSerialProtocol()
  {
    return preferAttrCustomValue(ENOCEAN_SERIAL_PROTOCOL, serialProtocol);
  }

  /**
   * Sets the EnOcean serial protocol (ESP2 or ESP3).
   *
   * @param serialProtocol  EnOcean serial protocol
   */
  public void setSerialProtocol(String serialProtocol)
  {
    this.serialProtocol = serialProtocol;
  }
}
