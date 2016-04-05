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

import org.junit.Assert;
import org.junit.Test;
import org.openremote.controller.protocol.enocean.profile.EepOutOfRangeException;

import java.math.BigDecimal;

/**
 * Unit tests for {@link LinearScale} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class LinearScaleTest
{

  // Tests ----------------------------------------------------------------------------------------

  @Test public void testBasicConstruction() throws Exception
  {
    DataRange rawRange = new DataRange(0, 255);
    DataRange unitsRange = new DataRange(50.0, 100.0);
    int fractionalDigits = 1;

    LinearScale ls = new LinearScale(rawRange, unitsRange, fractionalDigits);

    BigDecimal scaledValue = ls.scaleRawValue(255);
    Assert.assertTrue(BigDecimal.valueOf(100).setScale(fractionalDigits).compareTo(scaledValue) == 0);
  }

  @Test public void testScaling() throws Exception
  {
    DataRange rawRange = new DataRange(100, 200);
    DataRange unitsRange = new DataRange(50.0, 100.0);
    int fractionalDigits = 1;

    LinearScale ls = new LinearScale(rawRange, unitsRange, fractionalDigits);

    BigDecimal scaledValue = ls.scaleRawValue(100);
    Assert.assertTrue(BigDecimal.valueOf(50).setScale(fractionalDigits).compareTo(scaledValue) == 0);

    scaledValue = ls.scaleRawValue(200);
    Assert.assertTrue(BigDecimal.valueOf(100).setScale(fractionalDigits).compareTo(scaledValue) == 0);


    rawRange = new DataRange(200, 100);
    unitsRange = new DataRange(50.0, 100.0);

    ls = new LinearScale(rawRange, unitsRange, fractionalDigits);

    scaledValue = ls.scaleRawValue(100);
    Assert.assertTrue(BigDecimal.valueOf(100).setScale(fractionalDigits).compareTo(scaledValue) == 0);

    scaledValue = ls.scaleRawValue(200);
    Assert.assertTrue(BigDecimal.valueOf(50).setScale(fractionalDigits).compareTo(scaledValue) == 0);


    rawRange = new DataRange(0, 100);
    unitsRange = new DataRange(-50.0, 50.0);

    ls = new LinearScale(rawRange, unitsRange, fractionalDigits);

    scaledValue = ls.scaleRawValue(0);
    Assert.assertTrue(BigDecimal.valueOf(-50).setScale(fractionalDigits).compareTo(scaledValue) == 0);

    scaledValue = ls.scaleRawValue(50);
    Assert.assertTrue(BigDecimal.valueOf(0).setScale(fractionalDigits).compareTo(scaledValue) == 0);

    scaledValue = ls.scaleRawValue(100);
    Assert.assertTrue(BigDecimal.valueOf(50).setScale(fractionalDigits).compareTo(scaledValue) == 0);


    rawRange = new DataRange(0, 100);
    unitsRange = new DataRange(50.0, -50.0);

    ls = new LinearScale(rawRange, unitsRange, fractionalDigits);

    scaledValue = ls.scaleRawValue(0);
    Assert.assertTrue(BigDecimal.valueOf(50).setScale(fractionalDigits).compareTo(scaledValue) == 0);

    scaledValue = ls.scaleRawValue(50);
    Assert.assertTrue(BigDecimal.valueOf(0).setScale(fractionalDigits).compareTo(scaledValue) == 0);

    scaledValue = ls.scaleRawValue(100);
    Assert.assertTrue(BigDecimal.valueOf(-50).setScale(fractionalDigits).compareTo(scaledValue) == 0);
  }

  @Test public void testFractionalDigits() throws Exception
  {
    DataRange rawRange = new DataRange(0, 100);
    DataRange unitsRange = new DataRange(0.25, 1.25);
    int fractionalDigits = 2;

    LinearScale ls = new LinearScale(rawRange, unitsRange, fractionalDigits);

    BigDecimal scaledValue = ls.scaleRawValue(0);
    Assert.assertTrue(BigDecimal.valueOf(0.25).setScale(fractionalDigits).compareTo(scaledValue) == 0);

    scaledValue = ls.scaleRawValue(50);
    Assert.assertTrue(BigDecimal.valueOf(0.75).setScale(fractionalDigits).compareTo(scaledValue) == 0);

    scaledValue = ls.scaleRawValue(100);
    Assert.assertTrue(BigDecimal.valueOf(1.25).setScale(fractionalDigits).compareTo(scaledValue) == 0);


    fractionalDigits = 1;

    ls = new LinearScale(rawRange, unitsRange, fractionalDigits);

    scaledValue = ls.scaleRawValue(0);
    Assert.assertTrue(BigDecimal.valueOf(0.3).setScale(fractionalDigits).compareTo(scaledValue) == 0);

    scaledValue = ls.scaleRawValue(50);
    Assert.assertTrue(BigDecimal.valueOf(0.8).setScale(fractionalDigits).compareTo(scaledValue) == 0);

    scaledValue = ls.scaleRawValue(100);
    Assert.assertTrue(BigDecimal.valueOf(1.3).setScale(fractionalDigits).compareTo(scaledValue) == 0);
  }

  @Test public void testScaleOutOfRangeValue() throws Exception
  {
    DataRange rawRange = new DataRange(50, 250);
    DataRange unitsRange = new DataRange(50.0, 100.0);
    int fractionalDigits = 1;

    LinearScale ls = new LinearScale(rawRange, unitsRange, fractionalDigits);

    BigDecimal scaledValue;

    try
    {
      scaledValue = ls.scaleRawValue(49);

      Assert.fail();
    }
    catch (EepOutOfRangeException e)
    {
      // expected
    }

    try
    {
      scaledValue = ls.scaleRawValue(251);

      Assert.fail();
    }
    catch (EepOutOfRangeException e)
    {
      // expected
    }
  }

  @Test public void testNullArg() throws Exception
  {
    DataRange rawRange = new DataRange(0, 255);
    DataRange unitsRange = new DataRange(50.0, 100.0);
    int fractionalDigits = 1;

    LinearScale ls;

    try
    {
      ls = new LinearScale(null, unitsRange, fractionalDigits);

      Assert.fail();
    }
    catch (IllegalArgumentException e)
    {

    }

    try
    {
      ls = new LinearScale(rawRange, null, fractionalDigits);

      Assert.fail();
    }
    catch (IllegalArgumentException e)
    {

    }
  }
}
