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

import org.jdom.Element;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.openremote.controller.command.Command;
import org.openremote.controller.command.CommandBuilder;
import org.openremote.controller.command.ExecutableCommand;
import org.openremote.controller.exception.NoSuchCommandException;
import org.openremote.controller.protocol.enocean.port.EspPortConfiguration;

/**
 * Unit tests for {@link EnOceanCommandBuilder} class.
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EnOceanCommandBuilderTest
{

  // Test Setup -----------------------------------------------------------------------------------

  private EnOceanCommandBuilder builder = null;

  @Before public void setUp()
  {
    TestConfigurationManager configManager = new TestConfigurationManager();

    builder = new EnOceanCommandBuilder(configManager);
  }

  // Tests ----------------------------------------------------------------------------------------

  @Test public void testEepF60201() throws Exception
  {
    Command cmd = getCommand("ON", "0x01", "F6-02-01");

    Assert.assertTrue(cmd instanceof TransceiveCommand);

    cmd = getCommand("ON_ROCKER_A", "0x01", "F6-02-01");

    Assert.assertTrue(cmd instanceof TransceiveCommand);

    cmd = getCommand("ON_ROCKER_B", "0x01", "F6-02-01");

    Assert.assertTrue(cmd instanceof TransceiveCommand);


    cmd = getCommand("OFF", "0x01", "F6-02-01");

    Assert.assertTrue(cmd instanceof TransceiveCommand);

    cmd = getCommand("OFF_ROCKER_A", "0x01", "F6-02-01");

    Assert.assertTrue(cmd instanceof TransceiveCommand);

    cmd = getCommand("OFF_ROCKER_B", "0x01", "F6-02-01");

    Assert.assertTrue(cmd instanceof TransceiveCommand);


    cmd = getCommand("STATUS_ROCKER_A", "0x0080EC7E", "F6-02-01");

    Assert.assertTrue(cmd instanceof TransceiveCommand);


    cmd = getCommand("STATUS_ROCKER_B", "0x0080EC7E", "F6-02-01");

    Assert.assertTrue(cmd instanceof TransceiveCommand);
  }

  @Test public void testEepF60201MixedCase() throws Exception
  {
    Command cmd = getCommand("on", "0x01", "F6-02-01");

    Assert.assertTrue(cmd instanceof TransceiveCommand);

    cmd = getCommand("oN", "0x01", "F6-02-01");

    Assert.assertTrue(cmd instanceof TransceiveCommand);

    cmd = getCommand("On", "0x01", "F6-02-01");

    Assert.assertTrue(cmd instanceof TransceiveCommand);


    cmd = getCommand("on_rocker_a", "0x01", "F6-02-01");

    Assert.assertTrue(cmd instanceof TransceiveCommand);

    cmd = getCommand("On_roCker_A", "0x01", "F6-02-01");

    Assert.assertTrue(cmd instanceof TransceiveCommand);


    cmd = getCommand("on_rocker_b", "0x01", "F6-02-01");

    Assert.assertTrue(cmd instanceof TransceiveCommand);

    cmd = getCommand("On_roCker_B", "0x01", "F6-02-01");

    Assert.assertTrue(cmd instanceof TransceiveCommand);


    cmd = getCommand("status_rocker_a", "0x0080EC7E", "F6-02-01");

    Assert.assertTrue(cmd instanceof TransceiveCommand);

    cmd = getCommand("Status_rockEr_A", "0x0080EC7E", "F6-02-01");

    Assert.assertTrue(cmd instanceof TransceiveCommand);


    cmd = getCommand("status_rocker_b", "0x0080EC7E", "F6-02-01");

    Assert.assertTrue(cmd instanceof TransceiveCommand);

    cmd = getCommand("Status_rockEr_B", "0x0080EC7E", "F6-02-01");

    Assert.assertTrue(cmd instanceof TransceiveCommand);

  }

  @Test public void testEepA50205() throws Exception
  {
    Command cmd = getCommand("TMP", "0xFF800001", "A5-02-05");

    Assert.assertTrue(cmd instanceof ReceiveCommand);
  }

  @Test public void testEepA50205MixedCase() throws Exception
  {
    Command cmd = getCommand("tmp", "0xFF800001", "A5-02-05");

    Assert.assertTrue(cmd instanceof ReceiveCommand);

    cmd = getCommand("Tmp", "0xFF800001", "A5-02-05");

    Assert.assertTrue(cmd instanceof ReceiveCommand);
  }

  @Test public void testEepMixedCase() throws Exception
  {
    Command cmd = getCommand("TMP", "0xFF800001", "a5-02-05");

    Assert.assertTrue(cmd instanceof ReceiveCommand);
  }

  @Test (expected = NoSuchCommandException.class)
  public void testUnknownCommand() throws Exception
  {
    Command cmd = getCommand("garbage should not work", "0x01", "F6-02-01");
  }

  @Test (expected = NoSuchCommandException.class)
  public void testInvalidID() throws Exception
  {
    Command cmd = getCommand("ON", "garbage should not work", "F6-02-01");
  }

  @Test public void testSuperfluousProperties() throws Exception
  {
    Command cmd = getCommandWithExtraProperties("TMP", "0xFF800001", "A5-02-05");

    Assert.assertTrue(cmd instanceof ReceiveCommand);
  }

  @Test (expected = NoSuchCommandException.class)
  public void testMissingCommandProperty() throws Exception
  {
    getCommand(null, "0xFF800001", "A5-02-05");
  }

  @Test (expected = NoSuchCommandException.class)
  public void testMissingDeviceIDProperty() throws Exception
  {
    getCommand("TMP", null, "A5-02-05");
  }

  @Test (expected = NoSuchCommandException.class)
  public void testMissingDeviceEepProperty() throws Exception
  {
    getCommand("TMP", "0xFF800001", null);
  }

  @Test public void testArbitraryPropertyOrder() throws Exception
  {
    Command cmd = getCommandArbitraryPropertyOrder("TMP", "0xFF800001", "A5-02-05");

    Assert.assertTrue(cmd instanceof ReceiveCommand);
  }


  // Helpers --------------------------------------------------------------------------------------

  private Command getCommand(String cmd, String deviceID, String eep)
  {
    Element ele = new Element("command");
    ele.setAttribute("id", "test");
    ele.setAttribute(CommandBuilder.PROTOCOL_ATTRIBUTE_NAME, "enocean");

    if(deviceID != null)
    {
      Element propID = new Element(CommandBuilder.XML_ELEMENT_PROPERTY);
      propID.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_NAME,
                          EnOceanCommandBuilder.ENOCEAN_XMLPROPERTY_DEVICE_ID);
      propID.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_VALUE,
                          deviceID);

      ele.addContent(propID);
    }

    if(cmd != null)
    {
      Element propCmd = new Element(CommandBuilder.XML_ELEMENT_PROPERTY);
      propCmd.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_NAME,
                           EnOceanCommandBuilder.ENOCEAN_XMLPROPERTY_COMMAND);
      propCmd.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_VALUE,
                           cmd);

      ele.addContent(propCmd);
    }

    if(eep != null)
    {
      Element propEep = new Element(CommandBuilder.XML_ELEMENT_PROPERTY);
      propEep.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_NAME,
                           EnOceanCommandBuilder.ENOCEAN_XMLPROPERTY_EEP);
      propEep.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_VALUE,
                           eep);

      ele.addContent(propEep);
    }

    return builder.build(ele);
  }


  private Command getCommandWithExtraProperties(String cmd, String deviceID, String eep)
  {
    Element ele = new Element("command");
    ele.setAttribute("id", "test");
    ele.setAttribute(CommandBuilder.PROTOCOL_ATTRIBUTE_NAME, "enocean");

    Element propID = new Element(CommandBuilder.XML_ELEMENT_PROPERTY);
    propID.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_NAME,
                        EnOceanCommandBuilder.ENOCEAN_XMLPROPERTY_DEVICE_ID);
    propID.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_VALUE,
                        deviceID);

    ele.addContent(propID);

    Element propCmd = new Element(CommandBuilder.XML_ELEMENT_PROPERTY);
    propCmd.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_NAME,
                         EnOceanCommandBuilder.ENOCEAN_XMLPROPERTY_COMMAND);
    propCmd.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_VALUE,
                         cmd);

    ele.addContent(propCmd);

    Element propEep = new Element(CommandBuilder.XML_ELEMENT_PROPERTY);
    propEep.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_NAME,
                         EnOceanCommandBuilder.ENOCEAN_XMLPROPERTY_EEP);
    propEep.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_VALUE,
                         eep);

    ele.addContent(propEep);

    // empty properties..

    Element propEmpty = new Element(CommandBuilder.XML_ELEMENT_PROPERTY);
    propEmpty.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_NAME, "");
    propEmpty.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_VALUE, "");

    ele.addContent(propEmpty);

    // unknown properties

    Element propUnkown = new Element(CommandBuilder.XML_ELEMENT_PROPERTY);
    propUnkown.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_NAME, "test");
    propUnkown.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_VALUE, "unknown");

    ele.addContent(propUnkown);

    return builder.build(ele);
  }

  private Command getCommandArbitraryPropertyOrder(String cmd, String deviceID, String eep)
  {
    Element ele = new Element("command");
    ele.setAttribute("id", "test");
    ele.setAttribute(CommandBuilder.PROTOCOL_ATTRIBUTE_NAME, "enocean");

    Element propEep = new Element(CommandBuilder.XML_ELEMENT_PROPERTY);
    propEep.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_NAME,
        EnOceanCommandBuilder.ENOCEAN_XMLPROPERTY_EEP);
    propEep.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_VALUE,
        eep);

    ele.addContent(propEep);

    Element propID = new Element(CommandBuilder.XML_ELEMENT_PROPERTY);
    propID.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_NAME,
        EnOceanCommandBuilder.ENOCEAN_XMLPROPERTY_DEVICE_ID);
    propID.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_VALUE,
        deviceID);

    ele.addContent(propID);

    Element propCmd = new Element(CommandBuilder.XML_ELEMENT_PROPERTY);
    propCmd.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_NAME,
        EnOceanCommandBuilder.ENOCEAN_XMLPROPERTY_COMMAND);
    propCmd.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_VALUE,
        cmd);

    ele.addContent(propCmd);


    return builder.build(ele);
  }

  private class TestConfigurationManager implements ConfigurationManager
  {

    EspPortConfiguration portConfiguration;

    @Override public EspPortConfiguration getPortConfig()
    {
      if(portConfiguration == null)
      {
        portConfiguration = createPortConfig();
      }

      return portConfiguration;
    }

    @Override public boolean hasPortConfigChanged()
    {
      return portConfiguration == null;
    }

    private EspPortConfiguration createPortConfig()
    {
      EspPortConfiguration portConfig = new EspPortConfiguration();
      portConfig.setCommLayer(EspPortConfiguration.CommLayer.PAD);
      portConfig.setComPort("/dev/ttyUSB0");
      portConfig.setSerialProtocol(EspPortConfiguration.SerialProtocol.ESP3);

      return portConfig;
    }
  }
}
