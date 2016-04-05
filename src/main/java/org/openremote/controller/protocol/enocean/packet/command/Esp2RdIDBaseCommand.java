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
package org.openremote.controller.protocol.enocean.packet.command;

import org.openremote.controller.protocol.enocean.DeviceID;
import org.openremote.controller.protocol.enocean.EspException;
import org.openremote.controller.protocol.enocean.packet.Esp2Packet;

/**
 * Represents the RD_IDBASE command to read the base ID of an EnOcean module as defined
 * in the TCM120 and TCM300 user manual. <p>
 *
 * Each EnOcean module acting as a gateway has a base ID and an ID range starting with the
 * base ID.
 *
 * @see Esp2RdIDBaseResponse
 * @see org.openremote.controller.protocol.enocean.DeviceID
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp2RdIDBaseCommand extends AbstractEsp2Command
{

  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new read ID base command instance.
   */
  public Esp2RdIDBaseCommand()
  {
    super(CommandCode.RD_BASEID);
  }

  // AbstractEsp2RequestPacket Overrides ----------------------------------------------------------

  /**
   * Creates a read base ID response and returns it.
   *
   * @see Esp2RdIDBaseResponse
   *
   *
   * @param  response  response packet
   *
   * @return new base ID response instance
   *
   * @throws EspException
   *           if the response packet is an invalid or unknown response
   */
  @Override public Esp2RdIDBaseResponse createResponseFromPacket(Esp2Packet response)
      throws EspException
  {
    if(response == null)
    {
      throw new IllegalArgumentException("null response packet");
    }

    Esp2RdIDBaseResponse retResponse = new Esp2RdIDBaseResponse(response.getData());

    return retResponse;
  }

  /**
   * {@inheritDoc}
   */
  @Override public Esp2RdIDBaseResponse getResponse()
  {
    return (Esp2RdIDBaseResponse)response;
  }


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Returns base ID.
   *
   * @return base ID after command has been successfully executed, otherwise null
   */
  public DeviceID getBaseID()
  {
    if(response == null)
    {
      return null;
    }

    return ((Esp2RdIDBaseResponse)response).getBaseID();
  }
}
