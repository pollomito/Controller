/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2013, OpenRemote Inc.
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.controller.protocol.port;

import java.io.IOException;
import java.util.Map;

/**
 * The abstraction of a physical port (bus). <p>
 *
 * This interface describes the entry points of all the physical ports OpenRemote controller uses.
 *
 * @author Olivier Gandit
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public interface Port
{

  /**
   * Allows for configuring the concrete port implementation. Configuration is provided via
   * simple string to object map.
   *
   * @param configuration     a configuration map of named properties mapped to Java object
   *                          instances
   *
   * @throws IOException      if an I/O error communicating through the port occurred
   * @throws PortException    if an error related to port life-cycle or state occurred (port
   *                          open/closed, port locked, etc.)
   */
  void configure(Map<String, Object> configuration) throws IOException, PortException;
   
  /**
   * Starts the communication to the physical port.
   *
   * @throws IOException      if an I/O error communicating through the port occurred
   * @throws PortException    if an error related to port life-cycle or state occurred (port
   *                          open/closed, port locked, etc.)
   */
  void start() throws IOException, PortException;

  /**
   * Stops the physical port.
   *
   * @throws IOException      if an I/O error communicating through the port occurred
   * @throws PortException    if an error related to port life-cycle or state occurred (port
   *                          open/closed, port locked, etc.)
   */
  void stop() throws IOException, PortException;

  /**
   * Send a message to device(s) through the physical port.
   *
   * @param message
   *           The message to send.
   *
   * @throws IOException      if an I/O error communicating through the port occurred
   * @throws PortException    if an error related to port life-cycle or state occurred (port
   *                          open/closed, port locked, etc.)
   */
  void send(Message message) throws IOException, PortException;

  /**
   * Receive a message from the physical port. This method blocks until a message is received.
   *
   * @return Expected message.
   *
   * @throws IOException
   *            Message could not be received.
   */
  Message receive() throws IOException;

  // TODO :
  //        the method signature of receive() above is inconsistent with the other methods
  //        of the interface which allow separate port exception type to be communicated in
  //        case of port related errors       [JPL] 

}
