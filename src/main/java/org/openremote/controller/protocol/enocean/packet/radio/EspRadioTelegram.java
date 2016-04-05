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
package org.openremote.controller.protocol.enocean.packet.radio;

import org.openremote.controller.protocol.enocean.DeviceID;
import org.openremote.controller.utils.Strings;

/**
 * Common interface for all kinds of EnOcean radio telegrams and all EnOcean serial protocol
 * (ESP) versions.
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public interface EspRadioTelegram
{
  // Enums ----------------------------------------------------------------------------------------

  /**
   * Radio telegram types as defined in EnOcean Equipment Profiles 2.1 specification
   * chapter 1.4: Telegram types (RORG).
   */
  public enum RORG
  {
    /**
     * Repeated switch communication.
     */
    RPS("RPS", 0xF6),

    /**
     * Repeated switch communication (old ESP2 version). <p>
     *
     * RORG values are part of the EnOcean equipment profile (EEP) number. The new
     * {@link #RPS} value and this old value may be used interchangeably for defining
     * EnOcean equipment profiles no matter if it's a ESP2 or ESP3 setting.
     */
    RPS_ESP2("RPS", 0x05),

    /**
     * 1 byte communication.
     */
    BS1("1BS", 0xD5),

    /**
     * 1 byte communication (old ESP2 version).
     *
     * RORG values are part of the EnOcean equipment profile (EEP) number. The new
     * {@link #BS1} value and this old value may be used interchangeably for defining
     * EnOcean equipment profiles no matter if it's a ESP2 or ESP3 setting.
     */
    BS1_ESP2("1BS", 0x06),

    /**
     * 4 byte communication.
     */
    BS4("4BS", 0xA5),

    /**
     * 4 byte communication (old ESP2 version).
     *
     * RORG values are part of the EnOcean equipment profile (EEP) number. The new
     * {@link #BS4} value and this old value may be used interchangeably for defining
     * EnOcean equipment profiles no matter if it's a ESP2 or ESP3 setting.
     */
    BS4_ESP2("4BS", 0x07),

    /**
     * Variable length data.
     */
    VLD("VLD", 0xD2),

    /**
     * Manufacturer specific communication.
     */
    MSC("MSC", 0xD1),

    /**
     * Addressed destination telegram.
     */
    ADT("ADT", 0xA6),

    /**
     * Smart ack learn request.
     */
    SM_LRN_REQ("SM_LRN_REQ", 0xC6),

    /**
     * Smart ack learn answer.
     */
    SM_LRN_ANS("SM_LRN_ANS", 0xC7),

    /**
     * Smart ack reclaim.
     */
    SM_REC("SM_REC", 0xA7),

    /**
     * Remote management.
     */
    SYS_EX("SYS_EX", 0xC5);

    // Members ------------------------------------------------------------------------------------

    public static RORG resolve(int value) throws UnknownRorgException
    {
      RORG[] allTypes = RORG.values();

      byte radioTypeByte = (byte)(value & 0xFF);

      for (RORG radioType : allTypes)
      {
        if (radioType.value == radioTypeByte)
        {
          return radioType;
        }
      }

      throw new UnknownRorgException(
          "Unknown ESP3 radio telegram type (RORG) value : " +
          Strings.byteToUnsignedHexString(radioTypeByte)
      );
    }


    private byte value;
    private String rorgString;

    private RORG(String rorgString, int value)
    {
      this.rorgString = rorgString;
      this.value = (byte)(value & 0xFF);
    }

    public byte getValue()
    {
      return value;
    }

    @Override public String toString()
    {
      return rorgString;
    }
  }

  // Interface Methods ----------------------------------------------------------------------------

  /**
   * Returns the radio telegram type (RORG).
   *
   * @return radio telegram type
   */
  RORG getRORG();


  /**
   * Returns the sender device ID.
   *
   * @return sender device ID
   */
  DeviceID getSenderID();

  /**
   * Returns a copy of the payload field. <p>
   *
   * The length of the payload data field is determined by the {@link RORG radio telegram type}.
   * Radio telegrams of type {@link RORG#BS1} and {@link RORG#RPS} have a 1 byte payload field,
   * {@link RORG#BS4} telegrams have a 4 byte payload field.
   *
   * @return copy of payload field
   */
  byte[] getPayload();

  /**
   * Returns the value of the status field.
   *
   * @return status field value
   */
  byte getStatusByte();


  // Nested Classes -------------------------------------------------------------------------------

  /**
   * Indicates an unknown {@link RORG} type.
   */
  public static class UnknownRorgException extends Exception
  {
    public UnknownRorgException(String msg)
    {
      super(msg);
    }
  }
}
