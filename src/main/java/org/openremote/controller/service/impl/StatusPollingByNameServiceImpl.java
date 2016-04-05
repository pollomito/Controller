/*
 * OpenRemote, the Home of the Digital Home.
 *
 * Copyright 2008-2014, OpenRemote Inc. All rights reserved.
 *
 * The content of this file is available under commercial licensing only. It is *not* distributed
 * with any open source license. Please contact www.openremote.com for licensing.
 */
package org.openremote.controller.service.impl;

import org.openremote.controller.exception.ControllerRESTAPIException;
import org.openremote.controller.model.Command;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.service.DeployerCommandListener;
import org.openremote.controller.service.DeployerSensorListener;
import org.openremote.controller.service.StatusPollingByNameService;
import org.openremote.controller.statuscache.StatusCache;

import javax.servlet.http.HttpServletResponse;
import java.util.*;

/**
 * Service interface implementation that is used when polling sensor values by sensor names.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class StatusPollingByNameServiceImpl implements StatusPollingByNameService,
    DeployerCommandListener, DeployerSensorListener
{

  // Private Instance Fields ----------------------------------------------------------------------

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
  public StatusPollingByNameServiceImpl(StatusCache statusCache)
  {
    this.statusCache = statusCache;
  }


  // Implements StatusPollingByNameService --------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public synchronized Map<String, Integer> getSensorIDsFromNames(Set<String> sensorNames) throws ControllerRESTAPIException
  {
    Map<String, Integer> idMap = new HashMap<String, Integer>(sensorNames.size());

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
            "The command call was rejected by the controller because of an ambiguity error.",
            "Sensor name ambiguity error - a sensor with the name '" + curSensorName +
            "' exists more than once."
        );
      }

      Sensor sensor = sensorList.get(0);

      idMap.put(curSensorName, sensor.getSensorID());
    }

    return idMap;
  }

  /**
   * {@inheritDoc}
   */
  @Override public synchronized Map<String, Integer> getSensorIDsFromNames(String deviceName, Set<String> sensorNames) throws ControllerRESTAPIException
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
        deviceID2SensorName2SensorsMap.get(
            deviceID2SensorName2SensorsMap.keySet().toArray()[0]
        );


    Map<String, Integer> idMap = new HashMap<String, Integer>(sensorNames.size());

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

      idMap.put(curSensorName, sensor.getSensorID());
    }

    return idMap;
  }


  // Implements DeployerCommandListener -----------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public synchronized void onCommandsDeployed(Set<Command> commands)
  {
    updateDevice2CommandMap(commands);

    updateDevice2SensorMap(this.sensorSet);
  }


  // Implements DeployerSensorListener -----------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override public synchronized void onSensorsDeployed(Set<Sensor> sensors)
  {
    this.sensorSet.clear();
    this.sensorSet.addAll(sensors);

    updateSensorMap(sensors);

    updateDevice2SensorMap(sensors);
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
}
