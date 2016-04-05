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
import org.openremote.controller.Constants;
import org.openremote.controller.EnOceanConfiguration;
import org.openremote.controller.command.Command;
import org.openremote.controller.command.CommandBuilder;
import org.openremote.controller.command.CommandParameter;
import org.openremote.controller.exception.ConversionException;
import org.openremote.controller.exception.NoSuchCommandException;
import org.openremote.controller.protocol.enocean.port.EspPortConfiguration;
import org.openremote.controller.protocol.enocean.profile.Eep;
import org.openremote.controller.protocol.enocean.profile.EepType;
import org.openremote.controller.utils.Logger;

import java.util.List;

/**
 * EnOceanCommandBuilder is responsible for parsing the XML model from controller.xml and create
 * appropriate Command objects for it. <p>
 *
 * The structure of an EnOcean command XML snippet from controller.xml is shown below:
 *
 * <pre>{@code
 * <command protocol = "enocean" >
 *   <property name = "deviceID" value = "0x12345678"/>
 *   <property name = "EEP" value = "xx-xx-xx"/>
 *   <property name = "command" value = "ON|OFF|STATUS_ROCKER_A|STATUS_ROCKER_B"/>
 * </command>
 * }</pre>
 *
 * The protocol identifier is "enocean" as is shown in the command element's protocol attribute. <p>
 *
 * Nested are a number of properties to be included with EnOcean command. Properties named
 * {@link #ENOCEAN_XMLPROPERTY_DEVICE_ID}, {@link #ENOCEAN_XMLPROPERTY_EEP} and
 * {@link #ENOCEAN_XMLPROPERTY_COMMAND} are mandatory <p>
 *
 * The EnOcean equipment profile (EEP) number follows the established convention of 3
 * hexadecimal numbers with a hyphen as separator character. The valid values for command
 * property are implemented in special EnOcean equipment profile related classes.
 * (e.g property values for profile 'F6-02-01' can be found in the class
 * {@link org.openremote.controller.protocol.enocean.profile.EepF60201})
 *
 * @see org.openremote.controller.command.CommandBuilder
 *
 * @author <a href="mailto:rainer@openremote.org">Rainer Hitz</a>
 */
