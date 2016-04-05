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
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * Unit tests for {@link EepData} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepDataTest
{

  // Instance Fields ------------------------------------------------------------------------------

  private TestDataListener dataListener1;
  private TestDataListener dataListener2;
  private Set<EepDataListener> dataListeners;


  // Test Lifecycle -------------------------------------------------------------------------------

  @Before public void setUp() throws Exception
  {
    dataListener1 = new TestDataListener();
    dataListener2 = new TestDataListener();

    dataListeners = new HashSet<EepDataListener>(2);
    dataListeners.add(dataListener1);
    dataListeners.add(dataListener2);
  }

  // Tests ----------------------------------------------------------------------------------------

  @Test public void testBasicConstruction() throws Exception
  {

    int dataLength = 4;

    EepData data = new EepData(EepType.EEP_TYPE_A50205, dataLength, dataListeners);

    Assert.assertArrayEquals(new byte[dataLength], data.asByteArray());


    data = new EepData(EepType.EEP_TYPE_A50205, dataLength, dataListener1, dataListener2);

    Assert.assertArrayEquals(new byte[dataLength], data.asByteArray());
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNullArg() throws Exception
  {
    EepData data = new EepData(null, 4, dataListeners);
  }

  @Test public void testDataListenerCalls() throws Exception
  {
    int dataLength = 4;

    EepData data = new EepData(EepType.EEP_TYPE_A50205, dataLength, dataListener1, dataListener2);

    Assert.assertEquals(0, dataListener1.updateCallCount);
    Assert.assertEquals(0, dataListener2.updateCallCount);
    Assert.assertEquals(0, dataListener1.didUpdateCallCount);
    Assert.assertEquals(0, dataListener2.didUpdateCallCount);


    data.asByteArray();

    Assert.assertEquals(1, dataListener1.updateCallCount);
    Assert.assertEquals(1, dataListener2.updateCallCount);

    data.asByteArray();

    Assert.assertEquals(2, dataListener1.updateCallCount);
    Assert.assertEquals(2, dataListener2.updateCallCount);


    data.update(new byte[dataLength]);

    Assert.assertEquals(1, dataListener1.didUpdateCallCount);
    Assert.assertEquals(1, dataListener2.didUpdateCallCount);

    data.update(new byte[dataLength]);

    Assert.assertEquals(2, dataListener1.didUpdateCallCount);
    Assert.assertEquals(2, dataListener2.didUpdateCallCount);
  }

  @Test public void testUpdateData() throws Exception
  {
    EepData data = new EepData(EepType.EEP_TYPE_A50205, 4, dataListeners);

    Assert.assertArrayEquals(new byte[4], data.asByteArray());


    byte[] dataBytes = new byte[] {0x01, 0x02, 0x03, (byte)0xFF};

    data.update(dataBytes.clone());

    Assert.assertArrayEquals(dataBytes, data.asByteArray());
  }

  @Test public void testSetByteValue() throws Exception
  {
    EepData data = new EepData(EepType.EEP_TYPE_A50205, 4, dataListeners);

    data.setValue(0, 0x01);

    Assert.assertArrayEquals(new byte[] {0x01, 0x00, 0x00, 0x00}, data.asByteArray());


    data.setValue(3, 0xFF);

    Assert.assertArrayEquals(new byte[] {0x01, 0x00, 0x00, (byte)0xFF}, data.asByteArray());
  }

  @Test public void testGetByteValue() throws Exception
  {
    EepData data = new EepData(EepType.EEP_TYPE_A50205, 2, dataListeners);

    data.update(new byte[] {0x01, (byte)0xFF});


    int value = data.getValue(0);

    Assert.assertEquals(0x01, value);


    value = data.getValue(1);

    Assert.assertEquals(0xFF, value);
  }

  @Test public void testDataAsString() throws Exception
  {
    EepData data = new EepData(EepType.EEP_TYPE_A50205, 4, dataListeners);
    dataListener1.index = 0;
    dataListener1.value = 0x01;
    dataListener2.index = 3;
    dataListener2.value = 0x04;

    Assert.assertEquals("0x01 0x00 0x00 0x04", data.dataAsString());

    dataListener1.throwException = true;

    Assert.assertEquals("-- -- -- --", data.dataAsString());
  }

  @Test (expected = IllegalArgumentException.class)
  public void testNullUpdateData() throws Exception
  {
    EepData data = new EepData(EepType.EEP_TYPE_A50205, 2, dataListeners);

    data.update(null);
  }

  @Test public void testUpdateWithInvalidDataLength() throws Exception
  {
    EepData data = new EepData(EepType.EEP_TYPE_A50205, 2, dataListeners);

    try
    {
      data.update(new byte[1]);

      Assert.fail();
    }
    catch (IllegalArgumentException e)
    {
      // expected
    }

    try
    {
      data.update(new byte[3]);

      Assert.fail();
    }
    catch (IllegalArgumentException e)
    {
      // expected
    }
  }

  @Test public void testNullDataListeners() throws Exception
  {
    EepData data = new EepData(EepType.EEP_TYPE_A50205, 4, (EepDataListener)null);

    Assert.assertArrayEquals(new byte[4], data.asByteArray());


    data = new EepData(EepType.EEP_TYPE_A50205, 4, (Set<EepDataListener>)null);

    Assert.assertArrayEquals(new byte[4], data.asByteArray());
  }


  // Inner Classes --------------------------------------------------------------------------------

  private static class TestDataListener implements EepDataListener
  {
    int didUpdateCallCount;
    int updateCallCount;
    int index = -1;
    int value;
    boolean throwException = false;


    @Override public void didUpdateData(EepData data) throws EepOutOfRangeException
    {
      ++didUpdateCallCount;

      if(throwException)
      {
        throw new EepOutOfRangeException("");
      }
    }

    @Override public void updateData(EepData data) throws EepOutOfRangeException
    {
      ++updateCallCount;

      if(index >= 0)
      {
        data.setValue(index, value);
      }

      if(throwException)
      {
        throw new EepOutOfRangeException("");
      }
    }
  }
}
