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
import org.openremote.controller.protocol.enocean.packet.Esp3Packet;
import org.openremote.controller.protocol.enocean.packet.Esp3ResponsePacket;

/**
 * Represents the CO_RD_IDBASE command to read the base ID of an EnOcean module as defined
 * in EnOcean Serial Protocol 3 (ESP3) specification chapter 1.11.10: Code 08: CO_RD_IDBASE. <p>
 *
 * Each EnOcean module acting as a gateway has a base ID and an ID range starting with the
 * base ID.
 *
 * @see Esp3RdIDBaseResponse
 * @see org.openremote.controller.protocol.enocean.DeviceID
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp3RdIDBaseCommand extends AbstractEsp3Command
{

  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new read ID base command instance.
   */
  public Esp3RdIDBaseCommand()
  {
    super(CommandCode.CO_RD_IDBASE);
  }

  // AbstractEsp3RequestPacket Overrides ----------------------------------------------------------

  /**
   * Creates a read base ID response and returns it.
   *
   * @see Esp3RdIDBaseResponse
   *
   *
   * @param  response  response packet
   *
   * @return new base ID response instance
   *
   * @throws EspException
   *           if the response packet is an invalid or unknown response
   */
  @Override public Esp3RdIDBaseResponse createResponseFromPacket(Esp3Packet response)
      throws EspException
  {
    if(response == null)
    {
      throw new IllegalArgumentException("null response packet");
    }

    Esp3RdIDBaseResponse retResponse = null;

    if(isError(response))
    {
      retResponse = createResponseWithError(response);
    }
    else
    {
      retResponse = new Esp3RdIDBaseResponse(response.getData());
    }

    return retResponse;
  }

  /**
   * {@inheritDoc}
   */
  @Override public Esp3RdIDBaseResponse getResponse()
  {
     return (Esp3RdIDBaseResponse)response;
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

    return ((Esp3RdIDBaseResponse)response).getBaseID();
  }


  // Private Instance Methods ---------------------------------------------------------------------

  private boolean isError(Esp3Packet response)
  {
    return response.getData().length == Esp3ResponsePacket.ESP3_RESPONSE_RETURN_CODE_LENGTH;
  }

  private Esp3RdIDBaseResponse createResponseWithError(Esp3Packet response) throws EspException
  {
    byte[] dataBytes = new byte[Esp3RdIDBaseResponse.ESP3_RESPONSE_RD_IDBASE_DATA_LENGTH];

    dataBytes[Esp3ResponsePacket.ESP3_RESPONSE_RETURN_CODE_INDEX]
        = response.getData()[Esp3ResponsePacket.ESP3_RESPONSE_RETURN_CODE_INDEX];

    return new Esp3RdIDBaseResponse(dataBytes);
  }
}
