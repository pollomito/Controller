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
import org.openremote.controller.model.Command;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.service.DeployerCommandListener;
import org.openremote.controller.service.DeployerSensorListener;
import org.openremote.controller.service.DeviceService;
import org.openremote.controller.utils.Logger;

import java.util.*;

/**
 * Service interface implementation that is used to retrieve device information.
 */
public class DeviceServiceImpl implements DeviceService, DeployerCommandListener, DeployerSensorListener
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * Logging category for runtime execution of commands.
   */
  private final static Logger log = Logger.getLogger(
      Constants.RUNTIME_COMMAND_EXECUTION_LOG_CATEGORY
  );


  // Private Instance Fields ------------------------------------------------------------------------------

  private final Map<String, Map<Integer, Map<Integer,Command>>>deviceName2DeviceID2CmdID2CmdMap =
      new HashMap<String, Map<Integer, Map<Integer, Command>>>();

  private final Map<String, Map<Integer, Map<String, List<Sensor>>>> deviceName2DeviceID2SensorName2SensorsMap =
      new HashMap<String, Map<Integer, Map<String, List<Sensor>>>>();

  private final Map<Integer, String> deviceID2NameMap = new HashMap<Integer, String>();

  private final Set<Sensor> sensorSet = new HashSet<Sensor>();

  private final Map<String, List<Sensor>> sensorName2SensorsMap = new HashMap<String, List<Sensor>>();


  // Implements DeviceService ---------------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public synchronized List<Integer> getDeviceIDs()
  {
    List<Integer> deviceIDs = new ArrayList<Integer>();

    for (String curDeviceName : deviceName2DeviceID2CmdID2CmdMap.keySet())
    {
      for (Integer curDeviceID : deviceName2DeviceID2CmdID2CmdMap.get(curDeviceName).keySet())
      {
        deviceIDs.add(curDeviceID);
      }
    }

    return deviceIDs;
  }

  /**
   * {@inheritDoc}
   */
  @Override public String getDeviceName(int deviceID)
  {
    return deviceID2NameMap.get(deviceID);
  }

  /**
   * {@inheritDoc}
   */
  @Override public synchronized List<Integer> getDeviceIDs(String deviceName)
  {
    List<Integer> cmdIDs = null;

    Map<Integer, Map<Integer,Command>> deviceID2CmdID2CmdMap =
        deviceName2DeviceID2CmdID2CmdMap.get(deviceName);

    if (deviceID2CmdID2CmdMap != null)
    {
      cmdIDs = new ArrayList<Integer>(
          deviceID2CmdID2CmdMap.keySet()
      );
    }

    if (cmdIDs == null)
    {
      cmdIDs = new ArrayList<Integer>();
    }

    return cmdIDs;
  }

  /**
   * {@inheritDoc}
   */
  @Override public synchronized List<Command> getCommands(int deviceID)
  {
    List<Command> commands = null;

    String deviceName = deviceID2NameMap.get(deviceID);

    if (deviceName != null)
    {
      Map<Integer,Command> cmdID2CmdMap = deviceName2DeviceID2CmdID2CmdMap
                                                          .get(deviceName)
                                                          .get(deviceID);

      commands = new ArrayList<Command>(
          cmdID2CmdMap.values()
      );
    }

    if (commands == null)
    {
      commands = new ArrayList<Command>();
    }

    return commands;
  }

  /**
   * {@inheritDoc}
   */
  @Override public synchronized List<Sensor> getSensors(int deviceID)
  {
    List<Sensor> sensors = new ArrayList<Sensor>();

    String deviceName = deviceID2NameMap.get(deviceID);

    if (deviceName != null && deviceName2DeviceID2SensorName2SensorsMap.get(deviceName) != null)
    {
      Map<String, List<Sensor>> sensorName2SensorsMap = deviceName2DeviceID2SensorName2SensorsMap
                                                                                  .get(deviceName)
                                                                                  .get(deviceID);

      for (List<Sensor> curSensorList : sensorName2SensorsMap.values())
      {
        sensors.addAll(curSensorList);
      }
    }

    return sensors;
  }


  // Implements DeployerCommandListener -----------------------------------------------------------

  @Override public synchronized void onCommandsDeployed(Set<Command> commands)
  {
    updateDeviceIDMap(commands);

    updateDevice2CommandMap(commands);

    updateDevice2SensorMap(this.sensorSet);
  }


  // Implements DeployerSensorListener ------------------------------------------------------------

  @Override public synchronized void onSensorsDeployed(Set<Sensor> sensors)
  {
    this.sensorSet.clear();
    this.sensorSet.addAll(sensors);

    updateSensorMap(sensors);

    updateDevice2SensorMap(sensors);
  }

  // Private Instance Methods ---------------------------------------------------------------------

  private void updateDeviceIDMap(Set<Command> commands)
  {
    deviceID2NameMap.clear();

    for (Command curCmd : commands)
    {
      String deviceName = curCmd.getProperty(
          Command.COMMAND_DEVICE_NAME_PROPERTY
      );
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

      if ("".equals(deviceName))
      {
        continue;
      }

      deviceID2NameMap.put(deviceID, deviceName);
    }
  }

  private void updateSensorMap(Set<Sensor> sensors)
  {
    sensorName2SensorsMap.clear();

    for (Sensor curSensor : sensors)
    {
      String sensorName = curSensor.getName().trim();

      if ("".equals(sensorName))
      {
        continue;
      }

      if (sensorName2SensorsMap.get(sensorName) == null)
      {
        sensorName2SensorsMap.put(sensorName, new ArrayList<Sensor>());
      }

      List<Sensor> sensorList = sensorName2SensorsMap.get(sensorName);

      sensorList.add(curSensor);
    }
  }

  private void updateDevice2CommandMap(Set<Command> commands)
  {
    deviceName2DeviceID2CmdID2CmdMap.clear();

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

      if (deviceName2DeviceID2CmdID2CmdMap.get(deviceName) == null)
      {
        deviceName2DeviceID2CmdID2CmdMap.put(
            deviceName, new HashMap<Integer, Map<Integer, Command>>()
        );
      }

      Map<Integer, Map<Integer,Command>> deviceID2CmdID2CmdMap =
          deviceName2DeviceID2CmdID2CmdMap.get(deviceName);

      if (deviceID2CmdID2CmdMap.get(deviceID) == null)
      {
        deviceID2CmdID2CmdMap.put(deviceID, new HashMap<Integer, Command>());
      }

      Map<Integer,Command> cmdID2CmdMap = deviceID2CmdID2CmdMap.get(deviceID);

      cmdID2CmdMap.put(curCmd.getID(), curCmd);
    }
  }

  private void updateDevice2SensorMap(Set<Sensor> sensors)
  {
    deviceName2DeviceID2SensorName2SensorsMap.clear();

    for (Sensor curSensor : sensors)
    {
      String sensorName = curSensor.getName().trim();

      if ("".equals(sensorName))
      {
        continue;
      }

      Integer deviceID = getDeviceIDFromCommandID(curSensor.getCommandID());

      if (deviceID == null)
      {
        continue;
      }

      String deviceName = getDeviceNameFromDeviceID(deviceID);

      if (deviceName2DeviceID2SensorName2SensorsMap.get(deviceName) == null)
      {
        deviceName2DeviceID2SensorName2SensorsMap.put(
            deviceName, new HashMap<Integer, Map<String, List<Sensor>>>()
        );
      }

      Map<Integer, Map<String, List<Sensor>>> deviceID2SensorName2SensorsMap =
          deviceName2DeviceID2SensorName2SensorsMap.get(deviceName);

      if (deviceID2SensorName2SensorsMap.get(deviceID) == null)
      {
        deviceID2SensorName2SensorsMap.put(
            deviceID, new HashMap<String, List<Sensor>>()
        );
      }

      Map<String, List<Sensor>> sensorName2SensorsMap = deviceID2SensorName2SensorsMap.get(deviceID);

      if (sensorName2SensorsMap.get(sensorName) == null)
      {
        sensorName2SensorsMap.put(
            sensorName, new ArrayList<Sensor>()
        );
      }

      List<Sensor> sensorList = sensorName2SensorsMap.get(sensorName);

      sensorList.add(curSensor);
    }
  }

  private Integer getDeviceIDFromCommandID(int commandID)
  {
    for (String curDeviceName : deviceName2DeviceID2CmdID2CmdMap.keySet())
    {
      for (Integer curDeviceID : deviceName2DeviceID2CmdID2CmdMap.get(curDeviceName).keySet())
      {
        for (Integer curCmdID : deviceName2DeviceID2CmdID2CmdMap.get(curDeviceName).get(curDeviceID).keySet())
        {
          if (curCmdID == commandID)
          {
            return curDeviceID;
          }
        }
      }
    }

    return null;
  }

  private String getDeviceNameFromDeviceID(int deviceID)
  {
    for (String curDeviceName : deviceName2DeviceID2CmdID2CmdMap.keySet())
    {
      for (Integer curDeviceID : deviceName2DeviceID2CmdID2CmdMap.get(curDeviceName).keySet())
      {
        if (curDeviceID == deviceID)
        {
          return curDeviceName;
        }
      }
    }

    return null;
  }
}
