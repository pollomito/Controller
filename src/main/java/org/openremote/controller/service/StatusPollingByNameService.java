/*
 * OpenRemote, the Home of the Digital Home.
 *
 * Copyright 2008-2014, OpenRemote Inc. All rights reserved.
 *
 * The content of this file is available under commercial licensing only. It is *not* distributed
 * with any open source license. Please contact www.openremote.com for licensing.
 */
package org.openremote.controller.service;

import org.openremote.controller.exception.ControllerRESTAPIException;

import java.util.Map;
import java.util.Set;

/**
 * Service interface that is used used when polling sensor values by sensor name.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public interface StatusPollingByNameService
{
  /**
   * Converts sensor names to sensor ID's.
   *
   * @param   sensorNames  sensor names
   *
   * @return  map that contains the sensor ID for each sensor name.
   *
   * @throws ControllerRESTAPIException  if a sensor cannot be found or if a sensor
   *                                     name has been used more than once
   */
  public Map<String, Integer> getSensorIDsFromNames(Set<String> sensorNames)
      throws ControllerRESTAPIException;

  /**
   * Converts sensor names that are device specific to sensor ID's.
   *
   * @param   deviceName   device name
   *
   * @param   sensorNames  sensor names
   *
   * @return  map that contains the sensor ID for each sensor name.
   *
   * @throws ControllerRESTAPIException  if the device or a sensor cannot be found or if a device name or sensor
   *                                     name has been used more than once
   */
  public Map<String, Integer> getSensorIDsFromNames(String deviceName, Set<String> sensorNames)
      throws ControllerRESTAPIException;
}
