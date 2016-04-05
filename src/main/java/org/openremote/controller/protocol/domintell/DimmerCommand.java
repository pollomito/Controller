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
import org.openremote.controller.component.LevelSensor;
import org.openremote.controller.exception.NoSuchCommandException;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.model.sensor.SwitchSensor;
import org.openremote.controller.protocol.EventListener;
import org.openremote.controller.protocol.domintell.model.Dimmer;
import org.openremote.controller.protocol.domintell.model.DimmerModule;
import org.openremote.controller.protocol.domintell.model.DomintellModule;

/**
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class DimmerCommand extends DomintellCommand implements ExecutableCommand, EventListener {

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

      if (output == null) {
         throw new NoSuchCommandException("Output is required for a Domintell Dimmer command");
      }
      
      if ("FADE".equalsIgnoreCase(name) && level == null) {
         throw new NoSuchCommandException("Level is required for a dimmer Fade command");
       }

      return new DimmerCommand(name, gateway, moduleType, address, output, level);
    }

   // Private Instance Fields
   // ----------------------------------------------------------------------

   /**
    * Number of the output this command must actuate.
    */
   private Integer output;
   
   /**
    * Level to set the dimmer to
    */
   private Integer level;

   public DimmerCommand(String name, DomintellGateway gateway, String moduleType, DomintellAddress address, Integer output, Integer level) {
      super(name, gateway, moduleType, address);
      this.output = output;
      this.level = level;
   }

   @Override
   public void send() {
      try {
         Dimmer dimmer = (Dimmer) gateway.getDomintellModule(moduleType, address, DimmerModule.class);
         if ("ON".equals(name)) {
           dimmer.on(output);
         } else if ("OFF".equals(name)) {
           dimmer.off(output);
         } else if ("TOGGLE".equals(name)) {
           dimmer.toggle(output);
         } else if ("FADE".equals(name)) {
            dimmer.setLevel(output, level);
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
             DimmerModule dimmer = (DimmerModule) gateway.getDomintellModule(moduleType, address, DimmerModule.class);
             if (dimmer == null) {
               // This should never happen as above command is supposed to create device
               log.warn("Gateway could not create a Dimmer module we're receiving feedback for (" + address + ")");
             }

             // Register ourself with the Dimmer so it can propagate update when received
             dimmer.addCommand(this);
             addSensor(sensor);

             // Trigger a query to get the initial value
             dimmer.queryState(output);
          } catch (DomintellModuleException e) {
             log.error("Impossible to get module", e);
          }
       } else {
          addSensor(sensor);
       }
   }
   
   @Override
   public void stop(Sensor sensor) {
      stop(sensor, DimmerModule.class);
   }

   @Override
   protected void updateSensor(DomintellModule module, Sensor sensor) {
      DimmerModule dimmer = (DimmerModule)module;
      if (sensor instanceof SwitchSensor) {
         sensor.update((dimmer.getLevel(output) > 0) ? "on" : "off");
       } else if (sensor instanceof LevelSensor) {
          sensor.update(Integer.toString(dimmer.getLevel(output)));
       } else {
          log.warn("Query Dimmer status for incompatible sensor type (" + sensor + ")");
       }
   }
}
