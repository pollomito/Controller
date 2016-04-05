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
package org.openremote.controller.protocol.enocean.packet;


import org.openremote.controller.protocol.enocean.ConfigurationException;
import org.openremote.controller.protocol.enocean.ConnectionException;

/**
 * Interface for sending EnOcean Serial Protocol (ESP) request and response packets
 * to the EnOcean module. <p>
 *
 * Request packets are sent bidirectionally from the controller to the EnOcean module
 * and vice versa. All request packets are followed by a response packet from the
 * opposite side (see EnOcean Serial Protocol 3.0 Specification V1.17
 * chapter 1.6.3: Direction of packet types). <p>
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public interface EspProcessor<T extends EspPacket>
{
  /**
   * Starts ESP processor and the underlying port.
   *
   * @see org.openremote.controller.protocol.enocean.port.EspPort
   *
   *
   * @throws org.openremote.controller.protocol.enocean.ConfigurationException
   *           if starting the underlying port failed because of a configuration error
   *
   * @throws org.openremote.controller.protocol.enocean.ConnectionException
   *           if a connection error occurred for any reason
   */
  void start() throws ConfigurationException, ConnectionException;

  /**
   * Stops ESP processor and the underlying port.
   *
   * @see org.openremote.controller.protocol.enocean.port.EspPort
   *
   *
   * @throws ConnectionException
   *           if stopping the underlying port failed because of a connection error
   */
  void stop() throws ConnectionException;

  /**
   * Sends a request packet to the EnOcean module and returns the response
   * packet received from the EnOcean module.
   *
   * @param  packet  request packet to be sent to EnOcean module
   *
   * @return response packet received from EnOcean module
   *
   * @throws ConnectionException
   *           if the underlying ports indicates a connection error
   *
   * @throws InterruptedException
   *           if interrupted while waiting for a response packet
   *
   */
  T sendRequest(T packet) throws ConnectionException, InterruptedException;

  /**
   * Sends a response packet to the EnOcean module.
   *
   * @param  packet response packet to be sent
   *
   * @throws ConnectionException
   *           if a connection error occurred for any reason
   */
  void sendResponse(T packet) throws ConnectionException;
}
