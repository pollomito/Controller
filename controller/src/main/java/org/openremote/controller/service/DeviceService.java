/*
 * OpenRemote, the Home of the Digital Home.
 *
 * Copyright 2008-2015, OpenRemote Inc. All rights reserved.
 *
 * The content of this file is available under commercial licensing only. It is *not* distributed
 * with any open source license. Please contact www.openremote.com for licensing.
 */
package org.openremote.controller.service;

import org.openremote.controller.model.Command;
import org.openremote.controller.model.sensor.Sensor;

import java.util.List;

/**
 * Service interface that is used to retrieve device information.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public interface DeviceService
{
  List<Integer> getDeviceIDs();

  List<Integer> getDeviceIDs(String deviceName);

  String getDeviceName(int deviceID);

  List<Command> getCommands(int deviceID);

  List<Sensor> getSensors(int deviceID);
}