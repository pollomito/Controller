/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2016, OpenRemote Inc.
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
package org.openremote.controller.protocol.alarm;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.jdom.Element;
import org.openremote.controller.Constants;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.command.Command;
import org.openremote.controller.command.CommandBuilder;
import org.openremote.controller.exception.CommandBuildException;
import org.openremote.controller.protocol.alarm.Alarm.Day;
import org.openremote.controller.service.Deployer;
import org.openremote.controller.service.ServiceContext;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author <a href="mailto:richard@openremote.org">Richard Turner</a>
 *
 */
public class AlarmCommandBuilder implements CommandBuilder {
   public final static String ALARM_PROTOCOL_LOG_CATEGORY = Constants.CONTROLLER_PROTOCOL_LOG_CATEGORY + "alarm";
   private final static String STR_ATTRIBUTE_NAME_NAME = "alarmName";
   private final static String STR_ATTRIBUTE_NAME_COMMAND = "command";
   private final static String STR_ATTRIBUTE_NAME_VALUE = "value";
   private static Logger log = Logger.getLogger(ALARM_PROTOCOL_LOG_CATEGORY);
   private static boolean valid;
   private Deployer deployer;
   private static URI configUri;
      
   static {
      // Resolve config file
      URI dirUri = getDirUri();
      
      if (dirUri != null) {      
         configUri = dirUri.resolve("alarm-config.xml");
      }
   }
   
   public AlarmCommandBuilder(Deployer deployer) {
      this.deployer = deployer;
      
      if (configUri == null || !hasAccess(configUri)) {
         log.error("Alarm config file '" + configUri + "' does not exist or cannot read/write to it.");
         return;
      }
      
      // Parse the config file if it exists
      List<Alarm> alarms = parseConfig(configUri);

      // Add the alarms
      for (Alarm alarm : alarms) {
         AlarmManager.addAlarm(alarm);
      }
      
      valid = true;
   }  
   
   public Command build(Element element) {
      String alarmName = null;
      AlarmCommand.Action action = null;
      String commandValue = null;
      String[] args = null;
      
      List<Element> propertyElements = element.getChildren(XML_ELEMENT_PROPERTY, element.getNamespace());
      
      for (Element ele : propertyElements)
      {
         String elementName = ele.getAttributeValue(CommandBuilder.XML_ATTRIBUTENAME_NAME).trim();
         String elementValue = ele.getAttributeValue(CommandBuilder.XML_ATTRIBUTENAME_VALUE).trim();
         
         if (STR_ATTRIBUTE_NAME_COMMAND.equals(elementName))
         {
            try {
               action = AlarmCommand.Action.valueOf(elementValue.trim());
             } catch(Exception e) {
                throw new CommandBuildException("Invalid command action specified '" + elementValue + "'");
             }
         }
         else if (STR_ATTRIBUTE_NAME_VALUE.equals(elementName))
         {
            commandValue = elementValue.trim();
         }
         else if (STR_ATTRIBUTE_NAME_NAME.equals(elementName))
         {
            alarmName = elementValue.trim();
         }
      }
      
      // Check we have a valid command
      if (alarmName == null || "".equals(alarmName)) {
         throw new CommandBuildException("Alarm name is not valid");
      }
      
      if (action == null) {
         throw new CommandBuildException("Alarm command is not valid");
      }
      
      if (commandValue != null) {
         args = commandValue.split(":");
         
         for (int i=0; i< args.length; i++) {
            args[i] = args[i].trim();
         }
      }
      
      // Command specific validation
      switch (action) {
         case DAY:
         case DAY_STATUS:
         {
            int argLength = action == AlarmCommand.Action.DAY ? 2 : 1;
            if (args == null || args.length != argLength) {
               throw new CommandBuildException("Alarm command '" + action + "' must have " + argLength + " args in the command value (e.g. Action = DAY, Command Value = Alarm1:MON:TRUE; Action = DAY_STATUS, Command Value = Alarm1:MON)");
            }
            try {
               Day day = Day.valueOf(args[0]);
            } catch (Exception e) {
               throw new CommandBuildException("Alarm day argument is not valid must be name of day (e.g. SUN, MON, TUE, WED, THU, FRI, SAT)", e); 
            }
            break;
         }
         case ENABLED:
         {
            int argLength = 1;
            if (args.length != argLength) {
               throw new CommandBuildException("Alarm command '" + action + "' must have " + argLength + " args in the command value (e.g. Action=ENABLED, Name=Alarm1, Value=TRUE)");
            }
            break;
         }
         case TIME:
         case TIME_RELATIVE:
         {
            int argLength = 2;
            if (args.length != argLength) {
               throw new CommandBuildException("Alarm command '" + action + "' must have " + argLength + " args in the command value (e.g. Action=TIME, Name=Alarm1, Value=06:30; Action=TIME_RELATIVE, Name=Alarm1, Value=-1:0)");
            }
            
            // Check integers
            try {
               Integer.parseInt(args[0]);
               Integer.parseInt(args[1]);
            } catch (Exception e) {
               throw new CommandBuildException("Alarm command '" + action + "' time values must be integers for hours and mins (e.g. Action=TIME, Name=Alarm1, Value=06:30; Action=TIME_RELATIVE, Name=Alarm1, Value=-1:0)");
            }
            break;
         }
      }
      
      return new AlarmCommand(action, alarmName, args);
   }
   
   
   /**
    * Get the directory URI for where the alarm config file should exist
    * @return
    */
   private static URI getDirUri() {
      ControllerConfiguration config = ServiceContext.getControllerConfiguration();
      URI resourceURI;
      URI dirURI = null;
      
      try {
         resourceURI = new URI(config.getResourcePath());

         if (!resourceURI.isAbsolute()) {
            resourceURI = new File(config.getResourcePath()).toURI();
         }
         
         dirURI = resourceURI.resolve("alarm/");
      } catch (URISyntaxException e) {
         log.error("Property 'resource.path' value '" + config.getResourcePath() + "' cannot be parsed. "
               + "It must contain a valid URI : {1}", e);
      }

      return dirURI;
   }
   
