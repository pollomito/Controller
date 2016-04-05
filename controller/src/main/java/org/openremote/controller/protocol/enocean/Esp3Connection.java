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

import org.openremote.controller.protocol.enocean.packet.Esp3Packet;
import org.openremote.controller.protocol.enocean.packet.Esp3ProcessorListener;
import org.openremote.controller.protocol.enocean.packet.Esp3Request;
import org.openremote.controller.protocol.enocean.packet.EspProcessor;
import org.openremote.controller.protocol.enocean.packet.command.Esp3RdIDBaseCommand;
import org.openremote.controller.protocol.enocean.packet.radio.Esp31BSTelegram;
import org.openremote.controller.protocol.enocean.packet.radio.Esp34BSTelegram;
import org.openremote.controller.protocol.enocean.packet.radio.Esp3RPSTelegram;
import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram;
import org.openremote.controller.utils.Logger;

/**
 * EnOcean connection implementation based on the EnOcean serial protocol 3 (ESP3).
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp3Connection extends AbstractConnection<Esp3Packet, Esp3Request> implements Esp3ProcessorListener
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs an EnOcean serial protocol 3 (ESP3) based connection with given
   * serial protocol processor and radio telegram listener.
   *
   * @param processor   processor for handling the EnOcean serial protocol 3 (ESP3)
   *
   * @param listener    listener which will be notified of received radio telegrams
   */
  public Esp3Connection(EspProcessor<Esp3Packet> processor, RadioTelegramListener listener)
  {
    super(processor, listener);
  }


  // Implements AbstractConnection ----------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  protected DeviceID readBaseID(EspProcessor<Esp3Packet> processor) throws InterruptedException, ConnectionException
  {
    Esp3RdIDBaseCommand readBaseIDCmd = new Esp3RdIDBaseCommand();
    readBaseIDCmd.send(processor);

    return readBaseIDCmd.getBaseID();
  }

  /**
   * {@inheritDoc}
   */
  protected Esp3Request createRadioTelegram(EspRadioTelegram.RORG rorg, DeviceID deviceID, byte[] payload, byte statusByte)
  {
    Esp3Request radioTelegram = null;

    if(rorg == EspRadioTelegram.RORG.RPS ||
       rorg == EspRadioTelegram.RORG.RPS_ESP2)
    {
      radioTelegram = new Esp3RPSTelegram(deviceID, payload[0], statusByte);
    }

    else if(rorg == EspRadioTelegram.RORG.BS1 ||
            rorg == EspRadioTelegram.RORG.BS1_ESP2)
    {
      radioTelegram = new Esp31BSTelegram(deviceID, payload[0], statusByte);
    }

    else if(rorg == EspRadioTelegram.RORG.BS4 ||
            rorg == EspRadioTelegram.RORG.BS4_ESP2)
    {
      radioTelegram = new Esp34BSTelegram(deviceID, payload, statusByte);
    }

    else
    {
      throw new RuntimeException("Unhandled radio telegram type '" + rorg +"'.");
    }

    return radioTelegram;
  }

  /**
   * {@inheritDoc}
   */
  protected void sendRadioTelegram(Esp3Request radioTelegram, EspProcessor<Esp3Packet> processor)
      throws InterruptedException, ConnectionException
  {
    radioTelegram.send(processor);
  }


  // Implements Esp3ProcessorListener -------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public void radioTelegramReceived(EspRadioTelegram telegram)
  {
    if(radioListener != null)
    {
      radioListener.radioTelegramReceived(telegram);
    }
  }
}
