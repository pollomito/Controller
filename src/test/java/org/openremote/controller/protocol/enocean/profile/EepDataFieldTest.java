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
package org.openremote.controller.protocol.enocean.profile;

import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link EepDataField} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepDataFieldTest
{

  // Tests ----------------------------------------------------------------------------------------

  @Test public void testReadBoolValue() throws Exception
  {
    int offset = 0;
    int size = 1;

    assertReadValue(0x01, new byte[]{(byte) 0x80, 0x00}, offset, size);


    offset = 3;

    assertReadValue(0x01, new byte[]{0x10, 0x00}, offset, size);


    offset = 8;

    assertReadValue(0x01, new byte[]{0x00, (byte) 0x80}, offset, size);


    offset = 15;

    assertReadValue(0x01, new byte[]{0x00, (byte) 0x01}, offset, size);
  }


  @Test public void testReadNibbleValue() throws Exception
  {
    int offset = 0;
    int size = 4;

    assertReadValue(0x0F, new byte[]{(byte) 0xF0, 0x00}, offset, size);


    offset = 4;

    assertReadValue(0x0F, new byte[]{(byte) 0x0F, 0x00}, offset, size);


    offset = 6;

    assertReadValue(0x0F, new byte[]{0x03, (byte) 0xC0}, offset, size);


    offset = 8;

    assertReadValue(0x0F, new byte[]{0x00, (byte) 0xF0}, offset, size);


    offset = 12;

    assertReadValue(0x0F, new byte[]{0x00, (byte) 0x0F}, offset, size);
  }

  @Test public void testRead10BitValue() throws Exception
  {
    int offset = 2;
    int size = 10;

    assertReadValue(0x321, new byte[]{(byte) 0x32, 0x10}, offset, size);
  }

  @Test public void testWriteBoolValue() throws Exception
  {
    int offset = 0;
    int size = 1;

    assertWriteValue(new byte[]{(byte) 0x80, 0x00}, 0x01, offset, size);


    offset = 7;

    assertWriteValue(new byte[]{(byte) 0x01, 0x00}, 0x01, offset, size);


    offset = 8;

    assertWriteValue(new byte[]{0x00, (byte) 0x80}, 0x01, offset, size);


    offset = 15;

    assertWriteValue(new byte[]{0x00, (byte) 0x01}, 0x01, offset, size);
  }

  @Test public void testWriteByteValue() throws Exception
  {
    int offset = 0;
    int size = 8;

    assertWriteValue(new byte[]{0x21, 0x00}, 0x21, offset, size);


    offset = 8;

    assertWriteValue(new byte[]{0x00, 0x12}, 0x12, offset, size);
  }

  @Test public void testWrite10BitValue() throws Exception
  {
    int offset = 0;
    int size = 10;

    assertWriteValue(new byte[]{(byte) 0x8B, 0x40}, 0x22D, offset, size);
  }

  @Test public void testWriteOutOfRangeValue() throws Exception
  {

    int offset = 0;
    int size = 4;

    EepDataField df = new EepDataField("EDF1", offset, size);

    EepData data = new EepData(EepType.EEP_TYPE_F60201, 1, (EepDataListener)null);

    df.write(0x0F, data);

    try
    {
      df.write(0x1F, data);

      Assert.fail();
    }
    catch (EepOutOfRangeException e)
    {
      // expected
    }
  }


  @Test public void testInvalidDataLength() throws Exception
  {
    int offset = 8;
    int size = 1;

    EepDataField df = new EepDataField("EDF1", offset, size);

    EepData dataTooShort = new EepData(EepType.EEP_TYPE_F60201, 1, (EepDataListener)null);

    try
    {
      df.write(0x01, dataTooShort);

      Assert.fail();
    }
    catch(IllegalArgumentException e)
    {
      // expected
    }

    try
    {
      df.read(dataTooShort);

      Assert.fail();
    }
    catch(IllegalArgumentException e)
    {
      // expected
    }
  }

  // Helpers --------------------------------------------------------------------------------------

  private void assertReadValue(int expectedValue, byte[] data, int offset, int size)
  {
    EepData eepData = new EepData(EepType.EEP_TYPE_F60201, data.length, (EepDataListener)null);
    eepData.update(data);

    EepDataField df = new EepDataField("EDF1", offset, size);
    int actualValue = df.read(eepData);

    Assert.assertEquals(expectedValue, actualValue);
  }

  private void assertWriteValue(byte[] expectedData, int value, int offset, int size)
      throws EepOutOfRangeException
  {
    EepData eepData = new EepData(EepType.EEP_TYPE_F60201, expectedData.length, (EepDataListener)null);

    EepDataField df = new EepDataField("EDF1", offset, size);
    df.write(value, eepData);

    byte[] actualData = eepData.asByteArray();

    Assert.assertArrayEquals(expectedData, actualData);
  }
}
