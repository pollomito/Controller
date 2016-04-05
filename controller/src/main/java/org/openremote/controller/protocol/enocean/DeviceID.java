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


/**
 * Represents the 32 bit EnOcean device ID which is used for addressing. <p>
 *
 * The EnOcean Equipment Profiles (EEP) Specification V2.1 defines the EnOcean identifier
 * bit structure as follows: <p>
 *
 * <pre>{@code
 *
 *            +---------------------------------------------------------------+
 *  32 bits   |                           Sender ID                           |
 *            +-------------------------------+-------------------------------+
 *            |      ID_3     |      ID_2     |      ID_1     |      ID_0     |
 *            +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *     bits   |7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|7|6|5|4|3|2|1|0|
 *            +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 *
 * }</pre>
 *
 * Each EnOcean chip has a factory programmed unique ID. EnOcean transmitters insert their ID to
 * to the sent radio protocol packet. EnOcean receivers filter radio packets based on the sent
 * transmitter ID. <p>
 *
 * EnOcean transceiver gateway modules are special because they have an unique ID range for
 * sending packets. The range has a length of 128 ID's and starts with a so called base ID.
 * This enables EnOcean gateway modules to simulate 128 different physical devices. There is a
 * reserved ID range for gateway module base ID's: [0xFF800000 - 0xFFFFFF80]. <p>
 *
 * An ID with all bits set (0xFFFFFFFF) is used to address all EnOcean devices (broadcast ID).
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class DeviceID
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Reserved broadcast ID.
   */
  public static final DeviceID ENOCEAN_BROADCAST_ID = new DeviceID(0xFFFFFFFFL);

  /**
   * Start of device ID value range: {@value}
   */
  static final long ENOCEAN_MIN_ID = 0;

  /**
   * End of device ID value range: {@value}
   */
  static final long ENOCEAN_MAX_ID = 0xFFFFFFFFL;

  /**
   * Start of base ID value range: {@value} <p>
   *
   * Each EnOcean gateway module has a unique ID range for sending radio packets. To ensure the uniqueness of this
   * ID range, valid base ID's have to be within a reserved range. This is the lower bounds of the reserved base ID
   * range.
   *
   * @see #ENOCEAN_MAX_BASE_ID
   */
  static final long ENOCEAN_MIN_BASE_ID = 0xFF800000L;

  /**
   * End of base ID value range: {@value} <p>
   *
   * Each EnOcean gateway module has a unique ID range for sending radio packets. To ensure the uniqueness of this
   * ID range, valid base ID's have to be within a reserved range. This is the upper bounds of the reserved base ID
   * range.
   *
   * @see #ENOCEAN_MIN_BASE_ID
   */
  static final long ENOCEAN_MAX_BASE_ID = 0xFFFFFF80L;

  /**
   * Start of base ID offset value range: {@value} <p>
   *
   * Each EnOcean gateway module has a unique ID range for sending radio packets. Valid ID's are calculated by adding
   * an offset to the base ID. This is the lower bounds of the offset value range.
   *
   * @see #ENOCEAN_MAX_BASE_ID_OFFSET
   * @see #ENOCEAN_MIN_BASE_ID
   * @see #ENOCEAN_MAX_BASE_ID
   */
  static final long ENOCEAN_MIN_BASE_ID_OFFSET = 0;

  /**
   * End of base ID offset value range: {@value} <p>
   *
   * Each EnOcean gateway module has a unique ID range for sending radio packets. Valid ID's are calculated by adding
   * an offset to the base ID. This is the upper bounds of the offset value range.
   *
   * @see #ENOCEAN_MIN_BASE_ID_OFFSET
   * @see #ENOCEAN_MIN_BASE_ID
   * @see #ENOCEAN_MAX_BASE_ID
   */
  static final long ENOCEAN_MAX_BASE_ID_OFFSET = 127;

  /**
   * Length of an EnOcean device ID inside ESP protocol fields: {@value}
   */
  public static final int ENOCEAN_ESP_ID_LENGTH = 4;

  // Class Members --------------------------------------------------------------------------------

  /**
   * TODO : Logger
   */

  /**
   * Factory method for creating new device ID instances based on an ID string.
   *
   * @param   deviceID  EnOcean ID as a string. The ID has to be in the range ["0x00000000" - "0xFFFFFFFF"] with
   *                    hexadecimal or decimal representation
   *
   * @return  a new device ID instance
   *
   * @throws  InvalidDeviceIDException if parsing the ID string fails or the ID is out of bounds
   *
   */
  public static DeviceID fromString(String deviceID) throws InvalidDeviceIDException
  {
    return new DeviceID(parseDeviceID(deviceID));
  }

  /**
   * Factory method for creating new device ID instances based on an ID offset string and
   * and the base ID of an EnOcean Gateway module.
   *
   * @param  offset  ID offset added to the base ID. The offset has to be in the range ["0" - "127"] with a
   *                 hexadecimal or decimal representation
   * @param  baseID  Base ID of an EnOcean gateway module. The base ID has to be in the range [0xFF800000 - 0xFFFFFF80]
   *                 and align to blocks of 128 ID's (the 7 least significant bits have to be zero). Valid base ID's
   *                 are: 0xFF800000, 0xFF800080, 0xFF800100, 0xFF800180 ....
   *
   * @return a new device ID instance
   *
   * @throws InvalidDeviceIDException
   *                 if parsing the ID string fails or the resulting ID is out of bounds
   */
  public static DeviceID fromStringWithBaseID(String offset, DeviceID baseID) throws InvalidDeviceIDException
  {
    validateBaseID(baseID.deviceID);

    DeviceID newID = new DeviceID(baseID.deviceID + parseOffset(offset));
    
    validateDeviceID(newID.deviceID, null);

    if(newID.equals(ENOCEAN_BROADCAST_ID))
    {
      throw new InvalidDeviceIDException(
          "ID offset '" + offset + "' results in invalid broadcast ID (" +
          baseID + " + " + offset + " = " + ENOCEAN_BROADCAST_ID + ").",
          offset
      );
    }

    return newID;
  }

  /**
   * Factory method for creating new device ID based on a byte array.
   *
   * @param deviceIDBytes device ID byte array with exactly
   *                      {@value #ENOCEAN_ESP_ID_LENGTH} bytes
   *
   * @return a new device ID instance
   */
  public static DeviceID fromByteArray(byte[] deviceIDBytes)
  {
    if(deviceIDBytes == null)
    {
      throw new IllegalArgumentException("null device ID bytes");
    }

    if(deviceIDBytes.length != ENOCEAN_ESP_ID_LENGTH)
    {
      // TODO
      throw new IllegalArgumentException(
          "Expected " + ENOCEAN_ESP_ID_LENGTH + "device ID bytes, got " +
          deviceIDBytes.length
      );
    }

    long deviceIDValue = 0;

    deviceIDValue += (((long)deviceIDBytes[0] & 0xFFL) << 24);
    deviceIDValue += (((long)deviceIDBytes[1] & 0xFFL) << 16);
    deviceIDValue += (((long)deviceIDBytes[2] & 0xFFL) << 8);
    deviceIDValue += ((long)deviceIDBytes[3] & 0xFFL);

    return new DeviceID(deviceIDValue);
  }

  /**
   * Attempts to parse the device ID string with a hexadecimal or decimal representation.
   *
   * @param   deviceID   device ID as a string
   *
   * @return  device ID value
   *
   * @throws  InvalidDeviceIDException if parse fails or parsed ID is out of bounds
   */
  private static long parseDeviceID(String deviceID) throws InvalidDeviceIDException
  {
    long parsedID;

    try
    {
      parsedID = parseID(deviceID);
    }
    catch (NumberFormatException exception)
    {
      throw new InvalidDeviceIDException(
          "Cannot parse device ID '" + deviceID +
          "' (assuming 4 byte hexadecimal representation - e.g. 0xFF800001): " + exception.getMessage(),
          deviceID,
          exception
      );
    }

    return validateDeviceID(parsedID, deviceID);
  }

  /**
   * Attempts to parse the device ID offset string with a hexadecimal or decimal representation.
   *
   * @param   offset   device ID offset as a string
   *
   * @return  device ID offset value
   *
   * @throws  InvalidDeviceIDException if parse fails or parsed offset is out of bounds
   */
  private static long parseOffset(String offset) throws InvalidDeviceIDException
  {
    long parsedOffset;

    try
    {
      parsedOffset = parseID(offset);

      if(parsedOffset < ENOCEAN_MIN_BASE_ID_OFFSET || parsedOffset > ENOCEAN_MAX_BASE_ID_OFFSET)
      {
        throw new InvalidDeviceIDException(
            "Base ID offset value '" + offset + "' is too large " +
            "(valid range: 0x00 - " + String.format("0x%02X", ENOCEAN_MAX_BASE_ID_OFFSET) + ")",
            offset
        );
      }
    }

    catch (NumberFormatException exception)
    {
      throw new InvalidDeviceIDException(
          "Cannot parse device ID '" + offset +
          "' (assuming 1 byte hexadecimal representation - e.g. 0x01): " + exception.getMessage(),
          offset,
          exception
      );
    }

    return parsedOffset;
  }

  /**
   * Checks if base ID value is valid.
   *
   * @param  baseID   base ID of an EnOcean gateway module. Base ID has to be in the range [0xFF800000-0xFFFFFF80]
   *                  and align to blocks of 128 ID's (the 7 least significant bits have to be zero)
   *
   * @return  unmodified base ID value
   *
   * @throws InvalidDeviceIDException if base ID is invalid
   */
  private static long validateBaseID(long baseID) throws InvalidDeviceIDException
  {
    if(baseID < ENOCEAN_MIN_BASE_ID || baseID > ENOCEAN_MAX_BASE_ID)
    {
      throw new InvalidDeviceIDException(
          "Base device ID '" + String.format("0x%08X", baseID) +
          "' is out of the valid range (valid range: " + String.format("0x%08X", ENOCEAN_MIN_BASE_ID) +
          " - " + String.format("0x%08X", ENOCEAN_MAX_BASE_ID) + ")",
          String.format("0x%08X", baseID)
      );
    }

    if((baseID - ENOCEAN_MIN_BASE_ID) % (ENOCEAN_MAX_BASE_ID_OFFSET + 1) > 0)
    {
      throw new InvalidDeviceIDException(
          "Base device ID '" + String.format("0x%08X", baseID) +
          "' is not the beginning of a valid ID range",
          String.format("0x%08X", baseID)
      );
    }

    return baseID;
  }

  /**
   * Checks if device ID value is valid.
   *
   * @param   deviceID           device ID value. Device ID has to be in the range [0x0000000-0xFFFFFFFF]
   * @param   deviceIDAsString   device ID value as string, or <tt>null</tt> if not available
   *
   * @return  unmodified device ID value
   *
   * @throws  InvalidDeviceIDException if device ID is invalid
   */
  private static long validateDeviceID(long deviceID, String deviceIDAsString) throws  InvalidDeviceIDException
  {
    if(null == deviceIDAsString)
      deviceIDAsString = Long.toString(deviceID);

    if(deviceID < ENOCEAN_MIN_ID || deviceID > ENOCEAN_MAX_ID)
    {
      throw new InvalidDeviceIDException(
          "Device ID '" + deviceIDAsString + "' is too large.",
          deviceIDAsString
      );
    }

    return deviceID;
  }


  /**
   * Attempts to parse an ID string.
   *
   * @param   id   ID as string with hexadecimal or decimal representation
   *
   * @return  device ID value
   *
   * @throws  NumberFormatException if parse fails
   */
  private static long parseID(String id) throws NumberFormatException
  {
    return Long.decode(id);
  }


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * EnOcean device ID.
   */
  private final long deviceID;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs an EnOcean device ID instance from a device ID value.
   *
   * @param deviceIDAs32BitValue   EnOcean device ID as a 32 bit value
   */
  private DeviceID(long deviceIDAs32BitValue)
  {
    this.deviceID = deviceIDAs32BitValue & 0xFFFFFFFFL;
  }


  // Object Overrides -----------------------------------------------------------------------------

  /**
   * Tests device ID object equality based on device ID value.
   *
   * @param   o   device ID object to compare to
   *
   * @return  true if equals, false otherwise
   */
  @Override public boolean equals(Object o)
  {
    if(o == null)
      return false;

    if(!o.getClass().equals(this.getClass()))
      return false;

    DeviceID id = (DeviceID)o;

    return this.deviceID == id.deviceID;
  }

  /**
   * {@inheritDoc}
   */
  @Override public int hashCode()
  {
    return (int)deviceID;
  }

  /**
   * Returns string representation of this device ID.
   *
   * @return  device ID with hexadecimal representation
   */
  @Override public String toString()
  {
    return String.format("0x%08X", deviceID);
  }


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Returns the device ID as a 4-byte array.
   *
   * @return  4-byte array where the most significant bits of the device ID are at index 0 and
   *          the least significant bits are at index 3 (big-endian serial protocol byte order)
   */
  public byte[] asByteArray()
  {
    byte[] deviceIDBytes = new byte[4];

    deviceIDBytes[0] = (byte)((deviceID >> 24) & 0xFF);
    deviceIDBytes[1] = (byte)((deviceID >> 16) & 0xFF);
    deviceIDBytes[2] = (byte)((deviceID >> 8) & 0xFF);
    deviceIDBytes[3] = (byte)(deviceID & 0xFF);

    return deviceIDBytes;
  }

  /**
   * Resolves a relative device ID with given base ID to an absolute device ID.
   *
   * @param  baseID  Base ID of an EnOcean gateway module. The base ID has to be in the range [0xFF800000 - 0xFFFFFF80]
   *                 and align to blocks of 128 ID's (the 7 least significant bits have to be zero). Valid base ID's
   *                 are: 0xFF800000, 0xFF800080, 0xFF800100, 0xFF800180 ....
   *
   * @return a new device ID instance
   *
   * @throws InvalidDeviceIDException
   *           if the resulting device ID is out of bounds
   */
  public DeviceID resolve(DeviceID baseID) throws InvalidDeviceIDException
  {
    String offset = this.toString();

    return DeviceID.fromStringWithBaseID(offset, baseID);
  }
}
