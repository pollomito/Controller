/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2016, OpenRemote Inc.
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
package org.openremote.controller.protocol.alarm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.openremote.controller.component.LevelSensor;
import org.openremote.controller.component.RangeSensor;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.model.sensor.StateSensor;
import org.springframework.util.StringUtils;

/**
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 *
 */
class Alarm {
   enum Day {
      SUN,
      MON,
      TUE,
      WED,
      THU,
      FRI,
      SAT;
      
      private static Day[] values = Day.values(); 
      static final EnumSet<Day> ALL_OPTS = EnumSet.allOf(Day.class);
      
      public static EnumSet<Day> fromString(String str) {
         String[] arr = str.split(",");
         EnumSet<Day> set = EnumSet.noneOf(Day.class);
         for (String e : arr) {
            Day day = null;
            
            try {
               int i = Integer.parseInt(e);
               day = values[i-1];
            } catch (NumberFormatException ex) {
               try {
                  day = Day.valueOf(e.trim());
               } catch(Exception ex2) {
                  log.error("Cannot convert cron value for days of week to a day", ex2);
               }
            }
            if (day != null && !set.contains(day)) {
               set.add(day);
            }
         }
         return set;
     }
   }
   
   private static final Logger log = Logger.getLogger(AlarmCommandBuilder.ALARM_PROTOCOL_LOG_CATEGORY);
   boolean enabled;
   String name;
   List<AlarmCommandRef> commands;
   private String[] cronExpression;
   private int hours;
   private int mins;
   private EnumSet<Day> days = EnumSet.noneOf(Day.class);
   private Map<String, List<Pair<AlarmCommand,Sensor>>> sensorMap = new HashMap<String, List<Pair<AlarmCommand,Sensor>>>();
   private Map<String, Object> propMap = new HashMap<String, Object>();
   private boolean initialised = false;
   
   Alarm(String name, List<AlarmCommandRef> commands, String cronExpression, boolean enabled)
   {
      this.name = name;
      this.commands = commands;
      int mins = 0;
      int hours = 0;
      
      // Extract hours, mins and days of week from the cron expression
      // just in case user wants to change or display them
      this.cronExpression = cronExpression.split("\\s+");
      
      if (this.cronExpression.length < 6) {
         log.error("Invalid cron expression for alarm '" + cronExpression + "'");
         return;
      }
      
      String minsStr = this.cronExpression[1];
      String hoursStr = this.cronExpression[2];
      String dowStr = this.cronExpression[5];
      
      // Try and parse these strings
      try {
         mins = Integer.parseInt(minsStr);
         hours = Integer.parseInt(hoursStr);
      } catch(NumberFormatException e) {
         log.info("Cannot interpret alarm time from cron expression '" + cronExpression + "'");
      }
      
      this.days = Day.fromString(dowStr);
      this.mins = mins;
      this.hours = hours;
      setEnabled(enabled);
   }
   
   synchronized void setTime(boolean incrementalChange, int hours, int mins) {
      if (!initialised) {
         initialise();
      }
      
      if (incrementalChange) {
         mins = this.mins + mins;
         hours = this.hours + hours;
      }
      
      Pair<Integer,Integer> hoursAndMins = sanitiseHoursAndMins(hours, mins);
      
      // Update the alarm
      this.hours = hoursAndMins.getLeft();
      this.mins = hoursAndMins.getRight();
      cronExpression[1] = Integer.toString(mins);
      cronExpression[2] = Integer.toString(hours);
      
      // Get prop name
      String propName = AlarmCommand.Action.TIME.getAttributeName();
      
      // Notify the sensors
      updatePropAndSensors(propName, getTime());
   }
   
   synchronized void setDay(Day day, boolean enabled) {
      if (!initialised) {
         initialise();
      }
      
      if (!enabled && days.size() == 1 && days.contains(day)) {
         // Must have at least one day enabled
         return;
      }
      
      if (enabled) {
         days.add(day);
      } else {
         days.remove(day);
      }
      
      cronExpression[5] = getDaysCronExpression();
      
      // Notify the sensors
      updatePropAndSensors(AlarmCommand.Action.DAY.getAttributeName() + "_" + day.toString(),  enabled ? "on" : "off");
   }
   
   synchronized void setEnabled(boolean enabled) {
      this.enabled = enabled;
      
      // Update sensors
      updatePropAndSensors(AlarmCommand.Action.ENABLED.getAttributeName(), enabled ? "on" : "off");
   }
   
   synchronized String getCronExpression() {
      return StringUtils.arrayToDelimitedString(cronExpression, " ").trim();
   }
   
   synchronized void addSensor(AlarmCommand command, Sensor sensor) {
      String propertyName = getAttributeName(command);
      
      if (propertyName != null) {
         propertyName = propertyName.toUpperCase();
         List<Pair<AlarmCommand,Sensor>> sensors = sensorMap.get(propertyName);
         
         if (sensors == null) {
           sensors = new ArrayList<Pair<AlarmCommand,Sensor>>();
           sensorMap.put(propertyName, sensors);
         }
         
         sensors.add(Pair.of(command, sensor));
         
         // If sensor is anything but enabled status then initialise the time and DOW
         if (!initialised && command.getAction() != AlarmCommand.Action.ENABLED_STATUS) {
            initialise();
         }
         
         // Update the value of the sensor with value from cache
         Object currentValue = propMap.get(propertyName);
         updateSensor(command, sensor, currentValue != null ? currentValue.toString() : "N/A");
       }
    }
    
