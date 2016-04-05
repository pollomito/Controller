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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.openremote.controller.command.Command;
import org.openremote.controller.component.LevelSensor;
import org.openremote.controller.component.RangeSensor;
import org.openremote.controller.exception.ConfigurationException;
import org.openremote.controller.exception.NoSuchCommandException;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.model.sensor.StateSensor;
import org.openremote.controller.protocol.marantz_avr.MarantzAVRGateway.MarantzResponse;
import org.openremote.controller.utils.Logger;
import org.openremote.controller.utils.Strings;

/**
 * Base class for all Commands understood by Marantz AVR protocol.
 * 
 * Reads configuration of all known commands from marantz_avr_config.xml file.
 * 
 * Provides a basic mechanism to register and update sensors.
 * 
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public abstract class MarantzAVRCommand implements Command {

   // Class Members --------------------------------------------------------------------------------

   /**
    * Marantz AVR logger. Uses a common category for all Marantz AVR related logging.
    */
   private final static Logger log = Logger.getLogger(MarantzAVRCommandBuilder.MARANTZ_AVR_LOG_CATEGORY);

   // Keep a list of all the command strings we receive from OR Console and the associated command class to handle that command
   private static Map<String, CommandConfig> commandConfigurations;

   static {
      URL configResource = Thread.currentThread().getContextClassLoader().getResource("org/openremote/controller/protocol/marantz_avr/marantz_avr_config.xml");
      log.debug("Marantz configuration file is " + configResource);
      
      if (configResource != null) {
         try {
            commandConfigurations = CommandConfigurationParser.parseCommandConfiguration(configResource);
         } catch (ConfigurationException e) {
            log.error("Configuration of commands for Marantz AVR protocol failed.", e);
         }
      }
   }

   /** 
    * Factory method for creating Marantz AVR command instances based on a
    * human-readable configuration strings.
    * 
    * @return new Marantz AVR command instance
    */
   static MarantzAVRCommand createCommand(String name, MarantzAVRGateway gateway, String parameter, String zone) {
      log.debug("Received request to build command with name " + name);
    
      name = Strings.toUpperCase(name.trim());
      CommandConfig commandConfig = commandConfigurations.get(name);
      log.debug("This command maps to command config " + commandConfig);

      if (commandConfig == null) {
         throw new NoSuchCommandException("Unknown command '" + name + "'.");
      }
      
      Class<? extends MarantzAVRCommand> commandClass = commandConfig.getCommandClass();
      MarantzAVRCommand cmd = null;
      try {
         Method method = commandClass.getMethod("createCommand", CommandConfig.class, String.class, MarantzAVRGateway.class, String.class, String.class);
         log.debug("Got the creation method " + method + ", will call it");
         
         cmd = (MarantzAVRCommand) method.invoke(null, commandConfig, name, gateway, parameter, zone);
         log.debug("Creation successfull, got command " + cmd);
      } catch (SecurityException e) {
         // TODO: should this be logged, check other source code
         throw new NoSuchCommandException("Impossible to create command '" + name + "'.");
      } catch (NoSuchMethodException e) {
         // TODO: should this be logged, check other source code
         throw new NoSuchCommandException("Impossible to create command '" + name + "'.");
      } catch (IllegalArgumentException e) {
         // TODO: should this be logged, check other source code
         throw new NoSuchCommandException("Impossible to create command '" + name + "'.");
      } catch (IllegalAccessException e) {
         // TODO: should this be logged, check other source code
         throw new NoSuchCommandException("Impossible to create command '" + name + "'.");
      } catch (InvocationTargetException e) {
        if (e.getCause() instanceof NoSuchCommandException) {       
        // This means method threw an exception, re-throw it as is
        throw (NoSuchCommandException)e.getCause();
        } else {
          throw new NoSuchCommandException("Impossible to create command '" + name + "'.");
        }
      // TODO: should this be logged, check other source code
      }
      return cmd;
   }
   
   // Instance Fields ------------------------------------------------------------------------------

   /**
    * Gateway to be used to transmit this command.
    */
   protected MarantzAVRGateway gateway;

   /**
    * Name of the command
    */
   protected String name;
   
   /**
    * Sensors we are a command of
    */
   protected List<Sensor> sensors;
   
   // Constructors ---------------------------------------------------------------------------------

   /**
    * Constructs a Marantz AVR command with a given gateway.
    * 
    * @param gateway Marantz AVR gateway instance used for transmitting this command
    */
   public MarantzAVRCommand(String name, MarantzAVRGateway gateway) {
      this.name = name;
      this.gateway = gateway;
      this.sensors = new ArrayList<Sensor>();
   }
   
   /**
    * Add a sensor to update on value change.
    * 
    * @param sensor Sensor to add
    */
   public void addSensor(Sensor sensor) {
      sensors.add(sensor);
   }
   
   /**
    * Remove a sensor to update on value change.
    * 
    * @param sensor Sensor to remove
    */
   public void removeSensor(Sensor sensor) {
      sensors.remove(sensor);
   }
   
   abstract protected void updateWithResponse(MarantzResponse response);   
   
   /**
    * Update all registered sensors with provided value).
    */
   public void updateSensorsWithValue(Object value) {
      for (Sensor sensor : sensors) {
         updateSensorWithValue(sensor, value);
      }     
   }

   /**
    * Update the specified sensor with provided value.
    * 
    * Converts to provided value as appropriate for the type of sensor to be updated.
    * 
    * @param sensor Sensor to update
    */
   protected void updateSensorWithValue(Sensor sensor, Object value) {
      if (sensor instanceof StateSensor) { // Note this includes SwitchSensor
         sensor.update(value.toString());
      } else if (sensor instanceof RangeSensor) {
         try {
            Integer parsedValue = null;
            if (value instanceof Integer) {
               parsedValue = (Integer)value;
            }
            parsedValue = Integer.parseInt(value.toString());
            if (sensor instanceof LevelSensor) {
               sensor.update(Integer.toString(Math.min(100, Math.max(0, parsedValue))));
            } else {
               sensor.update(Integer.toString(parsedValue));
            }
         } catch (NumberFormatException e){
            log.warn("Received value (" + value + ") invalid, cannot be converted to integer");
         }
      } else{
         log.warn("Query level value for incompatible sensor type (" + sensor + ")");
      }
   }

}
