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
import org.openremote.controller.protocol.enocean.profile.EepTransmit;
import org.openremote.controller.utils.Logger;

/**
 * Command for transmitting commands to EnOcean actors. Each instance is associated with
 * an EnOcean equipment profile (EEP) specified for EnOcean actors. This class implements
 * the {@link ExecutableCommand} interface and therefore acts as an entry point in
 * controller/protocol SPI.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class TransmitCommand extends EnOceanCommand implements ExecutableCommand
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
  private EepTransmit eep;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new transmit command instance with given EnOcean equipment profile (EEP),
   * EnOcean device ID and radio interface.
   *
   * @param eep             EnOcean equipment profile (EEP)
   *
   * @param deviceID        EnOcean device ID for identifying the originator of a
   *                        sent radio telegram.
   *
   * @param radioInterface  interface for transmitting radio telegrams
   */
  public TransmitCommand(EepTransmit eep, DeviceID deviceID, RadioInterface radioInterface)
  {
    super(radioInterface, deviceID);

    this.eep = eep;
  }


  // Implements ExecutableCommand -----------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public void send()
  {
    send(eep);
  }
}
