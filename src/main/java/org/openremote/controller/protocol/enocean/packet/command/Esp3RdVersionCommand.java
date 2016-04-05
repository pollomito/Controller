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
 * Represents the CO_RD_VERSION command to read EnOcean module information (SW version,
 * chip ID, ..) as defined in EnOcean Serial Protocol 3 (ESP3) specification chapter
 * 1.11.5: Code 03: CO_RD_VERSION. <p>
 *
 * @see Esp3RdVersionResponse
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp3RdVersionCommand extends AbstractEsp3Command
{

  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new read version command instance.
   */
  public Esp3RdVersionCommand()
  {
    super(CommandCode.CO_RD_VERSION);
  }


  // AbstractEsp3RequestPacket Overrides ----------------------------------------------------------

  /**
   * Creates a read version response and returns it.
   *
   * @see Esp3RdVersionResponse
   *
   *
   * @param  response  response packet
   *
   * @return new read version response instance
   *
   * @throws org.openremote.controller.protocol.enocean.EspException
   *           if the generic response packet is not a valid read version response
   */
  @Override public Esp3RdVersionResponse createResponseFromPacket(Esp3Packet response)
      throws EspException
  {
    if(response == null)
    {
      throw new IllegalArgumentException("null response packet");
    }

    Esp3RdVersionResponse retResponse = null;

    if(isError(response))
    {
      retResponse = createResponseWithError(response);
    }
    else
    {
      retResponse = new Esp3RdVersionResponse(response.getData());
    }

    return retResponse;
  }

  /**
   * {@inheritDoc}
   */
  @Override public Esp3RdVersionResponse getResponse()
  {
    return (Esp3RdVersionResponse)response;
  }


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Returns the APP version.
   *
   * @return APP version if command has been successfully sent, null otherwise
   */
  public Esp3RdVersionResponse.Version getAppVersion()
  {
    if(response == null)
    {
      return null;
    }

    return ((Esp3RdVersionResponse)response).getAppVersion();
  }

  /**
   * Returns the API version.
   *
   * @return API version if command has been successfully sent, null otherwise
   */
  public Esp3RdVersionResponse.Version getApiVersion()
  {
    if(response == null)
    {
      return null;
    }

    return ((Esp3RdVersionResponse)response).getApiVersion();
  }

  /**
   * Returns the chip ID.
   *
   * @return chip ID if command has been successfully sent, null otherwise
   */
  public DeviceID getChipID()
  {
    if(response == null)
    {
      return null;
    }

    return ((Esp3RdVersionResponse)response).getChipID();
  }

  /**
   * Returns the application description.
   *
   * @return application description if command has been successfully sent, null otherwise
   */
  public String getAppDescription()
  {
    if(response == null)
    {
      return null;
    }

    return ((Esp3RdVersionResponse)response).getAppDescription();
  }


  // Private Instance Methods ---------------------------------------------------------------------

  private boolean isError(Esp3Packet response)
  {
    return response.getData().length == Esp3ResponsePacket.ESP3_RESPONSE_RETURN_CODE_LENGTH;
  }

  private Esp3RdVersionResponse createResponseWithError(Esp3Packet response) throws EspException
  {
    byte[] dataBytes = new byte[Esp3RdVersionResponse.ESP3_RESPONSE_RD_VERSION_DATA_LENGTH];

    // Set error code
    dataBytes[Esp3ResponsePacket.ESP3_RESPONSE_RETURN_CODE_INDEX]
        = response.getData()[Esp3ResponsePacket.ESP3_RESPONSE_RETURN_CODE_INDEX];

    return new Esp3RdVersionResponse(dataBytes);
  }
}
