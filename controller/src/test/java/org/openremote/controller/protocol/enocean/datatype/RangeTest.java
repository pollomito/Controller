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
import org.openremote.controller.protocol.enocean.profile.EepType;

import java.math.BigDecimal;

/**
 * Unit tests for {@link Range} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class RangeTest
{

  // Tests ----------------------------------------------------------------------------------------

  @Test public void testBasicConstruction() throws Exception
  {
    EepDataField dataField = new EepDataField("EDF1", 0, 8);

    DataRange rawDataRange = new DataRange(0, 255);
    DataRange unitsDataRange = new DataRange(0, 100);
    LinearScale scale = new LinearScale(rawDataRange, unitsDataRange, 2);


    Range range = new Range(dataField, scale);

    Assert.assertNull(range.rangeValue());


    range = new Range(dataField, 100);

    Assert.assertEquals(100, range.rangeValue().intValue());
  }

  @Test public void testUpdateValue() throws Exception
  {
    EepDataField dataField = new EepDataField("EDF1", 0, 8);

    DataRange rawDataRange = new DataRange(0, 255);
    DataRange unitsDataRange = new DataRange(0, 100);
    LinearScale scale = new LinearScale(rawDataRange, unitsDataRange, 2);

    Range range = new Range(dataField, scale);
    EepData data = new EepData(EepType.EEP_TYPE_F60201, 1, range);


    data.update(new byte[] {0x00});

    Assert.assertEquals(0.0, range.rangeValue(), 0.0);


    data.update(new byte[] {(byte)0xFF});

    Assert.assertEquals(100.0, range.rangeValue(), 0.0);
  }

  @Test public void testUpdateWithOutOfRangeValue() throws Exception
  {
    EepDataField dataField = new EepDataField("EDF1", 0, 8);

    DataRange rawDataRange = new DataRange(0x05, 0x0A);
    DataRange unitsDataRange = new DataRange(0, 100);
    LinearScale scale = new LinearScale(rawDataRange, unitsDataRange, 2);

    Range range = new Range(dataField, scale);
    EepData data = new EepData(EepType.EEP_TYPE_F60201, 1, range);


    data.update(new byte[] {0x00});

    Assert.assertNull(range.rangeValue());


    data.update(new byte[] {0x0A});

    Assert.assertEquals(100.0, range.rangeValue(), 0.0);


    data.update(new byte[] {0x0B});

    Assert.assertEquals(100.0, range.rangeValue(), 0.0);
  }

  @Test public void testSaveValueToEepData() throws Exception
  {
    EepDataField dataField = new EepDataField("EDF1", 8, 8);
    Range range = new Range(dataField, 0x55);
    EepData data = new EepData(EepType.EEP_TYPE_F60201, 2, range);

    Assert.assertArrayEquals(new byte[] {0x00, (byte)0x55}, data.asByteArray());


    range = new Range(dataField, 0xFF);
    data = new EepData(EepType.EEP_TYPE_F60201, 2, range);

    Assert.assertArrayEquals(new byte[] {0x00, (byte)0xFF}, data.asByteArray());
  }

  @Test public void testSaveOutOfRangeValueToEepData() throws Exception
  {
    EepDataField dataField = new EepDataField("EDF1", 0, 8);
    Range range = new Range(dataField, 0xFF);
    EepData data = new EepData(EepType.EEP_TYPE_F60201, 1, range);

    Assert.assertArrayEquals(new byte[]{(byte) 0xFF}, data.asByteArray());


    range = new Range(dataField, 0x1FF);
    data = new EepData(EepType.EEP_TYPE_F60201, 1, range);

    try
    {
      data.asByteArray();

      Assert.fail();
    }
    catch (Exception e)
    {
      // expected
    }
  }

  @Test public void testNullArgs() throws Exception
  {
    EepDataField dataField = new EepDataField("EDF1", 0, 8);

    DataRange rawDataRange = new DataRange(0, 255);
    DataRange unitsDataRange = new DataRange(0, 100);
    LinearScale scale = new LinearScale(rawDataRange, unitsDataRange, 2);

    Range range;

    try
    {
      range = new Range(dataField, null);

      Assert.fail();
    }
    catch (Exception e)
    {
      // expected
    }

    try
    {
      range = new Range(null, scale);

      Assert.fail();
    }
    catch (Exception e)
    {
      // expected
    }

    try
    {
      range = new Range(null, 10);

      Assert.fail();
    }
    catch (Exception e)
    {
      // expected
    }
  }
}
