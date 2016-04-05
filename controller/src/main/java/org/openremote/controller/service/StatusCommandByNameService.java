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
 * Service interface that is used to retrieve sensor values from the cache by sensor name.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public interface StatusCommandByNameService
{
  /**
   * Returns sensor values from the cache by sensor name.
   *
   * @param sensorNames  sensor names
   *
   * @return  map that contains the sensor values for the requested sensor names.
   *
   * @throws  ControllerRESTAPIException  if a sensor could not be found or a sensor
   *                                      name has been used more than once
   *
   * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
   */
  Map<String, String> readFromCache(Set<String> sensorNames) throws ControllerRESTAPIException;

  /**
   * Returns sensor values from the cache by sensor name that are restricted to a certain device.
   *
   * @param deviceName   name of the device
   *
   * @param sensorNames  sensor names
   *
   * @return  map that contains the sensor values for the requested sensor names.
   *
   * @throws  ControllerRESTAPIException  if a device or sensor could not be found or a
   *                                      device or sensor name has been used more than once
   *
   * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
   */
  Map<String, String> readFromCache(String deviceName, Set<String> sensorNames)
      throws ControllerRESTAPIException;
}
