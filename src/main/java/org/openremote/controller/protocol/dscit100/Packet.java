/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2013, OpenRemote Inc.
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
package org.openremote.controller.protocol.dscit100;

import java.io.IOException;

import org.openremote.controller.utils.Logger;
import org.openremote.controller.utils.Strings;

/**
 * @author Greg Rapp
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
class Packet
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * DSC logger. Uses a common category for all DSC related logging.
   */
  private final static Logger log = Logger.getLogger(DSCIT100CommandBuilder.DSC_LOG_CATEGORY);

  /** End of Packet CRLF */
  private static final String EOP = "\r\n";


  // Instance Fields ------------------------------------------------------------------------------

  private String command;
  private String data;
  private String checksum;
  private PacketCallback callback;


  // Constructors ---------------------------------------------------------------------------------

  protected Packet(String command, String data)
  {
    this.command = command;
    this.data = data;
    this.generateChecksum();

    log.debug("New packet created " + toString());
  }

  protected Packet(String command, String data, PacketCallback callback)
  {
    this(command, data);
    this.callback = callback;
  }

  /**
   * Parse raw data from host into an instance of Packet
   * 
   * @param raw
   *          Raw data from host
   *
   * @throws IOException
   *           Error parsing raw data
   */
  protected Packet(String raw) throws IOException
  {
    try
    {
      this.command = raw.substring(0, 3);
      this.checksum = raw.substring(raw.length() - 2, raw.length());
      this.data = raw.substring(3, raw.length() - 2);

      String calcChecksum = checksum();

      if (!this.checksum.equalsIgnoreCase(calcChecksum))
      {
        log.error(
            "Received packet with invalid checksum [packet=" + checksum + ",calculated=" +
            calcChecksum + "]"
        );
      }
    }
    catch (StringIndexOutOfBoundsException e)
    {
      log.error("Received bad packet [raw packet=" + raw + "]", e);
    }
  }


  // Public Methods -------------------------------------------------------------------------------

  protected String getCommand()
  {
    return command;
  }

  protected String getData()
  {
    return data;
  }

  protected PacketCallback getCallback()
  {
    return callback;
  }

  protected void generateChecksum()
  {
    checksum = checksum();
  }

  /**
   * Return the data to be sent to the IP gateway
   * 
   * @return packet data as string
   */
  protected String toPacket()
  {
    return command + data + checksum + EOP;
  }

  // Object Overrides -----------------------------------------------------------------------------

  @Override public String toString()
  {
    return "[command=" + command + ", data=" + data + ",checksum=" + checksum + "]";
  }



  // Private Methods ------------------------------------------------------------------------------

  /**
   * Calculate the packet's checksum
   * 
   * @return packet's checksum string
   */
  private String checksum()
  {
    int i;
    int iChecksum = 0;

    for (i = 0; i < command.length(); i++)
    {
      iChecksum += command.charAt(i);
    }

    for (i = 0; i < data.length(); i++)
    {
      iChecksum += data.charAt(i);
    }

    String sChecksum = Strings.toUpperCase(Integer.toHexString(iChecksum));
    sChecksum = sChecksum.substring(sChecksum.length() - 2);

    return sChecksum;
  }


  // Nested Interfaces ----------------------------------------------------------------------------

  interface PacketCallback
  {
    public void receive(DSCIT100Connection connection, Packet packet);
  }
}
