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
package org.openremote.controller.protocol.enocean.packet;

import org.openremote.controller.protocol.enocean.ConnectionException;
import org.openremote.controller.protocol.enocean.EspException;

/**
 * A common superclass for ESP3 packets which are sent to the EnOcean module and trigger
 * a response packet which is sent back from the EnOcean module to the controller. <p>
 *
 * This class provides a standard implementation for handling a request/response cycle. It
 * implements the interface {@link Esp3Request} for sending requests and returning
 * the received response.
 *
 * Subclasses should override the {@link #createResponseFromPacket(Esp3Packet)} method if
 * the request type requires a specialized response type.
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public abstract class AbstractEsp3RequestPacket extends Esp3Packet implements Esp3Request
{

  // Protected Instance Fields --------------------------------------------------------------------

  /**
   * Response packet.
   */
  protected Esp3ResponsePacket response = null;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new request packet instance with given packet type and packet data.
   *
   * @param packetType    packet type
   * @param data          data group
   * @param optionalData  optional data group
   */
  public AbstractEsp3RequestPacket(Esp3PacketHeader.PacketType packetType,
      byte[] data, byte[] optionalData)
  {
    super(packetType, data, optionalData);
  }


  // Implements Esp3Request ---------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public Esp3ResponsePacket send(EspProcessor<Esp3Packet> processor)
      throws ConnectionException, InterruptedException
  {
    if(processor == null)
    {
      throw new IllegalArgumentException("null ESP3 processor");
    }

    response = null;

    Esp3Packet responsePacket = null;

    responsePacket = processor.sendRequest(this);

    if(responsePacket != null)
    {
      response = createResponseFromPacket(responsePacket);

      checkIfSendWasSuccessful(response);
    }

    return response;
  }

  /**
   * {@inheritDoc}
   */
  @Override public Esp3ResponsePacket.ReturnCode getReturnCode()
  {
    if(response == null)
    {
      return Esp3ResponsePacket.ReturnCode.RET_CODE_NOT_SET;
    }

    return response.getReturnCode();
  }

  /**
   * {@inheritDoc}
   */
  @Override public Esp3ResponsePacket getResponse()
  {
    return response;
  }

  /**
   * Creates and returns a new generic response instance.
   *
   * Subclasses should override this method if the request type requires a
   * specialized response type.
   *
   * @param packet  response packet
   *
   * @return new response instance
   *
   * @throws EspException
   *           if response is invalid or unknown
   */
  protected Esp3ResponsePacket createResponseFromPacket(Esp3Packet packet) throws EspException
  {
    Esp3ResponsePacket retResponse = null;

    if(packet == null)
    {
      throw new IllegalArgumentException("null response packet");
    }

    if(packet.getPacketType() != Esp3PacketHeader.PacketType.RESPONSE)
    {
      throw new IllegalArgumentException("ESP3 packet is not a response packet.");
    }

    retResponse = new Esp3ResponsePacket(packet.getData(), packet.getOptionalData());

    return retResponse;
  }


  // Private Instance Methods -------------------------------------------------------------

  private void checkIfSendWasSuccessful(Esp3ResponsePacket response) throws EspException
  {
    if(response == null)
    {
      throw new IllegalArgumentException("null response");
    }

    if(response.getReturnCode() != Esp3ResponsePacket.ReturnCode.RET_OK)
    {
      EspException.ErrorCode errorCode;

      try
      {
        errorCode = EspException.ErrorCode.resolve(
            response.getReturnCode().getValue()
        );
      }

      catch (EspException.UnknownErrorCodeException e)
      {
        errorCode = EspException.ErrorCode.RESP_ERROR;
      }

      throw new EspException(errorCode, "Failed to send request.");
    }
  }
}
