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

import org.openremote.controller.protocol.enocean.EspException;
import org.openremote.controller.utils.Strings;

/**
 * This class represents a ESP3 response packet as defined in EnOcean Serial Protocol 3 V1.17
 * specification chapter 1.8: Packet Type 2: RESPONSE.
 *
 * <pre>
 *                                     |--------- Data -------|- Optional Data -|
 *   +--------+------...------+--------+--------+-----...-----+-------...-------+--------+
 *   |  Sync  |    Header     |  CRC8  | Return |   Response  |    Response     |  CRC8  |
 *   |  Byte  |               | Header |  Code  |    Data     |      Data       |  Data  |
 *   +--------+------...------+--------+--------+-----...-----+-------...-------+--------+
 *     1 byte      4 bytes      1 byte   1 byte    n bytes          n bytes       1 byte
 *</pre>
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp3ResponsePacket extends Esp3Packet
{

  // Enums ----------------------------------------------------------------------------------------

  /**
   * ESP3 response packet return code as defined in EnOcean Serial Protocol 3 specification V1.17
   * chapter 1.8.2: List of Return Codes.
   */
  public enum ReturnCode
  {
    /**
     * Command has been successfully executed or radio telegram has been successfully
     * sent respectively.
     */
    RET_OK(0x00),

    /**
     * An error occurred.
     */
    RET_ERROR(0x01),

    /**
     * Functionality is not supported.
     */
    RET_NOT_SUPPORTED(0x02),

    /**
     * There was a wrong parameter in the command.
     */
    RET_WRONG_PARAM(0x03),

    /**
     * Example: memory access denied (code protected).
     */
    RET_OPERATION_DENIED(0x04),

    /**
     * Default return code to indicate that the return code has not been received from the
     * EnOcean module.
     */
    RET_CODE_NOT_SET(0xFF);

    // Members ------------------------------------------------------------------------------------

    public static ReturnCode resolve(int value) throws UnknownReturnCodeException
    {
      ReturnCode[] allCodes = ReturnCode.values();

      byte returnCodeByte = (byte)(value & 0xFF);

      for (ReturnCode returnCode : allCodes)
      {
        if (returnCode.value == returnCodeByte)
        {
          return returnCode;
        }
      }

      throw new UnknownReturnCodeException(
          "Unknown ESP3 response return code value : " +
          Strings.byteToUnsignedHexString(returnCodeByte)
      );
    }

    private byte value;

    private ReturnCode(int value)
    {
      this.value = (byte)(value & 0xFF);
    }

    public byte getValue()
    {
      return value;
    }
  }


  // Constants ------------------------------------------------------------------------------------

  /**
   * Byte order index of {@link ReturnCode return code} relative to data group.
   */
  public static final int ESP3_RESPONSE_RETURN_CODE_INDEX = 0x00;

  /**
   * Length of return code field.
   */
  public static final int ESP3_RESPONSE_RETURN_CODE_LENGTH = 0x01;

  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Return code.
   */
  private ReturnCode returnCode;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new ESP3 response packet instance with given response data.
   *
   * @param data          data group
   *
   * @param optionalData  optional data group
   *
   * @throws EspException
   *           if response return code is unknown or missing
   */
  public Esp3ResponsePacket(byte[] data, byte[] optionalData) throws EspException
  {
    super(Esp3PacketHeader.PacketType.RESPONSE, data, optionalData);

    if(data == null)
    {
      throw new IllegalArgumentException("null ESP3 packet data");
    }

    if(data.length < 1)
    {
      throw new EspException(
          EspException.ErrorCode.RESP_INVALID_RESPONSE,
          "Failed to create response instance because of missing return code."
      );
    }

    try
    {
      this.returnCode = ReturnCode.resolve(
          data[ESP3_RESPONSE_RETURN_CODE_INDEX] & 0xFF
      );
    }
    catch (UnknownReturnCodeException e)
    {
      throw new EspException(
          EspException.ErrorCode.RESP_UNKNOWN_RETURN_CODE,
          "Failed to create response instance because of unknown return code."
      );
    }
  }


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Returns return code.
   *
   * @return return code
   */
  public ReturnCode getReturnCode()
  {
    return returnCode;
  }

  // Nested Classes -------------------------------------------------------------------------------

  /**
   * Indicates an unknown {@link Esp3ResponsePacket.ReturnCode return code}.
   */
  public static class UnknownReturnCodeException extends Exception
  {
    public UnknownReturnCodeException(String msg)
    {
      super(msg);
    }
  }
}
