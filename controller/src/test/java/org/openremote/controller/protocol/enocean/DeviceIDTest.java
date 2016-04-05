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
package org.openremote.controller.protocol.enocean;

import org.junit.Test;
import org.junit.Assert;

/**
 * Unit tests for {@link DeviceID} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class DeviceIDTest
{


  // Tests ----------------------------------------------------------------------------------------

  @Test public void testCreateDeviceIDFromString() throws InvalidDeviceIDException
  {
    DeviceID id = DeviceID.fromString("0x00000000");

    assertDeviceIDBytes(id, 0x00, 0x00, 0x00, 0x00);


    id = DeviceID.fromString("0xFFFFFFFF");

    assertDeviceIDBytes(id, 0xFF, 0xFF, 0xFF, 0xFF);


    id = DeviceID.fromString("0x87654321");

    assertDeviceIDBytes(id, 0x21, 0x43, 0x65, 0x87);

   // decimal representation..

    Long idValue = Long.decode("0xFF800001");
    id = DeviceID.fromString(idValue.toString());

    assertDeviceIDBytes(id, 0x01, 0x00, 0x80, 0xFF);

  }


  @Test public void testCreateDeviceIDFromStringWithBaseID() throws InvalidDeviceIDException
  {
    DeviceID base = DeviceID.fromString("0xFF800000");
    DeviceID id = DeviceID.fromStringWithBaseID("0x00", base);

    assertDeviceIDBytes(id, 0x00, 0x00, 0x80, 0xFF);


    base = DeviceID.fromString("0xFF800000");
    id = DeviceID.fromStringWithBaseID("0x01", base);

    assertDeviceIDBytes(id, 0x01, 0x00, 0x80, 0xFF);


    base = DeviceID.fromString("0xFF800000");
    id = DeviceID.fromStringWithBaseID("0x7F", base);

    assertDeviceIDBytes(id, 0x7F, 0x00, 0x80, 0xFF);


    base = DeviceID.fromString("0xFF800000");
    id = DeviceID.fromStringWithBaseID("127", base);

    assertDeviceIDBytes(id, 0x7F, 0x00, 0x80, 0xFF);


    base = DeviceID.fromString("0xFFFFFF80");
    id = DeviceID.fromStringWithBaseID("0", base);

    assertDeviceIDBytes(id, 0x80, 0xFF, 0xFF, 0xFF);


    base = DeviceID.fromString("0xFFFFFF80");
    id = DeviceID.fromStringWithBaseID("126", base);

    assertDeviceIDBytes(id, 0xFE, 0xFF, 0xFF, 0xFF);

  }

  @Test public void testCreateDeviceIDFromByteArray() throws Exception
  {
    byte[] deviceIDbytes = new byte[] {
        (byte)0x01, (byte)0x00, (byte)0x80, (byte)0xFF
    };

    DeviceID id = DeviceID.fromByteArray(deviceIDbytes);

    Assert.assertArrayEquals(deviceIDbytes, id.asByteArray());
  }

  @Test public void testResolve() throws Exception
  {
    DeviceID base = DeviceID.fromString("0xFF800000");
    DeviceID relativeID = DeviceID.fromString("0x11");

    DeviceID absoluteID = relativeID.resolve(base);

    assertDeviceIDBytes(absoluteID, 0x11, 0x00, 0x80, 0xFF);
  }

  @Test (expected = InvalidDeviceIDException.class)
  public void testOutOfBoundsResolve() throws Exception
  {
    DeviceID base = DeviceID.fromString("0xFF800000");
    DeviceID relativeID = DeviceID.fromString("128");

    DeviceID absoluteID = relativeID.resolve(base);
  }

  @Test (expected = IllegalArgumentException.class)
  public void testInvalidDeviceIDArrayLength() throws Exception
  {
    byte[] deviceIDbytes = new byte[] {
        (byte)0x01, (byte)0x02, (byte)0x03
    };

    DeviceID id = DeviceID.fromByteArray(deviceIDbytes);
  }

  @Test (expected = InvalidDeviceIDException.class)
  public void testOutOfBoundsDeviceID1() throws InvalidDeviceIDException
  {
    DeviceID base = DeviceID.fromString("0x1FFFFFFFF");
  }

  @Test (expected = InvalidDeviceIDException.class)
  public void testOutOfBoundsDeviceID2() throws InvalidDeviceIDException
  {
    DeviceID base = DeviceID.fromString("-1");
  }

  @Test (expected = InvalidDeviceIDException.class)
  public void testOutOfBoundsBaseIDOffset1() throws InvalidDeviceIDException
  {
    DeviceID base = DeviceID.fromString("0xFF800000");
    DeviceID id = DeviceID.fromStringWithBaseID("128", base);
  }

  @Test (expected = InvalidDeviceIDException.class)
  public void testOutOfBoundsBaseIDOffset2() throws InvalidDeviceIDException
  {
    DeviceID base = DeviceID.fromString("0xFF800000");
    DeviceID id = DeviceID.fromStringWithBaseID("-1", base);
  }

  @Test (expected = InvalidDeviceIDException.class)
  public void testOutOfBoundsBaseID1() throws Exception
  {
    DeviceID base = DeviceID.fromString("0xFFFFFF81");
    DeviceID id = DeviceID.fromStringWithBaseID("0", base);
  }

  @Test (expected = InvalidDeviceIDException.class)
  public void testOutOfBoundsBaseID2() throws Exception
  {
    DeviceID base = DeviceID.fromString("0xFF7FFFFF");
    DeviceID id = DeviceID.fromStringWithBaseID("0", base);
  }

  @Test (expected = InvalidDeviceIDException.class)
  public void testInvalidAlignmentBaseID() throws Exception
  {
    DeviceID base = DeviceID.fromString("0xFF800001");
    DeviceID id = DeviceID.fromStringWithBaseID("0", base);
  }

  @Test (expected = InvalidDeviceIDException.class)
  public void testInvalidBroadcastID() throws Exception
  {
    DeviceID base = DeviceID.fromString("0xFFFFFF80");
    DeviceID id = DeviceID.fromStringWithBaseID("0x7F", base);
  }

  @Test (expected = InvalidDeviceIDException.class)
  public void testInvalidEncodingDeviceID() throws Exception
  {
    DeviceID base = DeviceID.fromString("0xGG");
  }

  @Test (expected = InvalidDeviceIDException.class)
  public void testInvalidEncodingBaseIDOffset() throws Exception
  {
    DeviceID base = DeviceID.fromString("0xFF800000");
    DeviceID id = DeviceID.fromStringWithBaseID("0xGG", base);
  }


  @Test public void testEquals() throws InvalidDeviceIDException
  {
    DeviceID id1 = DeviceID.fromString("0x00000000");
    DeviceID id2 = DeviceID.fromString("0x00000000");

    DeviceID id3 = DeviceID.fromString("0xFFFFFFFF");
    DeviceID id4 = DeviceID.fromString("0xFFFFFFFF");

    Assert.assertTrue("Expecting ID1 to equals ID2", id1.equals(id2));
    Assert.assertTrue("Expecting ID2 to equals ID1", id2.equals(id1));

    Assert.assertFalse("Expecting ID1 NOT to equals ID3", id1.equals(id3));
    Assert.assertFalse("Expecting ID3 NOT to equals ID1", id3.equals(id1));

    Assert.assertTrue("Expecting ID3 to equals ID4", id3.equals(id4));
    Assert.assertTrue("Expecting ID4 to equals ID3", id4.equals(id3));

    Assert.assertFalse(id1.equals(null));
    Assert.assertFalse(id1.equals(new Object()));

  }

  @Test public void testHash() throws InvalidDeviceIDException
  {
    DeviceID id1 = DeviceID.fromString("0xF800008F");
    DeviceID id2 = DeviceID.fromString("0xF800008F");

    Assert.assertTrue(id1.equals(id1));

    Assert.assertTrue(id1.hashCode() == id2.hashCode());
    Assert.assertTrue(id1.equals(id2));

  }

  // Helpers --------------------------------------------------------------------------------------

  private void assertDeviceIDBytes(DeviceID id, int...deviceIDBytes)
  {
    byte[] b = id.asByteArray();

    Assert.assertTrue(b[0] == (byte)deviceIDBytes[3]);
    Assert.assertTrue(b[1] == (byte)deviceIDBytes[2]);
    Assert.assertTrue(b[2] == (byte)deviceIDBytes[1]);
    Assert.assertTrue(b[3] == (byte)deviceIDBytes[0]);
  }

}
