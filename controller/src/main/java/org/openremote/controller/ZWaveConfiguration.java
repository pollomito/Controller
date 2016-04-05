/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2014, OpenRemote Inc.
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
 * Class that is used to bring together Z-Wave serial port configuration parameters from
 * the properties file zwave.properties and the deployed controller configuration file
 * controller.xml.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class ZWaveConfiguration extends Configuration
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Name of the Z-Wave property that is used to configure the name of the serial port.
   */
  public static final String ZWAVE_COM_PORT = "zwave.comPort";

  /**
   * Name of the Z-Wave property that configures the communication layer to be used [RXTX, PAD].
   */
  public static final String ZWAVE_COMM_LAYER = "zwave.commLayer";

  /**
   * Name of the Z-Wave property that configures the host IP address used by the
   * 'Port Abstraction Daemon' (PAD) to listen for incoming connections.
   */
  public static final String ZWAVE_PAD_HOST = "zwave.pad.host";

  /**
   * Name of the Z-Wave property that configures the TCP port used by the
   * 'Port Abstraction Daemon' (PAD) to listen for incoming connections.
   */
  public static final String ZWAVE_PAD_PORT = "zwave.pad.port";

  /**
   * Name of the Z-Wave property that configures the Z-Wave security layer
   * network key (128 bit).
   */
  public static final String ZWAVE_NETWORK_KEY = "zwave.networkKey";


  // Class Members --------------------------------------------------------------------------------

  /**
   * Updates the Z-Wave configuration with Z-Wave configuration settings from the designer
   * (controller.xml) and returns the updated Z-Wave configuration.
   *
   * @return  updated Z-Wave configuration
   */
  public static ZWaveConfiguration readXML()
  {
    ZWaveConfiguration config = ServiceContext.getZWaveConfiguration();

    return (ZWaveConfiguration)Configuration.updateWithControllerXMLConfiguration(config);
  }


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Serial port name.
   */
  private String comPort;

  /**
   * Communication layer [RXTX, PAD].
   */
  private String commLayer;

  /**
   * 'Port Abstraction Daemon' (PAD) host IP address.
   */
  private String padHost;

  /**
   * 'Port Abstraction Daemon' (PAD) TCP port.
   */
  private int padPort;

  /**
   * Z-Wave security network key (128 bit). <p>
   *
   * Each byte separated by a comma
   * (e.g. 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10)
   */
  private String networkKey;


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Returns the serial port name.
   *
   * @return  serial port
   */
  public String getComPort()
  {
    return preferAttrCustomValue(ZWAVE_COM_PORT, comPort);
  }

  /**
   * Sets the serial port name.
   *
   * @param comPort  serial port
   */
  public void setComPort(String comPort)
  {
    this.comPort = comPort;
  }

  /**
   * Returns the communication layer (PAD or RXTX).
   *
   * @return  communication layer
   */
  public String getCommLayer()
  {
    return preferAttrCustomValue(ZWAVE_COMM_LAYER, commLayer);
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
   * Returns the host (IP) address of the 'Port Abstraction Daemon' (PAD).
   *
   * @return  host (IP) address.
   */
  public String getPadHost()
  {
    return preferAttrCustomValue(ZWAVE_PAD_HOST, padHost);
  }

  /**
   * Sets the host (IP) address of the 'Port Abstraction Daemon' (PAD).
   *
   * @param host  host (IP) address.
   */
  public void setPadHost(String host)
  {
    this.padHost = host;
  }

  /**
   * Returns the TCP port of the 'Port Abstraction Daemon' (PAD).
   *
   * @return  TCP port
   */
  public int getPadPort()
  {
    return preferAttrCustomValue(ZWAVE_PAD_PORT, padPort);
  }

  /**
   * Sets the TCP port of the 'Port Abstraction Daemon' (PAD).
   *
   * @param port  TCP port.
   */
  public void setPadPort(int port)
  {
    this.padPort = port;
  }

  /**
   * Returns the Z-Wave network key (128 bit) as a string. <p>
   *
   * Each byte separated by a comma
   * (e.g. 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10)
   *
   * @return  the Z-Wave network key
   */
  public String getNetworkKey()
  {
    return networkKey;
  }

  /**
   * Sets the Z-Wave network key.
   *
   * @param networkKey  Z-Wave network key (128 bit). Each byte is separated by a comma
   *                    (e.g. 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08,
   *                          0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10)
   */
  public void setNetworkKey(String networkKey)
  {
    this.networkKey = networkKey;
  }
}
