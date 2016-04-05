/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2012, OpenRemote Inc.
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
package org.openremote.controller.protocol.enocean.datatype;

import org.junit.Test;
import org.junit.Assert;
import org.openremote.controller.protocol.enocean.profile.EepOutOfRangeException;

/**
 * Unit tests for {@link BoolScale} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class BoolScaleTest
{

  // Tests ----------------------------------------------------------------------------------------

  @Test public void testBasicConstruction() throws Exception
  {
    ScaleCategory trueCategory = new ScaleCategory("True category", 1, 1, "on", 1);
    ScaleCategory falseCategory = new ScaleCategory("False category", 0, 0, "on", 0);

    BoolScale scale = new BoolScale(trueCategory, falseCategory);

    ScaleCategory category = scale.scaleRawValue(0);

    Assert.assertEquals(scale.getFalseCategory(), category);
  }

  @Test public void testScaling() throws Exception
  {
    ScaleCategory trueCategory = new ScaleCategory("True category", 1, 1, "on", 1);
    ScaleCategory falseCategory = new ScaleCategory("False category", 0, 0, "on", 0);

    BoolScale scale = new BoolScale(trueCategory, falseCategory);

    ScaleCategory category = scale.scaleRawValue(0);

    Assert.assertEquals(scale.getFalseCategory(), category);
    Assert.assertFalse(scale.isTrue(category));


    category = scale.scaleRawValue(1);

    Assert.assertEquals(scale.getTrueCategory(), category);
    Assert.assertTrue(scale.isTrue(category));


    trueCategory = new ScaleCategory("True category", 0, 0, "on", 1);
    falseCategory = new ScaleCategory("False category", 1, 1, "on", 0);

    scale = new BoolScale(trueCategory, falseCategory);

    category = scale.scaleRawValue(0);

    Assert.assertEquals(scale.getTrueCategory(), category);
    Assert.assertTrue(scale.isTrue(category));


    category = scale.scaleRawValue(1);

    Assert.assertEquals(scale.getFalseCategory(), category);
    Assert.assertFalse(scale.isTrue(category));


    trueCategory = new ScaleCategory("True category", 0, 5, "on", 1);
    falseCategory = new ScaleCategory("False category", 6, 10, "on", 0);

    scale = new BoolScale(trueCategory, falseCategory);

    category = scale.scaleRawValue(0);

    Assert.assertEquals(scale.getTrueCategory(), category);
    Assert.assertTrue(scale.isTrue(category));


    category = scale.scaleRawValue(5);

    Assert.assertEquals(scale.getTrueCategory(), category);
    Assert.assertTrue(scale.isTrue(category));


    category = scale.scaleRawValue(6);

    Assert.assertEquals(scale.getFalseCategory(), category);
    Assert.assertFalse(scale.isTrue(category));


    category = scale.scaleRawValue(10);

    Assert.assertEquals(scale.getFalseCategory(), category);
    Assert.assertFalse(scale.isTrue(category));
  }

  @Test public void testScaleOutOfRangeValue() throws Exception
  {
    ScaleCategory trueCategory = new ScaleCategory("True category", 1, 1, "on", 1);
    ScaleCategory falseCategory = new ScaleCategory("False category", 0, 0, "on", 0);

    BoolScale scale = new BoolScale(trueCategory, falseCategory);

    try
    {
      ScaleCategory category = scale.scaleRawValue(10);

      Assert.fail();
    }
    catch (EepOutOfRangeException e)
    {
      // expected
    }
  }

  @Test public void testNullArg() throws Exception
  {
    ScaleCategory trueCategory = new ScaleCategory("True category", 1, 1, "on", 1);
    ScaleCategory falseCategory = new ScaleCategory("False category", 0, 0, "on", 0);

    BoolScale scale;

    try
    {
      scale = new BoolScale(null, falseCategory);

      Assert.fail();
    }
    catch (Exception e)
    {
      // expected
    }

    try
    {
      scale = new BoolScale(trueCategory, null);

      Assert.fail();
    }
    catch (Exception e)
    {
      // expected
    }
  }
}
