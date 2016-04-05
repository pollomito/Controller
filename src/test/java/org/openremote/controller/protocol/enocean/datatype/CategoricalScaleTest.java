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

import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link CategoricalScale} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class CategoricalScaleTest
{

  // Tests ----------------------------------------------------------------------------------------


  @Test public void testBasicConstruction() throws Exception
  {
    ScaleCategory category1 = new ScaleCategory("Category 1", 0, 49, "CAT1", 1);
    ScaleCategory category2 = new ScaleCategory("Category 2", 50, 99, "CAT2", 2);

    Set<ScaleCategory> categories = new HashSet<ScaleCategory>();
    categories.add(category1);
    categories.add(category2);

    CategoricalScale scale = new CategoricalScale(categories);

    ScaleCategory scaledCategory = scale.scaleRawValue(25);

    Assert.assertEquals(category1, scaledCategory);


    scale = new CategoricalScale(category1, category2);

    scaledCategory = scale.scaleRawValue(25);

    Assert.assertEquals(category1, scaledCategory);
  }

  @Test public void testScaling() throws Exception
  {
    ScaleCategory category1 = new ScaleCategory("Category 1", 0, 49, "CAT1", 1);
    ScaleCategory category2 = new ScaleCategory("Category 2", 50, 99, "CAT2", 2);

    Set<ScaleCategory> categories = new HashSet<ScaleCategory>();
    categories.add(category1);
    categories.add(category2);

    CategoricalScale scale = new CategoricalScale(categories);


    ScaleCategory scaledCategory = scale.scaleRawValue(49);

    Assert.assertEquals(category1, scaledCategory);


    scaledCategory = scale.scaleRawValue(50);

    Assert.assertEquals(category2, scaledCategory);


    scaledCategory = scale.scaleRawValue(99);

    Assert.assertEquals(category2, scaledCategory);
  }

  @Test public void testScaleOutOfRangeValue() throws Exception
  {
    ScaleCategory category1 = new ScaleCategory("Category 1", 0, 49, "CAT1", 1);
    ScaleCategory category2 = new ScaleCategory("Category 2", 50, 99, "CAT2", 2);

    CategoricalScale scale = new CategoricalScale(category1, category2);

    try
    {
      ScaleCategory scaleCategory = scale.scaleRawValue(100);

      Assert.fail();
    }
    catch (EepOutOfRangeException e)
    {
      // expected
    }

  }

  @Test(expected = IllegalArgumentException.class)
  public void testNullArg1() throws Exception
  {
    CategoricalScale scale = new CategoricalScale((Set<ScaleCategory>)null);
  }

  @Test public void testNullArg2() throws Exception
  {
    CategoricalScale scale;

    try
    {
      scale = new CategoricalScale();

      Assert.fail();
    }
    catch (Exception e)
    {
      // expected
    }

    try
    {
      scale = new CategoricalScale((ScaleCategory)null);

      Assert.fail();
    }
    catch (Exception e)
    {
      // expected
    }
  }
}
