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

import org.openremote.controller.protocol.enocean.ConnectionException;

/**
 * Request packets like radio and command packets sent to EnOcean module are followed
 * by a response packet from the EnOcean module. <p>
 *
 * With this interface it's possible to send a request and get the response afterwards.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public interface Esp2Request
{
  /**
   * Sends request packet to the EnOcean module.
   *
   * @param  processor packet processor
   *
   * @return response packet
   *
   * @throws ConnectionException
   *           if a connection error occurred for any reason
   *
   * @throws InterruptedException
   *           if interrupted while waiting for response
   */
  Esp2ResponsePacket send(EspProcessor<Esp2Packet> processor) throws ConnectionException, InterruptedException;

  /**
   * Returns return code.
   *
   * @return return code
   */
  Esp2ResponsePacket.ReturnCode getReturnCode();

  /**
   * Returns response.
   *
   * @return response packet, null if response packet has not been received
   */
  Esp2ResponsePacket getResponse();
}
