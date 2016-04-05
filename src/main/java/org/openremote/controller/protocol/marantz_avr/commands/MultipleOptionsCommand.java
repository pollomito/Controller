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
import org.openremote.controller.model.sensor.StateSensor;
import org.openremote.controller.model.sensor.SwitchSensor;
import org.openremote.controller.protocol.EventListener;
import org.openremote.controller.protocol.marantz_avr.CommandConfig;
import org.openremote.controller.protocol.marantz_avr.MarantzAVRCommand;
import org.openremote.controller.protocol.marantz_avr.MarantzAVRCommandBuilder;
import org.openremote.controller.protocol.marantz_avr.MarantzAVRGateway;
import org.openremote.controller.protocol.marantz_avr.MarantzAVRGateway.MarantzResponse;
import org.openremote.controller.utils.Logger;

/**
 * Handle command that manages a value with multiple options in the protocol, such as source selection.
 * 
 * Maps to custom sensor values, all other sensor types are not supported.
 * 
 * This command supports zones.
 * 
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class MultipleOptionsCommand extends MarantzAVRCommand implements ExecutableCommand, EventListener {

   /**
    * Marantz AVR logger. Uses a common category for all Marantz AVR related logging.
    */
   protected final static Logger log = Logger.getLogger(MarantzAVRCommandBuilder.MARANTZ_AVR_LOG_CATEGORY);

   public static MultipleOptionsCommand createCommand(CommandConfig commandConfig, String name, MarantzAVRGateway gateway, String parameter, String zone) {
      // Check for mandatory attributes
      if (commandConfig == null) {
         throw new NoSuchCommandException("No configuration provided for " + name + " command.");
      }
      if (parameter == null) {
         throw new NoSuchCommandException("A parameter is always required for " + name + " command.");
      }

      return new MultipleOptionsCommand(commandConfig, name, gateway, parameter, zone);
    }

   public MultipleOptionsCommand(CommandConfig commandConfig, String name, MarantzAVRGateway gateway, String parameter, String zone) {
      super(name, gateway);
      this.parameter = parameter;
      this.commandConfig = commandConfig;
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
      if (commandConfig.getParameter(parameter) == null) {
         throw new NoSuchCommandException("Invalid parameter (" + parameter + ") for command " + name);
      }
     gateway.sendCommand(commandConfig.getValueToUseForZone(zone), commandConfig.getParameter(parameter));
   }

   // Implements EventListener -------------------------------------------------------------------

   @Override
   public void setSensor(Sensor sensor) {
       if (sensors.isEmpty()) {
          
          // First sensor registered, we also need to register ourself with the gateway
          gateway.registerCommand(commandConfig.getValueToUseForZone(zone), this);
          addSensor(sensor);

          // Trigger a query to get the initial value
          send();
       } else {
          addSensor(sensor);
       }
   }
   
   @Override
   public void stop(Sensor sensor) {
      removeSensor(sensor);
      if (sensors.isEmpty()) {
         // Last sensor removed, we may unregister ourself from gateway
         gateway.unregisterCommand(commandConfig.getValueToUseForZone(zone), this);
      }
   }
   
   @Override
   protected void updateWithResponse(MarantzResponse response) {
      String value = commandConfig.lookupResponseParam(response.parameter);
      // Only update if the value makes sense for us, otherwise ignore
      if (value != null) {
         updateSensorsWithValue(value);
      }
   }
   
   @Override
   protected void updateSensorWithValue(Sensor sensor, Object value) {
      if (sensor instanceof SwitchSensor) {
         log.warn("Switch sensor type is not supported by this command (sensor: " + sensor + ")");
      } else if (sensor instanceof StateSensor) { // Note this includes SwitchSensor
         sensor.update((String)value);
      } else {
         log.warn("Query value for incompatible sensor type (" + sensor + ")");
      }
   }

}