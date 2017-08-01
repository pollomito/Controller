/*
 * OpenRemote, the Home of the Digital Home.
 *
 * Copyright 2008-2014, OpenRemote Inc. All rights reserved.
 *
 * The content of this file is available under commercial licensing only. It is *not* distributed
 * with any open source license. Please contact www.openremote.com for licensing.
 */
package org.openremote.controller.service;

import org.openremote.controller.model.Command;

import java.util.Set;

/**
 * Listener interface that is used to notify implementations when a new controller configuration
 * has been deployed in order to send an update that is related to controller commands.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public interface DeployerCommandListener
{
  /**
   * Method is called when a controller configuration has been deployed in order to
   * update the listener implementation with the commands of the new configuration.
   *
   * @param commands  commands of the new controller configuration.
   */
  void onCommandsDeployed(Set<Command> commands);

  void onNewCommandsDeployed(Set<Command> newCommands);

}
