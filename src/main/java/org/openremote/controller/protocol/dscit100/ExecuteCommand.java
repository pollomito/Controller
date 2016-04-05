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

import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.protocol.dscit100.Packet.PacketCallback;
import org.openremote.controller.utils.Logger;
import org.openremote.controller.utils.Strings;

/**
 * Write command sent to DSC IP gateway (IT-100 or EnvisaLink)
 *
 * @author Greg Rapp
 * @author Phil Taylor
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class ExecuteCommand extends DSCIT100Command implements ExecutableCommand
{

  // TODO :
  //   - candidate for rename since the implementation is also used for EnvisaLink gateways,
  //     not only IT-100 anymore


  // Class Members --------------------------------------------------------------------------------

  /**
   * DSC logger. Uses a common category for all DSC related logging.
   */
  private static final Logger log = Logger.getLogger(DSCIT100CommandBuilder.DSC_LOG_CATEGORY);

  /**
   * Factory method for creating DSC command instances {@link ExecuteCommand} and
   * {@link ReadCommand} based on a human-readable configuration strings. <p>
   * 
   * Each DSC command instance is associated with a link to a connection manager and a destination
   * address.
   * 
   * @param name
   *          Command lookup name. This is usually a human-readable string used
   *          in configuration and tools. Note that multiple lookup names can be
   *          used to return Java equal() (but not same instance) commands
   *
   * @param address
   *          DSC address
   *
   * @param code
   *          Security code
   *
   * @param target
   *          Partition or zone that is the target of this command
   *
   * @param mgr
   *          DSC connection manager used to transmit this command
   * 
   * @return new DSC command instance, or null if lookup with the given name did not return
   *         anything
   */
  public static ExecuteCommand createCommand(String name, String address, String code,
                                             String target, DSCIT100ConnectionManager mgr)
  {
    if (name == null || address == null || mgr == null)
    {
      log.error("Implementation Error: DSC null parameter.");

      return null;
    }

    name = Strings.toUpperCase(name.trim());

    // Pad code with zeros to make it 6 digits in length
    code = String.format("%-6s", code).replace(' ', '0');

    Packet packet = Lookup.get(name, code, target);

    if (packet == null)
    {
      log.warn(
          "No corresponding packet found for name: ''{0}'', address: ''{1}'', target: ''{2}''",
          name, address, target
      );

      return null;
    }

    return new ExecuteCommand(packet, address, target, mgr);
  }

  // Private Instance Fields ----------------------------------------------------------------------


  /**
   * Command packet.
   */
  private Packet packet;

  /**
   * Security code for this command.
   */
  // private String code;


  // Constructors ---------------------------------------------------------------------------------

  public ExecuteCommand(Packet packet, String address, String target,
                        DSCIT100ConnectionManager connectionManager)
  {
    super(address, target, connectionManager);

    this.packet = packet;
    // this.code = code;

    log.info("Instantiating new ExecuteCommand instance - " + this.toString());
  }

  // Object Overrides -----------------------------------------------------------------------------

  /**
   * Returns a string representation of this command. Expected output is:
   * 
   * <pre>
   * {@code
   * 
   * [PACKET:command=xx, data=xx, address=xx, target=xx]
   * 
   * }
   * </pre>
   * 
   * @return this command as string
   */
  @Override public String toString()
  {
    StringBuilder buffer = new StringBuilder();

    String command = packet.getCommand();
    String data = packet.getData();

    buffer.append("[PACKET:command=").append(command).append(", data=")
        .append(data).append(", address=").append(address).append(", target=")
        .append(target).append("]");

    return buffer.toString();
  }

  // Implements ExecutableCommand -----------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public void send()
  {
    // delegate to super class...

    super.write(this);
  }

  // Public Instance Methods -------------------------------------------------------------

  /**
   * @return the command packet
   */
  public Packet getPacket()
  {
    return packet;
  }

  /**
   * @return the code
   */
  /*
   * public String getCode() { return codeString; }
   */


  // Nested Classes -------------------------------------------------------------------------------

  /**
   * Simple helper class to lookup user configured command names and match them
   * to Java instances.
   */
  private static class Lookup
  {
    /**
     * Lookup from user defined command strings in the designer (from which they
     * end up into controller.xml) to type safe Packets for DSC packets.
     * 
     * @param name
     *          lookup name
     *
     * @param code
     *          security code
     * 
     * @return complete packet with data, or <tt>null</tt> if command was not
     *         found by name
     */

    private static Packet get(final String name, final String code, final String target)
    {
      if (name.equals("POLL"))
      {
        return new Packet("000", "");
      }

      else if (name.equals("STATUS"))
      {
        return new Packet("001", "");
      }

      else if (name.equals("LOGIN"))
      {
        return new Packet("005", "user");
      }

      else if (name.equals("ZONE_TIMERS"))
      {
        return new Packet("008", "");
      }

      else if (name.equals("SET_DATETIME"))
      {
        return new Packet("010", code);
      }

      else if (name.equals("COMMAND_OUTPUT"))
      {
        return new Packet("020", target + code);
      }

      else if (name.equals("ARM_AWAY"))
      {
        return new Packet("030", target);
      }

      else if (name.equals("ARM_STAY"))
      {
        return new Packet("031", target, new PacketCallback()
        {
          @Override public void receive(DSCIT100Connection connection, Packet packet)
          {
            if (packet.getCommand().equals("900") && code != null)
            {
              connection.send(new Packet("200", target + code));
            }
          }
        });
      }

      else if (name.equals("ARM_NO_ENTRY_DELAY"))
      {
        return new Packet("032", target, new PacketCallback()
        {
          @Override public void receive(DSCIT100Connection connection, Packet packet)
          {
            if (packet.getCommand().equals("900") && code != null)
            {
              connection.send(new Packet("200", target + code));
            }
          }
        });
      }

      else if (name.equals("ARM"))
      {
        return new Packet("033", target + code);
      }

      else if (name.equals("DISARM"))
      {
        return new Packet("040", target + code);
      }

      else if (name.equals("TIMESTAMP"))
      {
        return new Packet("055", target);
      }

      else if (name.equals("TIME_BROADCAST"))
      {
        return new Packet("056", target);
      }

      else if (name.equals("TEMP_BROADCAST"))
      {
        return new Packet("057", target);
      }

      else if (name.equals("PANIC_FIRE"))
      {
        return new Packet("060", "1");
      }

      else if (name.equals("PANIC_AMBULANCE"))
      {
        return new Packet("060", "2");
      }

      else if (name.equals("PANIC"))
      {
        return new Packet("060", "3");
      }

      else if (name.equals("SINGLE_KEY"))
      {
        return new Packet("070", target);
      }

      else if (name.equals("KEYSTROKE"))
      {
        return new Packet("071", target + code);
      }

      else if (name.equals("USER_CODE_PROG"))
      {
        return new Packet("072", target);
      }

      else if (name.equals("USER_PROG"))
      {
        return new Packet("073", target);
      }

      else if (name.equals("KEEPALIVE"))
      {
        return new Packet("074", target);
      }

      else
      {
        return null;
      }
    }
  }
}
