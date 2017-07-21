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

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.protocol.EventListener;

/**
 * Alarm commands are used to modify the alarms defined in the config file.
 * Alarms must first be created in the config file in order to be used.
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 *
 */
public class AlarmCommand implements ExecutableCommand, EventListener  {
   public enum Action {
      ENABLED(false, "ENABLED"),
      ENABLED_STATUS(true, "ENABLED"),
      TIME(false, "TIME"),
      TIME_RELATIVE(false, "TIME"),
      TIME_STATUS(true, "TIME"),
      DAY(false, "DAY"),
      DAY_STATUS(true, "DAY");
      
      private boolean isRead;
      private String attributeName;

      private Action(boolean isRead, String attributeName)
      {
        this.isRead = isRead;
        this.attributeName = attributeName;
      }
      
      public boolean isRead()
      {
        return isRead;
      }
      
      public String getAttributeName() {
         return attributeName;
      }
    }
   
   private static Logger log = Logger.getLogger(AlarmCommandBuilder.ALARM_PROTOCOL_LOG_CATEGORY);
   private Action action;
   private String[] args;
   private String alarmName;

   public AlarmCommand(Action action, String alarmName, String[] args) {
      this.alarmName = alarmName;
      this.action = action;
      this.args = args;
   }
   
   public Action getAction() {
      return action;
   }
   
   public String[] getArgs() {
      return args;
   }
   
   public String getAlarmName() {
      return alarmName;
   }

   @Override
   public void send() {
     if (action.isRead()) {
        log.info("Read command is being used for sending commands, this won't work so ignoring the request");
        return;
     }
     
     AlarmManager.sendAlarmCommand(this);
   }

   /* (non-Javadoc)
    * @see org.openremote.controller.protocol.EventListener#setSensor(org.openremote.controller.model.sensor.Sensor)
    */
   @Override
   public void setSensor(Sensor sensor) {
      AlarmManager.addSensor(this, sensor);
   }

   /* (non-Javadoc)
    * @see org.openremote.controller.protocol.EventListener#stop(org.openremote.controller.model.sensor.Sensor)
    */
   @Override
   public void stop(Sensor sensor) {
      AlarmManager.removeSensor(this, sensor);
   }

   static Pair<Integer,Integer> getHoursAndMinsFromArgs(String[] args) {
      if (args == null || args.length != 2) {
         return null;
      }

      String hoursStr = args[0];
      String minsStr = args[1];
      try {
         int hours = Integer.parseInt((hoursStr.charAt(0) == '+') ? hoursStr.substring(1) : hoursStr);
         int mins = Integer.parseInt((minsStr.charAt(0) == '+') ? minsStr.substring(1) : minsStr);
         return Pair.of(hours, mins);
      } catch (NumberFormatException e) {
         log.error("Invalid command value must be +/-HH:+/-MM e.g. 01:00 or +2:10 or 0:-30", e);
      }
      return null;
   }
}
