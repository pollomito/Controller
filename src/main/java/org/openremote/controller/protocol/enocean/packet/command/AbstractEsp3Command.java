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

import org.openremote.controller.protocol.enocean.packet.AbstractEsp3RequestPacket;
import org.openremote.controller.protocol.enocean.packet.Esp3PacketHeader;

/**
 * A common superclass for ESP3 command packets.
 *
 * The main purpose of the class is to manage the {@link CommandCode command code}. According to
 * the EnOcean Serial Protocol 3 specification chapter: Packet Type 5:COMMON_COMMAND, the
 * command code is stored in the first byte of the data group.
 *
 * <pre>
 *                                     |---- Data Group ---|
 *   +--------+------...------+--------+-------+----...----+----...----+--------+
 *   |  Sync  |      Header   |  CRC8  |Command|  Command  |  Optional |  CRC8  |
 *   |  Byte  |               | Header | Code  |    Data   |    Data   |  Data  |
 *   +--------+------...------+--------+-------+----...----+----...----+--------+
 *     1 byte      4 bytes      1 byte  1 byte    n bytes     n bytes    1 byte
 *</pre>
 *
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public abstract class AbstractEsp3Command extends AbstractEsp3RequestPacket
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Byte order index of the command code field relative to the data group : {@value}
   */
  public static final int ESP3_COMMAND_CODE_INDEX = 0x00;


  // Class Members --------------------------------------------------------------------------------

  /**
   * Copies command code to the data group and returns the modified data group. <p>
   *
   * If the data group parameter is null, a default data group with length 1 is created and
   * the command code is copied to this default data group.
   *
   * @param commandCode  command code
   * @param data         data group
   *
   * @return data group with copied command code
   */
  private static byte[] copyCommandCode(CommandCode commandCode, byte[] data)
  {
    if(commandCode == null)
    {
      return data;
    }

    byte[] retData = data;

    if(data == null || data.length == 0)
    {
      retData = new byte[ESP3_COMMAND_CODE_INDEX + 1];
    }

    retData[ESP3_COMMAND_CODE_INDEX] = commandCode.getValue();

    return retData;
  }


  // Enums ----------------------------------------------------------------------------------------

  /**
   * Command code used to indicate the command type as defined in EnOcean Serial Protocol 3 (ESP3)
   * specification chapter 1.11.2: List of COMMON_COMMAND Codes.
   */
  public enum CommandCode
  {
    /**
     * Read the device (SW) version / (HW) version, chip ID etc.
     */
    CO_RD_VERSION(0x03),

    /**
     * Read ID range base number.
     */
    CO_RD_IDBASE(0x08);


    // Members ------------------------------------------------------------------------------------

    private byte value;

    private CommandCode(int value)
    {
      this.value = (byte)(value & 0xFF);
    }

    public byte getValue()
    {
      return value;
    }
  }


  // Private Instance Fields ----------------------------------------------------------------------

  /**
   * Command code.
   */
  private CommandCode commandCode;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new command instance with given command code and data. <p>
   *
   * The implementation copies the command code byte to the data group. In case of a null data
   * parameter, the implementation creates a default data group with length 1 and copies the
   * command code byte to this default data group.
   *
   *
   * @param commandCode   command code
   * @param data          data group
   * @param optionalData  optional data group
   */
  public AbstractEsp3Command(CommandCode commandCode, byte[] data, byte[] optionalData)
  {
    super(
        Esp3PacketHeader.PacketType.COMMON_COMMAND,
        copyCommandCode(commandCode, data), optionalData
    );

    if(commandCode == null)
    {
      throw new IllegalArgumentException("null command code");
    }

    this.commandCode = commandCode;
  }

  /**
   * Constructs a new command instance with given command code.
   *
   * @param commandCode command code
   */
  public AbstractEsp3Command(CommandCode commandCode)
  {
    this(commandCode, null, null);
  }


  // Public Instance Methods ----------------------------------------------------------------------

  /**
   * Returns command code.
   *
   * @return command code
   */
  public CommandCode getCommandCode()
  {
    return commandCode;
  }
}
