/* OpenRemote, the Home of the Digital Home.
* Copyright 2008-2011, OpenRemote Inc.
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
package org.openremote.controller.protocol.socket;

import java.util.List;

import org.jdom.Element;
import org.openremote.controller.Constants;
import org.openremote.controller.command.Command;
import org.openremote.controller.command.CommandBuilder;
import org.openremote.controller.exception.NoSuchCommandException;
import org.openremote.controller.utils.CommandUtil;
import org.openremote.controller.utils.Logger;
import org.openremote.controller.utils.Strings;


/**
 * The Class TCPSocketCommandBuilder.
 *
 * @author Marcus 2009-4-26
 */
public class TCPSocketCommandBuilder implements CommandBuilder {

   // Constants
   // ------------------------------------------------------------------------------------

   /**
    * Common log category name for all HTTP protocol related logging.
    */
   public final static String TCP_PROTOCOL_LOG_CATEGORY = Constants.CONTROLLER_PROTOCOL_LOG_CATEGORY + "tcp";

   private final static String STR_ATTRIBUTE_NAME_PORT = "port";
   private final static String STR_ATTRIBUTE_NAME_IP = "ipAddress";
   private final static String STR_ATTRIBUTE_NAME_COMMAND = "command";
   private final static String STR_ATTRIBUTE_NAME_REGEX = "regex";
   private final static String STR_ATTRIBUTE_NAME_POLLINGINTERVAL = "pollingInterval";
   
   // Class Members
   // --------------------------------------------------------------------------------

   /**
    * Logger for this HTTP protocol implementation.
    */
   private final static Logger logger = Logger.getLogger(TCP_PROTOCOL_LOG_CATEGORY);
   
   /**
    * {@inheritDoc}
    */
   @SuppressWarnings("unchecked")
   public Command build(Element element) {
      logger.debug("Building HttGetCommand");
      List<Element> propertyEles = element.getChildren("property", element.getNamespace());
      String port = null;
      String ipAddress = null;
      String command = null;
      String regex = null;
      String interval = null;
      Integer intervalInMillis = null;

      // read values from config xml

      for (Element ele : propertyEles)
      {
        String elementName = ele.getAttributeValue(CommandBuilder.XML_ATTRIBUTENAME_NAME);
        String elementValue = ele.getAttributeValue(CommandBuilder.XML_ATTRIBUTENAME_VALUE);

        if (STR_ATTRIBUTE_NAME_PORT.equals(elementName))
        {
          port = elementValue;
          logger.debug("TCPSocketCommand: port = " + port);
        } else if (STR_ATTRIBUTE_NAME_IP.equals(elementName))
        {
          ipAddress = elementValue;
          logger.debug("TCPSocketCommand: ipAddress = " + ipAddress);
        } else if (STR_ATTRIBUTE_NAME_COMMAND.equals(elementName))
        {
          command = CommandUtil.parseStringWithParam(element, elementValue);
          logger.debug("TCPSocketCommand: command = " + command);
        } else if (STR_ATTRIBUTE_NAME_POLLINGINTERVAL.equals(elementName))
        {
          interval = elementValue;
          logger.debug("TCPSocketCommand: pollingInterval = " + interval);
        } else if (STR_ATTRIBUTE_NAME_REGEX.equals(elementName))
        {
          regex = elementValue;
          logger.debug("TCPSocketCommand: regex = " + regex);
        }
      }
      try
      {
        if (null != interval) {
          intervalInMillis = Integer.valueOf(Strings.convertPollingIntervalString(interval));
        }
      } catch (Exception e1)
      {
        throw new NoSuchCommandException("Unable to create TCPSocketCommand, pollingInterval could not be converted into milliseconds");
      }

      return new TCPSocketCommand(ipAddress, port, command, regex, intervalInMillis);
   }

}
