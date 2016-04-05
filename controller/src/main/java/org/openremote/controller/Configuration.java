/* 
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2015, OpenRemote Inc.
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
package org.openremote.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.openremote.controller.exception.ConfigurationException;
import org.openremote.controller.exception.OpenRemoteException;
import org.openremote.controller.service.ServiceContext;
import org.openremote.controller.utils.Logger;

/**
 * Configuration class acts as a common superclass for various configuration segments. <p>
 *
 * Controller configuration can be set either by modifying local property files or by setting
 * configuration properties in the controller.xml file. If a configuration value is present in both
 * the controller.xml file and in a local configuration (text) file, then the controller.xml
 * configuration takes precedence. <p>
 *
 * If the controller.xml is created with OpenRemote Designer tool then the configuration values
 * are stored in the user's online account and distributed to the Controller when it is
 * synchronized with the online account. The online mode allows support/service personnel to modify
 * Controller configuration remotely.  <p>
 *
 * Note that the functionality described above requires that the subclasses use the configuration
 * value methods of this superclass.
 *
 * TODO:
 *  - this implementation still needs further re-structuring, see
 *    ORCJAVA-183 (http://jira.openremote.org/browse/ORCJAVA-183)
 *    ORCJAVA-193 (http://jira.openremote.org/browse/ORCJAVA-193)
 *
 *
 * @author <a href="mailto:juha@openremote.org>Juha Lindfors</a>
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 *
 * @see ControllerConfiguration
 * @see RoundRobinConfiguration
 */
public abstract class Configuration
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * Common log category for configuration related information.
   */
  protected final static Logger log = Logger.getLogger(Constants.INIT_LOG_CATEGORY + ".configuration");

  protected static Configuration updateWithControllerXMLConfiguration(Configuration config)
  {
    // TODO : remove dependency to deprecated API, see ORCJAVA-193
    Map<String, String> properties = ServiceContext.getDeployer().getConfigurationProperties();

    config.setConfigurationProperties(properties);

    return config;
  }

  /**
   * Utility method to parse and replace a given property with a timeout value. This
   * implementation will convert user convenience values such as '10m' (ten minutes) or
   * '2s' (two seconds) to millisecond values and store them in the original property map.
   *
   * @param properties    the property map the converted value is retrieved from and
   *                      stored into after conversion
   *
   * @param propertyName  the property name to convert
   */
  protected static void setTimeoutValue(Map<String, String> properties, String propertyName)
  {
    if (properties == null)
    {
      return;
    }

    String value = properties.get(propertyName);

    if (value == null)
    {
      return;
    }

    try
    {
      properties.put(propertyName, Integer.toString(timeStringToMillis(value)));
    }

    catch (InvalidTimeException e)
    {
      log.warn(
          "Invalid value {0} for configuration property ''{1}'', timeout has been disabled.",
          value, propertyName
      );

      properties.put(propertyName, "0");
    }

    catch (ConfigurationException e)
    {
      log.warn("Invalid value ''{0}'' for configuration property ''{1}''.", value, propertyName);
    }
  }


  /**
   * Utility method to convert certain string conventions to millisecond values. An integer
   * string with a suffix 'm', 's' or 'ms' are recognized and interpreted as minutes,
   * seconds or milliseconds, respectively.
   *
   * @param timeString  the string representing a time value to convert to milliseconds
   *
   * @return  time value in milliseconds
   *
   * @throws InvalidTimeException     if given time value is negative
   * @throws ConfigurationException   if given time value cannot be converted to an integer
   */
  protected static int timeStringToMillis(String timeString) throws InvalidTimeException,
                                                                    ConfigurationException
  {
    if (timeString == null)
    {
      throw new ConfigurationException("null time");
    }

    timeString = timeString.trim();
    int timeInMillis = 0;

    try
    {
      if (timeString.endsWith("ms"))
      {
        timeInMillis = Integer.parseInt(timeString.substring(0, timeString.length() - 2).trim());
      }

      else if (timeString.endsWith("s"))
      {
        timeInMillis = Integer.parseInt(timeString.substring(0, timeString.length() - 1).trim()) * 1000;
      }

      else if (timeString.endsWith("m"))
      {
        timeInMillis = Integer.parseInt(timeString.substring(0, timeString.length() - 1).trim()) * 1000 * 60;
      }

      else
      {
        timeInMillis = Integer.parseInt(timeString) * 1000;
      }

      if (timeInMillis < 0)
      {
        throw new InvalidTimeException("Invalid time value : {0}", timeInMillis);
      }

      return timeInMillis;
    }

    catch (NumberFormatException e)
    {
      throw new ConfigurationException("Unrecognized time value : ''{0}''", timeString);
    }
  }

  // Instance Fields ------------------------------------------------------------------------------


  private Map<String, String> configurationProperties = new HashMap<String, String>();




  // Protected Methods ----------------------------------------------------------------------------


  public /* TODO */  void setConfigurationProperties(Map<String, String> configurationProperties)
  {
    if (configurationProperties == null)
    {
       return;
    }

    Set<String> keys = configurationProperties.keySet();

    if (keys.contains(ControllerConfiguration.REMOTE_COMMAND_CONNECTION_TIMEOUT))
    {
      setTimeoutValue(
          configurationProperties,
          ControllerConfiguration.REMOTE_COMMAND_CONNECTION_TIMEOUT
      );
    }

    if (keys.contains(ControllerConfiguration.REMOTE_COMMAND_RESPONSE_TIMEOUT))
    {
      setTimeoutValue(
          configurationProperties,
          ControllerConfiguration.REMOTE_COMMAND_RESPONSE_TIMEOUT
      );
    }

    this.configurationProperties = new HashMap<String, String>(configurationProperties);
  }


  protected String preferAttrCustomValue(String attrName, String defaultValue)
  {
    return configurationProperties.containsKey(attrName) ?
        configurationProperties.get(attrName) : defaultValue;
  }

  protected boolean preferAttrCustomValue(String attrName, boolean defaultValue)
  {
    return configurationProperties.containsKey(attrName) ?
        Boolean.valueOf(configurationProperties.get(attrName)) : defaultValue;
  }

  protected int preferAttrCustomValue(String attrName, int defaultValue)
  {
    try
    {
      return configurationProperties.containsKey(attrName)
          ? Integer.valueOf(configurationProperties.get(attrName))
          : defaultValue;
    }

    catch (NumberFormatException e)
    {
      log.warn(
          "Invalid integer value ''{0}'' for property ''{1}''. " +
          "Using default value {2} instead.",
          configurationProperties.get(attrName), attrName, defaultValue
      );

      return defaultValue;
    }
  }

  protected long preferAttrCustomValue(String attrName, long defaultValue)
  {
    try
    {
      return configurationProperties.containsKey(attrName)
          ? Long.valueOf(configurationProperties.get(attrName))
          : defaultValue;
    }

    catch (NumberFormatException e)
    {
        log.warn(
            "Invalid long value ''{0}'' for property ''{1}''. " +
            "Using default value {2} instead.",
            configurationProperties.get(attrName), attrName, defaultValue
        );

        return defaultValue;
    }
  }

  protected String[] preferAttrCustomValue(String attrName, String[] defaultValue)
  {
    return configurationProperties.containsKey(attrName) ?
        configurationProperties.get(attrName).split(",") : defaultValue;
  }

  // Nested Classes -------------------------------------------------------------------------------

  public static class InvalidTimeException extends OpenRemoteException
  {
    public InvalidTimeException(String message, Object... args)
    {
      super(message, args);
    }
  }

}
