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
package org.openremote.controller.model.event;

import org.junit.Test;
import org.junit.Assert;

/**
 * Basic tests for {@link org.openremote.controller.model.event.Range} class.  <p>
 *
 * (ORCJAVA-85 -- http://jira.openremote.org/browse/ORCJAVA-85)
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class RangeTest
{

  /**
   * Basic constructor and validation.
   */
  @Test public void basicConstruction()
  {
    Range r = new Range(0, "name", 50, 10, 80);

    Assert.assertTrue(r.getValue() == 50);
    Assert.assertTrue(r.getSource().equals("name"));
    Assert.assertTrue(r.getSourceID() == 0);
    Assert.assertTrue(r.getMinValue() == 10);
    Assert.assertTrue(r.getMaxValue() == 80);

    Assert.assertTrue(r.serialize().equals("50"));

    Assert.assertTrue(r.toString() != null);
    Assert.assertFalse(r.toString().equals(""));
  }

  /**
   * Basic constructor and validation with value parameter above max limit.
   */
  @Test public void basicConstructionMaxLimit() throws Exception
  {
    Range r = new Range(3, "xXx", 111, -10, 110);

    Assert.assertTrue(r.getValue() == 110);
    Assert.assertTrue(r.getSource().equals("xXx"));
    Assert.assertTrue(r.getSourceID() == 3);
    Assert.assertTrue(r.getMinValue() == -10);
    Assert.assertTrue(r.getMaxValue() == 110);

    Assert.assertTrue(r.serialize().equals("110"));

    Assert.assertTrue(r.toString() != null);
    Assert.assertFalse(r.toString().equals(""));
  }

  /**
   * Basic constructor and validation with value parameter below min limit.
   */
  @Test public void basicConstructionMinLimit() throws Exception
  {
    Range r = new Range(444, "aaa", -11, -10, 100);

    Assert.assertTrue(r.getValue() == -10);
    Assert.assertTrue(r.getSource().equals("aaa"));
    Assert.assertTrue(r.getSourceID() == 444);
    Assert.assertTrue(r.getMinValue() == -10);
    Assert.assertTrue(r.getMaxValue() == 100);

    Assert.assertTrue(r.serialize().equals("-10"));

    Assert.assertTrue(r.toString() != null);
    Assert.assertFalse(r.toString().equals(""));
  }

  @Test public void testEquals() throws Exception
  {
    Range r1 = new Range(1, "Source-1", 50, -10, 110);
    Range r2 = new Range(1, "Source-1", 50, -10, 110);

    Assert.assertTrue(r1.equals(r1));
    Assert.assertTrue(r1.equals(r2));
    Assert.assertTrue(r2.equals(r1));
    Assert.assertTrue(r1.hashCode() == r2.hashCode());


    Range r3 = new Range(2, "Source-1", 50, -10, 110);

    Assert.assertFalse(r3.equals(r1));


    Range r4 = new Range(1, "Source-2", 50, -10, 110);

    Assert.assertFalse(r4.equals(r1));


    Range r5 = new Range(1, "Source-1", 0, -10, 110);

    Assert.assertFalse(r5.equals(r1));


    Range r6 = new Range(1, "Source-1", 50, 0, 110);

    Assert.assertTrue(r6.equals(r1));


    Range r7 = new Range(1, "Source-1", 50, -10, 90);

    Assert.assertTrue(r7.equals(r1));


    Level l = new Level(1, "Source-1", 50);

    Assert.assertFalse(l.equals(r1));
    Assert.assertFalse(r1.equals(l));
  }
}

