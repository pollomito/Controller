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

import org.junit.Assert;
import org.junit.Test;
import org.openremote.controller.protocol.marantz_avr.commands.BooleanCommand;

/**
 * Basic unit tests for functionality provided by CommandConfig class,
 * the class representing the configuration of a command for the Marantz protocol.
 * 
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class MarantzCommandConfigTest {

   /**
    * Validates that the getValueToUseForZone does provide the appropriate value
    * with a configuration with values for different zones.
    */
   @Test
   public void testGetValueToUseForZoneWhenMultiZonesOnly() {
      CommandConfig config = new CommandConfig("POWER", null, BooleanCommand.class);
      config.addValuePerZone("MAIN", "PW");
      config.addValuePerZone("ZONE2", "Z2");
      
      Assert.assertEquals("Value for 'MAIN' zone should be 'PW'", "PW", config.getValueToUseForZone("MAIN"));
      Assert.assertEquals("Value for 'ZONE2' zone should be 'Z2'", "Z2", config.getValueToUseForZone("ZONE2"));
      Assert.assertEquals("Value for 'ZONE3' zone is undefined and should be 'PW'", "PW", config.getValueToUseForZone("ZONE3"));
   }
   
   /**
    * Validates that the getValueToUseForZone does provide the appropriate value
    * with a configuration with a single value defined.
    */
   @Test
   public void testGetValueToUseForZoneWhenNoZone() {
      CommandConfig config = new CommandConfig("POWER", "PW", BooleanCommand.class);
      
      Assert.assertEquals("Value for 'MAIN' zone should be 'PW'", "PW", config.getValueToUseForZone("MAIN"));
      Assert.assertEquals("Value for 'ZONE2' zone is undefined and should be 'PW'", "PW", config.getValueToUseForZone("ZONE2"));
   }
   
   /**
    * Validates that the getValueToUseForZone does provide the appropriate value
    * with a configuration with a single value and values for different zones defined.
    */
   @Test
   public void testGetValueToUseForZoneWhenSingleValueAndMultiZones() {
      CommandConfig config = new CommandConfig("POWER", "PW", BooleanCommand.class);
      config.addValuePerZone("MAIN", "PWMAIN");
      config.addValuePerZone("ZONE2", "Z2");

      Assert.assertEquals("Value for 'MAIN' zone should be 'PW'", "PW", config.getValueToUseForZone("MAIN"));
      Assert.assertEquals("Value for 'ZONE2' zone should be 'PW'", "PW", config.getValueToUseForZone("ZONE2"));
      Assert.assertEquals("Value for 'ZONE3' zone is undefined and should be 'PW'", "PW", config.getValueToUseForZone("ZONE3"));
   }

}
