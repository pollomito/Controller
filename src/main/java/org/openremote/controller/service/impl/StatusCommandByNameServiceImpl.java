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
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.service.DeployerCommandListener;
import org.openremote.controller.service.DeployerSensorListener;
import org.openremote.controller.service.StatusCommandByNameService;
import org.openremote.controller.statuscache.StatusCache;
import org.openremote.controller.utils.Logger;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Service interface implementation that is used to request sensor values by name.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class StatusCommandByNameServiceImpl implements StatusCommandByNameService,
    DeployerCommandListener, DeployerSensorListener
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * Logging category for runtime execution of commands.
   */
  private final static Logger log = Logger.getLogger(
      Constants.RUNTIME_COMMAND_EXECUTION_LOG_CATEGORY
  );


  // Private Instance Fields ------------------------------------------------------------------------------

  /**
   * Sensor value cache.
   */
  private final StatusCache statusCache;

  private final Map<String, Map<Integer, Map<Integer,Command>>>deviceName2DeviceID2CmdID2CmdMap =
      new HashMap<String, Map<Integer, Map<Integer, Command>>>();

  private final Map<String, Map<Integer, Map<String, List<Sensor>>>> deviceName2DeviceID2SensorName2SensorsMap =
      new HashMap<String, Map<Integer, Map<String, List<Sensor>>>>();

  private final Set<Sensor> sensorSet = new HashSet<Sensor>();

  private final Map<String, List<Sensor>> sensorMap = new HashMap<String, List<Sensor>>();

  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a service instance.
   *
   * @param statusCache  sensor value cache.
   */
  public StatusCommandByNameServiceImpl(StatusCache statusCache)
  {
    this.statusCache = statusCache;
  }

  /**
   * {@inheritDoc}
   */
  @Override public synchronized Map<String, String> readFromCache(Set<String> sensorNames)
      throws ControllerRESTAPIException
  {
    Map<String, String> valueMap = new HashMap<String, String>(sensorNames.size());

    for (String curSensorName : sensorNames)
    {
      List<Sensor> sensorList = sensorMap.get(curSensorName);

      if (sensorList == null)
      {
        throw new ControllerRESTAPIException(
            HttpServletResponse.SC_NOT_FOUND, // 404
            ControllerRESTAPIException.ERROR_CODE_SENSOR_NOT_FOUND,
            "The command call was rejected by the controller because an addressed sensor could not be found.",
            "Unknown sensor '" + curSensorName + "'."
        );
      }

      if (sensorList.size() > 1)
      {
        throw new ControllerRESTAPIException(
            HttpServletResponse.SC_CONFLICT, // 409
            ControllerRESTAPIException.ERROR_CODE_SENSOR_AMBIGUITY,
            "The command call was rejected by the controller because of a sensor ambiguity error.",
            "Sensor name ambiguity error - a sensor with the name '" + curSensorName +
            "' exists more than once."
        );
      }

      Sensor sensor = sensorList.get(0);

      String value = statusCache.queryStatus(sensor.getSensorID());

      valueMap.put(curSensorName, value);
    }

    return valueMap;
  }

  /**
   * {@inheritDoc}
   */
  @Override public synchronized Map<String, String> readFromCache(String deviceName, Set<String> sensorNames)
      throws ControllerRESTAPIException
  {
    Map<Integer, Map<String, List<Sensor>>> deviceID2SensorName2SensorsMap =
        deviceName2DeviceID2SensorName2SensorsMap.get(deviceName);

    if (deviceID2SensorName2SensorsMap == null)
    {
      throw new ControllerRESTAPIException(
          HttpServletResponse.SC_NOT_FOUND, // 404
          ControllerRESTAPIException.ERROR_CODE_DEVICE_NOT_FOUND,
          "The command call was rejected by the controller because the addressed device could not be found.",
          "Unknown device '" + deviceName + "'."
      );
    }

    if (deviceID2SensorName2SensorsMap.keySet().size() > 1)
    {
      throw new ControllerRESTAPIException(
          HttpServletResponse.SC_CONFLICT, // 409
          ControllerRESTAPIException.ERROR_CODE_DEVICE_AMBIGUITY,
          "The command call was rejected by the controller because of an ambiguity error.",
          "Device name ambiguity error - a device with the name '" + deviceName +
          "' exists more than once."
      );
    }

    Map<String, List<Sensor>> sensorName2SensorsMap =
        deviceID2SensorName2SensorsMap.values().iterator().next();

    Map<String, String> valueMap = new HashMap<String, String>(sensorNames.size());

    for (String curSensorName : sensorNames)
    {
      List<Sensor> sensorList = sensorName2SensorsMap.get(curSensorName);

      if (sensorList == null)
      {
        throw new ControllerRESTAPIException(
            HttpServletResponse.SC_NOT_FOUND, // 404
            ControllerRESTAPIException.ERROR_CODE_SENSOR_NOT_FOUND,
            "The command call was rejected by the controller because an addressed sensor could not be found.",
            "Unknown sensor '" + curSensorName + "'."
        );
      }

      if (sensorList.size() > 1)
      {
        throw new ControllerRESTAPIException(
            HttpServletResponse.SC_CONFLICT, // 409
            ControllerRESTAPIException.ERROR_CODE_SENSOR_AMBIGUITY,
            "The command call was rejected by the controller because of an ambiguity error.",
            "Sensor name ambiguity error - a sensor with the name '" + curSensorName +
            "' exists more than once."
        );
      }

      Sensor sensor = sensorList.get(0);

      String value = statusCache.queryStatus(sensor.getSensorID());

      valueMap.put(curSensorName, value);
    }

    return valueMap;
  }


  // Implements DeployerCommandListener -----------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public synchronized void onCommandsDeployed(Set<Command> commands)
  {
    updateDevice2CommandMap(commands);

    updateDevice2SensorMap(this.sensorSet);

    /*
    logDevice2CommandMap();

    if (this.sensorSet.size() > 0)
    {
      logDevice2SensorMap();
    }
    */
  }


  // Implements DeployerSensorListener ------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public synchronized void onSensorsDeployed(Set<Sensor> sensors)
  {
    this.sensorSet.clear();
    this.sensorSet.addAll(sensors);

    updateSensorMap(sensors);

    updateDevice2SensorMap(sensors);

    //logDevice2SensorMap();
  }


  // Private Instance Methods ---------------------------------------------------------------------

  private void updateSensorMap(Set<Sensor> sensors)
  {
    sensorMap.clear();

    for (Sensor curSensor : sensors)
    {
      String sensorName = curSensor.getName().trim();

      if ("".equals(sensorName))
      {
        continue;
      }

      if (sensorMap.get(sensorName) == null)
      {
        sensorMap.put(sensorName, new ArrayList<Sensor>());
      }

      List<Sensor> sensorList = sensorMap.get(sensorName);

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

      if (deviceName == null)
      {
        continue;
      }

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

  private void logDevice2CommandMap()
  {
    log.error("##### BEGIN - DEVICE_2_COMMAND #####");

    for (String curDeviceName : deviceName2DeviceID2CmdID2CmdMap.keySet())
    {
      log.error("##### DeviceName = ''{0}''", curDeviceName);

      for (Integer curDeviceID : deviceName2DeviceID2CmdID2CmdMap.get(curDeviceName).keySet())
      {
        log.error("####### DeviceID = ''{0}''", curDeviceID);

        for (Integer curCmdID : deviceName2DeviceID2CmdID2CmdMap.get(curDeviceName).get(curDeviceID).keySet())
        {
          Command cmd = deviceName2DeviceID2CmdID2CmdMap.get(curDeviceName).get(curDeviceID).get(curCmdID);

          log.error("######### CommandID = ''{0}'', CommandName=''{1}''", curCmdID, cmd.getName());
        }
      }
    }

    log.error("##### END - DEVICE_2_COMMAND #####");
  }

  private void logDevice2SensorMap()
  {
    log.error("##### BEGIN - DEVICE_2_SENSOR #####");

    for (String curDeviceName : deviceName2DeviceID2SensorName2SensorsMap.keySet())
    {
      log.error("##### DeviceName = ''{0}''", curDeviceName);

      for (Integer curDeviceID : deviceName2DeviceID2SensorName2SensorsMap.get(curDeviceName).keySet())
      {
        log.error("####### DeviceID = ''{0}''", curDeviceID);

        for (String curSensorName : deviceName2DeviceID2SensorName2SensorsMap.get(curDeviceName).get(curDeviceID).keySet())
        {
          log.error("######### SensorName = ''{0}''", curSensorName);

          for (Sensor curSensor : deviceName2DeviceID2SensorName2SensorsMap.get(curDeviceName).get(curDeviceID).get(curSensorName))
          {
            log.error("######### SensorName = ''{0}'', SensorID = ''{1}''", curSensorName, curSensor.getSensorID());
          }
        }

      }

    }

    log.error("##### END - DEVICE_2_SENSOR #####");
  }
}
