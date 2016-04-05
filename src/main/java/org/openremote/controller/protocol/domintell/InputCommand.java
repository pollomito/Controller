/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2015, OpenRemote Inc.
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
package org.openremote.controller.protocol.domintell;

import org.apache.log4j.Logger;
import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.exception.NoSuchCommandException;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.model.sensor.SwitchSensor;
import org.openremote.controller.protocol.EventListener;
import org.openremote.controller.protocol.domintell.model.DomintellModule;
import org.openremote.controller.protocol.domintell.model.Input;
import org.openremote.controller.protocol.domintell.model.InputModule;

/**
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class InputCommand extends DomintellCommand implements ExecutableCommand, EventListener {

   /**
    * Domintell logger. Uses a common category for all Domintell related logging.
    */
   private final static Logger log = Logger.getLogger(DomintellCommandBuilder.DOMINTELL_LOG_CATEGORY);

   public static DomintellCommand createCommand(String name, DomintellGateway gateway, String moduleType, DomintellAddress address, Integer output, Integer level, Float floatValue, TemperatureMode mode) {

      log.info("createCommand (" + name + "," + gateway + "," + moduleType + "," + address + "," + output + ")");

      // Check for mandatory attributes
      if (moduleType == null) {
         throw new NoSuchCommandException("Module type is required for any Domintell command");
      }
      
      if (address == null) {
        throw new NoSuchCommandException("Address is required for any Domintell command");
      }

      // TODO: not output but input, maybe rename to "element number" or "I/O number"
      
      return new InputCommand(name, gateway, moduleType, address, output);
    }

   /**
    * Number of the input this command must actuate.
    */
   private Integer input;
   
   public InputCommand(String name, DomintellGateway gateway, String moduleType, DomintellAddress address, Integer output) {
      super(name, gateway, moduleType, address);
      this.input = output;
   }

   @Override
   public void send() {
      try {
         Input inputModule = (Input) gateway.getDomintellModule(moduleType, address, InputModule.class);
         if ("BEGIN_SHORT_PUSH".equals(name)) {
           inputModule.beginShortPush(input);
         } else if ("END_SHORT_PUSH".equals(name)) {
            inputModule.endShortPush(input);
         } else if ("BEGIN_LONG_PUSH".equals(name)) {
            inputModule.beginLongPush(input);
         } else if ("END_LONG_PUSH".equals(name)) {
            inputModule.endLongPush(input);
         }
       } catch (DomintellModuleException e) {
         log.error("Impossible to get module", e);
       }      
   }

   @Override
   public void setSensor(Sensor sensor) {
      if (sensors.isEmpty()) {
         // First sensor registered, we also need to register ourself with the device
         try {
            InputModule inputModule = (InputModule) gateway.getDomintellModule(moduleType, address, InputModule.class);
            if (inputModule == null) {
              // This should never happen as above command is supposed to create device
              log.warn("Gateway could not create a Input module we're receiving feedback for (" + address + ")");
            }

            // Register ourself with the Relay so it can propagate update when received
            inputModule.addCommand(this);
            addSensor(sensor);

            // Trigger a query to get the initial value
            inputModule.queryState(input);
         } catch (DomintellModuleException e) {
            log.error("Impossible to get module", e);
         }
      } else {
         addSensor(sensor);
      }
   }

   @Override
   public void stop(Sensor sensor) {
      stop(sensor, InputModule.class);
   }

   @Override
   protected void updateSensor(DomintellModule module, Sensor sensor) {
      InputModule inputModule = (InputModule)module;
      if (sensor instanceof SwitchSensor) {
         sensor.update(inputModule.getState(input) ? "on" : "off");
       } else {
          log.warn("Query Input status for incompatible sensor type (" + sensor + ")");
       }
   }

}
