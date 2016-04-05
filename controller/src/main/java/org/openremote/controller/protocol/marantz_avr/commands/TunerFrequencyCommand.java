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
 * Specific command to handle tuner frequency.
 * 
 * Only handles custom sensor type and returns formatted string with units.
 * 
 * This command does NOT support zones.
 * 
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class TunerFrequencyCommand extends MarantzAVRCommand implements ExecutableCommand, EventListener {

   /**
    * Marantz AVR logger. Uses a common category for all Marantz AVR related logging.
    */
   protected final static Logger log = Logger.getLogger(MarantzAVRCommandBuilder.MARANTZ_AVR_LOG_CATEGORY);

   public static TunerFrequencyCommand createCommand(CommandConfig commandConfig, String name, MarantzAVRGateway gateway, String parameter, String zone) {
      // Check for mandatory attributes
      if (parameter == null) {
        throw new NoSuchCommandException("A parameter is always required for the Tuner Frequency command.");
      }

      return new TunerFrequencyCommand(name, gateway, parameter);
    }

   public TunerFrequencyCommand(String name, MarantzAVRGateway gateway, String parameter) {
      super(name, gateway);
      this.parameter = parameter;
      frequencyFormat = NumberFormat.getInstance();
      frequencyFormat.setMinimumFractionDigits(2);
      frequencyFormat.setMaximumFractionDigits(2);
   }

   // Private Instance Fields ----------------------------------------------------------------------

   /**
    * Parameter used by this command.
    */
   private String parameter;
   
   private NumberFormat frequencyFormat;

   // Implements ExecutableCommand -----------------------------------------------------------------

   /**
    * {@inheritDoc}
    */
   public void send() {
     if ("STATUS".equals(parameter)) {
        gateway.sendCommand("TFAN",  "?");
     } else if ("UP".equals(parameter) || "DOWN".equals(parameter)) {
        gateway.sendCommand("TFAN", parameter);
     } else {
        // This should then be a value, for now, just use as is as frequency value
        try {
           
           // TODO : validation and potentially reformatting
           
           gateway.sendCommand("TFAN", parameter);
        } catch (NumberFormatException e) {
           throw new NoSuchCommandException("Invalid frequency parameter value (" + parameter + ")");
        }
     }
   }

   // Implements EventListener -------------------------------------------------------------------

   @Override
   public void setSensor(Sensor sensor) {
       if (sensors.isEmpty()) {
          
          // First sensor registered, we also need to register ourself with the gateway
          gateway.registerCommand("TF", this);
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
         gateway.unregisterCommand("TF", this);
      }
   }
   
   @Override
   protected void updateWithResponse(MarantzResponse response)
   {
      try {
         if (response.parameter.startsWith("AN")) {
            int frequency = Integer.parseInt(response.parameter.substring(2));
            if (frequency > 50000) { // AM
               updateSensorsWithValue(frequencyFormat.format((float)frequency / 100.0) + " kHz");
            } else {
               updateSensorsWithValue(frequencyFormat.format((float)frequency / 100.0) + " MHz");
            }
         }
      } catch (NumberFormatException e) {
         // Just ignore, seems some undocumented feedback is sent
      }
   }
   
   @Override
   protected void updateSensorWithValue(Sensor sensor, Object value) {
      if (sensor instanceof StateSensor && !(sensor instanceof SwitchSensor)) { // SwitchSensor not supported
         sensor.update((String)value);
      } else {
         log.warn("Query value for incompatible sensor type (" + sensor + ")");
      }
   }
}
