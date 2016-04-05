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
package org.openremote.controller.protocol.enocean;

import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.protocol.EventListener;
import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram;
import org.openremote.controller.protocol.enocean.profile.EepReceive;

/**
 * Command for processing data received from EnOcean sensors. Each instance is associated
 * with an EnOcean equipment profile (EEP) specified for sensors. This class implements
 * the {@link EventListener} interface and therefore acts as an entry point in
 * controller/protocol SPI.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class ReceiveCommand extends EnOceanCommand implements EventListener, RadioTelegramListener
{

  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * EnOcean equipment profile (EEP).
   */
  private EepReceive eep;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new receiver command instance with given EnOcean equipment profile (EEP),
   * EnOcean device ID and radio interface.
   *
   * @param eep              EnOcean equipment profile (EEP) for processing received EnOcean
   *                         radio telegrams
   *
   * @param deviceID         EnOcean device ID for filtering EnOcean radio telegrams
   *
   * @param radioInterface   interface for receiving EnOcean radio telegrams
   */
  public ReceiveCommand(EepReceive eep, DeviceID deviceID, RadioInterface radioInterface)
  {
    super(radioInterface, deviceID);

    this.eep = eep;
  }


  // Object Overrides -----------------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public String toString()
  {
    return "Command (ID = '"+ getDeviceID() +"', " + eep + ")";
  }


  // Implements EventListener ---------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public void setSensor(Sensor sensor)
  {
    registerSensor(sensor, this);
  }

  /**
   * {@inheritDoc}
   */
  /*@Override*/ public void stop(Sensor sensor)
  {
    unregisterSensor(sensor, this);
  }


  // Implements RadioTelegramListener -------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public void radioTelegramReceived(EspRadioTelegram telegram)
  {
    update(eep, telegram);
  }
}
