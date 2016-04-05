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

import java.text.NumberFormat;

import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.component.LevelSensor;
import org.openremote.controller.component.RangeSensor;
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
 * Specific command to handle volume.
 * 
 * Handles all sensor types. Switch sensor is considered on as soon as volume is non zero.
 * 
 * This command supports zones.
 * 
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class VolumeCommand extends MarantzAVRCommand implements ExecutableCommand, EventListener {

   /**
    * Marantz AVR logger. Uses a common category for all Marantz AVR related logging.
    */
   protected final static Logger log = Logger.getLogger(MarantzAVRCommandBuilder.MARANTZ_AVR_LOG_CATEGORY);

   public static VolumeCommand createCommand(CommandConfig commandConfig, String name, MarantzAVRGateway gateway, String parameter, String zone) {
      // Check for mandatory attributes
      if (parameter == null) {
        throw new NoSuchCommandException("A parameter is always required for the VOLUME command.");
      }

      return new VolumeCommand(commandConfig, name, gateway, parameter, zone);
    }

   public VolumeCommand(CommandConfig commandConfig, String name, MarantzAVRGateway gateway, String parameter, String zone) {
      super(name, gateway);
      this.commandConfig = commandConfig;
      this.parameter = parameter;
      this.zone = zone;
      threeDigitsVolumeFormat = NumberFormat.getInstance();
      threeDigitsVolumeFormat.setMaximumFractionDigits(0);
      threeDigitsVolumeFormat.setMinimumIntegerDigits(3);
      threeDigitsVolumeFormat.setMaximumIntegerDigits(3);
      twoDigitsVolumeFormat = NumberFormat.getInstance();
      twoDigitsVolumeFormat.setMaximumFractionDigits(0);
      twoDigitsVolumeFormat.setMinimumIntegerDigits(2);
      twoDigitsVolumeFormat.setMaximumIntegerDigits(2);

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

   /**
    * Number format to format the volume as a 3 digit string.
    */
   private NumberFormat threeDigitsVolumeFormat;
   
   /**
    * Number format to format the volume as a 2 digit string.
    */
   private NumberFormat twoDigitsVolumeFormat;

   // Implements ExecutableCommand -----------------------------------------------------------------

   /**
    * {@inheritDoc}
    */
   public void send() {
     if ("STATUS".equals(parameter)) {
        gateway.sendCommand(commandConfig.getValueToUseForZone(zone),  "?");
     } else if ("UP".equals(parameter) || "DOWN".equals(parameter)) {
        gateway.sendCommand(commandConfig.getValueToUseForZone(zone), parameter);
     } else {
        // This should then be a value, parse it and reformat appropriately
        try {
           float value = Float.parseFloat(parameter);
           if (zone == null || "MAIN".equals(zone)) {
              // Only main zone supports 3 digits volume format, with .5 dB increments.
              value = Math.round(value * 2.0f) / 2.0f; // Round to closest .5 value
              gateway.sendCommand(commandConfig.getValueToUseForZone(zone), threeDigitsVolumeFormat.format(value * 10.0f)); // Sent string is 3 digits without decimal point
           } else {
              value = Math.round(value);
              gateway.sendCommand(commandConfig.getValueToUseForZone(zone), twoDigitsVolumeFormat.format(value)); // Sent string is 2 digits, only integral part
           }
        } catch (NumberFormatException e) {
           throw new NoSuchCommandException("Invalid volume parameter value (" + parameter + ")");
        }
     }
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
   protected void updateWithResponse(MarantzResponse response)
   {
      // MVMAX comes here also, don't handle it
      // TODO: in later version, better parsing of response should mean MVMAX command is not associated with this class
      if (!response.parameter.startsWith("MAX")) {
         try {
            float value = Float.parseFloat(response.parameter);
            if (response.parameter.length() == 3) {
               // 3 characters value such as 275 mean 27.5 volume.
               value = value / 10.0f;
            }
            
            updateSensorsWithValue(value);
         } catch (NumberFormatException e) {
            // No update, this does not represent a volume
         }
      }
   }
   
   @Override
   protected void updateSensorWithValue(Sensor sensor, Object value) {
      Float sensorValue = (Float)value;
      if (sensor instanceof SwitchSensor) {
         sensor.update((sensorValue != 0)?"on":"off");
      }
      if (sensor instanceof StateSensor) { // Note this includes SwitchSensor
         sensor.update(Float.toString(sensorValue));
      } else if (sensor instanceof RangeSensor) {
         Integer parsedValue = sensorValue.intValue();
         if (sensor instanceof LevelSensor) {
            sensor.update(Integer.toString(Math.min(100, Math.max(0, parsedValue))));
         } else {
            sensor.update(Integer.toString(parsedValue));
         }
      } else {
         log.warn("Query value for incompatible sensor type (" + sensor + ")");
      }
   }

}