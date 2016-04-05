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

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openremote.controller.exception.ConfigurationException;
import org.openremote.controller.protocol.marantz_avr.commands.BooleanCommand;
import org.openremote.controller.protocol.marantz_avr.commands.TunerFrequencyCommand;
import org.openremote.controller.suite.AllTests;

/**
 * Basic unit tests for parsing XML configuration of Marantz AVR protocol.
 * 
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class MarantzCommandConfigurationParserTest {

   /**
    * Tests parsing of XML command configuration for a command with a single value and no parameters.
    */
   @Test
   public void testParseConfigCommandWithNoZoneAndNoParameters() throws ConfigurationException, MalformedURLException {
      URI configurationURI = AllTests.getAbsoluteFixturePath().resolve("protocol/marantz_avr/CommandNoZoneNoParameter.xml");
      Map<String, CommandConfig> config = CommandConfigurationParser.parseCommandConfiguration(configurationURI.toURL());
      
      Assert.assertNotNull("Parsing valid XML configuration should return a configuration", config);
      Assert.assertEquals("Parsed configuration only contains one command", 1, config.size());
      Assert.assertEquals("Configuration contains command named 'TUNER_FREQUENCY'", "TUNER_FREQUENCY", config.keySet().iterator().next());
      
      CommandConfig commandConfig = config.get("TUNER_FREQUENCY");
      Assert.assertEquals("Name of 'TUNER_FREQUENCY' command should be 'TUNER_FREQUENCY'", "TUNER_FREQUENCY", commandConfig.getName());
      Assert.assertEquals("Class of 'TUNER_FREQUENCY' command should be org.openremote.controller.protocol.marantz_avr.commands.TunerFrequencyCommand",
            TunerFrequencyCommand.class, commandConfig.getCommandClass());
      Assert.assertEquals("Value of 'TUNER_FREQUENCY' command should be 'TF'", "TF", commandConfig.getValue());
   }
   
   /**
    * Tests parsing of XML command configuration for a command with values defined for multiple zones and no parameters.
    */
   @Test
   public void testParseConfigCommandWithZonesAndNoParameters() throws ConfigurationException, MalformedURLException {
      URI configurationURI = AllTests.getAbsoluteFixturePath().resolve("protocol/marantz_avr/CommandWithZonesNoParameter.xml");
      Map<String, CommandConfig> config = CommandConfigurationParser.parseCommandConfiguration(configurationURI.toURL());
      
      Assert.assertNotNull("Parsing valid XML configuration should return a configuration", config);
      Assert.assertEquals("Parsed configuration only contains one command", 1, config.size());
      Assert.assertEquals("Configuration contains command named 'TUNER_FREQUENCY'", "TUNER_FREQUENCY", config.keySet().iterator().next());
      
      CommandConfig commandConfig = config.get("TUNER_FREQUENCY");
      Assert.assertEquals("Name of 'TUNER_FREQUENCY' command should be 'TUNER_FREQUENCY'", "TUNER_FREQUENCY", commandConfig.getName());
      Assert.assertEquals("Class of 'TUNER_FREQUENCY' command should be org.openremote.controller.protocol.marantz_avr.commands.TunerFrequencyCommand",
            TunerFrequencyCommand.class, commandConfig.getCommandClass());
      
      Assert.assertNull("'TUNER_FREQUENCY' should not have a single value", commandConfig.getValue());
      
      Assert.assertEquals("Value of 'TUNER_FREQUENCY' command for 'MAIN' zone should be 'TF_MAIN'", "TF_MAIN", commandConfig.getValuePerZone("MAIN"));
      Assert.assertEquals("Value of 'TUNER_FREQUENCY' command for 'ZONE2' zone should be 'TF_Z2'", "TF_Z2", commandConfig.getValuePerZone("ZONE2"));
      Assert.assertNull("Value of 'TUNER_FREQUENCY' command for 'ZONE3' zone should not exist'", commandConfig.getValuePerZone("ZONE3"));
   }
   
   /**
    * Tests parsing of XML command configuration for a command with a single value and parameters.
    */
   @Test
   public void testParseConfigCommandWithNoZoneAndParameters() throws ConfigurationException, MalformedURLException {
      URI configurationURI = AllTests.getAbsoluteFixturePath().resolve("protocol/marantz_avr/CommandNoZoneWithParameters.xml");
      Map<String, CommandConfig> config = CommandConfigurationParser.parseCommandConfiguration(configurationURI.toURL());
      
      Assert.assertNotNull("Parsing valid XML configuration should return a configuration", config);
      Assert.assertEquals("Parsed configuration only contains one command", 1, config.size());      
      Assert.assertEquals("Configuration contains command named 'MAIN_POWER'", "MAIN_POWER", config.keySet().iterator().next());
      
      CommandConfig commandConfig = config.get("MAIN_POWER");
      Assert.assertEquals("Name of 'MAIN_POWER' command should be 'MAIN_POWER'", "MAIN_POWER", commandConfig.getName());
      Assert.assertEquals("Class of 'MAIN_POWER' command should be org.openremote.controller.protocol.marantz_avr.commands.BooleanCommand",
            BooleanCommand.class, commandConfig.getCommandClass());
      Assert.assertEquals("Value of 'MAIN_POWER' command should be 'PW'", "PW", commandConfig.getValue());
      
      Assert.assertEquals("Value of 'MAIN_POWER' command 'ON' parameter should be 'ON'", "ON", commandConfig.getParameter("ON"));
      Assert.assertEquals("Value of 'MAIN_POWER' command 'OFF' parameter should be 'STANDBY'", "STANDBY", commandConfig.getParameter("OFF"));
      Assert.assertEquals("Value of 'MAIN_POWER' command 'STATUS' parameter should be '?'", "?", commandConfig.getParameter("STATUS"));
      Assert.assertNull("There should be no 'NON EXISTENT' parameter for command 'MAIN_POWER'", commandConfig.getParameter("NON EXISTENT"));
   }

   /**
    * Tests parsing of XML command configuration for a command with values defined for multiple zones and parameters.
    */
   @Test
   public void testParseConfigCommandWithZonesAndParameters() throws ConfigurationException, MalformedURLException {
      URI configurationURI = AllTests.getAbsoluteFixturePath().resolve("protocol/marantz_avr/CommandWithZonesAndParameters.xml");
      Map<String, CommandConfig> config = CommandConfigurationParser.parseCommandConfiguration(configurationURI.toURL());
      
      Assert.assertNotNull("Parsing valid XML configuration should return a configuration", config);
      Assert.assertEquals("Parsed configuration only contains one command", 1, config.size());      
      Assert.assertEquals("Configuration contains command named 'POWER'", "POWER", config.keySet().iterator().next());
      
      CommandConfig commandConfig = config.get("POWER");
      Assert.assertEquals("Name of 'POWER' command should be 'POWER'", "POWER", commandConfig.getName());
      Assert.assertEquals("Class of 'POWER' command should be org.openremote.controller.protocol.marantz_avr.commands.BooleanCommand",
            BooleanCommand.class, commandConfig.getCommandClass());
      
      Assert.assertNull("'POWER' should not have a single value", commandConfig.getValue());
      
      Assert.assertEquals("Value of 'POWER' command for 'MAIN' zone should be 'ZM'", "ZM", commandConfig.getValuePerZone("MAIN"));
      Assert.assertEquals("Value of 'POWER' command for 'ZONE2' zone should be 'Z2'", "Z2", commandConfig.getValuePerZone("ZONE2"));
      Assert.assertEquals("Value of 'POWER' command for 'ZONE3' zone should be 'Z3'", "Z3", commandConfig.getValuePerZone("ZONE3"));
      Assert.assertNull("Value of 'POWER' command for 'ZONE4' zone should not exist'", commandConfig.getValuePerZone("ZONE4"));
      
      Assert.assertEquals("Value of 'POWER' command 'ON' parameter should be 'ON'", "ON", commandConfig.getParameter("ON"));
      Assert.assertEquals("Value of 'POWER' command 'OFF' parameter should be 'STANDBY'", "STANDBY", commandConfig.getParameter("OFF"));
      Assert.assertEquals("Value of 'POWER' command 'STATUS' parameter should be '?'", "?", commandConfig.getParameter("STATUS"));
      Assert.assertNull("There should be no 'NON EXISTENT' parameter for command 'MAIN_POWER'", commandConfig.getParameter("NON EXISTENT"));
   }
   
   /**
    * Validates parses throws a ConfigurationException when provided with an invalid URL.
    */
   @Test(expected=ConfigurationException.class)
   public void testParseNonExistantConfiguration() throws ConfigurationException, MalformedURLException {
      CommandConfigurationParser.parseCommandConfiguration(new URL("file://no where"));
   }
   
   /**
    * Validates parses throws a ConfigurationException when configuration XML is invalid.
    */
   @Test(expected=ConfigurationException.class)
   public void testParseInvalidXMLConfiguration() throws ConfigurationException, MalformedURLException {
      URI configurationURI = AllTests.getAbsoluteFixturePath().resolve("protocol/marantz_avr/InvalidXMLConfiguration.xml");
      CommandConfigurationParser.parseCommandConfiguration(configurationURI.toURL());
   }

   /**
    * Validates parses throws a ConfigurationException when configuration references a non existent command class.
    */
   @Test(expected=ConfigurationException.class)
   public void testParseConfigurationWithNonExistentCommandClass() throws ConfigurationException, MalformedURLException {
      URI configurationURI = AllTests.getAbsoluteFixturePath().resolve("protocol/marantz_avr/NonExistentCommandClass.xml");
      CommandConfigurationParser.parseCommandConfiguration(configurationURI.toURL());
   }

}
