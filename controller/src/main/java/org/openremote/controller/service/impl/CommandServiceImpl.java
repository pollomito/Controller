/*
 * OpenRemote, the Home of the Digital Home.
 *
 * Copyright 2008-2015, OpenRemote Inc. All rights reserved.
 *
 * The content of this file is available under commercial licensing only. It is *not* distributed
 * with any open source license. Please contact www.openremote.com for licensing.
 */
package org.openremote.controller.service.impl;

import org.openremote.controller.Constants;
import org.openremote.controller.exception.ControllerRESTAPIException;
import org.openremote.controller.model.Command;
import org.openremote.controller.service.CommandService;
import org.openremote.controller.service.DeployerCommandListener;
import org.openremote.controller.utils.Logger;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Service interface implementation that is used to execute OpenRemote controller commands
 * by command name.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class CommandServiceImpl implements CommandService, DeployerCommandListener
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * Logging category for runtime execution of commands.
   */
  private final static Logger log = Logger.getLogger(
      Constants.RUNTIME_COMMAND_EXECUTION_LOG_CATEGORY
  );


  // Private Instance Fields ----------------------------------------------------------------------

  private final Map<String, Map<Integer, Map<String, List<Command>>>> deviceName2DeviceID2CmdName2CmdMap
      = new HashMap<String, Map<Integer, Map<String, List<Command>>>>();


  // Implements CommandService --------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public synchronized void execute(String deviceName, String commandName, String commandParam)
      throws ControllerRESTAPIException
  {
    Command cmd = null;

    if (commandName != null)
    {
      Map<Integer, Map<String, List<Command>>> deviceID2CmdName2CmdMap =
          deviceName2DeviceID2CmdName2CmdMap.get(deviceName);

      if (deviceID2CmdName2CmdMap == null)
      {
        throw new ControllerRESTAPIException(
            HttpServletResponse.SC_NOT_FOUND, // 404
            ControllerRESTAPIException.ERROR_CODE_DEVICE_NOT_FOUND,
            "The command call was rejected by the controller because the addressed device is unknown.",
            "Unknown device '" + deviceName + "'."
        );
      }

      if (deviceID2CmdName2CmdMap.size() > 1)
      {
        throw new ControllerRESTAPIException(
            HttpServletResponse.SC_CONFLICT, // 409
            ControllerRESTAPIException.ERROR_CODE_DEVICE_AMBIGUITY,
            "The command call was rejected by the controller because of a device ambiguity error.",
            "Device name ambiguity error - a device with the name '" + deviceName +
            "' exists more than once."
        );
      }

      Map<String, List<Command>> cmdName2CmdMap =
          deviceID2CmdName2CmdMap.values().iterator().next();

      List<Command> cmdList = cmdName2CmdMap.get(commandName);

      if (cmdList == null)
      {
        throw new ControllerRESTAPIException(
            HttpServletResponse.SC_NOT_FOUND, // 404
            ControllerRESTAPIException.ERROR_CODE_COMMAND_NOT_FOUND,
            "The command call was rejected by the controller because the command is unknown.",
            "Unknown command '" + deviceName + "'."
        );
      }

      if (cmdList.size() > 1)
      {
        throw new ControllerRESTAPIException(
            HttpServletResponse.SC_CONFLICT, // 409
            ControllerRESTAPIException.ERROR_CODE_COMMAND_AMBIGUITY,
            "The command call was rejected by the controller because of an ambiguity error.",
            "Command name ambiguity error - the device '" + deviceName +
            "' has more than one command with the name '" + commandName + "'."
        );
      }

      cmd = cmdList.get(0);
    }

    if (cmd != null)
    {
      if (commandParam != null)
      {
        cmd.execute(commandParam);
      }

      else
      {
        cmd.execute();
      }
    }
  }


  // Implements DeployerCommandListener -----------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public synchronized void onCommandsDeployed(Set<Command> commands)
  {
    deployCommands(commands, true);
  }

  @Override
  public void onNewCommandsDeployed(Set<Command> newCommands) {
    deployCommands(newCommands, false);
  }

  private void deployCommands(Set<Command> commands, boolean clear) {
    if (clear) {
      deviceName2DeviceID2CmdName2CmdMap.clear();
    }

    for (Command curCmd : commands)
    {
      String cmdName = curCmd.getName();

      String deviceName = curCmd.getProperty(
          Command.COMMAND_DEVICE_NAME_PROPERTY
      ).trim();
      String deviceIDAsString = curCmd.getProperty(
          Command.COMMAND_DEVICE_ID_PROPERTY
      );

      Integer deviceID = null;

      try
      {
        deviceID = Integer.parseInt(deviceIDAsString);
      }

      catch(NumberFormatException e)
      {
        continue;
      }

      if (Command.DEFAULT_NAME_PROPERTY_VALUE.equals(cmdName) || "".equals(deviceName))
      {
        continue;
      }

      if (deviceName2DeviceID2CmdName2CmdMap.get(deviceName) == null)
      {
        deviceName2DeviceID2CmdName2CmdMap.put(
            deviceName, new HashMap<Integer, Map<String, List<Command>>>()
        );
      }

      Map<Integer, Map<String, List<Command>>> deviceID2CmdName2CmdMap =
          deviceName2DeviceID2CmdName2CmdMap.get(deviceName);

      if (deviceID2CmdName2CmdMap.get(deviceID) == null)
      {
        deviceID2CmdName2CmdMap.put(deviceID, new HashMap<String, List<Command>>());
      }

      Map<String, List<Command>> cmdName2CmdMap = deviceID2CmdName2CmdMap.get(deviceID);

      if (cmdName2CmdMap.get(cmdName) == null)
      {
        cmdName2CmdMap.put(cmdName, new ArrayList<Command>());
      }

      List<Command> cmdList = cmdName2CmdMap.get(cmdName);

      cmdList.add(curCmd);
    }
  }


}
