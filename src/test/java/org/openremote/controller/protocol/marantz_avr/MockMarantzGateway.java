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
package org.openremote.controller.protocol.marantz_avr;

/**
 * Mock gateway that simply stores the information that would have been sent to the device.
 * 
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class MockMarantzGateway extends MarantzAVRGateway {

   private String command;
   private String parameter;
   
   /**
    * Stores command and parameter for later retrieval.
    */
   @Override
   public void sendCommand(String command, String parameter) {
      this.command = command;
      this.parameter = parameter;
   }

   public String getCommand() {
      return command;
   }

   public String getParameter() {
      return parameter;
   }
   
   /**
    * Returns the combined command and parameter as one string.
    * @return String command and parameter
    */
   public String getSentString() {
      return command + parameter;
   }

   /**
    * Reset command and parameter to null, preparing gateway for next send operation.
    */
   public void reset() {
      command = null;
      parameter = null;
   }
}
