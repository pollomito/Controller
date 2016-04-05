/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2014, OpenRemote Inc.
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

import junit.framework.Assert;
import org.junit.Test;
import org.openremote.controller.exception.ConfigurationException;

import java.util.HashMap;
import java.util.Map;

/**
 * Unit tests for {@link org.openremote.controller.Configuration} class.
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class ConfigurationTest
{

  // SetConfigurationProperties Tests -------------------------------------------------------------

  /**
   * Test conversion is done when {@link ControllerConfiguration#REMOTE_COMMAND_CONNECTION_TIMEOUT}
   * is present in configuration properties.
   */
  @Test public void testSetConfigurationPropertiesRemoteConnectionTimeout()
  {
    Map<String, String> props = new HashMap<String, String>();
    props.put(ControllerConfiguration.REMOTE_COMMAND_CONNECTION_TIMEOUT, "1m");

    Configuration config = new ControllerConfiguration();
    config.setConfigurationProperties(props);

    int val = config.preferAttrCustomValue(ControllerConfiguration.REMOTE_COMMAND_CONNECTION_TIMEOUT, 1234);

    Assert.assertTrue(val == 60000);
  }

  /**
   * Test conversion is done when {@link ControllerConfiguration#REMOTE_COMMAND_CONNECTION_TIMEOUT}
   * is present in configuration properties (millisecond values).
   */
  @Test public void testSetConfigurationPropertiesRemoteConnectionTimeoutMillis()
  {
    Map<String, String> props = new HashMap<String, String>();
    props.put(ControllerConfiguration.REMOTE_COMMAND_CONNECTION_TIMEOUT, "8 ms");

    Configuration config = new ControllerConfiguration();
    config.setConfigurationProperties(props);

    int val = config.preferAttrCustomValue(ControllerConfiguration.REMOTE_COMMAND_CONNECTION_TIMEOUT, 1234);

    Assert.assertTrue(val == 8);
  }

  /**
   * Test conversion is done when {@link ControllerConfiguration#REMOTE_COMMAND_CONNECTION_TIMEOUT}
   * is present in configuration properties (value in seconds)
   */
  @Test public void testSetConfigurationPropertiesRemoteConnectionTimeoutSeconds()
  {
    Map<String, String> props = new HashMap<String, String>();
    props.put(ControllerConfiguration.REMOTE_COMMAND_CONNECTION_TIMEOUT, "34s");

    Configuration config = new ControllerConfiguration();
    config.setConfigurationProperties(props);

    int val = config.preferAttrCustomValue(ControllerConfiguration.REMOTE_COMMAND_CONNECTION_TIMEOUT, 1234);

    Assert.assertTrue(val == 34000);
  }

  /**
   * Test conversion is done when {@link ControllerConfiguration#REMOTE_COMMAND_CONNECTION_TIMEOUT}
   * is present in configuration properties (implicit seconds value).
   */
  @Test public void testSetConfigurationPropertiesRemoteConnectionTimeoutDefaultSuffix()
  {
    Map<String, String> props = new HashMap<String, String>();
    props.put(ControllerConfiguration.REMOTE_COMMAND_CONNECTION_TIMEOUT, "34");

    Configuration config = new ControllerConfiguration();
    config.setConfigurationProperties(props);

    int val = config.preferAttrCustomValue(ControllerConfiguration.REMOTE_COMMAND_CONNECTION_TIMEOUT, 1234);

    Assert.assertTrue(val == 34000);
  }

  /**
   * Test error behavior when invalid time value is set for
   * {@link ControllerConfiguration#REMOTE_COMMAND_CONNECTION_TIMEOUT} property.
   */
  @Test public void testSetConfigurationPropertiesRemoteConnectionTimeoutInvalidValue()
  {
    Map<String, String> props = new HashMap<String, String>();
    props.put(ControllerConfiguration.REMOTE_COMMAND_CONNECTION_TIMEOUT, "foo");

    // We only handle the conversion errors but don't actually prevent incorrect values.
    // Not ideal but will have to do for now.

    Configuration config = new ControllerConfiguration();
    config.setConfigurationProperties(props);

    String val = config.preferAttrCustomValue(ControllerConfiguration.REMOTE_COMMAND_CONNECTION_TIMEOUT, "bar");

    Assert.assertTrue(val.equals("foo"));
  }

  /**
   * Test behavior when invalid time value is set for
   * {@link ControllerConfiguration#REMOTE_COMMAND_CONNECTION_TIMEOUT} property.
   */
  @Test public void testSetConfigurationPropertiesRemoteConnectionTimeoutNegativeValue()
  {
    Map<String, String> props = new HashMap<String, String>();
    props.put(ControllerConfiguration.REMOTE_COMMAND_CONNECTION_TIMEOUT, "-1");

    Configuration config = new ControllerConfiguration();
    config.setConfigurationProperties(props);

    int val = config.preferAttrCustomValue(ControllerConfiguration.REMOTE_COMMAND_CONNECTION_TIMEOUT, 10);

    Assert.assertTrue(val == 0);
  }


  /**
   * Test conversion is done when {@link ControllerConfiguration#REMOTE_COMMAND_RESPONSE_TIMEOUT}
   * is present in configuration properties.
   */
  @Test public void testSetConfigurationPropertiesRemoteCommandResponseTimeout()
  {
    Map<String, String> props = new HashMap<String, String>();
    props.put(ControllerConfiguration.REMOTE_COMMAND_RESPONSE_TIMEOUT, "1m");

    Configuration config = new ControllerConfiguration();
    config.setConfigurationProperties(props);

    int val = config.preferAttrCustomValue(ControllerConfiguration.REMOTE_COMMAND_RESPONSE_TIMEOUT, 1234);

    Assert.assertTrue(val == 60000);
  }

  /**
   * Test conversion is done when {@link ControllerConfiguration#REMOTE_COMMAND_RESPONSE_TIMEOUT}
   * is present in configuration properties (millisecond values).
   */
  @Test public void testSetConfigurationPropertiesRemoteCommandResponseTimeoutMillis()
  {
    Map<String, String> props = new HashMap<String, String>();
    props.put(ControllerConfiguration.REMOTE_COMMAND_RESPONSE_TIMEOUT, "8 ms");

    Configuration config = new ControllerConfiguration();
    config.setConfigurationProperties(props);

    int val = config.preferAttrCustomValue(ControllerConfiguration.REMOTE_COMMAND_RESPONSE_TIMEOUT, 1234);

    Assert.assertTrue(val == 8);
  }

  /**
   * Test conversion is done when {@link ControllerConfiguration#REMOTE_COMMAND_RESPONSE_TIMEOUT}
   * is present in configuration properties (value in seconds)
   */
  @Test public void testSetConfigurationPropertiesRemoteCommandResponseTimeoutSeconds()
  {
    Map<String, String> props = new HashMap<String, String>();
    props.put(ControllerConfiguration.REMOTE_COMMAND_RESPONSE_TIMEOUT, "34s");

    Configuration config = new ControllerConfiguration();
    config.setConfigurationProperties(props);

    int val = config.preferAttrCustomValue(ControllerConfiguration.REMOTE_COMMAND_RESPONSE_TIMEOUT, 1234);

    Assert.assertTrue(val == 34000);
  }


  /**
   * Test conversion is done when {@link ControllerConfiguration#REMOTE_COMMAND_RESPONSE_TIMEOUT}
   * is present in configuration properties (implicit seconds value).
   */
  @Test public void testSetConfigurationPropertiesRemoteCommandResponseTimeoutDefaultSuffix()
  {
    Map<String, String> props = new HashMap<String, String>();
    props.put(ControllerConfiguration.REMOTE_COMMAND_RESPONSE_TIMEOUT, "34");

    Configuration config = new ControllerConfiguration();
    config.setConfigurationProperties(props);

    int val = config.preferAttrCustomValue(ControllerConfiguration.REMOTE_COMMAND_RESPONSE_TIMEOUT, 1234);

    Assert.assertTrue(val == 34000);
  }

  /**
   * Test error behavior when invalid time value is set for
   * {@link ControllerConfiguration#REMOTE_COMMAND_RESPONSE_TIMEOUT} property.
   */
  @Test public void testSetConfigurationPropertiesRemoteCommandResponseTimeoutInvalidValue()
  {
    Map<String, String> props = new HashMap<String, String>();
    props.put(ControllerConfiguration.REMOTE_COMMAND_RESPONSE_TIMEOUT, "foo");

    // We only handle the conversion errors but don't actually prevent incorrect values.
    // Not ideal but will have to do for now.

    Configuration config = new ControllerConfiguration();
    config.setConfigurationProperties(props);

    String val = config.preferAttrCustomValue(ControllerConfiguration.REMOTE_COMMAND_RESPONSE_TIMEOUT, "bar");

    Assert.assertTrue(val.equals("foo"));
  }

  /**
   * Test behavior when invalid time value is set for
   * {@link ControllerConfiguration#REMOTE_COMMAND_RESPONSE_TIMEOUT} property.
   */
  @Test public void testSetConfigurationPropertiesRemoteCommandResponseTimeoutNegativeValue()
  {
    Map<String, String> props = new HashMap<String, String>();
    props.put(ControllerConfiguration.REMOTE_COMMAND_RESPONSE_TIMEOUT, "-1");

    Configuration config = new ControllerConfiguration();
    config.setConfigurationProperties(props);

    int val = config.preferAttrCustomValue(ControllerConfiguration.REMOTE_COMMAND_RESPONSE_TIMEOUT, 10);

    Assert.assertTrue(val == 0);
  }



  // SetTimeoutValue Tests ------------------------------------------------------------------------

  /**
   * Basic set test.
   */
  @Test public void testSetTimeoutValue()
  {
    Map<String, String> props = new HashMap<String, String>();
    props.put("test", "1m");

    Configuration.setTimeoutValue(props, "test");

    Assert.assertTrue(props.get("test").equals("60000"));
  }

  /**
   * Basic set test with seconds suffix.
   */
  @Test public void testSetTimeoutValueSeconds()
  {
    Map<String, String> props = new HashMap<String, String>();
    props.put("test", "3 s");

    Configuration.setTimeoutValue(props, "test");

    Assert.assertTrue(props.get("test").equals("3000"));
  }

  /**
   * Basic set test with milliseconds suffix.
   */
  @Test public void testSetTimeoutValueMillis()
  {
    Map<String, String> props = new HashMap<String, String>();
    props.put("test", "1ms");

    Configuration.setTimeoutValue(props, "test");

    Assert.assertTrue(props.get("test").equals("1"));
  }

  /**
   * Basic number value test (should be interpreted as seconds).
   */
  @Test public void testSetTimeoutValueNoSuffix()
  {
    Map<String, String> props = new HashMap<String, String>();
    props.put("test", "7");

    Configuration.setTimeoutValue(props, "test");

    Assert.assertTrue(props.get("test").equals("7000"));
  }


  /**
   * Test for negative time value handling (should reset to zero).
   */
  @Test public void testSetTimeoutValueNegative()
  {
    Map<String, String> props = new HashMap<String, String>();
    props.put("test", "-3 ms");

    Configuration.setTimeoutValue(props, "test");

    Assert.assertTrue(props.get("test").equals("0"));
  }

  /**
   * Test for non-integer values.
   */
  @Test public void testSetTimeoutValueInvalid()
  {
    Map<String, String> props = new HashMap<String, String>();
    props.put("test", "foo");

    // We handle the conversion error but don't do anything else with
    // the value for now, so the incorrect value remains in the property
    // map. There might be a better default behavior to implement on this later.

    Configuration.setTimeoutValue(props, "test");

    Assert.assertTrue(props.get("test").equals("foo"));
  }

  /**
   * Test to guard against NPE in properties argument.
   */
  @Test public void testSetTimeoutValueNullProps()
  {
    Configuration.setTimeoutValue(null, "test");
  }

  /**
   * Test to guard against NPE with null property name.
   */
  @Test public void testSetTimeoutValueNullName()
  {
    Map<String, String> props = new HashMap<String, String>();
    props.put("test", "7");

    Configuration.setTimeoutValue(props, null);

    Assert.assertTrue(props.get("test").equals("7"));
  }


  // TimeStringToMillis Tests ---------------------------------------------------------------------


  /**
   * Basic test for converting time values.
   *
   * @throws Exception  if test fails
   */
  @Test public void testTimeStringToMillis() throws Exception
  {
    int millis = Configuration.timeStringToMillis("12");

    Assert.assertTrue(millis == 12000);

    millis = Configuration.timeStringToMillis(" 12 ");

    Assert.assertTrue(millis == 12000);


    millis = Configuration.timeStringToMillis("12ms");

    Assert.assertTrue(millis == 12);

    millis = Configuration.timeStringToMillis("12  ms");

    Assert.assertTrue(millis == 12);

    millis = Configuration.timeStringToMillis("   12  ms   ");

    Assert.assertTrue(millis == 12);


    millis = Configuration.timeStringToMillis("2 m");

    Assert.assertTrue(millis == 120000);

    millis = Configuration.timeStringToMillis("2m");

    Assert.assertTrue(millis == 120000);

    millis = Configuration.timeStringToMillis("  2  m  ");

    Assert.assertTrue(millis == 120000);


    millis = Configuration.timeStringToMillis("2 s");

    Assert.assertTrue(millis == 2000);

    millis = Configuration.timeStringToMillis("2s");

    Assert.assertTrue(millis == 2000);

    millis = Configuration.timeStringToMillis("  2  s  ");

    Assert.assertTrue(millis == 2000);

  }

  /**
   * Test for error behavior when non-integer string.
   *
   * @throws Configuration.InvalidTimeException if test fails
   */
  @Test public void testTimeStringToMillisInvalidValue() throws Configuration.InvalidTimeException
  {
    try
    {
      Configuration.timeStringToMillis("foo");

      Assert.fail("should not get here");
    }

    catch (ConfigurationException e)
    {
      // expected...
    }
  }

  /**
   * Test for error behavior when negative integer value.
   *
   * @throws ConfigurationException if test fails
   */
  @Test public void testTimeStringToMillisNegativeValue() throws ConfigurationException
  {
    try
    {
      Configuration.timeStringToMillis("-10");

      Assert.fail("should not get here");
    }

    catch (Configuration.InvalidTimeException e)
    {
      // expected...
    }
  }


  /**
   * Test for error behavior with null value.
   *
   * @throws Configuration.InvalidTimeException if test fails
   */
  @Test public void testTimeStringToMillisNullValue() throws Configuration.InvalidTimeException
  {
    try
    {
      Configuration.timeStringToMillis(null);

      Assert.fail("should not get here");
    }

    catch (ConfigurationException e)
    {
      // expected...
    }
  }

  /**
   * Test for error with empty string value.
   *
   * @throws Configuration.InvalidTimeException if test fails
   */
  @Test public void testTimeStringToMillisEmptyValue() throws Configuration.InvalidTimeException
  {
    try
    {
      Configuration.timeStringToMillis("");

      Assert.fail("should not get here");
    }

    catch (ConfigurationException e)
    {
      // expected...
    }
  }


}

