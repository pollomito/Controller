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

import org.openremote.controller.protocol.enocean.packet.Esp2Packet;
import org.openremote.controller.protocol.enocean.packet.Esp2ProcessorListener;
import org.openremote.controller.protocol.enocean.packet.Esp2Request;
import org.openremote.controller.protocol.enocean.packet.EspProcessor;
import org.openremote.controller.protocol.enocean.packet.command.Esp2RdIDBaseCommand;
import org.openremote.controller.protocol.enocean.packet.radio.Esp21BSTelegram;
import org.openremote.controller.protocol.enocean.packet.radio.Esp24BSTelegram;
import org.openremote.controller.protocol.enocean.packet.radio.Esp2RPSTelegram;
import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram;
import org.openremote.controller.utils.Logger;

/**
 * EnOcean connection implementation based on the EnOcean serial protocol 2 (ESP2).
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp2Connection extends AbstractConnection<Esp2Packet, Esp2Request> implements Esp2ProcessorListener
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs an EnOcean serial protocol 2 (ESP2) based connection with given
   * serial protocol processor and radio telegram listener.
   *
   * @param processor   processor for handling the EnOcean serial protocol 2 (ESP2)
   *
   * @param listener    listener which will be notified of received radio telegrams
   */
  public Esp2Connection(EspProcessor<Esp2Packet> processor, RadioTelegramListener listener)
  {
    super(processor, listener);
  }


  // Implements AbstractConnection ----------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  protected DeviceID readBaseID(EspProcessor<Esp2Packet> processor) throws InterruptedException, ConnectionException
  {
    Esp2RdIDBaseCommand readBaseIDCmd = new Esp2RdIDBaseCommand();
    readBaseIDCmd.send(processor);

    return readBaseIDCmd.getBaseID();
  }

  /**
   * {@inheritDoc}
   */
  protected Esp2Request createRadioTelegram(EspRadioTelegram.RORG rorg, DeviceID deviceID, byte[] payload, byte statusByte)
  {
    Esp2Request radioTelegram = null;

    if(rorg == EspRadioTelegram.RORG.RPS ||
       rorg == EspRadioTelegram.RORG.RPS_ESP2)
    {
      radioTelegram = new Esp2RPSTelegram(deviceID, payload[0], statusByte);
    }

    else if(rorg == EspRadioTelegram.RORG.BS1 ||
            rorg == EspRadioTelegram.RORG.BS1_ESP2)
    {
      radioTelegram = new Esp21BSTelegram(deviceID, payload[0], statusByte);
    }

    else if(rorg == EspRadioTelegram.RORG.BS4 ||
            rorg == EspRadioTelegram.RORG.BS4_ESP2)
    {
      radioTelegram = new Esp24BSTelegram(deviceID, payload, statusByte);
    }

    else
    {
      throw new RuntimeException("Unhandled radio telegram type '" + rorg + "'.");
    }

    return radioTelegram;
  }

  /**
   * {@inheritDoc}
   */
  protected void sendRadioTelegram(Esp2Request radioTelegram, EspProcessor<Esp2Packet> processor)
      throws InterruptedException, ConnectionException
  {
    radioTelegram.send(processor);
  }


  // Implements Esp2ProcessorListener -------------------------------------------------------------

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