   synchronized void removeSensor(AlarmCommand command, Sensor sensor) {
      for(Entry<String, List<Pair<AlarmCommand,Sensor>>> entry : sensorMap.entrySet())
      {
         Pair<AlarmCommand,Sensor> toRemove = null;
         for (int i=0; i<entry.getValue().size(); i++) {
            Pair<AlarmCommand,Sensor> commandSensorPair = entry.getValue().get(i);
            if (commandSensorPair.getRight() == sensor) {
               toRemove = commandSensorPair;
               break;
            }
         }
         if (toRemove != null) {
            entry.getValue().remove(toRemove);
            break;
         }
      }
    }
      
   String toXml() {
      String commandStr = "";
      
      for (AlarmCommandRef command : commands) {
         commandStr += "<commandRef device=\"" + command.deviceName + "\" name=\""+ command.commandName + "\" delay=\"" + command.commandDelay + "\" />\n";
      }
      
      String str =   "<alarm name=\"" + name + "\">\n" +
                     "<cronExpression>" + getCronExpression() + "</cronExpression>\n" +
                     "<commands>\n" +
                     commandStr +
                     "</commands>\n" +
                     "<enabled>" + enabled + "</enabled>\n" +
                     "</alarm>\n";
      
      return str;
   }

   private String getTime() {
      return getTimeAsString(hours, mins);
   }

   private static String getTimeAsString(int hours, int mins) {
       return String.format("%02d:%02d", hours, mins);
   }
   
   private String getAttributeName(AlarmCommand command) {
      String name = null;
      
      switch (command.getAction()) {
         case DAY_STATUS:
         case DAY:
            name = command.getAction().getAttributeName();

            // Add DOW SUFFIX
            name += "_" + command.getArgs()[0];
            break;
         default:
            name = command.getAction().getAttributeName();
      }
      return name;
   }
   
   private void updatePropAndSensors(String propName, String value) {
      if (propName == null) {
         return;
      }
      
      // Update the prop map
      propMap.put(propName, value);
      
      List<Pair<AlarmCommand,Sensor>> commandSensorPairs = sensorMap.get(propName);
      if (commandSensorPairs != null) {
         for (Pair<AlarmCommand,Sensor> commandSensorPair : commandSensorPairs) {
            AlarmCommand command = commandSensorPair.getLeft();
            Sensor sensor = commandSensorPair.getRight();
            updateSensor(command, sensor, value);
         }
      }
   }
   
   private void updateSensor(AlarmCommand command, Sensor sensor, String sensorValue) {
      if (sensor == null) {
        return;
      }

       if (command.getAction() == AlarmCommand.Action.TIME_STATUS) {
           // Check for offset value
           Pair<Integer,Integer> hoursAndMins = AlarmCommand.getHoursAndMinsFromArgs(command.getArgs());
           if (hoursAndMins != null) {
               hoursAndMins = sanitiseHoursAndMins(this.hours + hoursAndMins.getLeft(), this.mins + hoursAndMins.getRight());
               sensorValue = getTimeAsString(hoursAndMins.getLeft(), hoursAndMins.getRight());
           }
       }
      
      if (sensor instanceof StateSensor) {
        // State sensors are case sensitive and expect lower case
        sensorValue = sensorValue.toLowerCase();
      } else if (sensor instanceof RangeSensor) {
        try {
          // Value must be an integer
          BigDecimal parsedValue = new BigDecimal(sensorValue);
          
          if (sensor instanceof LevelSensor) {
             sensorValue = Integer.toString(Math.min(100, Math.max(0, parsedValue.intValue())));
          } else {
            sensorValue = Integer.toString(parsedValue.intValue());
          }
        } catch (NumberFormatException e) {
           log.warn("Received value (" + sensorValue + ") invalid, cannot be converted to integer");
           sensorValue = "0";
        }
      }
      
      sensor.update(sensorValue);
    }
   
   private String getDaysCronExpression() {
      String dayStr = "";
     
      for (Day d : days) {
         dayStr += d.toString() + ",";
      }
      dayStr = dayStr.substring(0, dayStr.length()-1);
      
      return dayStr;
   }
   
   private void initialise() {
      initialised = true;
      
      // Call setters so values are pushed into the prop map
      setTime(false, hours, mins);
      
      if (days.size() == 0) {
         // Enable all days
         days = Day.ALL_OPTS;
      }
      
      for (Day day : Day.values) {
         setDay(day, days.contains(day));
      }
      
      // Overwrite existing cron args so alarm is now time and DOW based
      cronExpression[0] = "0";
      cronExpression[3] = "?";
      cronExpression[4] = "*";
      cronExpression[5] = getDaysCronExpression();
   }


   private static Pair<Integer,Integer> sanitiseHoursAndMins(int hours, int mins) {
       // Sanity check hours and mins
       int sign = mins >= 0 ? 1 : -1;
       mins = Math.abs(mins);
       int minHours = (int)Math.floor(((double)mins)/60);
       mins = mins % 60;
       hours += (minHours * sign);


       if (sign == -1 && mins > 0) {
           hours--;
           mins = 60-mins;
       }

       if (hours < 0) {
           hours = 24-hours;
       }

       hours = Math.min(23, Math.max(0, hours));
       mins = Math.min(59, Math.max(0, mins));
       return Pair.of(hours, mins);
   }
}
