/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2014, OpenRemote Inc.
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
import org.openremote.controller.command.CommandParameter;
import org.openremote.controller.protocol.enocean.*;
import org.openremote.controller.protocol.enocean.packet.radio.*;

/**
 * Unit tests for {@link EepA53808} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EepA53808Test
{

  // Constants ------------------------------------------------------------------------------------

  private static final String DIM_STATUS_COMMAND = "EDIM";
  private static final String DIM_SPEED_STATUS_COMMAND = "RMP";
  private static final String DIM_RANGE_STATUS_COMMAND = "EDIMR";
  private static final String DIM_STORE_DIM_VALUE_STATUS_COMMAND = "STR";
  private static final String SWITCH_STATUS_COMMAND = "SW";
  private static final String DIM_COMMAND = "DIM";
  private static final String DIM_COMMAND_ELTAKO = "DIM_ELTAKO";

  // Private Instance Fields ----------------------------------------------------------------------

  private DeviceID deviceID;


  // Test Lifecycle -------------------------------------------------------------------------------

  @Before public void setUp() throws Exception
  {
    deviceID = DeviceID.fromString("0xFF800001");
  }

  @Test public void testBasicConstruction() throws Exception
  {
    // New EEP number...

    Eep eep = EepType.lookup("A5-38-08").createEep(
        deviceID, "EDIM", null
    );

    Assert.assertTrue(eep instanceof EepA53808);
    Assert.assertEquals(EepType.EEP_TYPE_A53808, eep.getType());

    // Old EEP number...

    eep = EepType.lookup("07-38-08").createEep(
            deviceID, "EDIM", null
    );

    Assert.assertTrue(eep instanceof EepA53808);
    Assert.assertEquals(EepType.EEP_TYPE_A53808, eep.getType());
  }

  @Test public void testUpdateCommandID() throws Exception
  {
    EepA53808 eep = (EepA53808)EepType.lookup("A5-38-08").createEep(
        deviceID, DIM_STATUS_COMMAND, null
    );

    Assert.assertNull(eep.getCommandID());

    int commandID = 2;
    int dimValue = 25;
    int dimSpeed = 4;
    boolean isTeachIn = false;
    boolean isAbsoluteRange = false;
    boolean isStoreDimValue = false;
    boolean isSwitchOn = true;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
        isStoreDimValue, isSwitchOn, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(2), eep.getCommandID());


    commandID = 2;

    telegram = createRadioTelegramESP3(
        deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
        isStoreDimValue, isSwitchOn, isTeachIn
    );


    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Integer.valueOf(2), eep.getCommandID());


    // Invalid Command ID

    commandID = 0xFF;

    telegram = createRadioTelegramESP3(
        deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
        isStoreDimValue, isSwitchOn, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Integer.valueOf(2), eep.getCommandID());

    /*
    // "0x01 Switching" not implemented yet

    commandID = 1;

    telegram = createRadioTelegramESP3(
            deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
            isStoreDimValue, isSwitchOn, isTeachIn
    );


    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(1), eep.getCommandID());
    */
  }

  @Test public void testUpdateRelativeDimValue() throws Exception
  {
    EepA53808 eep = (EepA53808)EepType.lookup("A5-38-08").createEep(
       deviceID, DIM_STATUS_COMMAND, null
    );

    Assert.assertNull(eep.getRelativeDimValue());

    int commandID = 2;
    int dimSpeed = 4;
    boolean isTeachIn = false;
    boolean isStoreDimValue = false;
    boolean isSwitchOn = true;

    boolean isAbsoluteRange = false;
    int dimValue = 25;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
        isStoreDimValue, isSwitchOn, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(25), eep.getRelativeDimValue());


    dimValue = 0;

    telegram = createRadioTelegramESP3(
        deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
        isStoreDimValue, isSwitchOn, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(0), eep.getRelativeDimValue());


    dimValue = 100;

    telegram = createRadioTelegramESP2(
            deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
            isStoreDimValue, isSwitchOn, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(100), eep.getRelativeDimValue());


    // Exceeding valid value range...

    dimValue = 101;

    telegram = createRadioTelegramESP2(
            deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
            isStoreDimValue, isSwitchOn, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertFalse(isUpdate);
    Assert.assertEquals(Integer.valueOf(100), eep.getRelativeDimValue());
  }

  @Test public void testUpdateAbsoluteDimValue() throws Exception
  {
    EepA53808 eep = (EepA53808)EepType.lookup("A5-38-08").createEep(
       deviceID, DIM_STATUS_COMMAND, null
    );

    Assert.assertNull(eep.getAbsoluteDimValue());

    int commandID = 2;
    int dimSpeed = 4;
    boolean isTeachIn = false;
    boolean isStoreDimValue = false;
    boolean isSwitchOn = true;

    boolean isAbsoluteRange = true;
    int dimValue = 0;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
        isStoreDimValue, isSwitchOn, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(0), eep.getAbsoluteDimValue());


    dimValue = 255;

    telegram = createRadioTelegramESP2(
        deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
        isStoreDimValue, isSwitchOn, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(100), eep.getAbsoluteDimValue());
  }

  @Test public void testUpdateDimSpeedValue() throws Exception
  {
    EepA53808 eep = (EepA53808)EepType.lookup("A5-38-08").createEep(
       deviceID, DIM_SPEED_STATUS_COMMAND, null
    );

    Assert.assertNull(eep.getDimSpeedValue());

    int commandID = 2;
    int dimValue = 0;
    boolean isTeachIn = false;
    boolean isAbsoluteRange = true;
    boolean isStoreDimValue = false;
    boolean isSwitchOn = true;

    int dimSpeed = 0;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
        isStoreDimValue, isSwitchOn, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(0), eep.getDimSpeedValue());


    dimSpeed = 255;

    telegram = createRadioTelegramESP2(
            deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
            isStoreDimValue, isSwitchOn, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertEquals(Integer.valueOf(255), eep.getDimSpeedValue());
  }

  @Test public void testUpdateDimRangeFlag() throws Exception
  {
    EepA53808 eep = (EepA53808)EepType.lookup("A5-38-08").createEep(
        deviceID, DIM_RANGE_STATUS_COMMAND, null
    );

    Assert.assertNull(eep.isRelativeDimRange());

    int commandID = 2;
    int dimValue = 0xFF;
    int dimSpeed = 0xFF;
    boolean isTeachIn = false;
    boolean isStoreDimValue = false;
    boolean isSwitchOn = true;

    boolean isAbsoluteRange = true;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
        isStoreDimValue, isSwitchOn, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertFalse(eep.isRelativeDimRange());


    isAbsoluteRange = false;

    telegram = createRadioTelegramESP3(
        deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
        isStoreDimValue, isSwitchOn, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertTrue(eep.isRelativeDimRange());
  }

  @Test public void testUpdateStoreDimFlag() throws Exception
  {
    EepA53808 eep = (EepA53808)EepType.lookup("A5-38-08").createEep(
        deviceID, DIM_STORE_DIM_VALUE_STATUS_COMMAND, null
    );

    Assert.assertNull(eep.isStoreDimValue());

    int commandID = 2;
    int dimValue = 0xFF;
    int dimSpeed = 0xFF;
    boolean isTeachIn = false;
    boolean isAbsoluteRange = true;
    boolean isSwitchOn = true;

    boolean isStoreDimValue = false;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
        isStoreDimValue, isSwitchOn, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertFalse(eep.isStoreDimValue());


    isStoreDimValue = true;

    telegram = createRadioTelegramESP3(
        deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
        isStoreDimValue, isSwitchOn, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertTrue(eep.isStoreDimValue());
  }

  @Test public void testUpdateSwitchState() throws Exception
  {
    EepA53808 eep = (EepA53808)EepType.lookup("A5-38-08").createEep(
        deviceID, SWITCH_STATUS_COMMAND, null
    );

    Assert.assertNull(eep.isSwitchOn());

    int commandID = 2;
    int dimValue = 0xFF;
    int dimSpeed = 0xFF;
    boolean isTeachIn = false;
    boolean isAbsoluteRange = true;
    boolean isStoreDimValue = false;

    boolean isSwitchOn = true;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
        isStoreDimValue, isSwitchOn, isTeachIn
    );

    Boolean isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertTrue(eep.isSwitchOn());


    isSwitchOn = false;

    telegram = createRadioTelegramESP3(
        deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
        isStoreDimValue, isSwitchOn, isTeachIn
    );

    isUpdate = eep.update(telegram);

    Assert.assertTrue(isUpdate);
    Assert.assertFalse(eep.isSwitchOn());
  }

  @Test public void testDimCommandWithValueParameter() throws Exception
  {
    int dimValue = 100;

    CommandParameter parameter = new CommandParameter(String.valueOf(dimValue));

    EepA53808 eep = (EepA53808)EepType.lookup("A5-38-08").createEep(
        deviceID, DIM_COMMAND , parameter
    );

    Assert.assertNotNull(eep);


    int commandID = 2;
    int dimSpeed = 0;
    boolean isTeachIn = false;
    boolean isAbsoluteRange = false;
    boolean isStoreDimValue = false;
    boolean isSwitchOn = true;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
        isStoreDimValue, isSwitchOn, isTeachIn
    );

    TestRadioInterface radioInterface = new TestRadioInterface();

    eep.send(radioInterface);

    Assert.assertArrayEquals(telegram.getPayload(), radioInterface.payload);
    Assert.assertEquals(telegram.getRORG(), radioInterface.rorg);
    Assert.assertEquals(telegram.getSenderID(), radioInterface.deviceID);


    dimValue = 0;
    isSwitchOn = false;

    parameter = new CommandParameter(String.valueOf(dimValue));

    eep = (EepA53808)EepType.lookup("A5-38-08").createEep(
            deviceID, DIM_COMMAND , parameter
    );

    telegram = createRadioTelegramESP3(
            deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
            isStoreDimValue, isSwitchOn, isTeachIn
    );

    radioInterface = new TestRadioInterface();

    eep.send(radioInterface);

    Assert.assertArrayEquals(telegram.getPayload(), radioInterface.payload);
    Assert.assertEquals(telegram.getRORG(), radioInterface.rorg);
    Assert.assertEquals(telegram.getSenderID(), radioInterface.deviceID);
  }

  @Test public void testDimCommandWithStaticDimValue() throws Exception
  {
    int dimValue = 100;

    EepA53808 eep = (EepA53808)EepType.lookup("A5-38-08").createEep(
        deviceID, DIM_COMMAND + " " + String.valueOf(dimValue) , null
    );

    Assert.assertNotNull(eep);


    int commandID = 2;
    int dimSpeed = 0;
    boolean isTeachIn = false;
    boolean isAbsoluteRange = false;
    boolean isStoreDimValue = false;
    boolean isSwitchOn = true;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
        isStoreDimValue, isSwitchOn, isTeachIn
    );

    TestRadioInterface radioInterface = new TestRadioInterface();

    eep.send(radioInterface);

    Assert.assertArrayEquals(telegram.getPayload(), radioInterface.payload);
    Assert.assertEquals(telegram.getRORG(), radioInterface.rorg);
    Assert.assertEquals(telegram.getSenderID(), radioInterface.deviceID);


    dimValue = 0;
    isSwitchOn = false;

    eep = (EepA53808)EepType.lookup("A5-38-08").createEep(
            deviceID,  DIM_COMMAND + " " + String.valueOf(dimValue) , null
    );

    telegram = createRadioTelegramESP3(
            deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
            isStoreDimValue, isSwitchOn, isTeachIn
    );

    radioInterface = new TestRadioInterface();

    eep.send(radioInterface);

    Assert.assertArrayEquals(telegram.getPayload(), radioInterface.payload);
    Assert.assertEquals(telegram.getRORG(), radioInterface.rorg);
    Assert.assertEquals(telegram.getSenderID(), radioInterface.deviceID);
  }

  @Test public void testDimCommandWithValueParameterEltako() throws Exception
  {
    int dimValue = 100;

    CommandParameter parameter = new CommandParameter(String.valueOf(dimValue));

    EepA53808 eep = (EepA53808)EepType.lookup("A5-38-08").createEep(
        deviceID, DIM_COMMAND_ELTAKO , parameter
    );

    Assert.assertNotNull(eep);


    int commandID = 2;
    int dimSpeed = 0;
    boolean isTeachIn = false;
    boolean isAbsoluteRange = true;   // Eltako does not set the range flag correctly
    boolean isStoreDimValue = false;
    boolean isSwitchOn = true;

    EspRadioTelegram telegram = createRadioTelegramESP3(
        deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
        isStoreDimValue, isSwitchOn, isTeachIn
    );

    TestRadioInterface radioInterface = new TestRadioInterface();

    eep.send(radioInterface);

    Assert.assertArrayEquals(telegram.getPayload(), radioInterface.payload);
    Assert.assertEquals(telegram.getRORG(), radioInterface.rorg);
    Assert.assertEquals(telegram.getSenderID(), radioInterface.deviceID);


    dimValue = 0;
    isSwitchOn = false;

    parameter = new CommandParameter(String.valueOf(dimValue));

    eep = (EepA53808)EepType.lookup("A5-38-08").createEep(
            deviceID, DIM_COMMAND_ELTAKO , parameter
    );

    telegram = createRadioTelegramESP3(
            deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
            isStoreDimValue, isSwitchOn, isTeachIn
    );

    radioInterface = new TestRadioInterface();

    eep.send(radioInterface);

    Assert.assertArrayEquals(telegram.getPayload(), radioInterface.payload);
    Assert.assertEquals(telegram.getRORG(), radioInterface.rorg);
    Assert.assertEquals(telegram.getSenderID(), radioInterface.deviceID);
  }

  @Test public void testCommandStringWithBlanksAndLowerCase() throws Exception
  {
    EepA53808 eep = (EepA53808)EepType.lookup("A5-38-08").createEep(
        deviceID, " " + DIM_STATUS_COMMAND.toLowerCase() + " ", null
    );

    Assert.assertNotNull(eep);
  }

  @Test public void testDimCommandWithStaticDimValueEltako() throws Exception
  {
    int dimValue = 100;

    EepA53808 eep = (EepA53808)EepType.lookup("A5-38-08").createEep(
        deviceID, DIM_COMMAND_ELTAKO + " " + String.valueOf(dimValue) , null
    );

    Assert.assertNotNull(eep);


    int commandID = 2;
    int dimSpeed = 0;
    boolean isTeachIn = false;
    boolean isAbsoluteRange = true;   // Eltako does not set the range flag correctly
    boolean isStoreDimValue = false;
    boolean isSwitchOn = true;

    EspRadioTelegram telegram = createRadioTelegramESP3(
            deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
            isStoreDimValue, isSwitchOn, isTeachIn
    );

    TestRadioInterface radioInterface = new TestRadioInterface();

    eep.send(radioInterface);

    Assert.assertArrayEquals(telegram.getPayload(), radioInterface.payload);
    Assert.assertEquals(telegram.getRORG(), radioInterface.rorg);
    Assert.assertEquals(telegram.getSenderID(), radioInterface.deviceID);


    dimValue = 0;
    isSwitchOn = false;

    eep = (EepA53808)EepType.lookup("A5-38-08").createEep(
        deviceID,  DIM_COMMAND_ELTAKO + " " + String.valueOf(dimValue) , null
    );

    telegram = createRadioTelegramESP3(
        deviceID, commandID, dimValue, dimSpeed, isAbsoluteRange,
        isStoreDimValue, isSwitchOn, isTeachIn
    );

    radioInterface = new TestRadioInterface();

    eep.send(radioInterface);

    Assert.assertArrayEquals(telegram.getPayload(), radioInterface.payload);
    Assert.assertEquals(telegram.getRORG(), radioInterface.rorg);
    Assert.assertEquals(telegram.getSenderID(), radioInterface.deviceID);
  }

  // Helpers --------------------------------------------------------------------------------------

  private Esp34BSTelegram createRadioTelegramESP3(DeviceID deviceID, int commandID, int rawDimValue,
                                                  int rawDimSpeed, boolean isAbsoluteDimRange,
                                                  boolean isStoreDimValue, boolean isSwitchOn,
                                                  boolean isTeachIn)
  {
    byte[] payload = createPayload(
        deviceID, commandID, rawDimValue, rawDimSpeed, isAbsoluteDimRange,
        isStoreDimValue, isSwitchOn, isTeachIn
    );

    Esp34BSTelegram telegram = new Esp34BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;

  }

  private Esp24BSTelegram createRadioTelegramESP2(DeviceID deviceID, int commandID, int rawDimValue,
                                                  int rawDimSpeed, boolean isAbsoluteDimRange,
                                                  boolean isStoreDimValue, boolean isSwitchOn,
                                                  boolean isTeachIn)
  {
    byte[] payload = createPayload(
            deviceID, commandID, rawDimValue, rawDimSpeed, isAbsoluteDimRange,
            isStoreDimValue, isSwitchOn, isTeachIn
    );

    Esp24BSTelegram telegram = new Esp24BSTelegram(deviceID, payload, (byte)0x00);

    return telegram;

  }

  private byte[] createPayload(DeviceID deviceID, int commandID, int rawDimValue,
                               int rawDimSpeed, boolean isAbsoluteDimRange,
                               boolean isStoreDimValue, boolean isSwitchOn,
                               boolean isTeachIn)
  {
    byte[] payload = new byte[4];
    payload[0] = (byte)commandID;
    payload[1] = (byte)rawDimValue;
    payload[2] = (byte)rawDimSpeed;
    payload[3] |= (byte)(isTeachIn ? 0x00 : 0x08);
    payload[3] |= (byte)(isAbsoluteDimRange ? 0x00 : 0x04);
    payload[3] |= (byte)(isStoreDimValue ? 0x02 : 0x00);
    payload[3] |= (byte)(isSwitchOn ? 0x01 : 0x00);

    return payload;
  }


  // Nested Classes -------------------------------------------------------------------------------

  private class TestRadioInterface implements RadioInterface
  {
    EspRadioTelegram.RORG rorg;
    DeviceID deviceID;
    byte[] payload;

    @Override public void sendRadio(EspRadioTelegram.RORG rorg, DeviceID deviceID, byte[] payload, byte statusByte) throws ConfigurationException, ConnectionException
    {
      this.payload = payload;
      this.deviceID = deviceID;
      this.rorg = rorg;
    }

    @Override
    public void addRadioListener(DeviceID deviceID, RadioTelegramListener listener)
    {

    }

    @Override
    public void removeRadioListener(DeviceID deviceID, RadioTelegramListener listener)
    {

    }
  }
}
