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

import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.protocol.enocean.ConfigurationException;
import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram;

/**
 * Interface for EnOcean equipment profile (EEP) implementations acting as a receiver
 * of radio telegrams.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public interface EepReceive extends Eep
{
  /**
   * Updates the state of an EnOcean equipment profile (EEP) instance
   * with the data of a received radio telegram.
   *
   * @param telegram  radio telegram
   */
  boolean update(EspRadioTelegram telegram);

  /**
   * Updates the sensor value with data from the internal state of an
   * EnOcean equipment profile (EEP) instance.
   *
   * @param sensor  the sensor
   */
  void updateSensor(Sensor sensor) throws ConfigurationException;
}
