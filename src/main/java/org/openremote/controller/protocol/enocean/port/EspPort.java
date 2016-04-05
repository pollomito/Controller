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

/**
 * The EnOcean Serial Protocol versions (ESP2, ESP3) have different COM port communications settings.
 * In addition there are TCP/IP EnOcean gateways which act as remote serial port. <p>
 *
 * This interface abstracts away the details of the different EnOcean Serial Protocol (ESP) communication settings
 * and the underlying transport and provides an uniform access to the different communication port implementations.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public interface EspPort
{

  // Interface Definition -------------------------------------------------------------------------

  /**
   * Configures the port and creates a port connection.
   *
   * @throws org.openremote.controller.protocol.enocean.ConnectionException
   *           if port connection creation failed
   *
   * @throws org.openremote.controller.protocol.enocean.ConfigurationException
   *           if the port connection cannot be created because of an invalid configuration
   */
  void start() throws ConnectionException, ConfigurationException;

  /**
   * Terminates port connection.
   *
   * @throws ConnectionException
   *           if port cannot be terminated for any reason
   *
   */
  void stop() throws ConnectionException;

  /**
   * Indicates if port has been started.
   *
   * @return  true if started, false otherwise
   */
  boolean isStarted();

  /**
   *  Sends data to the EnOcean interface.
   *
   * @param   data  data to be sent
   *
   * @throws  ConnectionException  if data cannot be sent for any reason
   */
  void send(byte[] data) throws ConnectionException;

  /**
   * Reads data from the EnOcean interface.
   *
   * @return  received data
   *
   * @throws  ConnectionException  if data cannot be received for any reason
   */
  byte[] receive() throws ConnectionException;
}
