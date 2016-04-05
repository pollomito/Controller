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
import org.openremote.controller.component.RangeSensor;
import org.openremote.controller.exception.NoSuchCommandException;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.model.sensor.StateSensor;
import org.openremote.controller.model.sensor.SwitchSensor;
import org.openremote.controller.protocol.EventListener;
import org.openremote.controller.protocol.domintell.model.DomintellModule;
import org.openremote.controller.protocol.domintell.model.Temperature;
import org.openremote.controller.protocol.domintell.model.TemperatureModule;

/**
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class TemperatureCommand extends DomintellCommand implements ExecutableCommand, EventListener {

   /**
    * Domintell logger. Uses a common category for all Domintell related logging.
    */
   private final static Logger log = Logger.getLogger(DomintellCommandBuilder.DOMINTELL_LOG_CATEGORY);

   public static DomintellCommand createCommand(String name, DomintellGateway gateway, String moduleType, DomintellAddress address, Integer output, Integer level, Float setPoint, TemperatureMode mode) {

      log.info("createCommand (" + name + "," + gateway + "," + moduleType + "," + address + "," + output + ")");

      // Check for mandatory attributes
      if (moduleType == null) {
         throw new NoSuchCommandException("Module type is required for any Domintell command");
      }
      
      if (address == null) {
        throw new NoSuchCommandException("Address is required for any Domintell command");
      }
      
      // TODO: more on parameters
      
      /*

      if ("FADE".equalsIgnoreCase(name) && level == null) {
         throw new NoSuchCommandException("Level is required for a dimmer Fade command");
       }

*/
      return new TemperatureCommand(name, gateway, moduleType, address, setPoint, mode);
    }
   
   // Private Instance Fields
   // ----------------------------------------------------------------------

   /**
    * Set point.
    */
   private Float setPoint;
   
   /**
    * Temperature mode.
    */
   private TemperatureMode mode;

   public TemperatureCommand(String name, DomintellGateway gateway, String moduleType, DomintellAddress address, Float setPoint, TemperatureMode mode) {
      super(name, gateway, moduleType, address);
      this.setPoint = setPoint;
      this.mode = mode;
   }

   @Override
   public void send() {
      try {
         Temperature temperature = (Temperature) gateway.getDomintellModule(moduleType, address, TemperatureModule.class);
         if ("SET_SET_POINT".equals(name)) {
            temperature.setSetPoint(setPoint);
         } else if ("SET_MODE".equals(name)) {
            temperature.setMode(mode);
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
            TemperatureModule temperature = (TemperatureModule) gateway.getDomintellModule(moduleType, address, TemperatureModule.class);
            if (temperature == null) {
              // This should never happen as above command is supposed to create device
              log.warn("Gateway could not create a Temperature module we're receiving feedback for (" + address + ")");
            }

            // Register ourself with the Temperature so it can propagate update when received
            temperature.addCommand(this);
            addSensor(sensor);

            // Trigger a query to get the initial value
            temperature.queryState();
         } catch (DomintellModuleException e) {
            log.error("Impossible to get module", e);
         }
      } else {
         addSensor(sensor);
      }
   }

   @Override
   public void stop(Sensor sensor) {
      stop(sensor, TemperatureModule.class);
   }

   private void updateSensorForCurrentTemperature(TemperatureModule temperature, Sensor sensor) {
      if (sensor instanceof LevelSensor) {
         sensor.update(Integer.toString((int)temperature.getCurrentTemperature()));
       } else if (sensor instanceof RangeSensor) {
         sensor.update(Integer.toString((int)temperature.getCurrentTemperature()));
       } else if (sensor instanceof StateSensor) {
         sensor.update(Float.toString(temperature.getCurrentTemperature()));
       } else {
          log.warn("Query Relay status for incompatible sensor type (" + sensor + ")");
       }
   }
   
   private void updateSensorForSetPoint(TemperatureModule temperature, Sensor sensor) {
      if (sensor instanceof LevelSensor) {
         sensor.update(Integer.toString((int)temperature.getSetPoint()));
       } else if (sensor instanceof RangeSensor) {
         sensor.update(Integer.toString((int)temperature.getSetPoint()));
       } else if (sensor instanceof StateSensor) {
         sensor.update(Float.toString(temperature.getSetPoint()));
       } else {
          log.warn("Query Relay status for incompatible sensor type (" + sensor + ")");
       }
   }
   
   private void updateSensorForMode(TemperatureModule temperature, Sensor sensor) {
      if (sensor instanceof SwitchSensor) {
         if (TemperatureMode.ABSENCE == mode) {
            sensor.update(TemperatureMode.ABSENCE == temperature.getMode()?"on":"off");
         } else if (TemperatureMode.AUTO == mode) {
            sensor.update(TemperatureMode.AUTO == temperature.getMode()?"on":"off");
         } else if (TemperatureMode.COMFORT == mode) {
            sensor.update(TemperatureMode.COMFORT == temperature.getMode()?"on":"off");
         } else if (TemperatureMode.FROST == mode) {
            sensor.update(TemperatureMode.FROST == temperature.getMode()?"on":"off");
         } else {
            log.warn(""); // TODO
         }
         
      }
      if (sensor instanceof StateSensor) {
         sensor.update(temperature.getMode().toString());
       } else {
          log.warn("Query Relay status for incompatible sensor type (" + sensor + ")");
       }
   }

   private void updateSensorForPresetSetPoint(TemperatureModule temperature, Sensor sensor) {
      if (sensor instanceof LevelSensor) {
         sensor.update(Integer.toString((int)temperature.getPresetSetPoint()));
       } else if (sensor instanceof RangeSensor) {
         sensor.update(Integer.toString((int)temperature.getPresetSetPoint()));
       } else if (sensor instanceof StateSensor) {
         sensor.update(Float.toString(temperature.getPresetSetPoint()));
       } else {
          log.warn("Query Relay status for incompatible sensor type (" + sensor + ")");
       }
   }

   @Override
   protected void updateSensor(DomintellModule module, Sensor sensor) {
      if ("READ_CURRENT_TEMP".equals(name)) {
         updateSensorForCurrentTemperature((TemperatureModule)module, sensor);
      } else if ("READ_SET_POINT".equals(name)) {
         updateSensorForSetPoint((TemperatureModule)module, sensor);
      } else if ("READ_MODE".equals(name)) {
         updateSensorForMode((TemperatureModule)module, sensor);
      } else if ("READ_PRESET_SET_POINT".equals(name)) {
         updateSensorForPresetSetPoint((TemperatureModule)module, sensor);
      }
   }

}
