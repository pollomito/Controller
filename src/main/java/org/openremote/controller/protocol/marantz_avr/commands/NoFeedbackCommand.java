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
package org.openremote.controller.protocol.marantz_avr.commands;

import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.exception.NoSuchCommandException;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.protocol.EventListener;
import org.openremote.controller.protocol.marantz_avr.CommandConfig;
import org.openremote.controller.protocol.marantz_avr.MarantzAVRCommand;
import org.openremote.controller.protocol.marantz_avr.MarantzAVRCommandBuilder;
import org.openremote.controller.protocol.marantz_avr.MarantzAVRGateway;
import org.openremote.controller.protocol.marantz_avr.MarantzAVRGateway.MarantzResponse;
import org.openremote.controller.utils.Logger;

/**
 * Handle command that sends a value but never receives any feedback, such as cursor/menu navigation.
 * 
 * Does not provide any feedback to sensors and ignores feedback strings.
 * 
 * This command supports zones.
 * 
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class NoFeedbackCommand extends MarantzAVRCommand implements ExecutableCommand, EventListener {

   /**
    * Marantz AVR logger. Uses a common category for all Marantz AVR related logging.
    */
   protected final static Logger log = Logger.getLogger(MarantzAVRCommandBuilder.MARANTZ_AVR_LOG_CATEGORY);

   public static NoFeedbackCommand createCommand(CommandConfig commandConfig, String name, MarantzAVRGateway gateway, String parameter, String zone) {
      // Check for mandatory attributes
      if (commandConfig == null) {
         throw new NoSuchCommandException("No configuration provided for " + name + " command.");
      }

      // parameter is optional

      return new NoFeedbackCommand(commandConfig, name, gateway, parameter, zone);
    }

   public NoFeedbackCommand(CommandConfig commandConfig, String name, MarantzAVRGateway gateway, String parameter, String zone) {
      super(name, gateway);
      this.commandConfig = commandConfig;
      this.parameter = parameter;
      this.zone = zone;
   }

   // Private Instance Fields ----------------------------------------------------------------------

   /**
    * Configuration defining this command.
    */
   private CommandConfig commandConfig;

   /**
    * Parameter used by this command.
    */
   private String parameter;
   
   /**
    * Zone used by this command.
    */
   private String zone;

   // Implements ExecutableCommand -----------------------------------------------------------------

   /**
    * {@inheritDoc}
    */
   public void send() {
      // Parameter is not mandatory, if no mapping is provided, just pass value as is
      String parameterValue = "";
      if (parameter != null) {
         if (commandConfig.getParameter(parameter) != null) {
            parameterValue = commandConfig.getParameter(parameter);
         }
      }
     gateway.sendCommand(commandConfig.getValueToUseForZone(zone), parameterValue);
   }

   // Implements EventListener -------------------------------------------------------------------

   @Override
   public void setSensor(Sensor sensor) {
      throw new NoSuchCommandException("This command can not be associated with a Sensor");
   }
   
   @Override
   public void stop(Sensor sensor) {
      // Don't do anything, no sensor should be registered
   }
   
   @Override
   protected void updateWithResponse(MarantzResponse response) {
      // Don't do anything, no sensor should be registered
   }
   
}