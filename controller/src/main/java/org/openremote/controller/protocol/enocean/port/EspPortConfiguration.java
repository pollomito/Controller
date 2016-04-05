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

import org.openremote.controller.Configuration;

/**
 * EnOcean Serial Protocol (ESP) port configuration.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EspPortConfiguration extends Configuration
{

  // Enums ----------------------------------------------------------------------------------------

  /**
   * Communication layer.
   */
  public enum CommLayer
  {
    RXTX,
    PAD
  }

  /**
   * EnOcean serial protocol version.
   */
  public enum SerialProtocol
  {
    /**
     * EnOcean serial protocol version 2.
     */
    ESP2,

    /**
     * EnOcean serial protocol version 3.
     */
    ESP3
  }

  // Instance Fields ------------------------------------------------------------------------------

  /**
   * COM port identifier
   */
  private String comPort = null;

  /**
   * Communication layer.
   */
  private CommLayer commLayer = null;

  /**
   * EnOcean serial protocol version.
   */
  private SerialProtocol serialProtocol = null;


  // Object Overrides -----------------------------------------------------------------------------

  /**
   * Tests device ID object equality based on device ID value.
   *
   * @param   o   device ID object to compare to
   *
   * @return  true if equals, false otherwise
   */
  @Override public boolean equals(Object o)
  {
    if(o == null)
      return false;

    if(!o.getClass().equals(this.getClass()))
      return false;

    EspPortConfiguration config = (EspPortConfiguration)o;

    return (comPort == null ? config.getComPort() == null : comPort.equals(config.getComPort())) &&
           (commLayer == config.getCommLayer()) &&
           (serialProtocol == config.getSerialProtocol());
  }

  /**
   * {@inheritDoc}
   */
  @Override public int hashCode()
  {
    int result = 17;

    result = 31 * result + (comPort == null ? 0 : comPort.hashCode());
    result = 31 * result + (commLayer == null ? 0 : commLayer.hashCode());
    result = 31 * result + (serialProtocol == null ? 0 : serialProtocol.hashCode());

    return result;
  }


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Sets COM port identifier
   *
   * @param comPort  COM port identifier
   */
  public void setComPort(String comPort)
  {
    this.comPort = comPort;
  }

  /**
   * Returns COM port identifier
   *
   * @return  COM port identifier
   */
  public String getComPort()
  {
    return comPort;
  }

  /**
   * Sets communication layer.
   */
  public void setCommLayer(CommLayer commLayer)
  {
    this.commLayer = commLayer;
  }

  /**
   * Returns communication layer
   *
   * @return  the communication layer
   */
  public CommLayer getCommLayer()
  {
    return commLayer;
  }

  /**
   * Sets EnOcean serial protocol version.
   *
   * @param protocol  EnOcean serial protocol version.
   */
  public void setSerialProtocol(SerialProtocol protocol)
  {
    this.serialProtocol = protocol;
  }

  /**
   * Returns EnOcean serial protocol version.
   *
   * @return  EnOcean serial protocol version.
   */
  public SerialProtocol getSerialProtocol()
  {
    return serialProtocol;
  }
}
