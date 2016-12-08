/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2011, OpenRemote Inc.
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
package org.openremote.controller.component;

import org.junit.Test;
import org.junit.Assert;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.protocol.ReadCommand;
import org.openremote.controller.statuscache.StatusCache;

/**
 * Tests for {@link org.openremote.controller.component.RangeSensor}.
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class RangeSensorTest
{


  @Test public void testRangeBoundaryEnforcement()
  {
    StatusCache cache = new StatusCache();


    DummyCommand cmd1 = new DummyCommand("-10");

    Sensor s1 = new RangeSensor("test1", 1, cache, cmd1, 1, -10, 10);
    cache.registerSensor(s1);
    s1.start();

    waitForUpdate();

    Assert.assertTrue(cache.queryStatus(1).equals("-10"));


    DummyCommand cmd2 = new DummyCommand("10");

    Sensor s2 = new RangeSensor("test2", 2, cache, cmd2, 2, -10, 10);
    cache.registerSensor(s2);
    s2.start();

    waitForUpdate();

    Assert.assertTrue(cache.queryStatus(2).equals("10"));


    DummyCommand cmd3 = new DummyCommand("-11");

    Sensor s3 = new RangeSensor("test3", 3, cache, cmd3, 3, -10, 10);
    cache.registerSensor(s3);
    s3.start();

    waitForUpdate();

    Assert.assertTrue(cache.queryStatus(3).equals("-10"));


    DummyCommand cmd4 = new DummyCommand("11");

    Sensor s4 = new RangeSensor("test4", 4, cache, cmd4, 4, -10, 10);
    cache.registerSensor(s4);
    s4.start();

    waitForUpdate();

    Assert.assertTrue(cache.queryStatus(4).equals("10"));
  }


  // Nested Classes -------------------------------------------------------------------------------

  private static class DummyCommand extends ReadCommand
  {
    private String value;

    DummyCommand(String value)
    {
      this.value = value;
    }

    public String read(Sensor s)
    {
      return value;
    }
  }


  // Helper Methods -------------------------------------------------------------------------------

  private void waitForUpdate()
  {
    try
    {
      Thread.sleep(ReadCommand.POLLING_INTERVAL * 2);
    }

    catch (InterruptedException e)
    {
      junit.framework.Assert.fail(e.getMessage());
    }
  }
}

