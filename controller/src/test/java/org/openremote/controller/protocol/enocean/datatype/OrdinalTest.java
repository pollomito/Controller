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
import org.openremote.controller.protocol.enocean.profile.EepData;
import org.openremote.controller.protocol.enocean.profile.EepDataField;
import org.openremote.controller.protocol.enocean.profile.EepOutOfRangeException;
import org.openremote.controller.protocol.enocean.profile.EepType;

import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link Ordinal} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class OrdinalTest
{

  // Tests ----------------------------------------------------------------------------------------

  @Test public void testBasicConstruction() throws Exception
  {
    EepDataField dataField = new EepDataField("EDF1", 0, 8);

    ScaleCategory category1 = new ScaleCategory("Category 1", 0x00, 0x05, "CAT1", 1);
    ScaleCategory category2 = new ScaleCategory("Category 2", 0x06, 0x0A, "CAT2", 2);
    CategoricalScale scale = new CategoricalScale(category1, category2);


    Ordinal ordinal = new Ordinal(dataField, scale);

    Assert.assertNull(ordinal.ordinalValue());


    ordinal = new Ordinal(dataField, 10);

    Assert.assertEquals(10, (int)ordinal.ordinalValue());
  }

  @Test public void testUpdateValue() throws Exception
  {
    EepDataField dataField = new EepDataField("EDF1", 0, 8);

    ScaleCategory category1 = new ScaleCategory("Category 1", 0x00, 0x05, "CAT1", 1);
    ScaleCategory category2 = new ScaleCategory("Category 2", 0x06, 0x0A, "CAT2", 2);
    CategoricalScale scale = new CategoricalScale(category1, category2);

    Ordinal ordinal = new Ordinal(dataField, scale);

    EepData data = new EepData(EepType.EEP_TYPE_F60201, 1, ordinal);


    data.update(new byte[] {0x00});

    Assert.assertEquals(1, (int)ordinal.ordinalValue());


    data.update(new byte[] {0x05});

    Assert.assertEquals(1, (int)ordinal.ordinalValue());


    data.update(new byte[] {0x06});

    Assert.assertEquals(2, (int)ordinal.ordinalValue());


    category1 = new ScaleCategory("Category 1", 0x03, 0x03, "CAT1", 1);
    category2 = new ScaleCategory("Category 2", 0x04, 0x04, "CAT2", 2);
    scale = new CategoricalScale(category1, category2);

    ordinal = new Ordinal(dataField, scale);

    data = new EepData(EepType.EEP_TYPE_F60201, 1, ordinal);


    data.update(new byte[] {0x03});

    Assert.assertEquals(1, (int)ordinal.ordinalValue());


    data.update(new byte[] {0x04});

    Assert.assertEquals(2, (int)ordinal.ordinalValue());
  }

  @Test public void testUpdateWithOutOfRangeValue() throws Exception
  {
    EepDataField dataField = new EepDataField("EDF1", 0, 8);

    ScaleCategory category1 = new ScaleCategory("Category 1", 0x00, 0x05, "CAT1", 1);
    ScaleCategory category2 = new ScaleCategory("Category 2", 0x06, 0x0A, "CAT2", 2);
    CategoricalScale scale = new CategoricalScale(category1, category2);

    Ordinal ordinal = new Ordinal(dataField, scale);


    EepData data = new EepData(EepType.EEP_TYPE_F60201, 1, ordinal);

    Assert.assertNull(ordinal.ordinalValue());


    data.update(new byte[]{(byte)0xFF});

    Assert.assertNull(ordinal.ordinalValue());


    data.update(new byte[]{(byte)0x06});

    Assert.assertEquals(2, (int)ordinal.ordinalValue());


    data.update(new byte[]{(byte)0xFF});

    Assert.assertEquals(2, (int) ordinal.ordinalValue());


    data.update(new byte[]{(byte)0x05});

    Assert.assertEquals(1, (int)ordinal.ordinalValue());
  }

  @Test public void testSaveValueToEepData() throws Exception
  {
    EepDataField dataField = new EepDataField("EDF1", 8, 8);

    Ordinal ordinal = new Ordinal(dataField, 0x55);
    EepData data = new EepData(EepType.EEP_TYPE_F60201, 2, ordinal);

    Assert.assertArrayEquals(new byte[] {0x00, 0x55}, data.asByteArray());


    ordinal = new Ordinal(dataField, 0xFF);
    data = new EepData(EepType.EEP_TYPE_F60201, 2, ordinal);

    Assert.assertArrayEquals(new byte[] {0x00, (byte)0xFF}, data.asByteArray());
  }

  @Test public void testSaveOutOfRangeValueToEepData() throws Exception
  {
    EepDataField dataField = new EepDataField("EDF1", 0, 8);
    Ordinal ordinal = new Ordinal(dataField, 0xFF);
    EepData data = new EepData(EepType.EEP_TYPE_F60201, 1, ordinal);

    Assert.assertArrayEquals(new byte[] {(byte)0xFF}, data.asByteArray());


    ordinal = new Ordinal(dataField, 0x1FF);
    data = new EepData(EepType.EEP_TYPE_F60201, 1, ordinal);

    try
    {
      data.asByteArray();

      Assert.fail();
    }
    catch (EepOutOfRangeException e)
    {
      // expected
    }
  }

  @Test public void testNullArgs() throws Exception
  {
    EepDataField dataField = new EepDataField("EDF1", 0, 8);

    ScaleCategory category1 = new ScaleCategory("Category 1", 0x00, 0x05, "CAT1", 1);
    ScaleCategory category2 = new ScaleCategory("Category 2", 0x06, 0x0A, "CAT2", 2);
    CategoricalScale scale = new CategoricalScale(category1, category2);

    Ordinal ordinal;

    try
    {
      ordinal = new Ordinal(dataField, null);

      Assert.fail();
    }
    catch (IllegalArgumentException e)
    {
      // expected
    }

    try
    {
      ordinal = new Ordinal(null, scale);

      Assert.fail();
    }
    catch (IllegalArgumentException e)
    {
      // expected
    }

    try
    {
      ordinal = new Ordinal(null, 10);

      Assert.fail();
    }
    catch (IllegalArgumentException e)
    {
      // expected
    }
  }
}
