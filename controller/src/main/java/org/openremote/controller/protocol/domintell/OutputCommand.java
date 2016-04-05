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
import org.openremote.controller.protocol.domintell.model.Output;
import org.openremote.controller.protocol.domintell.model.RelayModule;

/**
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class OutputCommand extends DomintellCommand implements ExecutableCommand, EventListener {
   
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

      return new OutputCommand(name, gateway, moduleType, address, output);
    }

   // Private Instance Fields
   // ----------------------------------------------------------------------

   /**
    * Number of the output this command must actuate.
    */
   private Integer output;

   public OutputCommand(String name, DomintellGateway gateway, String moduleType, DomintellAddress address, Integer output) {
      super(name, gateway, moduleType, address);
      this.output = output;
   }

   @Override
   public void send() {
      try {
         Output relay = (Output) gateway.getDomintellModule(moduleType, address, RelayModule.class);
         if ("ON".equals(name)) {
           relay.on(output);
         } else if ("OFF".equals(name)) {
           relay.off(output);
         } else if ("TOGGLE".equals(name)) {
           relay.toggle(output);
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
             RelayModule relay = (RelayModule) gateway.getDomintellModule(moduleType, address, RelayModule.class);
             if (relay == null) {
               // This should never happen as above command is supposed to create device
               log.warn("Gateway could not create a Relay module we're receiving feedback for (" + address + ")");
             }

             // Register ourself with the Relay so it can propagate update when received
             relay.addCommand(this);
             addSensor(sensor);

             // Trigger a query to get the initial value
             relay.queryState(output);
          } catch (DomintellModuleException e) {
             log.error("Impossible to get module", e);
          }
       } else {
          addSensor(sensor);
       }
   }
   
   @Override
   public void stop(Sensor sensor) {
      stop(sensor, RelayModule.class);
   }

   @Override
   protected void updateSensor(DomintellModule module, Sensor sensor) {
      RelayModule relay = (RelayModule)module;
      if (sensor instanceof SwitchSensor) {
         sensor.update(relay.getState(output) ? "on" : "off");
       } else {
          log.warn("Query Relay status for incompatible sensor type (" + sensor + ")");
       }
   }
}
