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

/**
 * A kind of tagging interface to indicate that the EnOcean equipment profile (EEP)
 * implementation may receive and/or transmit EnOcean radio telegrams.
 *
 * @see EepReceive
 * @see EepTransmit
 * @see EepTransceive
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public interface Eep
{
  /**
   * Returns the EnOcean equipment profile (EEP) type.
   *
   * @return the EnOcean equipment profile (EEP) type.
   */
  EepType getType();
}
