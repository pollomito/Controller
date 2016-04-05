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

import org.apache.log4j.Logger;
import org.openremote.controller.exception.ControllerRESTAPIException;
import org.openremote.controller.service.CommandService;
import org.openremote.controller.service.ServiceContext;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 *
 */
public class AlarmJob implements Job {
   static final String ALARM_PARAMETER_NAME = "Alarm";
   private static Logger log = Logger.getLogger(AlarmCommandBuilder.ALARM_PROTOCOL_LOG_CATEGORY);
   private final static CommandService commandService  = ServiceContext.getCommandService();
   
   public AlarmJob() {
   }
   
   @Override
   public void execute(JobExecutionContext context) throws JobExecutionException {
      
      // Get the alarm
      Alarm alarm = (Alarm)context.getMergedJobDataMap().get(ALARM_PARAMETER_NAME);
      
      // Execute the alarm commands
      System.out.println("ALARM '" + alarm.name + "' TRIGGERED");
      log.debug("Executing alarm '" + alarm.name + "'");

      for (AlarmCommandRef commandRef : alarm.commands)
      {         
         String commandName = commandRef.commandName;
         long delay = commandRef.commandDelay;
         
         // Sleep for delay period
         if (delay > 0) {
            try {
               Thread.sleep(delay);
            } catch (InterruptedException e) {
               log.info("Alarm execution interrupted", e);
            }
         }
         
         log.debug("ALARM '" + alarm.name + "' SEND COMMAND '" + commandName + "'");
         
         try {
            commandService.execute(commandRef.deviceName, commandName, commandRef.commandParameter);
         } catch (ControllerRESTAPIException e) {
            log.error("ALARM '" + alarm.name + "' SEND COMMAND '" + commandName + "' FAILED", e);
         }
      }
   }
}