public class EnOceanCommandBuilder implements CommandBuilder
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * A common log category name intended to be used across all classes related to
   * EnOcean implementation.
   */
  public final static String ENOCEAN_LOG_CATEGORY =
      Constants.CONTROLLER_PROTOCOL_LOG_CATEGORY + "enocean";

  /**
   * String constant for parsing EnOcean protocol XML entries from controller.xml file.
   *
   * This constant is the expected property name value for EnOcean device ID's
   * (<code>{@value}</code>):
   *
   * <pre>{@code
   * <command protocol = "enocean" >
   *   <property name = "deviceID" value = "0x12"/>
   *   <property name = "EEP" value = "xx-xx-xx"/>
   *   <property name = "command" value = "ON"/>
   * </command>
   * }</pre>
   */
  public final static String ENOCEAN_XMLPROPERTY_DEVICE_ID = "deviceID";

  /**
   * String constant for parsing EnOcean protocol XML entries from controller.xml file.
   *
   * This constant is the expected property name value for EnOcean equipment profiles
   * (<code>{@value}</code>):
   *
   * <pre>{@code
   * <command protocol = "enocean" >
   *   <property name = "deviceID" value = "0x12"/>
   *   <property name = "EEP" value = "xx-xx-xx"/>
   *   <property name = "command" value = "ON"/>
   * </command>
   * }</pre>
   */
  public final static String ENOCEAN_XMLPROPERTY_EEP = "EEP";

  /**
   * String constant for parsing EnOcean protocol XML entries from controller.xml file.
   *
   * This constant is the expected property name value for EnOcean commands
   * (<code>{@value}</code>):
   *
   * <pre>{@code
   * <command protocol = "enocean" >
   *   <property name = "deviceID" value = "0x12"/>
   *   <property name = "EEP" value = "xx-xx-xx"/>
   *   <property name = "command" value = "ON"/>
   * </command>
   * }</pre>
   */
  public final static String ENOCEAN_XMLPROPERTY_COMMAND = "command";


  // Class Members --------------------------------------------------------------------------------

  /**
   * EnOcean logger. Uses a common category for all EnOcean related logging.
   */
  private final static Logger log = Logger.getLogger(EnOceanCommandBuilder.ENOCEAN_LOG_CATEGORY);


  // Instance Fields ------------------------------------------------------------------------------

  /**
   * EnOcean configuration manager.
   */
  private ConfigurationManager configManager = null;

  /**
   * EnOcean gateway for sending and receiving radio telegrams.
   */
  private EnOceanGateway gateway = null;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs an EnOcean command builder instance.
   */
  public EnOceanCommandBuilder()
  {
    configManager = new EnOceanConfigurationManager();
  }

  /**
   * Constructs an EnOcean command builder instance with given configuration manager.
   *
   * @param configManager  configuration manager
   */
  public EnOceanCommandBuilder(ConfigurationManager configManager)
  {
    this.configManager = configManager;
  }

  /**
   * {@inheritDoc}
   */
  @Override public Command build(Element element)
  {
    initEnOceanGateway();

    String deviceIDString = null;
    String eepString = null;
    String commandAsString = null;

    // Get the list of properties from XML...

    List<Element> propertyElements = element.getChildren(XML_ELEMENT_PROPERTY, element.getNamespace());

    for (Element el : propertyElements)
    {
      String knxPropertyName = el.getAttributeValue(XML_ATTRIBUTENAME_NAME);
      String knxPropertyValue = el.getAttributeValue(XML_ATTRIBUTENAME_VALUE);

      if (ENOCEAN_XMLPROPERTY_DEVICE_ID.equalsIgnoreCase(knxPropertyName))
      {
        deviceIDString = knxPropertyValue;
      }

      else if (ENOCEAN_XMLPROPERTY_EEP.equalsIgnoreCase(knxPropertyName))
      {
        eepString = knxPropertyValue;
      }

      else if (ENOCEAN_XMLPROPERTY_COMMAND.equalsIgnoreCase(knxPropertyName))
      {
        commandAsString = knxPropertyValue;
      }

      else
      {
        log.warn(
            "Unknown EnOcean property '<" + XML_ELEMENT_PROPERTY + " " +
            XML_ATTRIBUTENAME_NAME + " = \"" + knxPropertyName + "\" " +
            XML_ATTRIBUTENAME_VALUE + " = \"" + knxPropertyValue + "\"/>'."
        );
      }
    }


    // Sanity check on mandatory properties 'command', 'deviceID' and 'EEP'...

    if (deviceIDString == null || "".equals(deviceIDString))
    {
      throw new NoSuchCommandException(
          "EnOcean command must have a '" + ENOCEAN_XMLPROPERTY_DEVICE_ID + "' property."
      );
    }

    if (commandAsString == null || "".equals(commandAsString))
    {
      throw new NoSuchCommandException(
          "EnOcean command must have a '" + ENOCEAN_XMLPROPERTY_COMMAND + "' property."
      );
    }

    if (eepString == null || "".equals(eepString))
    {
      throw new NoSuchCommandException(
          "EnOcean command must have a '" + ENOCEAN_XMLPROPERTY_EEP + "' property."
      );
    }


    // Attempt to build DeviceID instance...

    DeviceID deviceID = null;

    try
    {
      deviceID = DeviceID.fromString(deviceIDString);
    }
    catch (InvalidDeviceIDException e)
    {
      throw new NoSuchCommandException(e.getMessage(), e);
    }


    // Check for and create a parameterized command if present, and translate the command string
    // to a type safe EnOcean Command types...

    String paramValue = element.getAttributeValue(Command.DYNAMIC_VALUE_ATTR_NAME);

    CommandParameter parameter = null;

    if (paramValue != null && !paramValue.equals(""))
    {
      try
      {
        parameter = new CommandParameter(paramValue);
      }
      catch (ConversionException exception)
      {
        throw new NoSuchCommandException(
            "Cannot convert '" + paramValue + "' to command parameter : " + exception.getMessage(),
            exception
        );
      }
    }


    // Translate EEP string into a type save instance...

    EepType eepType = null;

    try
    {
      eepType = EepType.lookup(eepString);
    }
    catch(EepType.InvalidEepTypeExpception e)
    {
      throw new NoSuchCommandException(e.getMessage(), e);
    }

    if(eepType == null)
    {
      throw new NoSuchCommandException("Unsupported EEP type '" + eepString + "'.");
    }

    // Create EEP instance...

    Eep eep = null;

    try
    {
      eep = eepType.createEep(deviceID, commandAsString);
    }
    catch (ConfigurationException e)
    {
      throw new NoSuchCommandException(e.getMessage(), e);
    }

    Command cmd = EnOceanCommand.createCommand(eep, deviceID, this.gateway);

    log.info("Created EnOcean " + cmd + ".");

    return cmd;
  }


  // Private Instance Methods ---------------------------------------------------------------------

  /**
   * Creates an EnOcean gateway instance and establishes a connection to the EnOcean module
   * or reconnects to the EnOcean module if the EnOcean serial port configuration has been
   * changed.
   */
  private synchronized void initEnOceanGateway()
  {
    if(this.gateway == null || configManager.hasPortConfigChanged())
    {
      if(this.gateway != null)
      {
        try
        {
          this.gateway.disconnect();
        }

        catch (ConnectionException e)
        {
          log.error("Failed to disconnect from EnOcean module: {0}", e.getMessage());
        }

        this.gateway = null;
      }

      this.gateway = new EnOceanGateway(
          new EnOceanConnectionManager(), configManager.getPortConfig()
      );

      try
      {
        this.gateway.connect();
      }

      catch (ConfigurationException e)
      {
        log.error("Failed to connect to EnOcean module: {0}", e.getMessage());
      }

      catch (ConnectionException e)
      {
        log.error("Failed to connect to EnOcean module: {0}", e.getMessage());
      }
    }
  }
}
