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

import java.text.MessageFormat;

/**
 * Unit tests for {@link Bool} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class BoolTest
{

  // Tests ----------------------------------------------------------------------------------------

  @Test public void testBasicConstruction() throws Exception
  {
    EepDataField dataField = new EepDataField("EDF1", 7, 1);

    ScaleCategory trueCategory = new ScaleCategory("True category" , 1, 1, "on", 1);
    ScaleCategory falseCategory = new ScaleCategory("False category" , 0, 0, "off", 0);
    BoolScale scale = new BoolScale(trueCategory, falseCategory);


    Bool b = new Bool(dataField, scale);

    Assert.assertNull(b.boolValue());


    b = new Bool(dataField, false);

    Assert.assertFalse(b.boolValue());


    b = new Bool(dataField, true);

    Assert.assertTrue(b.boolValue());
  }

  @Test public void testUpdateValue() throws Exception
  {
    EepDataField dataField = new EepDataField("EDF1", 7, 1);

    ScaleCategory trueCategory = new ScaleCategory("True category" , 1, 1, "on", 1);
    ScaleCategory falseCategory = new ScaleCategory("False category" , 0, 0, "off", 0);
    BoolScale scale = new BoolScale(trueCategory, falseCategory);

    Bool bool = new Bool(dataField, scale);

    EepData data = new EepData(EepType.EEP_TYPE_F60201, 1, bool);



    data.update(new byte[] {0x01});

    Assert.assertTrue(bool.boolValue());


    data.update(new byte[] {0x00});

    Assert.assertFalse(bool.boolValue());


    trueCategory = new ScaleCategory("True category" , 0, 0, "on", 1);
    falseCategory = new ScaleCategory("False category" , 1, 1, "off", 0);
    scale = new BoolScale(trueCategory, falseCategory);

    bool = new Bool(dataField, scale);

    data = new EepData(EepType.EEP_TYPE_F60201, 1, bool);


    data.update(new byte[] {0x01});

    Assert.assertFalse(bool.boolValue());


    data.update(new byte[] {0x00});

    Assert.assertTrue(bool.boolValue());


    dataField = new EepDataField("EDF1", 0, 8);

    trueCategory = new ScaleCategory("True category" , 0, 5, "on", 1);
    falseCategory = new ScaleCategory("False category" , 6, 9, "off", 0);
    scale = new BoolScale(trueCategory, falseCategory);

    bool = new Bool(dataField, scale);

    data = new EepData(EepType.EEP_TYPE_F60201, 1, bool);


    data.update(new byte[] {0x00});

    Assert.assertTrue(bool.boolValue());


    data.update(new byte[] {0x05});

    Assert.assertTrue(bool.boolValue());


    data.update(new byte[] {0x06});

    Assert.assertFalse(bool.boolValue());


    data.update(new byte[] {0x09});

    Assert.assertFalse(bool.boolValue());


    // update with out of range value

    data.update(new byte[] {(byte)0xFF});

    Assert.assertFalse(bool.boolValue());
  }

  @Test public void testSaveValueToEepData() throws Exception
  {
    EepDataField dataField = new EepDataField("EDF1", 0, 1);
    Bool bool = new Bool(dataField, true);
    EepData data = new EepData(EepType.EEP_TYPE_F60201, 1, bool);

    Assert.assertArrayEquals(new byte[] {(byte)0x80}, data.asByteArray());


    bool = new Bool(dataField, false);
    data = new EepData(EepType.EEP_TYPE_F60201, 1, bool);

    Assert.assertArrayEquals(new byte[] {(byte)0x00}, data.asByteArray());
  }

  @Test public void testNullArgs() throws Exception
  {
    EepDataField dataField = new EepDataField("EDF1", 7, 1);

    ScaleCategory trueCategory = new ScaleCategory("True category" , 1, 1, "on", 1);
    ScaleCategory falseCategory = new ScaleCategory("False category" , 0, 0, "off", 0);
    BoolScale scale = new BoolScale(trueCategory, falseCategory);

    Bool bool;

    try
    {
      bool = new Bool(dataField, null);

      Assert.fail();
    }
    catch (IllegalArgumentException e)
    {
      // expected
    }

    try
    {
      bool = new Bool(null, scale);

      Assert.fail();
    }
    catch (IllegalArgumentException e)
    {
      // expected
    }

    try
    {
      bool = new Bool(null, true);

      Assert.fail();
    }
    catch (IllegalArgumentException e)
    {
      // expected
    }
  }
}
