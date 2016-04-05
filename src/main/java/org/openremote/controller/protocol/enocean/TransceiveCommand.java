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

import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.protocol.EventListener;
import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram;
import org.openremote.controller.protocol.enocean.profile.EepTransceive;
import org.openremote.controller.utils.Logger;

/**
 * Command for processing data received from EnOcean sensors or transmitting commands to EnOcean
 * actors respectively. Each instance is associated with an EnOcean equipment profile (EEP)
 * specified for EnOcean actors or bidirectional communication. This class implements the
 * {@link EventListener} and {@link ExecutableCommand} interfaces and therefore acts
 * as an entry point in controller/protocol SPI.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class TransceiveCommand extends EnOceanCommand implements ExecutableCommand, EventListener, RadioTelegramListener
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * EnOcean equipment profile (EEP).
   */
  private EepTransceive eep;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new transceiver command instance with given EnOcean equipment profile (EEP),
   * EnOcean device ID and radio interface.
   *
   * @param eep             EnOcean equipment profile (EEP) for processing received EnOcean
   *                        radio telegrams or creating transmit commands respectively.
   *
   * @param deviceID        EnOcean device ID for filtering received EnOcean radio telegrams or
   *                        identifying the originator of a sent radio telegram respectively.
   *
   * @param radioInterface  interface for receiving/transmitting radio telegrams
   */
  public TransceiveCommand(EepTransceive eep, DeviceID deviceID, RadioInterface radioInterface)
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


  // Implements ExecutableCommand -----------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public void send()
  {
    send(eep);
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
