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

import org.openremote.controller.command.Command;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram;
import org.openremote.controller.protocol.enocean.profile.Eep;
import org.openremote.controller.protocol.enocean.profile.EepReceive;
import org.openremote.controller.protocol.enocean.profile.EepTransceive;
import org.openremote.controller.protocol.enocean.profile.EepTransmit;
import org.openremote.controller.utils.Logger;

import java.util.HashSet;
import java.util.Set;

/**
 * This class is an abstract superclass for EnOcean transmit/receive commands.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
abstract public class EnOceanCommand implements Command
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  /**
   * Factory method for creating EnOcean command instances {@link ReceiveCommand} and
   * {@link TransceiveCommand}.
   *
   * @param eep             EnOcean equipment profile (EEP) associated with this command
   *
   * @param deviceID        EnOcean device ID
   *
   * @param radioInterface  interface for sending and receiving EnOcean radio telegrams
   *
   * @return  new EnOcean command instance
   */
  static EnOceanCommand createCommand(Eep eep, DeviceID deviceID, RadioInterface radioInterface)
  {
    EnOceanCommand cmd = null;

    if(eep instanceof EepTransceive)
    {
      cmd = new TransceiveCommand((EepTransceive)eep, deviceID, radioInterface);
    }

    else if(eep instanceof EepReceive)
    {
      cmd = new ReceiveCommand((EepReceive)eep, deviceID, radioInterface);
    }

    else if(eep instanceof EepTransmit)
    {
      cmd = new TransmitCommand((EepTransmit)eep, deviceID, radioInterface);
    }

    return cmd;
  }

  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Interface for sending and receiving EnOcean radio telegrams.
   */
  private RadioInterface radioInterface;

  /**
   * EnOcean device ID.
   */
  private DeviceID deviceID;

  /**
   * All sensors linked to this command.
   */
  private Set<Sensor> sensors = new HashSet<Sensor>();


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs an EnOcean command with given radio interface and EnOcean device ID.
   *
   * @param radioInterface  interface for sending and receiving EnOcean radio telegrams.
   *
   * @param deviceID        EnOcean device ID
   */
  public EnOceanCommand(RadioInterface radioInterface, DeviceID deviceID)
  {
    this.radioInterface = radioInterface;
    this.deviceID = deviceID;
  }


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Returns the EnOcean device ID.
   *
   * @return  EnOcean device ID
   */
  public DeviceID getDeviceID()
  {
    return deviceID;
  }

  // Protected Instance Methods -------------------------------------------------------------------

  /**
   * Relay command which is based on the given EnOcean equipment profile (EEP) instance
   * to the radio interface.
   *
   * @param eep  EnOcean equipment profile (EEP)
   */
  protected void send(EepTransmit eep)
  {
    try
    {
      eep.send(radioInterface);
    }

    catch (ConfigurationException e)
    {
      log.error("Failed to send ''{0}'': {1}", this, e.getMessage());
    }

    catch (ConnectionException e)
    {
      log.error("Failed to send ''{0}'': {1}", this, e.getMessage());
    }
  }

  /**
   * Stores the sensor in the sensor list and registers an EnOcean radio
   * telegram listener for updating the sensor value.
   *
   * @param sensor     the sensor
   *
   * @param listener   EnOcean radio telegram listener
   */
  protected synchronized void registerSensor(Sensor sensor, RadioTelegramListener listener)
  {
    sensors.add(sensor);
    radioInterface.addRadioListener(deviceID, listener);
  }

  /**
   * Removes sensor from the sensor list and unregisters EnOcean radio
   * telegram listener.
   *
   * @param sensor     the sensor
   *
   * @param listener   EnOcean radio telegram listener
   */
  protected synchronized void unregisterSensor(Sensor sensor, RadioTelegramListener listener)
  {
    sensors.remove(sensor);
    radioInterface.removeRadioListener(deviceID, listener);
  }

  /**
   * Updates the EnOcean equipment profile (EEP) data and all linked
   * sensors afterwards.
   *
   * @param eep            the EnOcean equipment profile (EEP) instance to be updated
   *
   * @param radioTelegram  the received EnOcean radio telegram
   */
  protected synchronized void update(EepReceive eep, EspRadioTelegram radioTelegram)
  {
    eep.update(radioTelegram);

    for(Sensor sensor : sensors)
    {
      try
      {
        eep.updateSensor(sensor);
      }
      catch (ConfigurationException e)
      {
        log.error("Failed to update sensor ''{0}'': {1}", sensor, e.getMessage());
      }
    }
  }
}
