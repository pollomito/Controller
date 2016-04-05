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
package org.openremote.controller.protocol.dscit100;

import java.util.List;

import org.jdom.Element;
import org.openremote.controller.command.Command;
import org.openremote.controller.command.CommandBuilder;
import org.openremote.controller.exception.NoSuchCommandException;
import org.openremote.controller.Constants;
import org.openremote.controller.utils.Logger;

/**
 * IP-based integration to DSC security systems. This implementation currently covers integration
 * via two different gateways -- DSC IT-100 and Envisalink.
 *
 * @author Greg Rapp
 * @author Phil Taylor
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class DSCIT100CommandBuilder implements CommandBuilder
{

  //
  // TODO  :
  //
  //   - This implementation is now used for both DSC IT-100 interface and Envisalink
  //     interface -- may be a candidate for a rename to simply DSC


  // Constants ------------------------------------------------------------------------------------

  /**
   * A common log category name intended to be used across all classes related
   * to DSC implementation.
   */
  public final static String DSC_LOG_CATEGORY  = Constants.CONTROLLER_PROTOCOL_LOG_CATEGORY + "dsc";

  /**
   * String constant for parsing DSC protocol XML entries from controller.xml file.
   * 
   * This constant is the expected property name value for DSC addresses
   * (<code>{@value}</code>):
   * 
   * <pre>
   * {@code
   * <command protocol = "dscit100" >
   *   <property name = "address" value = "x.x.x.x:xxxx"/>
   *   <property name = "command" value = "ARM"/>
   *   <property name = "code" value = "1234"/>
   *   <property name = "target" value = "1"/>
   * </command>
   * }
   * </pre>
   */
  public final static String DSC_XMLPROPERTY_ADDRESS = "address";

  /**
   * String constant for parsing DSC protocol XML entries from controller.xml file.
   * 
   * This constant is the expected property name value for DSC commands ({@value} ):
   * 
   * <pre>
   * {@code
   * <command protocol = "dscit100" >
   *   <property name = "address" value = "x.x.x.x:xxxx"/>
   *   <property name = "command" value = "ARM"/>
   *   <property name = "code" value = "1234"/>
   *   <property name = "target" value = "1"/>
   * </command>
   * }
   * </pre>
   */
  public final static String DSC_XMLPROPERTY_COMMAND = "command";

  /**
   * String constant for parsing DSC protocol XML entries from controller.xml file.
   * 
   * This constant is the expected property name value for DSC codes ({@value} ):
   * 
   * <pre>
   * {@code
   * <command protocol = "dscit100" >
   *   <property name = "address" value = "x.x.x.x:xxxx"/>
   *   <property name = "command" value = "ARM"/>
   *   <property name = "code" value = "1234"/>
   *   <property name = "target" value = "1"/>
   * </command>
   * }
   * </pre>
   */
  public final static String DSC_XMLPROPERTY_CODE = "code";

  /**
   * String constant for parsing DSC protocol XML entries from controller.xml file.
   * 
   * This constant is the expected property name value for DSC targets ({@value} ):
   * 
   * <pre>
   * {@code
   * <command protocol = "dscit100" >
   *   <property name = "address" value = "x.x.x.x:xxxx"/>
   *   <property name = "command" value = "ARM"/>
   *   <property name = "code" value = "1234"/>
   *   <property name = "target" value = "1"/>
   * </command>
   * }
   * </pre>
   */
  public final static String DSC_XMLPROPERTY_TARGET = "target";

  /**
   * Implicit name property for all commands that were introduced in Designer 2.13.x and later.
   * This property should be eventually provided by the API, at which point this constant can
   * be replaced.
   */
  public final static String COMMAND_XMLPROPERTY_NAME = "name";


  // Class Members --------------------------------------------------------------------------------

  /**
   * Logging. Use common DSC log category for all DSC related classes.
   */
  private static Logger log = Logger.getLogger(DSC_LOG_CATEGORY);


  // Instance Fields ------------------------------------------------------------------------------

  /**
   * Connection manager handles the IP connections to gateway(s). The commands parsed and
   * instantiated by this builder will be handled to this connection manager to send to the
   * gateway.
   */
  private final DSCIT100ConnectionManager connectionManager;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * DSCIT100CommandBuilder is responsible for parsing the XML model from controller.xml and
   * creating appropriate DSC command objects. These commands are IP based commands towards
   * either an IT-100 or Envisalink gateways. <p>
   *
   * The structure of a DSC command XML snippet from controller.xml is shown below:
   *
   * <pre>{@code
   * <command protocol = "dscit100" >
   *   <property name = "address" value = "x.x.x.x:xxxx"/>
   *   <property name = "command" value = "ARM"/>
   *   <property name = "code" value = "1234"/>
   *   <property name = "target" value = "1"/>
   * </command>
   * }</pre>
   *
   * The protocol identifier is "dscit100" as is shown in the command element's protocol attribute. <p>
   *
   * @param credentials
   *          Connection authentication credentials for the gateway. These are required for
   *          connections towards Envisalink gateways. For IT-100 gateway, use a null argument
   *          for no credentials (IT-100 does not require connection credentials).
   */
  public DSCIT100CommandBuilder(String credentials)
  {
	  connectionManager = new DSCIT100ConnectionManager(credentials);
  }

  /**
   * Parses the DSC command XML snippets and builds a corresponding DSC command instance. <p>
   * 
   * The expected XML structure is:
   * 
   * <pre>
   * {@code
   * <command protocol = "dscit100" >
   *   <property name = "address" value = "x.x.x.x:xxxx"/>
   *   <property name = "command" value = "ARM"/>
   *   <property name = "code" value = "1234"/>
   *   <property name = "target" value = "1"/>
   * </command>
   * }
   * </pre>
   * 
   * @see DSCIT100Command
   * 
   * @throws NoSuchCommandException
   *           if the DSC command instance cannot be constructed from the
   *           XML snippet for any reason
   * 
   * @return an immutable DSC command instance with known configured
   *         properties set
   */
  @Override public Command build(Element element)
  {
    String address = null;
    String command = null;
    String code = null;
    String target = null;

    // Implementation note:
    //
    //  - Command names were added as implicit command properties since OpenRemote Designer 2.13.x
    //    releases. Configuration created by earlier versions of Designer may not include the
    //    name property, hence the default value set below.

    String commandName = "<not available>";

    // Get the list of properties from XML...

    @SuppressWarnings("unchecked")
    List<Element> propertyElements = element.getChildren(XML_ELEMENT_PROPERTY,
        element.getNamespace());

    for (Element el : propertyElements)
    {
      String propertyName = el.getAttributeValue(XML_ATTRIBUTENAME_NAME);
      String propertyValue = el.getAttributeValue(XML_ATTRIBUTENAME_VALUE);

      if (DSC_XMLPROPERTY_ADDRESS.equalsIgnoreCase(propertyName))
      {
        address = propertyValue;
      }

      else if (DSC_XMLPROPERTY_COMMAND.equalsIgnoreCase(propertyName))
      {
        command = propertyValue;
      }

      else if (DSC_XMLPROPERTY_CODE.equalsIgnoreCase(propertyName))
      {
        code = propertyValue;
      }

      else if (DSC_XMLPROPERTY_TARGET.equalsIgnoreCase(propertyName))
      {
        target = propertyValue;
      }

      // NOTE : grabbing the implicit command name so not to generate a misleading warning
      //        about unused variable.

      else if (COMMAND_XMLPROPERTY_NAME.equalsIgnoreCase(propertyName))
      {
        commandName = propertyValue;
      }

      else
      {
        log.warn("Unknown DSC property '<" + XML_ELEMENT_PROPERTY + " "
            + XML_ATTRIBUTENAME_NAME + " = \"" + propertyName + "\" "
            + XML_ATTRIBUTENAME_VALUE + " = \"" + propertyValue + "\"/>'. Value is ignored.");
      }
    }

    // Sanity check on mandatory properties 'command' and 'address'...

    if (address == null || "".equals(address))
    {
      throw new NoSuchCommandException("DSC command must have a '"
          + DSC_XMLPROPERTY_ADDRESS + "' property.");
    }

    if (command == null || "".equals(command))
    {
      throw new NoSuchCommandException("DSC command must have a '"
          + DSC_XMLPROPERTY_COMMAND + "' property.");
    }

    Command cmd = DSCIT100Command.createCommand(
        command, address, code, target, connectionManager
    );

    log.info("Created DSC Command " + cmd);

    return cmd;
  }

}
