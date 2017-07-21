/*
 * Copyright 2017, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
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

import org.junit.Assert;
import org.junit.Test;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.model.sensor.StateSensor;
import org.openremote.controller.statuscache.StatusCache;

import java.util.Arrays;

public class AlarmProtocolCommandTest {

    @Test
    public void incrementDecrementAlarmTime() {
        Alarm alarm = new Alarm("Test", null, "0 35 15 ? * SUN,MON,TUE,WED,THU,FRI,SAT", true);
        AlarmCommand alarmReadCommand = new AlarmCommand(AlarmCommand.Action.TIME_STATUS, "Test", null);
        StatusCache statusCache = new StatusCache();
        StateSensor sensor = new StateSensor("Test", 1, statusCache, alarmReadCommand, 1, null);
        sensor.setStrictStateMapping(false);
        AlarmManager.addAlarm(alarm);
        AlarmManager.addSensor(alarmReadCommand, sensor);

        // check sensor value in the status cache
        String status = statusCache.queryStatus(1);
        Assert.assertEquals("15:35", status);

        // Increment the alarm time by 1hr
        AlarmCommand alarmWriteCommand = new AlarmCommand(AlarmCommand.Action.TIME_RELATIVE, "Test", new String[] {"+01", "00"});
        AlarmManager.sendAlarmCommand(alarmWriteCommand);

        status = statusCache.queryStatus(1);
        Assert.assertEquals("16:35", status);

        // Deccrement the alarm time by 2:45
        alarmWriteCommand = new AlarmCommand(AlarmCommand.Action.TIME_RELATIVE, "Test", new String[] {"-2", "-45"});
        AlarmManager.sendAlarmCommand(alarmWriteCommand);

        status = statusCache.queryStatus(1);
        Assert.assertEquals("13:50", status);
    }

    @Test
    public void relativeAlarmTimeDisplay() {
        Alarm alarm = new Alarm("Test", null, "0 35 15 ? * SUN,MON,TUE,WED,THU,FRI,SAT", true);
        AlarmCommand alarmReadCommand1 = new AlarmCommand(AlarmCommand.Action.TIME_STATUS, "Test", new String[] {"+01", "00"});
        AlarmCommand alarmReadCommand2 = new AlarmCommand(AlarmCommand.Action.TIME_STATUS, "Test", new String[] {"+2", "-50"});
        StatusCache statusCache = new StatusCache();
        StateSensor sensor1 = new StateSensor("Test", 1, statusCache, alarmReadCommand1, 1, null);
        StateSensor sensor2 = new StateSensor("Test2", 2, statusCache, alarmReadCommand2, 2, null);
        sensor1.setStrictStateMapping(false);
        sensor2.setStrictStateMapping(false);
        AlarmManager.addAlarm(alarm);
        AlarmManager.addSensor(alarmReadCommand1, sensor1);
        AlarmManager.addSensor(alarmReadCommand2, sensor2);

        // check sensor value in the status cache
        String status = statusCache.queryStatus(1);
        Assert.assertEquals("16:35", status);

        // check sensor value in the status cache
        status = statusCache.queryStatus(2);
        Assert.assertEquals("16:45", status);
    }
}
