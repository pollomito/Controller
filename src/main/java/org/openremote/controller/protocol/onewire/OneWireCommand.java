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
package org.openremote.controller.protocol.onewire;

import java.io.IOException;

import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.protocol.ReadCommand;
import org.openremote.controller.utils.Logger;
import org.owfs.jowfsclient.Enums.OwBusReturn;
import org.owfs.jowfsclient.Enums.OwDeviceDisplayFormat;
import org.owfs.jowfsclient.Enums.OwPersistence;
import org.owfs.jowfsclient.Enums.OwTemperatureScale;
import org.owfs.jowfsclient.OwfsConnection;
import org.owfs.jowfsclient.OwfsConnectionConfig;
import org.owfs.jowfsclient.OwfsConnectionFactory;
import org.owfs.jowfsclient.OwfsException;

/**
 * 1-wire protocol implementation for OpenRemote. Program accesses 1-wire network (owserver) via
 * library jowfsclient.
 *
 * Parameters used:
 *
 * hostname        hostname of owserver
 * port            port where owserver is running, by default it is 4304
 * deviceAddress   address of the 1-wire device, such as /1F.E9E803000000/main/28.25E9E3010000
 * filename        or sensor attribute - is filename in owfs that holds values, such as
 *                 "temperature", "temperature9", "humidity" or similar
 * pollingIntercal time interval in seconds between two calls to owserver.
 *
 * @author <a href="mailto:jmisura@gmail.com">Jaroslav Misura</a>
 * @author Marcus
 * @author <a href="matilto:eric@openremote.org>Eric Bariaux</a>
 */
public class OneWireCommand extends ReadCommand implements ExecutableCommand
{

  // Class Members --------------------------------------------------------------------------------

  private final static Logger logger = Logger.getLogger(OneWireCommandBuilder.ONEWIRE_PROTOCOL_LOG_CATEGORY);


  // Instance Fields ------------------------------------------------------------------------------

  private String hostname;
  private int port = 4304;
  private String deviceAddress;
  private String filename;
  private int pollingInterval;
  private TemperatureScale tempScale;
  private String data;

  // Constructors ---------------------------------------------------------------------------------

  public OneWireCommand(String hostname, int port, String deviceAddress, String filename, int pollingInterval, TemperatureScale tempScale, String data)
  {
    this.hostname = hostname;
    this.port = port;
    this.deviceAddress = deviceAddress;
    this.filename = filename;
    this.pollingInterval = pollingInterval;
    this.tempScale = tempScale;
    this.data = data;

    logger.debug("OneWireCommand created with values hostname=" + hostname +
               "; port=" + port + "; deviceAddress=" + deviceAddress + "; filename=" +filename +
               "; pollingInterval=" + pollingInterval);
  }


  // Implements ReadCommand ---------------------------------------------------------------------

  
  @Override
  public int getPollingInterval() {
    return pollingInterval;
  }

  /**
   * Access value from 1-wire sensor.
   *
   * Values from 1-wire sensors are just passed as string the way they are into sensor.update(). The sensor
   * has to map the result to it's internal data type.
   *
   * @return string value independent from sensor type
   */
  @Override
  public String read(Sensor sensor)
  {
    logger.debug("1-Wire sensor " + deviceAddress + "/" + filename + " value is going to be read.");
    
    OwfsConnectionConfig config = new OwfsConnectionConfig(hostname, port);
    config.setDeviceDisplayFormat(OwDeviceDisplayFormat.F_DOT_I);
    config.setBusReturn(OwBusReturn.ON);
    config.setPersistence(OwPersistence.ON);

    switch (tempScale) {
      case Celsius:
         config.setTemperatureScale(OwTemperatureScale.CELSIUS);
        break;
      case Fahrenheit:
         config.setTemperatureScale(OwTemperatureScale.FAHRENHEIT);
         break;
      case Kelvin:
         config.setTemperatureScale(OwTemperatureScale.KELVIN);
         break;
      case Rankine:
         config.setTemperatureScale(OwTemperatureScale.RANKINE);
         break;
    }
    
    String value = null;

    OwfsConnection client = OwfsConnectionFactory.newOwfsClient(config);

    logger.debug("Client created, before call");

    try
    {
      value = client.read(deviceAddress+"/"+filename);
    }
    catch (Exception e)
    {
      logger.error("Unable to read from OWSERVER.", e);
      value = null;
    }
    logger.debug("After client call, value = '"+value+"'");

    if (null == value)
    {
      return "N/A";
    }

    value = value.trim();
    logger.debug("Sensor " + deviceAddress + "/" + filename + " returns value '"+ value + "'");
    return value;
  }


  // Implements ExecutableCommand -----------------------------------------------------------------

  /**
   * Takes the given 'data' and writes it to the given attribute
   *
   */
  public void send()
  {

    logger.debug("1-Wire sensor " + deviceAddress + "/" + filename + " value is going to be changed to: '" + data);

    OwfsConnectionConfig config = new OwfsConnectionConfig(hostname, port);
    config.setDeviceDisplayFormat(OwDeviceDisplayFormat.F_DOT_I);
    config.setBusReturn(OwBusReturn.ON);
    config.setPersistence(OwPersistence.ON);
    OwfsConnection client = OwfsConnectionFactory.newOwfsClient(config);

    try
    {
      client.write(deviceAddress+"/"+filename, data);
    }
    catch (OwfsException e)
    {
      logger.error("OneWire error, unable to write to OWSERVER.", e);
    }
    catch (IOException e)
    {
      logger.error("OneWire IO error, unable to write to OWSERVER.", e);
    }
  }

}
