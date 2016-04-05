/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2013, OpenRemote Inc.
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
package org.openremote.controller.protocol.marantz_avr;

import org.jdom.Element;
import org.junit.Assert;
import org.junit.Test;
import org.openremote.controller.command.CommandBuilder;
import org.openremote.controller.exception.NoSuchCommandException;
import org.openremote.controller.protocol.marantz_avr.commands.BooleanCommand;
import org.openremote.controller.protocol.marantz_avr.commands.MultipleOptionsCommand;
import org.openremote.controller.protocol.marantz_avr.commands.NoFeedbackCommand;
import org.openremote.controller.protocol.marantz_avr.commands.OnScreenDisplayInfoCommand;
import org.openremote.controller.protocol.marantz_avr.commands.TunerFrequencyCommand;
import org.openremote.controller.protocol.marantz_avr.commands.TunerPresetCommand;
import org.openremote.controller.protocol.marantz_avr.commands.VolumeCommand;

/**
 * Basic unit tests for parsing XML elements in
 * {@link org.openremote.controller.protocol.marantz_avr.MarantzAVRCommandBuilder} and building commands.
 * 
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class MarantzCommandBuilderTest {

  @Test
  public void testMainPowerCommands() {
    CommandBuilder cb = new MarantzAVRCommandBuilder();
    Assert.assertTrue("'MAIN_POWER' command maps to a BooleanCommand class", cb.build(getCommandElement("MAIN_POWER", "ON", null)) instanceof BooleanCommand);
    Assert.assertTrue("'MAIN_POWER' command maps to a BooleanCommand class", cb.build(getCommandElement("MAIN_POWER", "OFF", null)) instanceof BooleanCommand);
    Assert.assertTrue("'MAIN_POWER' command maps to a BooleanCommand class", cb.build(getCommandElement("MAIN_POWER", "STATUS", null)) instanceof BooleanCommand);
  }
    
  @Test(expected = NoSuchCommandException.class)
  public void testBuildMainPowerCommandNoParameterShouldFail() {
    CommandBuilder cb = new MarantzAVRCommandBuilder();
    cb.build(getCommandElement("MAIN_POWER", null, null));
  }
  
  @Test
  public void testZonePowerCommands() {
     CommandBuilder cb = new MarantzAVRCommandBuilder();
     Assert.assertTrue("'POWER' command maps to a BooleanCommand class", cb.build(getCommandElement("POWER", "ON", null)) instanceof BooleanCommand);
     Assert.assertTrue("'POWER' command maps to a BooleanCommand class", cb.build(getCommandElement("POWER", "OFF", null)) instanceof BooleanCommand);
     Assert.assertTrue("'POWER' command maps to a BooleanCommand class", cb.build(getCommandElement("POWER", "STATUS", null)) instanceof BooleanCommand);
  }

  @Test(expected = NoSuchCommandException.class)
  public void testBuildZonePowerCommandNoParameterShouldFail() {
    CommandBuilder cb = new MarantzAVRCommandBuilder();
    cb.build(getCommandElement("POWER", null, null));
  }

  @Test
  public void testMuteCommands() {
     CommandBuilder cb = new MarantzAVRCommandBuilder();
     Assert.assertTrue("'MUTE' command maps to a BooleanCommand class", cb.build(getCommandElement("MUTE", "ON", null)) instanceof BooleanCommand);
     Assert.assertTrue("'MUTE' command maps to a BooleanCommand class", cb.build(getCommandElement("MUTE", "OFF", null)) instanceof BooleanCommand);
     Assert.assertTrue("'MUTE' command maps to a BooleanCommand class", cb.build(getCommandElement("MUTE", "STATUS", null)) instanceof BooleanCommand);
  }

  @Test(expected = NoSuchCommandException.class)
  public void testBuildMuteCommandNoParameterShouldFail() {
    CommandBuilder cb = new MarantzAVRCommandBuilder();
    cb.build(getCommandElement("MUTE", null, null));
  }

  @Test
  public void testInputCommands() {
     CommandBuilder cb = new MarantzAVRCommandBuilder();
     Assert.assertTrue("'INPUT' command maps to a MultipleOptionsCommand class", cb.build(getCommandElement("INPUT", "PHONO", null)) instanceof MultipleOptionsCommand);
     Assert.assertTrue("'INPUT' command maps to a MultipleOptionsCommand class", cb.build(getCommandElement("INPUT", "CD", null)) instanceof MultipleOptionsCommand);
     Assert.assertTrue("'INPUT' command maps to a MultipleOptionsCommand class", cb.build(getCommandElement("INPUT", "STATUS", null)) instanceof MultipleOptionsCommand);
  }

  @Test(expected = NoSuchCommandException.class)
  public void testBuildInputCommandNoParameterShouldFail() {
    CommandBuilder cb = new MarantzAVRCommandBuilder();
    cb.build(getCommandElement("INPUT", null, null));
  }
  
  @Test
  public void testTunerFrequencyCommands() {
     CommandBuilder cb = new MarantzAVRCommandBuilder();
     Assert.assertTrue("'TUNER_FREQUENCY' command maps to a TunerFrequencyCommand class", cb.build(getCommandElement("TUNER_FREQUENCY", "UP", null)) instanceof TunerFrequencyCommand);
     Assert.assertTrue("'TUNER_FREQUENCY' command maps to a TunerFrequencyCommand class", cb.build(getCommandElement("TUNER_FREQUENCY", "DOWN", null)) instanceof TunerFrequencyCommand);
     Assert.assertTrue("'TUNER_FREQUENCY' command maps to a TunerFrequencyCommand class", cb.build(getCommandElement("TUNER_FREQUENCY", "STATUS", null)) instanceof TunerFrequencyCommand);
  }
  
  @Test(expected = NoSuchCommandException.class)
  public void testBuildTunerFrequencyCommandNoParameterShouldFail() {
    CommandBuilder cb = new MarantzAVRCommandBuilder();
    cb.build(getCommandElement("TUNER_FREQUENCY", null, null));
  }

  @Test
  public void testTunerPresetCommands() {
     CommandBuilder cb = new MarantzAVRCommandBuilder();
     Assert.assertTrue("'TUNER_PRESET' command maps to a TunerPresetCommand class", cb.build(getCommandElement("TUNER_PRESET", "UP", null)) instanceof TunerPresetCommand);
     Assert.assertTrue("'TUNER_PRESET' command maps to a TunerPresetCommand class", cb.build(getCommandElement("TUNER_PRESET", "DOWN", null)) instanceof TunerPresetCommand);
     Assert.assertTrue("'TUNER_PRESET' command maps to a TunerPresetCommand class", cb.build(getCommandElement("TUNER_PRESET", "STATUS", null)) instanceof TunerPresetCommand);
  }
  
  @Test(expected = NoSuchCommandException.class)
  public void testBuildTunerPresetCommandNoParameterShouldFail() {
    CommandBuilder cb = new MarantzAVRCommandBuilder();
    cb.build(getCommandElement("TUNER_PRESET", null, null));
  }

  @Test
  public void testOSDTextCommands() {
     CommandBuilder cb = new MarantzAVRCommandBuilder();
     Assert.assertTrue("'OSD_LINE_TEXT' command maps to a OnScreenDisplayInfoCommand class", cb.build(getCommandElement("OSD_LINE_TEXT", "1", null)) instanceof OnScreenDisplayInfoCommand);
     Assert.assertTrue("'OSD_LINE_TEXT' command maps to a OnScreenDisplayInfoCommand class", cb.build(getCommandElement("OSD_LINE_TEXT", "2", null)) instanceof OnScreenDisplayInfoCommand);
  }
  
  @Test(expected = NoSuchCommandException.class)
  public void testBuildOSDTextCommandNoParameterShouldFail() {
    CommandBuilder cb = new MarantzAVRCommandBuilder();
    cb.build(getCommandElement("OSD_LINE_TEXT", null, null));
  }

  @Test
  public void testOSDSelectedCommands() {
     CommandBuilder cb = new MarantzAVRCommandBuilder();
     Assert.assertTrue("'OSD_LINE_SELECTED' command maps to a OnScreenDisplayInfoCommand class", cb.build(getCommandElement("OSD_LINE_SELECTED", "1", null)) instanceof OnScreenDisplayInfoCommand);
     Assert.assertTrue("'OSD_LINE_SELECTED' command maps to a OnScreenDisplayInfoCommand class", cb.build(getCommandElement("OSD_LINE_SELECTED", "2", null)) instanceof OnScreenDisplayInfoCommand);
  }
  
  @Test(expected = NoSuchCommandException.class)
  public void testBuildOSDSelectedCommandNoParameterShouldFail() {
    CommandBuilder cb = new MarantzAVRCommandBuilder();
    cb.build(getCommandElement("OSD_LINE_SELECTED", null, null));
  }

  @Test
  public void testSurroundModeCommands() {
     CommandBuilder cb = new MarantzAVRCommandBuilder();
     Assert.assertTrue("'SURROUND_MODE' command maps to a MultipleOptionsCommand class", cb.build(getCommandElement("SURROUND_MODE", "MOVIE", null)) instanceof MultipleOptionsCommand);
     Assert.assertTrue("'SURROUND_MODE' command maps to a MultipleOptionsCommand class", cb.build(getCommandElement("SURROUND_MODE", "MUSIC", null)) instanceof MultipleOptionsCommand);
     Assert.assertTrue("'SURROUND_MODE' command maps to a MultipleOptionsCommand class", cb.build(getCommandElement("SURROUND_MODE", "STATUS", null)) instanceof MultipleOptionsCommand);
  }
  
  @Test(expected = NoSuchCommandException.class)
  public void testBuildSurroundModeCommandNoParameterShouldFail() {
    CommandBuilder cb = new MarantzAVRCommandBuilder();
    cb.build(getCommandElement("SURROUND_MODE", null, null));
  }

  @Test
  public void testVolumeCommands() {
     CommandBuilder cb = new MarantzAVRCommandBuilder();
     Assert.assertTrue("'VOLUME' command maps to a VolumeCommand class", cb.build(getCommandElement("VOLUME", "UP", null)) instanceof VolumeCommand);
     Assert.assertTrue("'VOLUME' command maps to a VolumeCommand class", cb.build(getCommandElement("VOLUME", "DOWN", null)) instanceof VolumeCommand);
     Assert.assertTrue("'VOLUME' command maps to a VolumeCommand class", cb.build(getCommandElement("VOLUME", "STATUS", null)) instanceof VolumeCommand);
     Assert.assertTrue("'VOLUME' command maps to a VolumeCommand class", cb.build(getCommandElement("VOLUME", "80", null)) instanceof VolumeCommand);
     Assert.assertTrue("'VOLUME' command maps to a VolumeCommand class", cb.build(getCommandElement("VOLUME", "UP", "ZONE2")) instanceof VolumeCommand);
     Assert.assertTrue("'VOLUME' command maps to a VolumeCommand class", cb.build(getCommandElement("VOLUME", "DOWN", "ZONE2")) instanceof VolumeCommand);
     Assert.assertTrue("'VOLUME' command maps to a VolumeCommand class", cb.build(getCommandElement("VOLUME", "STATUS", "ZONE2")) instanceof VolumeCommand);
     Assert.assertTrue("'VOLUME' command maps to a VolumeCommand class", cb.build(getCommandElement("VOLUME", "80", "ZONE2")) instanceof VolumeCommand);
  }
  
  @Test(expected = NoSuchCommandException.class)
  public void testBuildVolumeCommandNoParameterShouldFail() {
    CommandBuilder cb = new MarantzAVRCommandBuilder();
    cb.build(getCommandElement("VOLUME", null, null));
  }

  @Test
  public void testMainControlCommands() {
     CommandBuilder cb = new MarantzAVRCommandBuilder();
     Assert.assertTrue("'MAIN_CONTROL' command maps to a NoFeedbackCommand class", cb.build(getCommandElement("MAIN_CONTROL", "CURSOR UP", null)) instanceof NoFeedbackCommand);
     Assert.assertTrue("'MAIN_CONTROL' command maps to a NoFeedbackCommand class", cb.build(getCommandElement("MAIN_CONTROL", "CURSOR DOWN", null)) instanceof NoFeedbackCommand);
     Assert.assertTrue("'MAIN_CONTROL' command maps to a NoFeedbackCommand class", cb.build(getCommandElement("MAIN_CONTROL", "INFO", null)) instanceof NoFeedbackCommand);
  }
  
  @Test
  public void testNetworkControlCommands() {
     CommandBuilder cb = new MarantzAVRCommandBuilder();
     Assert.assertTrue("'NETWORK_CONTROL' command maps to a NoFeedbackCommand class", cb.build(getCommandElement("NETWORK_CONTROL", "CURSOR UP", null)) instanceof NoFeedbackCommand);
     Assert.assertTrue("'NETWORK_CONTROL' command maps to a NoFeedbackCommand class", cb.build(getCommandElement("NETWORK_CONTROL", "CURSOR DOWN", null)) instanceof NoFeedbackCommand);
     Assert.assertTrue("'NETWORK_CONTROL' command maps to a NoFeedbackCommand class", cb.build(getCommandElement("NETWORK_CONTROL", "PAUSE", null)) instanceof NoFeedbackCommand);
  }
  
  // Note : NoFeedbackCommand does not enforce a mandatory parameter, hence no tests for that

  @Test(expected = NoSuchCommandException.class)
  public void testNonExistentCommand() {
    CommandBuilder cb = new MarantzAVRCommandBuilder();
    Assert.assertTrue(cb.build(getCommandElement("DOES NOT EXIST", null, null)) instanceof BooleanCommand);
  }

  private Element getCommandElement(String cmd, String parameter, String zone) {
    Element ele = new Element("command");
    ele.setAttribute("id", "test");
    ele.setAttribute(CommandBuilder.PROTOCOL_ATTRIBUTE_NAME, "marantz_avr");

    if (cmd != null) {
      Element propAddr = new Element(CommandBuilder.XML_ELEMENT_PROPERTY);
      propAddr.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_NAME, MarantzAVRCommandBuilder.MARANTZ_AVR_XMLPROPERTY_COMMAND);
      propAddr.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_VALUE, cmd);
      ele.addContent(propAddr);
    }

    if (parameter != null) {
      Element propAddr = new Element(CommandBuilder.XML_ELEMENT_PROPERTY);
      propAddr.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_NAME, MarantzAVRCommandBuilder.MARANTZ_AVR_XMLPROPERTY_PARAMETER);
      propAddr.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_VALUE, parameter);
      ele.addContent(propAddr);
    }

    if (zone != null) {
      Element propAddr = new Element(CommandBuilder.XML_ELEMENT_PROPERTY);
      propAddr.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_NAME, MarantzAVRCommandBuilder.MARANTZ_AVR_XMLPROPERTY_ZONE);
      propAddr.setAttribute(CommandBuilder.XML_ATTRIBUTENAME_VALUE, zone);
      ele.addContent(propAddr);
    }

    return ele;
  }
   
}
