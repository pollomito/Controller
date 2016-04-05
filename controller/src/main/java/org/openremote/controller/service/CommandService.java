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
import org.openremote.controller.exception.NoSuchCommandException;

/**
 * Service interface that is used to execute OpenRemote controller commands by command
 * name.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public interface CommandService
{
  /**
   * Executes an OpenRemote controller command by the command name.
   *
   * @param deviceName    name of the device.
   *
   * @param commandName   command name.
   *
   * @param commandParam  command parameter, <tt>null</tt> if not available.
   *
   * @throws NoSuchCommandException  if the command could not be found
   */
  void execute(String deviceName, String commandName, String commandParam)
      throws ControllerRESTAPIException;
}
