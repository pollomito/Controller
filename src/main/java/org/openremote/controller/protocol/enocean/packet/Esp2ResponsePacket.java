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
 * This class represents a ESP2 receive command telegram (RCT) as defined in the
 * TCM300 and TCM120 User Manual.
 *
 * <pre>
 *   |-----------   Header  ----------|
 *   +---------+---------+------------+---------+------...------+--------+
 *   |Sync Byte|Sync Byte|H_SEQ|LENGTH|  Return |    Command    |CHECKSUM|
 *   |  (0xA5) |  (0x5A) |     |      |   Code  |      Data     |        |
 *   +---------+---------+------------+---------+------...------+--------+
 *     1 byte     1 byte     1 byte      1byte       9 bytes      1 byte
 *</pre>
 *
 * Note: the TCM120 User Manual calls this message type "Received Message Telegram (RMT)".
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class Esp2ResponsePacket extends Esp2Packet
{

  // Enums ----------------------------------------------------------------------------------------

  /**
   * ESP2 receive command telegram (RCT) return codes.
   */
  public enum ReturnCode
  {

    /**
     *  Standard return code to indicate that an action has been successfully
     *  executed by the EnOcean module.
     */
    OK(0x58),

    /**
     * Standard error code to indicate that the EnOcean module could not execute
     * a transmit command telegram (TCT).
     */
    ERR(0x19),

    /**
     * Indicates that radio telegram couldn't be sent because of an ID out of range
     * error.
     */
    ERR_TX_ID_RANGE(0x22),

    /**
     * Indicates a protocol error related to the H_SEQ data field.
     */
    ERR_SYNTAX_H_SEQ(0x08),

    /**
     * Indicates a protocol error related to the LENGTH data field.
     */
    ERR_SYNTAX_LENGTH(0x09),

    /**
     * Indicates a checksum error.
     */
    ERR_SYNTAX_CHKSUM(0x0A),

    /**
     * Indicates a protocol error related to the ORG data field.
     */
    ERR_SYNTAX_ORG(0x0B),

    /**
     * Indicates that the telegram contains information about the current
     * EnOcean module status after power-on, a hardware reset or a RESET
     * command.
     */
    INF_INIT(0x89),

    /**
     * Indicates that the telegram contains the base ID of the EnOcean module
     * in response to a previously sent RD_IDBASE command.
     */
    INF_BASEID(0x98),

    /**
     * Indicates that the telegram contains the software version number in
     * response to a previously sent RD_SW_VER command.
     */
    INF_SW_VER(0x8C),

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
        "Unknown ESP2 receive command telegram (RCT) return code value : " +
        Strings.byteToUnsignedHexString(returnCodeByte)
      );
    }

    public static boolean isErrorCode(ReturnCode code)
    {
       if(code == ERR ||
          code == ERR_SYNTAX_CHKSUM ||
          code == ERR_SYNTAX_H_SEQ ||
          code == ERR_SYNTAX_LENGTH ||
          code == ERR_SYNTAX_ORG ||
          code == ERR_TX_ID_RANGE )
       {
         return true;
       }

       return false;
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
   * Byte order index of {@link ReturnCode return code } field relative to data group.
   */
  public static final int ESP2_RCT_RETURN_CODE_INDEX = 0x00;

  /**
   * Length of return code field.
   */
  public static final int ESP2_RCT_RETURN_CODE_LENGTH = 0x01;


  // Private Instance Fields ----------------------------------------------------------------------

  private ReturnCode returnCode;


  // Constructors ---------------------------------------------------------------------------------

  public Esp2ResponsePacket(byte[] data) throws EspException
  {
    super(Esp2PacketHeader.PacketType.RCT, data);

    if(data == null)
    {
      throw new IllegalArgumentException("null ESP2 packet data");
    }

    if(data.length < (ESP2_RCT_RETURN_CODE_INDEX + ESP2_RCT_RETURN_CODE_LENGTH))
    {
      throw new EspException(
          EspException.ErrorCode.RESP_INVALID_RESPONSE,
          "Failed to create response packet because of missing return code."
      );
    }

    try
    {
      this.returnCode = ReturnCode.resolve(
          data[ESP2_RCT_RETURN_CODE_INDEX] & 0xFF
      );
    }
    catch (UnknownReturnCodeException e)
    {
      throw new EspException(
          EspException.ErrorCode.RESP_UNKNOWN_RETURN_CODE,
          "Failed to create response packet because received unknown " +
          "return code from EnOcean module."
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
   * Indicates an unknown {@link Esp2ResponsePacket.ReturnCode return code}.
   */
  public static class UnknownReturnCodeException extends Exception
  {
    public UnknownReturnCodeException(String msg)
    {
      super(msg);
    }
  }
}
