/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2013, OpenRemote Inc.
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
package org.openremote.controller.protocol.port;

import java.net.InetSocketAddress;

/**
 * A message used by <code>DatagramSocketPort</code>.
 * 
 * @see DatagramSocketPort
 * @see Message
 *
 * @author Olivier Gandit
 */
public class DatagramSocketMessage extends Message
{
  private InetSocketAddress destAddr;

  /**
   * Constructs a new UDP message.
   *
   * @param destAddr
   *           destination address
   * @param content
   *           message content
   */
  public DatagramSocketMessage(InetSocketAddress destAddr, byte[] content)
  {
    super(content);
    this.destAddr = destAddr;
  }

  /**
   * Returns message destination address
   *
   * @return Destination address as a <code>InetSocketAddress</code>.
   */
  public InetSocketAddress getDestAddr()
  {
    return this.destAddr;
  }
}
