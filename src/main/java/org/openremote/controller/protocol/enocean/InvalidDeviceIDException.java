/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2012, OpenRemote Inc.
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
package org.openremote.controller.protocol.enocean;

/**
 * Exception raised by incorrectly formatted EnOcean device ID when attempting to convert
 * a string representation into an instance of {@link DeviceID}
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class InvalidDeviceIDException extends Exception
{

  // Instance Fields ------------------------------------------------------------------------------

  /**
   * Keep the original string format around, useful for error reporting and logging.
   */
  private String invalidDeviceID;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new exception instance with a given message and the string representation
   * of an EnOcean device ID that could not be parsed.
   *
   * @param message           exception message
   * @param invalidDeviceID   original EnOcean device ID that did not convert into
   *                          {@link DeviceID} instance
   */
  public InvalidDeviceIDException(String message, String invalidDeviceID)
  {
    super(message);

    this.invalidDeviceID = invalidDeviceID;
  }

  /**
   * Constructs a new exception instance with a given message and the string representation
   * of an EnOcean device ID that could not be parsed and a root exception cause.
   *
   * @param message           exception message
   * @param invalidDeviceID   original EnOcean device ID that did not convert into
   *                          {@link DeviceID} instance
   * @param rootCause         root cause of this exception
   */
  public InvalidDeviceIDException(String message, String invalidDeviceID, Throwable rootCause)
  {
    super(message, rootCause);

    this.invalidDeviceID = invalidDeviceID;
  }


  // Instance Methods -----------------------------------------------------------------------------

  /**
   * Returns the string that was attempted to parse into a {@link DeviceID} instance.
   * 
   * @return  the original device ID string that failed to parse correctly
   */
  String invalidDeviceID()
  {
    return invalidDeviceID;
  }
}
