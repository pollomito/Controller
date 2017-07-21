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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.protocol.alarm.Alarm.Day;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;

/**
 * This static class is responsible for managing the alarms
 * 
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 *
 */
class AlarmManager {
   private static final Logger log = Logger.getLogger(AlarmCommandBuilder.ALARM_PROTOCOL_LOG_CATEGORY);
   private static final int ALARM_UPDATE_DELAY_MS = 10000;
   private static final Map<String, Alarm> alarms = new HashMap<String, Alarm>();
   private static List<String> updateList = new ArrayList<String>();
   private static Scheduler scheduler;
   private static Object lock = new Object();
   private static ScheduledFuture alarmUpdater;
   private static ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
   private static Runnable updateTask;
   
   static {
      updateTask = new Runnable() {
         @Override
         public void run() {
            synchronized (lock) {
               for (String alarmName : updateList) {
                  Alarm alarm = alarms.get(alarmName);

                  if (alarm != null && alarm.enabled) {
                     startAlarm(alarm);
                  }
               }
               
               updateList.clear();
               updateConfig();
            }
         }
      };
      
      // Initialise Quartz Scheduler
      SchedulerFactory factory = new StdSchedulerFactory();
      
      try {
         scheduler = factory.getScheduler();
      } catch (SchedulerException e) {
         log.error(e);
      }
   }
   
   private AlarmManager() {
   }
   
   static void addAlarm(Alarm alarm) {
      if (alarm == null) {
         return;
      }
      
      synchronized(lock) {
         if (alarms.containsKey(alarm.name)) {
            log.error("Alarm name '" + alarm.name + "' already used");
         }

         alarms.put(alarm.name, alarm);
         
         // Schedule the alarm if it is enabled
         if (alarm.enabled) {
            startAlarm(alarm);
         }
      }
   }
   
   static void removeAlarm(String name) {
      removeAlarm(getAlarm(name));
   }
   
   static void removeAlarm(Alarm alarm) {
      if (alarm == null) {
         return;
      }
      
      synchronized(lock) {
         alarms.remove(alarm.name);
         stopAlarm(alarm);
       }
   }
  
   private static void startAlarm(Alarm alarm) {
      try {
         JobDetail alarmJob = JobBuilder.newJob(AlarmJob.class)
               .withIdentity("job1", alarm.name)
               .build();
         scheduler.scheduleJob(alarmJob, createTrigger(alarm));

         if (!scheduler.isStarted()) {
            scheduler.start();
         }
      } catch (SchedulerException e) {
         log.error("Unable to start alarm '" + alarm.name + "'", e);
      }
   }
   
   private static void stopAlarm(Alarm alarm) {      
      // Un-schedule the alarm
      try {
         scheduler.unscheduleJob(TriggerKey.triggerKey(alarm.name));
      } catch (SchedulerException e) {
         log.error("Unable to stop alarm '" + alarm.name + "'", e);
      }
   }
   
   private static void setAlarmTime(String name, boolean incrementalChange, int hours, int mins) {
      synchronized(lock) {
         Alarm alarm = alarms.get(name);
         
         if(alarm == null) {
            return;
         }
         
         // Push new time into the alarm
         alarm.setTime(incrementalChange, hours, mins);

         // stop the alarm from triggering
         stopAlarm(alarm);
         
         updateAlarm(name);
      }
   }
   
   private static void setAlarmEnabled(String name, boolean enabled) {
      synchronized(lock) {
         Alarm alarm = alarms.get(name);
         
         if(alarm == null) {
            return;
         }
         
         if (alarm.enabled == enabled) {
            return;
         }         
         
         alarm.setEnabled(enabled);
         
         // stop the alarm from triggering
         stopAlarm(alarm);
         
         updateAlarm(name);
      }
   }
   
   private static void setAlarmDay(String name, Day day, boolean enabled) {
      synchronized(lock) {
         Alarm alarm = alarms.get(name);
         
         if(alarm == null) {
            return;
         }
         
         alarm.setDay(day, enabled);
         
         // stop the alarm from triggering
         stopAlarm(alarm);
         
         updateAlarm(name);
      }
   }
   
