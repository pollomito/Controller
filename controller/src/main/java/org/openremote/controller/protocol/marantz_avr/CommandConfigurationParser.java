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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.openremote.controller.exception.ConfigurationException;

/**
 * Class responsible for reading the command configuration from an XML representation
 * and providing it as a collection of CommandConfig objects.
 * 
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class CommandConfigurationParser {

   /**
    * Parses the XML configuration at given URL and creates a collection of CommandConfig instances.
    * Collection is a map, with key being the command name.
    * 
    * @param configResource URL of XML configuration to parse
    * @return Map<String, CommandConfig> CommandConfig instances per name
    * @throws ConfigurationException if configuration can not be read or is invalid
    */
   public static Map<String, CommandConfig> parseCommandConfiguration(URL configResource) throws ConfigurationException {
      Map<String, CommandConfig> commandConfigurations = new HashMap<String, CommandConfig>();

      if (configResource != null) {
         SAXBuilder builder = new SAXBuilder();

         try {
            Document doc = builder.build(configResource);
            @SuppressWarnings("unchecked")
            List<Element> commandElements = doc.getRootElement().getChildren();
            for (Element commandElement : commandElements) {
               String commandName = commandElement.getAttributeValue("name");

               @SuppressWarnings("unchecked")
               Class<? extends MarantzAVRCommand> clazz = (Class<? extends MarantzAVRCommand>)Class.forName(commandElement.getAttributeValue("class"));
               CommandConfig commandConfig = new CommandConfig(commandName, commandElement.getAttributeValue("value"), clazz);
               
               Element valuesElement = commandElement.getChild("values");
               if (valuesElement != null) {
                  @SuppressWarnings("unchecked")
                  List<Element> valueElements = valuesElement.getChildren();
                  if (valueElements != null) {
                     for (Element valueElement : valueElements) {
                        commandConfig.addValuePerZone(valueElement.getAttributeValue("zone"), valueElement.getText());
                     }
                  }
               }
               
               Element parametersElement = commandElement.getChild("parameters");
               if (parametersElement != null) {
                  @SuppressWarnings("unchecked")
                  List<Element> parameterElements = parametersElement.getChildren();
                  if (parameterElements != null) {
                     for (Element parameterElement : parameterElements) {
                        commandConfig.addParameter(parameterElement.getAttributeValue("name"), parameterElement.getText());
                     }
                  }
               }
               commandConfigurations.put(commandName, commandConfig);
            }
         } catch (JDOMException e) {
            throw new ConfigurationException("Configuration of commands for Marantz AVR protocol failed.", e);
         } catch (IOException e) {
            throw new ConfigurationException("Configuration of commands for Marantz AVR protocol failed.", e);
         } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Configuration of commands for Marantz AVR protocol failed.", e);
         } catch (SecurityException e) {
            throw new ConfigurationException("Configuration of commands for Marantz AVR protocol failed.", e);
         }
      }
      return commandConfigurations;
   }
   
}
