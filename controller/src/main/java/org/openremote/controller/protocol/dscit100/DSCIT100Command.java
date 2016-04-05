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

import org.openremote.controller.command.Command;
import org.openremote.controller.exception.NoSuchCommandException;
import org.openremote.controller.utils.Logger;
import org.openremote.controller.utils.Strings;

/**
 * This class is an abstract superclass for DSC protocol read/write commands.
 * 
 * @see ExecuteCommand
 * @see ReadCommand
 *
 * @author Greg Rapp
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public abstract class DSCIT100Command implements Command
{

  // TODO:
  //   - since both IT100 and Envisalink gateways are now supported, this class should drop the
  //     IT100 from the name


  // Class Members --------------------------------------------------------------------------------

  /**
   * DSC logger. Uses a common category for all DSC related logging.
   */
  private final static Logger log = Logger.getLogger(DSCIT100CommandBuilder.DSC_LOG_CATEGORY);

  /**
   * Factory method for creating DSC command instances {@link ExecuteCommand} and
   * {@link ReadCommand} based on a human-readable configuration strings. <p>
   * 
   * Each DSCIT100 command instance is associated with a link to a connection
   * manager and a destination address.
   * 
   * @param command
   *          Command lookup name. This is usually a human-readable string used
   *          in configuration and tools. Note that multiple lookup names can be
   *          used to return Java equal() (but not same instance) commands.
   *
   * @param mgr
   *          DSC gateway connection manager used to transmit this command
   *
   * @param address
   *          DSC destination address.
   * 
   * @throws NoSuchCommandException
   *           If command cannot be created by its lookup name
   * 
   * @return new DSC command instance
   */
  public static DSCIT100Command createCommand(String command, String address, String code,
                                              String target, DSCIT100ConnectionManager mgr)
  {
    command = Strings.toUpperCase(command.trim());

    ExecuteCommand execCommand = ExecuteCommand.createCommand(command, address, code, target, mgr);

    if (execCommand != null)
    {
      return execCommand;
    }

    ReadCommand readCommand = ReadCommand.createCommand(command, address, target, mgr);

    if (readCommand != null)
    {
      return readCommand;
    }

    throw new NoSuchCommandException("Unknown command '" + command + "'.");
  }

  // Instance Fields ------------------------------------------------------------------------------

  /**
   * Destination address for this command.
   */
  protected String address;

  /**
   * Target partition or zone for this command.
   */
  protected String target;

  /**
   * Connection manager to be used to transmit this command.
   */
  private DSCIT100ConnectionManager connectionManager;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new DSC command with a given address, target partition or zone and connection
   * manager
   * 
   * @param address
   *          IT100 address
   *
   * @param target
   *          Target partition or zone
   *
   * @param connectionManager
   *          DSC connection manager to access the DSC connection(s)
   *
   */
  public DSCIT100Command(String address, String target, DSCIT100ConnectionManager connectionManager)
  {
    this.address = address;
    this.target = target;
    this.connectionManager = connectionManager;
  }


  // Protected Instance Methods -------------------------------------------------------------

  /**
   * Send a command instance out a DSC connection
   * 
   * @param command An instance of ExecuteCommand
   */
  protected void write(ExecuteCommand command)
  {
    try
    {
      DSCIT100Connection connection = connectionManager.getConnection(address);

      if (connection != null)
      {
        connection.send(command);
      }

      else
      {
        log.error("Failed to send command ''{0}'' due to lost/non-existent connection.", command);
      }
    }

    catch (Exception e)
    {
      log.error("Unable to send " + this + " : " + e.getMessage(), e);
    }
  }

  /**
   * Read the internal state map of the associated connection.
   * 
   * @param command
   *          DSC ReadCommand instance
   * 
   * @return Returns a State object
   * 
   *         NOTE: may return <code>null</code> in case there's a connection
   *         exception or the state map is not populated.
   */
  protected PanelState.State read(ReadCommand command)
  {
    try
    {
      DSCIT100Connection connection = connectionManager.getConnection(address);

      if (connection != null)
      {
        return connection.getState(command.getStateDefinition());
      }

      else
      {
        log.error("Could not get connection instance for address : " + address);

        return null;
      }
    }
    catch (Exception e)
    {
      log.error("Unable to read " + this + " : " + e.getMessage(), e);
    }
    return null;
  }

}