   static URI getConfigUri() {
      return configUri;
   }
   
   /**
    * Checks URI exists and can be read and written to
    * @param uri
    * @return
    */
   private boolean hasAccess(URI uri) {
      File dir = new File(uri);

      try {
         return dir.exists() && dir.canRead() && dir.canWrite();
      } catch (SecurityException e) {
         log.error(e);
      }
      
      return false;
   }
   
   private List<Alarm> parseConfig(URI configUri) {
      List<Alarm> alarms = new ArrayList<Alarm>();
      
      try {
         File configFile = new File(configUri);
         DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
         Document doc = dBuilder.parse(configFile);
         doc.getDocumentElement().normalize();
         
         NodeList alarmNodes = doc.getElementsByTagName("alarm");
         
         // Parse alarm nodes
         for (int i=0; i<alarmNodes.getLength(); i++) {
            Node alarmNode = alarmNodes.item(i);
            NamedNodeMap attributes = alarmNode.getAttributes();
            String alarmName = attributes.getNamedItem("name").getNodeValue();
            String cronExpression = null;
            boolean enabled = true;
            List<AlarmCommandRef> commands = new ArrayList<AlarmCommandRef>();
            
            NodeList alarmPropNodes = alarmNode.getChildNodes();
            
            for (int j=0; j<alarmPropNodes.getLength(); j++) {
               Node alarmPropNode = alarmPropNodes.item(j);

               if (alarmPropNode.getNodeName().equalsIgnoreCase("cronExpression")) {
                  cronExpression = alarmPropNode.getTextContent();
               } else if (alarmPropNode.getNodeName().equalsIgnoreCase("enabled")) {
                     enabled = new Boolean(alarmPropNode.getTextContent());
               } else if (alarmPropNode.getNodeName().equalsIgnoreCase("commands")) {
                  // Parse Command Refs
                  NodeList commandNodes = alarmPropNode.getChildNodes();
                  
                  for (int k=0; k<commandNodes.getLength(); k++) {
                     Node commandNode = commandNodes.item(k);
                     String deviceName = null;
                     String commandName = null;
                     String commandParameter = null;
                     int commandDelay = 0;
                     
                     if (commandNode.getNodeName().equalsIgnoreCase("commandRef")) {
                        NamedNodeMap commandAttributes = commandNode.getAttributes();
                        Node deviceNode = commandAttributes.getNamedItem("device");
                        Node nameNode = commandAttributes.getNamedItem("name");
                        Node parameterNode = commandAttributes.getNamedItem("parameter");
                        Node delayNode = commandAttributes.getNamedItem("delay");
                        deviceName = deviceNode != null ? deviceNode.getNodeValue() : null;
                        commandName = nameNode != null ? nameNode.getNodeValue() : null;
                        commandParameter = parameterNode != null ? parameterNode.getNodeValue() : null;
                        if (delayNode != null) {
                           commandDelay = Integer.parseInt(delayNode.getNodeValue());
                           commandDelay = Math.max(0, commandDelay);
                        }
                     }
                     
                     if (deviceName != null && !deviceName.equals("") && commandName != null && !commandName.equals("")) {
                        AlarmCommandRef commandRef = new AlarmCommandRef(deviceName, commandName, commandParameter, commandDelay);
                        commands.add(commandRef);
                     }
                  }
               }
            }
            
            // Check validity of alarm
            if (alarmName != null && !alarmName.equals("") && cronExpression != null && !cronExpression.equals("") && commands.size() > 0) {
               // Create the alarm
               Alarm alarm = new Alarm(alarmName, commands, cronExpression, enabled);                              
               alarms.add(alarm);
            } else {
               log.error("Invalid alarm definition found in alarm-config.xml");
            }
         }
         
      } catch (Exception e) {
         log.error("Error parsing alarm-config.xml", e);
      }
      
      return alarms;
   }
}
