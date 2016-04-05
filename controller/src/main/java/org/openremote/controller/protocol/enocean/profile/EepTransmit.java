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
package org.openremote.controller.protocol.enocean.profile;

import org.openremote.controller.protocol.enocean.ConfigurationException;
import org.openremote.controller.protocol.enocean.ConnectionException;
import org.openremote.controller.protocol.enocean.EnOceanGateway;
import org.openremote.controller.protocol.enocean.RadioInterface;

/**
 * Interface for EnOcean equipment profile (EEP) implementations acting as a transmitter
 * of radio telegrams.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public interface EepTransmit extends Eep
{
  /**
   * Creates a radio telegram with payload data from the EnOcean equipment profile (EEP)
   * instance and sends the radio telegram.
   *
   * @param radioInterface  radio interface used to send radio telegram
   *
   * @throws ConfigurationException
   *           if radio telegram cannot be sent because of a connection configuration error
   *
   * @throws ConnectionException
   *           if a connection error occurred while sending the radio telegram
   */
  void send(RadioInterface radioInterface) throws ConfigurationException, ConnectionException;
}
