/*
 * OpenRemote, the Home of the Digital Home.
 *
 * Copyright 2008-2015, OpenRemote Inc. All rights reserved.
 *
 * The content of this file is available under commercial licensing only. It is *not* distributed
 * with any open source license. Please contact www.openremote.com for licensing.
 */
package org.openremote.controller.service;

import org.openremote.controller.model.sensor.Sensor;

import java.util.Set;

/**
 * Listener interface that is used to notify implementations when a new controller configuration
 * has been deployed in order to send the updated list of sensors.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public interface DeployerSensorListener
{
  /**
   * Method is called when a controller configuration has been deployed in order to
   * update the listener implementation with the sensors of the new configuration.
   *
   * @param sensors  sensors of the new controller configuration.
   */
  void onSensorsDeployed(Set<Sensor> sensors);
}