   private static CronTrigger createTrigger(Alarm alarm) {
      try {
         // Create the cron schedule
         CronScheduleBuilder cronSchedule = CronScheduleBuilder.cronSchedule(alarm.getCronExpression());
         
         // Create the cron trigger for this alarm
         CronTrigger trigger = TriggerBuilder.newTrigger()
               .withIdentity(TriggerKey.triggerKey(alarm.name))
               .withSchedule(cronSchedule)
               .build();
   
         // Add alarm as parameter
         trigger.getJobDataMap().put(AlarmJob.ALARM_PARAMETER_NAME, alarm);
         
         return trigger;
      } catch (Exception e) {
         log.error(e);
         return null;
      }
   }
   
   private static void updateConfig() {
     synchronized(lock) {
         String configStr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                            "<alarms>\n";
         
         for (Alarm alarm : alarms.values()) {
            configStr += alarm.toXml();
         }
         
         configStr += "</alarms>";
         
         // Write the config to disk
         URI configUri = AlarmCommandBuilder.getConfigUri();
         if (configUri == null) {
            log.error("Config URI is null so cannot write config file");
         }
         
         FileWriter fileWriter = null;
         
         try {
            File file = new File(configUri);
            fileWriter = new FileWriter(file, false);
            fileWriter.write(configStr);
         } catch (IOException e) {
            log.error("Failed to write alarm config file", e);
         } finally {
            if (fileWriter != null) {
               try {
                  fileWriter.close();
               } catch (IOException e) {
                  log.error("Failed to close alarm config file writer", e);
               }
            }
         }
     }
   }
   
   private static void updateAlarm(String name) {
      // Add the alarm to the update list
      if (!updateList.contains(name)) {
         updateList.add(name);
      }
      
      // Use a scheduler to prevent excessive updates
      if (alarmUpdater != null) {
         alarmUpdater.cancel(false);
      }
      
      alarmUpdater = executor.schedule(updateTask, ALARM_UPDATE_DELAY_MS, TimeUnit.MILLISECONDS);
   }
   
   static void sendAlarmCommand(AlarmCommand command) {
      switch (command.getAction()) {
         case DAY:
            Day day = Day.valueOf(command.getArgs()[0]);
            setAlarmDay(command.getAlarmName(), day, Boolean.parseBoolean(command.getArgs()[1]));
            break;
         case ENABLED:
            setAlarmEnabled(command.getAlarmName(), Boolean.parseBoolean(command.getArgs()[0]));
            break;
         case TIME:
            setAlarmTime(command.getAlarmName(), false, Integer.parseInt(command.getArgs()[0]), Integer.parseInt(command.getArgs()[1]));
            break;
         case TIME_RELATIVE:
            Pair<Integer, Integer> hoursAndMins = AlarmCommand.getHoursAndMinsFromArgs(command.getArgs());
            if (hoursAndMins != null) {
               setAlarmTime(command.getAlarmName(), true, hoursAndMins.getLeft(), hoursAndMins.getRight());
            }
            break;
      }
   }


   
   static void addSensor(AlarmCommand command, Sensor sensor) {      
      // Ensure command is a read command
      if (!command.getAction().isRead()) {
         return;
      }
      
      synchronized(lock) {
         Alarm alarm = getAlarm(command);
         
         if (alarm != null) {
            alarm.addSensor(command, sensor);
         }
      }
   }
   
   static void removeSensor(AlarmCommand command, Sensor sensor) {
      // Ensure command is a read command
      if (!command.getAction().isRead()) {
         return;
      }
      
      synchronized(lock) {
         Alarm alarm = getAlarm(command);
         
         if (alarm != null) {
            alarm.removeSensor(command, sensor);
         }         
      }
   }
   
   private static Alarm getAlarm(AlarmCommand command) {
      return getAlarm(command.getAlarmName());
   }
   
   private static Alarm getAlarm(String name) {
      Alarm alarm = null;
      
      synchronized(lock) {
         alarm = alarms.get(name);
         
//         for (Alarm alarm : alarms) {
//            if (alarm.name.equalsIgnoreCase(name)) {
//               result = alarm;
//               break;
//            }
//         }
      }
      
      return alarm;
   }
}
