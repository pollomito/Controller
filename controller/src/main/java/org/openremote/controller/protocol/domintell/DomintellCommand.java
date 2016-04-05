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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.openremote.controller.command.Command;
import org.openremote.controller.exception.NoSuchCommandException;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.protocol.domintell.model.DomintellModule;

/**
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public abstract class DomintellCommand implements Command {

   // Class Members --------------------------------------------------------------------------------

   /**
    * Domintell logger. Uses a common category for all Domintell related logging.
    */
   private final static Logger log = Logger.getLogger(DomintellCommandBuilder.DOMINTELL_LOG_CATEGORY);

   // Keep a list of all the command strings we receive from OR Console and the associated command class to handle that command
   private static HashMap<String, Class<? extends DomintellCommand>> commandClasses = new HashMap<String, Class<? extends DomintellCommand>>();

   static {
      commandClasses.put("BIRON", OutputCommand.class);
      commandClasses.put("BIROFF", OutputCommand.class);
      commandClasses.put("BIRTOGGLE", OutputCommand.class);
      commandClasses.put("BIRSTATUS", OutputCommand.class);
      commandClasses.put("DMRON", OutputCommand.class);
      commandClasses.put("DMROFF", OutputCommand.class);
      commandClasses.put("DMRTOGGLE", OutputCommand.class);
      commandClasses.put("DMRSTATUS", OutputCommand.class);
      commandClasses.put("TRPON", OutputCommand.class);
      commandClasses.put("TRPOFF", OutputCommand.class);
      commandClasses.put("TRPTOGGLE", OutputCommand.class);
      commandClasses.put("TRPSTATUS", OutputCommand.class);
      commandClasses.put("DIMON", DimmerCommand.class);
      commandClasses.put("DIMOFF", DimmerCommand.class);
      commandClasses.put("DIMTOGGLE", DimmerCommand.class);
      commandClasses.put("DIMFADE", DimmerCommand.class);
      commandClasses.put("DIMSTATUS", DimmerCommand.class);
      commandClasses.put("D10ON", DimmerCommand.class);
      commandClasses.put("D10OFF", DimmerCommand.class);
      commandClasses.put("D10TOGGLE", DimmerCommand.class);
      commandClasses.put("D10FADE", DimmerCommand.class);
      commandClasses.put("D10STATUS", DimmerCommand.class);
      commandClasses.put("TSBREAD_CURRENT_TEMP", TemperatureCommand.class);
      commandClasses.put("TSBREAD_SET_POINT", TemperatureCommand.class);
      commandClasses.put("TSBSET_SET_POINT", TemperatureCommand.class);
      commandClasses.put("TSBREAD_MODE", TemperatureCommand.class);
      commandClasses.put("TSBSET_MODE", TemperatureCommand.class);
      commandClasses.put("TSBREAD_PRESET_SET_POINT", TemperatureCommand.class);
      commandClasses.put("TE1READ_CURRENT_TEMP", TemperatureCommand.class);
      commandClasses.put("TE1READ_SET_POINT", TemperatureCommand.class);
      commandClasses.put("TE1SET_SET_POINT", TemperatureCommand.class);
      commandClasses.put("TE1READ_MODE", TemperatureCommand.class);
      commandClasses.put("TE1SET_MODE", TemperatureCommand.class);
      commandClasses.put("TE1READ_PRESET_SET_POINT", TemperatureCommand.class);
      commandClasses.put("TE2READ_CURRENT_TEMP", TemperatureCommand.class);
      commandClasses.put("TE2READ_SET_POINT", TemperatureCommand.class);
      commandClasses.put("TE2SET_SET_POINT", TemperatureCommand.class);
      commandClasses.put("TE2READ_MODE", TemperatureCommand.class);
      commandClasses.put("TE2SET_MODE", TemperatureCommand.class);
      commandClasses.put("TE2READ_PRESET_SET_POINT", TemperatureCommand.class);
      commandClasses.put("LC3READ_CURRENT_TEMP", TemperatureCommand.class);
      commandClasses.put("LC3READ_SET_POINT", TemperatureCommand.class);
      commandClasses.put("LC3SET_SET_POINT", TemperatureCommand.class);
      commandClasses.put("LC3READ_MODE", TemperatureCommand.class);
      commandClasses.put("LC3SET_MODE", TemperatureCommand.class);
      commandClasses.put("LC3READ_PRESET_SET_POINT", TemperatureCommand.class);
      commandClasses.put("PBLREAD_CURRENT_TEMP", TemperatureCommand.class);
      commandClasses.put("PBLREAD_SET_POINT", TemperatureCommand.class);
      commandClasses.put("PBLSET_SET_POINT", TemperatureCommand.class);
      commandClasses.put("PBLREAD_MODE", TemperatureCommand.class);
      commandClasses.put("PBLSET_MODE", TemperatureCommand.class);
      commandClasses.put("PBLREAD_PRESET_SET_POINT", TemperatureCommand.class);
      
      commandClasses.put("IS4BEGIN_SHORT_PUSH", InputCommand.class);
      commandClasses.put("IS4END_SHORT_PUSH", InputCommand.class);
      commandClasses.put("IS4BEGIN_LONG_PUSH", InputCommand.class);
      commandClasses.put("IS4END_LONG_PUSH", InputCommand.class);
      commandClasses.put("IS4STATUS", InputCommand.class);
      commandClasses.put("IS8BEGIN_SHORT_PUSH", InputCommand.class);
      commandClasses.put("IS8END_SHORT_PUSH", InputCommand.class);
      commandClasses.put("IS8BEGIN_LONG_PUSH", InputCommand.class);
      commandClasses.put("IS8END_LONG_PUSH", InputCommand.class);
      commandClasses.put("IS8STATUS", InputCommand.class);
   }

   /**
    * Factory method for creating Domintell command instances based on a
    * human-readable configuration strings.
    * 
    * @return new Domintell command instance
    */
   static DomintellCommand createCommand(String name, DomintellGateway gateway, String moduleType, DomintellAddress address, Integer output, Integer level, Float setPoint, TemperatureMode mode) {
     log.debug("Received request to build command with name " + name);
    
      name = name.trim().toUpperCase();
      moduleType = moduleType.trim().toUpperCase();
      Class<? extends DomintellCommand> commandClass = commandClasses.get(moduleType + name);

      log.debug("This command maps to the command class " + commandClass);

      if (commandClass == null) {
         throw new NoSuchCommandException("Unknown command '" + name + "'.");
      }
      DomintellCommand cmd = null;
      try {
         Method method = commandClass.getMethod("createCommand", String.class, DomintellGateway.class, String.class, DomintellAddress.class, Integer.class, Integer.class, Float.class, TemperatureMode.class);
         log.debug("Got the creation method " + method + ", will call it");
         
         cmd = (DomintellCommand) method.invoke(null, name, gateway, moduleType, address, output, level, setPoint, mode);
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
   protected DomintellGateway gateway;

   /**
    * Name of the command
    */
   protected String name;
   
   protected String moduleType;
   
   protected DomintellAddress address;
   
   /**
    * Sensors we are a command of
    */
   protected List<Sensor> sensors = new ArrayList<Sensor>();

   // Constructors ---------------------------------------------------------------------------------

   /**
    * Constructs a Domintell command with a given gateway.
    * 
    * @param gateway
    *            Domintell gateway instance used for transmitting this command
    */
   public DomintellCommand(String name, DomintellGateway gateway, String moduleType, DomintellAddress address) {
      this.name = name;
      this.gateway = gateway;
      this.moduleType = moduleType;
      this.address = address;
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
   
   /**
    * Update all registered sensor (because of value change on given device).
    * 
    * @param device DomintellModule that triggers the update
    */
   public void updateSensors(DomintellModule module) {
      for (Sensor sensor : sensors) {
         updateSensor(module, sensor);
      }     
   }

   /**
    * Update the specified sensor based on value of given device.
    * 
    * @param device DomintellModule that triggers the update
    * @param sensor Sensor to update
    */
   abstract protected  void updateSensor(DomintellModule module, Sensor sensor);
   
   public void stop(Sensor sensor, Class<? extends DomintellModule> moduleClass) {
      removeSensor(sensor);
      if (sensors.isEmpty()) {
         // Last sensor removed, we may unregister ourself from device
         try {
            DomintellModule module = (DomintellModule) gateway.getDomintellModule(moduleType, address, moduleClass);
            if (module == null) {
              // This should never happen as above command is supposed to create device
              log.warn("Gateway could not create a module we're receiving feedback for (" + address + ")");
            }

            module.removeCommand(this);
         } catch (DomintellModuleException e) {
            log.error("Impossible to get module", e);
         }
      }
   }

}