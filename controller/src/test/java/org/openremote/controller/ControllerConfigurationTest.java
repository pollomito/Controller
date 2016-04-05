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

import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Unit tests for {@link org.openremote.controller.ControllerConfiguration} class.
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class ControllerConfigurationTest
{


  // SetRemoteCommandServiceURI Tests -------------------------------------------------------------

  /**
   * Test setting the remote command service URI value.
   */
  @Test public void testRemoteCommandServiceURI()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandServiceURI("localhost:8080");

    Assert.assertTrue(config.getRemoteCommandServiceURI().equals("localhost:8080"));

    Assert.assertTrue(config.getRemoteCommandURIs().length == 1);
  }

  /**
   * Test setting comma separated list of remote command service URIs for client side
   * failover.
   */
  @Test public void testRemoteCommandServiceURIs()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandServiceURI("localhost:8080, localhost:8081, localhost:8082");

    Assert.assertTrue(
        config.getRemoteCommandServiceURI().equals("localhost:8080, localhost:8081, localhost:8082")
    );

    Assert.assertTrue(config.getRemoteCommandURIs().length == 3);
  }

  /**
   * Test setting comma separated list of remote command service URIs for client side
   * failover but with some incorrect values.
   */
  @Test public void testRemoteCommandServiceURIsPartialError()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandServiceURI("localhost:8080, localhost 8081, localhost:8082");

    Assert.assertTrue(
        config.getRemoteCommandServiceURI().equals("localhost:8080, localhost 8081, localhost:8082")
    );

    Assert.assertTrue(config.getRemoteCommandURIs().length == 2);
  }

  /**
   * Test default undefined remote command service URI property.
   */
  @Test public void testRemoteCommandServiceURINoValue()
  {
    ControllerConfiguration config = new ControllerConfiguration();

    Assert.assertTrue(config.getRemoteCommandServiceURI().equals("<undefined>"));

    Assert.assertTrue(config.getRemoteCommandURIs().length == 0);
  }

  /**
   * Test property map override of remote service URI value.
   */
  @Test public void testRemoteCommandServiceURIOverride()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandServiceURI("foobar");

    Map<String, String> props = new HashMap<String, String>(3);
    props.put(ControllerConfiguration.REMOTE_COMMAND_SERVICE_URI, "lapa, tapa:21");
    config.setConfigurationProperties(props);

    Assert.assertTrue(config.getRemoteCommandServiceURI().equals("lapa, tapa:21"));
    Assert.assertTrue(config.getRemoteCommandURIs().length == 2);
  }

  /**
   * Test property map override of remote service URI values when multiple comma separated
   * URIs are set for failover.
   */
  @Test public void testRemoteCommandServiceURIOverrideMultiValue()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandServiceURI("localhost");

    Map<String, String> props = new HashMap<String, String>(3);
    props.put(ControllerConfiguration.REMOTE_COMMAND_SERVICE_URI, "foo, bar:80, https://acme.com");
    config.setConfigurationProperties(props);

    Assert.assertTrue(config.getRemoteCommandServiceURI().equals("foo, bar:80, https://acme.com"));
    Assert.assertTrue(config.getRemoteCommandURIs().length == 3);

    Set<String> uris = new HashSet<String>();

    for (URI uri : config.getRemoteCommandURIs())
    {
      uris.add(uri.toString());
    }

    Assert.assertTrue(uris.contains("foo"));
    Assert.assertTrue(uris.contains("bar:80"));
    Assert.assertTrue(uris.contains("https://acme.com"));
  }

  /**
   * Test property map override of remote service URI value with invalid value.
   */
  @Test public void testRemoteCommandServiceURIOverrideInvalidValue()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandServiceURI("localhost");

    // We don't fail early in this case which is not nice, ideally we should detect
    // invalid URIs already at set stage. Now it will only cause late stage fail
    // by returning empty list in a later stage which is not as nice. But will have to do
    // for now.

    Map<String, String> props = new HashMap<String, String>(3);
    props.put(ControllerConfiguration.REMOTE_COMMAND_SERVICE_URI, "foo 80");
    config.setConfigurationProperties(props);

    Assert.assertTrue(config.getRemoteCommandServiceURI().equals("foo 80"));
    Assert.assertTrue(config.getRemoteCommandURIs().length == 0);
  }

  /**
   * Test property map ovveride of remote service URI when no mapping value has been set.
   */
  @Test public void testRemoteCommandServiceURIOverrideEmptyValue()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandServiceURI("http://localhost:9876");

    Map<String, String> props = new HashMap<String, String>(3);
    config.setConfigurationProperties(props);

    Assert.assertTrue(config.getRemoteCommandServiceURI().equals("http://localhost:9876"));
    Assert.assertTrue(config.getRemoteCommandURIs().length == 1);
    Assert.assertTrue((config.getRemoteCommandURIs() [0]).toString().equals("http://localhost:9876"));
  }



  // SetRemoteConnectionTimeout Tests -------------------------------------------------------------

  /**
   * Test setting connection timeout values via configuration API.
   */
  @Test public void testSetRemoteCommandConnectionTimeout()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandConnectionTimeout("1");

    Assert.assertTrue(config.getRemoteCommandConnectionTimeoutMillis() == 1000);

    config.setRemoteCommandConnectionTimeout("-1");

    Assert.assertTrue(config.getRemoteCommandConnectionTimeoutMillis() == 0);

    config.setRemoteCommandConnectionTimeout("0");

    Assert.assertTrue(config.getRemoteCommandConnectionTimeoutMillis() == 0);

    config.setRemoteCommandConnectionTimeout("1s");

    Assert.assertTrue(config.getRemoteCommandConnectionTimeoutMillis() == 1000);

    config.setRemoteCommandConnectionTimeout("-1s");

    Assert.assertTrue(config.getRemoteCommandConnectionTimeoutMillis() == 0);

    config.setRemoteCommandConnectionTimeout("0s");

    Assert.assertTrue(config.getRemoteCommandConnectionTimeoutMillis() == 0);

    config.setRemoteCommandConnectionTimeout("1ms");

    Assert.assertTrue(config.getRemoteCommandConnectionTimeoutMillis() == 1);

    config.setRemoteCommandConnectionTimeout("0ms");

    Assert.assertTrue(config.getRemoteCommandConnectionTimeoutMillis() == 0);

    config.setRemoteCommandConnectionTimeout("-1ms");

    Assert.assertTrue(config.getRemoteCommandConnectionTimeoutMillis() == 0);

    config.setRemoteCommandConnectionTimeout("1m");

    Assert.assertTrue(config.getRemoteCommandConnectionTimeoutMillis() == 1000 * 60);

    config.setRemoteCommandConnectionTimeout("0m");

    Assert.assertTrue(config.getRemoteCommandConnectionTimeoutMillis() == 0);

    config.setRemoteCommandConnectionTimeout("-1m");

    Assert.assertTrue(config.getRemoteCommandConnectionTimeoutMillis() == 0);
  }

  /**
   * Test setting connection timeout values via configuration API. When a non-integer
   * value is provided, should fall back to a default value.
   */
  @Test public void testSetRemoteCommandConnectionTimeoutError()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandConnectionTimeout("foo");

    Assert.assertTrue(
        config.getRemoteCommandConnectionTimeoutMillis() ==
        ControllerConfiguration.DEFAULT_REMOTE_COMMAND_CONNECTION_TIMEOUT
    );
  }

  /**
   * Test setting connection timeout values via configuration API. Uses convenience
   * formatting for users to specify seconds, minutes or milliseconds explicitly.
   */
  @Test public void testSetRemoteCommandConnectionTimeout2()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandConnectionTimeout("5");

    Assert.assertTrue(config.getRemoteCommandConnectionTimeoutMillis() == 5000);

    config.setRemoteCommandConnectionTimeout("10s");

    Assert.assertTrue(config.getRemoteCommandConnectionTimeoutMillis() == 10000);

    config.setRemoteCommandConnectionTimeout("200ms");

    Assert.assertTrue(config.getRemoteCommandConnectionTimeoutMillis() == 200);

    config.setRemoteCommandConnectionTimeout("5m");

    Assert.assertTrue(config.getRemoteCommandConnectionTimeoutMillis() == 1000 * 60 * 5);
  }


  /**
   * Test setting connection timeout via overriding property list. The direct
   * API provided value should be overridden.
   */
  @Test public void testSetRemoteCommandConnectionTimeoutOverride()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandConnectionTimeout("5");

    Map<String, String> props = new HashMap<String, String>(3);
    props.put(ControllerConfiguration.REMOTE_COMMAND_CONNECTION_TIMEOUT, "1");
    config.setConfigurationProperties(props);

    Assert.assertTrue(
        "Got " + config.getRemoteCommandConnectionTimeoutMillis(),
        config.getRemoteCommandConnectionTimeoutMillis() == 1000
    );
  }

  /**
   * Test setting connection timeout via overriding property list. The direct
   * API provided value should be overridden. Using convenience formatting
   * provided to users to specify minute values.
   */
  @Test public void testSetRemoteCommandConnectionTimeoutOverride2()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandConnectionTimeout("5");

    Map<String, String> props = new HashMap<String, String>(3);
    props.put(ControllerConfiguration.REMOTE_COMMAND_CONNECTION_TIMEOUT, "2 m");
    config.setConfigurationProperties(props);

    Assert.assertTrue(
        "Got " + config.getRemoteCommandConnectionTimeoutMillis(),
        config.getRemoteCommandConnectionTimeoutMillis() == 1000 * 60 * 2
    );
  }

  /**
   * Test setting connection timeout via overriding property list. The direct
   * API provided value should be overridden. Using convenience formatting
   * provided to users to specify millisecond values.
   */
  @Test public void testSetRemoteCommandConnectionTimeoutOverride3()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandConnectionTimeout("5");

    Map<String, String> props = new HashMap<String, String>(3);
    props.put(ControllerConfiguration.REMOTE_COMMAND_CONNECTION_TIMEOUT, "12 ms");
    config.setConfigurationProperties(props);

    Assert.assertTrue(
        "Got " + config.getRemoteCommandConnectionTimeoutMillis(),
        config.getRemoteCommandConnectionTimeoutMillis() == 12
    );
  }


  /**
   * Test setting connection timeout via overriding property list. The invalid
   * value should result in falling back to API provided value.
   */
  @Test public void testSetRemoteCommandConnectionTimeoutOverrideInvalidValue()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandConnectionTimeout("5");

    Map<String, String> props = new HashMap<String, String>(3);
    props.put(ControllerConfiguration.REMOTE_COMMAND_CONNECTION_TIMEOUT, "foo");
    config.setConfigurationProperties(props);

    Assert.assertTrue(
        "Got " + config.getRemoteCommandConnectionTimeoutMillis(),
        config.getRemoteCommandConnectionTimeoutMillis() == 5000
    );
  }

  /**
   * Test setting connection timeout via overriding property list. The invalid
   * value should result in falling back default value.
   */
  @Test public void testSetRemoteCommandConnectionTimeoutOverrideInvalidValue2()
  {
    ControllerConfiguration config = new ControllerConfiguration();

    Map<String, String> props = new HashMap<String, String>(3);
    props.put(ControllerConfiguration.REMOTE_COMMAND_CONNECTION_TIMEOUT, "foo");
    config.setConfigurationProperties(props);

    Assert.assertTrue(
        config.getRemoteCommandConnectionTimeoutMillis() ==
        ControllerConfiguration.DEFAULT_REMOTE_COMMAND_CONNECTION_TIMEOUT
    );
  }


  /**
   * Test setting connection timeout via overriding property list. The negative
   * value should be reset to zero equaling disabled timeout.
   */
  @Test public void testSetRemoteCommandConnectionTimeoutOverrideNegativeValue()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandConnectionTimeout("7");

    Map<String, String> props = new HashMap<String, String>(3);
    props.put(ControllerConfiguration.REMOTE_COMMAND_CONNECTION_TIMEOUT, "-1");
    config.setConfigurationProperties(props);

    Assert.assertTrue(
        "Got " + config.getRemoteCommandConnectionTimeoutMillis(),
        config.getRemoteCommandConnectionTimeoutMillis() == 0
    );
  }

  /**
   * Test setting connection timeout via overriding property list. If no matching
   * overriding value is provided, the original API provided value should remain.
   */
  @Test public void testSetRemoteCommandConnectionTimeoutOverrideEmptyValue()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandConnectionTimeout("1s");

    Map<String, String> props = new HashMap<String, String>(3);
    config.setConfigurationProperties(props);

    Assert.assertTrue(config.getRemoteCommandConnectionTimeoutMillis() == 1000);
  }

  /**
   * Test setting connection timeout via overriding property list. If no matching
   * overriding value is provided, the original API provided value should remain.
   */
  @Test public void testSetRemoteCommandConnectionTimeoutOverrideEmptyValue2()
  {
    ControllerConfiguration config = new ControllerConfiguration();

    Map<String, String> props = new HashMap<String, String>(3);
    config.setConfigurationProperties(props);

    Assert.assertTrue(
        config.getRemoteCommandConnectionTimeoutMillis() ==
        ControllerConfiguration.DEFAULT_REMOTE_COMMAND_CONNECTION_TIMEOUT
    );
  }

  /**
   * Test connection timeout default value.
   */
  @Test public void testSetRemoteCommandConnectionTimeoutDefaultValue()
  {
    ControllerConfiguration config = new ControllerConfiguration();

    Assert.assertTrue(
        config.getRemoteCommandConnectionTimeoutMillis() ==
        ControllerConfiguration.DEFAULT_REMOTE_COMMAND_CONNECTION_TIMEOUT
    );
  }


  // SetRemoteCommandResponseTimeout Tests ------------------------------------------------------------

  /**
   * Test setting response timeout values via configuration API.
   */
  @Test public void testSetRemoteCommandResponseTimeout()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandResponseTimeout("1");

    Assert.assertTrue(config.getRemoteCommandResponseTimeoutMillis() == 1000);

    config.setRemoteCommandResponseTimeout("-1");

    Assert.assertTrue(config.getRemoteCommandResponseTimeoutMillis() == 0);

    config.setRemoteCommandResponseTimeout("0");

    Assert.assertTrue(config.getRemoteCommandResponseTimeoutMillis() == 0);

    config.setRemoteCommandResponseTimeout("1s");

    Assert.assertTrue(config.getRemoteCommandResponseTimeoutMillis() == 1000);

    config.setRemoteCommandResponseTimeout("-1s");

    Assert.assertTrue(config.getRemoteCommandResponseTimeoutMillis() == 0);

    config.setRemoteCommandResponseTimeout("0s");

    Assert.assertTrue(config.getRemoteCommandResponseTimeoutMillis() == 0);

    config.setRemoteCommandResponseTimeout("1ms");

    Assert.assertTrue(config.getRemoteCommandResponseTimeoutMillis() == 1);

    config.setRemoteCommandResponseTimeout("0ms");

    Assert.assertTrue(config.getRemoteCommandResponseTimeoutMillis() == 0);

    config.setRemoteCommandResponseTimeout("-1ms");

    Assert.assertTrue(config.getRemoteCommandResponseTimeoutMillis() == 0);

    config.setRemoteCommandResponseTimeout("1m");

    Assert.assertTrue(config.getRemoteCommandResponseTimeoutMillis() == 1000 * 60);

    config.setRemoteCommandResponseTimeout("0m");

    Assert.assertTrue(config.getRemoteCommandResponseTimeoutMillis() == 0);

    config.setRemoteCommandResponseTimeout("-1m");

    Assert.assertTrue(config.getRemoteCommandResponseTimeoutMillis() == 0);
  }

  /**
   * Test setting response timeout values via configuration API. When a non-integer
   * value is provided, should fall back to a default value.
   */
  @Test public void testSetRemoteCommandResponseTimeoutError()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandResponseTimeout("foo");

    Assert.assertTrue(
        config.getRemoteCommandResponseTimeoutMillis() ==
        ControllerConfiguration.DEFAULT_REMOTE_COMMAND_RESPONSE_TIMEOUT
    );
  }

  /**
   * Test setting response timeout values via configuration API. Uses convenience
   * formatting for users to specify seconds, minutes or milliseconds explicitly.
   */
  @Test public void testSetRemoteCommandResponseTimeout2()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandResponseTimeout("5");

    Assert.assertTrue(config.getRemoteCommandResponseTimeoutMillis() == 5000);

    config.setRemoteCommandResponseTimeout("10s");

    Assert.assertTrue(config.getRemoteCommandResponseTimeoutMillis() == 10000);

    config.setRemoteCommandResponseTimeout("200ms");

    Assert.assertTrue(config.getRemoteCommandResponseTimeoutMillis() == 200);

    config.setRemoteCommandResponseTimeout("5m");

    Assert.assertTrue(config.getRemoteCommandResponseTimeoutMillis() == 1000 * 60 * 5);
  }


  /**
   * Test setting response timeout via overriding property list. The direct
   * API provided value should be overridden.
   */
  @Test public void testSetRemoteCommandResponseTimeoutOverride()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandResponseTimeout("5");

    Map<String, String> props = new HashMap<String, String>(3);
    props.put(ControllerConfiguration.REMOTE_COMMAND_RESPONSE_TIMEOUT, "1");
    config.setConfigurationProperties(props);

    Assert.assertTrue(
        "Got " + config.getRemoteCommandResponseTimeoutMillis(),
        config.getRemoteCommandResponseTimeoutMillis() == 1000
    );
  }

  /**
   * Test setting response timeout via overriding property list. The direct
   * API provided value should be overridden. Using convenience formatting
   * provided to users to specify minute values.
   */
  @Test public void testSetRemoteCommandResponseTimeoutOverrideMinuteValue()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandResponseTimeout("5");

    Map<String, String> props = new HashMap<String, String>(3);
    props.put(ControllerConfiguration.REMOTE_COMMAND_RESPONSE_TIMEOUT, "2 m");
    config.setConfigurationProperties(props);

    Assert.assertTrue(
        "Got " + config.getRemoteCommandResponseTimeoutMillis(),
        config.getRemoteCommandResponseTimeoutMillis() == 1000 * 60 * 2
    );
  }

  /**
   * Test setting response timeout via overriding property list. The direct
   * API provided value should be overridden. Using convenience formatting
   * provided to users to specify millisecond values.
   */
  @Test public void testSetRemoteCommandResponseTimeoutOverrideMillisecond()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandResponseTimeout("5");

    Map<String, String> props = new HashMap<String, String>(3);
    props.put(ControllerConfiguration.REMOTE_COMMAND_RESPONSE_TIMEOUT, "12 ms");
    config.setConfigurationProperties(props);

    Assert.assertTrue(
        "Got " + config.getRemoteCommandResponseTimeoutMillis(),
        config.getRemoteCommandResponseTimeoutMillis() == 12
    );
  }


  /**
   * Test setting response timeout via overriding property list. The invalid
   * value should result in falling back to API provided value.
   */
  @Test public void testSetRemoteCommandResponseTimeoutOverrideInvalidValue()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandResponseTimeout("5");

    Map<String, String> props = new HashMap<String, String>(3);
    props.put(ControllerConfiguration.REMOTE_COMMAND_RESPONSE_TIMEOUT, "foo");
    config.setConfigurationProperties(props);

    Assert.assertTrue(
        "Got " + config.getRemoteCommandResponseTimeoutMillis(),
        config.getRemoteCommandResponseTimeoutMillis() == 5000
    );
  }

  /**
   * Test setting response timeout via overriding property list. The invalid
   * value should result in falling back default value.
   */
  @Test public void testSetRemoteCommandResponseTimeoutOverrideInvalidValue2()
  {
    ControllerConfiguration config = new ControllerConfiguration();

    Map<String, String> props = new HashMap<String, String>(3);
    props.put(ControllerConfiguration.REMOTE_COMMAND_RESPONSE_TIMEOUT, "foo");
    config.setConfigurationProperties(props);

    Assert.assertTrue(
        config.getRemoteCommandResponseTimeoutMillis() ==
        ControllerConfiguration.DEFAULT_REMOTE_COMMAND_RESPONSE_TIMEOUT
    );
  }


  /**
   * Test setting response timeout via overriding property list. The negative
   * value should be reset to zero equaling disabled timeout.
   */
  @Test public void testSetRemoteCommandResponseTimeoutOverrideNegativeValue()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandResponseTimeout("2");

    Map<String, String> props = new HashMap<String, String>(3);
    props.put(ControllerConfiguration.REMOTE_COMMAND_RESPONSE_TIMEOUT, "-1");
    config.setConfigurationProperties(props);

    Assert.assertTrue(
        "Got " + config.getRemoteCommandResponseTimeoutMillis(),
        config.getRemoteCommandResponseTimeoutMillis() == 0
    );
  }

  /**
   * Test setting response timeout via overriding property list. If no matching
   * overriding value is provided, the original API provided value should remain.
   */
  @Test public void testSetRemoteCommandResponseTimeoutOverrideEmptyValue()
  {
    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandResponseTimeout("6 ms");

    Map<String, String> props = new HashMap<String, String>(3);
    config.setConfigurationProperties(props);

    Assert.assertTrue(config.getRemoteCommandResponseTimeoutMillis() == 6);
  }

  /**
   * Test setting response timeout via overriding property list. If no matching
   * overriding value is provided, the original API provided value should remain.
   */
  @Test public void testSetRemoteCommandResponseTimeoutOverrideEmptyValue2()
  {
    ControllerConfiguration config = new ControllerConfiguration();

    Map<String, String> props = new HashMap<String, String>(3);
    config.setConfigurationProperties(props);

    Assert.assertTrue(
        config.getRemoteCommandResponseTimeoutMillis() ==
        ControllerConfiguration.DEFAULT_REMOTE_COMMAND_RESPONSE_TIMEOUT
    );
  }

  /**
   * Test response timeout default value.
   */
  @Test public void testSetRemoteCommandResponseTimeoutDefaultValue()
  {
    ControllerConfiguration config = new ControllerConfiguration();

    Assert.assertTrue(
        config.getRemoteCommandResponseTimeoutMillis() ==
        ControllerConfiguration.DEFAULT_REMOTE_COMMAND_RESPONSE_TIMEOUT
    );
  }
  
  /**
   * Test setting security password storage using an Enum.
   */
  @Test
  public void testSetSecurityPasswordStorage()
  {
     ControllerConfiguration config = new ControllerConfiguration();
     
     config.setSecurityPasswordStorage(SecurityPasswordStorage.KEYSTORE);     
     Assert.assertEquals(SecurityPasswordStorage.KEYSTORE, config.getSecurityPasswordStorage());
     
     config.setSecurityPasswordStorage(SecurityPasswordStorage.PLAINTEXT);     
     Assert.assertEquals(SecurityPasswordStorage.PLAINTEXT, config.getSecurityPasswordStorage());
  }
  
  /**
   * Test setting security password storage using a String value.
   */
  @Test
  public void testSetSecurityPasswordStorageAsString()
  {
     ControllerConfiguration config = new ControllerConfiguration();
     
     config.setSecurityPasswordStorageAsString("keystore");     
     Assert.assertEquals(SecurityPasswordStorage.KEYSTORE, config.getSecurityPasswordStorage());
     
     config.setSecurityPasswordStorageAsString("plaintext");     
     Assert.assertEquals(SecurityPasswordStorage.PLAINTEXT, config.getSecurityPasswordStorage());
  }

  /**
   * Test setting security password storage using a String value with leading / trailing spaces.
   */
  @Test
  public void testSetSecurityPasswordStorageAsStringWithSpaces()
  {
     ControllerConfiguration config = new ControllerConfiguration();
     
     config.setSecurityPasswordStorageAsString("  keystore  ");     
     Assert.assertEquals(SecurityPasswordStorage.KEYSTORE, config.getSecurityPasswordStorage());
     
     config.setSecurityPasswordStorageAsString("  plaintext  ");     
     Assert.assertEquals(SecurityPasswordStorage.PLAINTEXT, config.getSecurityPasswordStorage());
  }

  /**
   * Test setting security password storage via configuration API. When a non-valid (as per Enum definition)
   * value is provided, should fall back to a default value.
   */
  @Test
  public void testSetSecurityPasswordStorageAsStringInvalidValue()
  {
     ControllerConfiguration config = new ControllerConfiguration();
     
     config.setSecurityPasswordStorageAsString("fjhsdhjkfsd");
     Assert.assertEquals(SecurityPasswordStorage.KEYSTORE, config.getSecurityPasswordStorage());
  }
  
  /**
   * Test setting security password storage via overriding property list.
   * The direct API provided value should be overridden.
   */
  @Test
  public void testSetSecurityPasswordStorageOverride()
  {
    ControllerConfiguration config = new ControllerConfiguration();

    Map<String, String> props = new HashMap<String, String>(3);
    props.put("security.passwordStorage", "plaintext");
    config.setConfigurationProperties(props);

    Assert.assertEquals(SecurityPasswordStorage.PLAINTEXT, config.getSecurityPasswordStorage());
  }

  /**
   * Test setting security password storage via overriding property list. If an invalid
   * overriding value is provided, the original API provided value should remain.
   */
  @Test
  public void testSetSecurityPasswordStorageOverrideInvalidValue()
  {
    ControllerConfiguration config = new ControllerConfiguration();

    Map<String, String> props = new HashMap<String, String>(3);
    props.put("security.passwordStorage", "gfhjd");
    config.setConfigurationProperties(props);

    Assert.assertEquals(SecurityPasswordStorage.KEYSTORE, config.getSecurityPasswordStorage());
  }

  /**
   * Test setting security password storage via overriding property list. If no matching
   * overriding value is provided, the original API provided value should remain.
   */
  @Test
  public void testSetSecurityPasswordStorageOverrideEmptyValue()
  {
    ControllerConfiguration config = new ControllerConfiguration();

    Map<String, String> props = new HashMap<String, String>(3);
    config.setConfigurationProperties(props);

    Assert.assertEquals(SecurityPasswordStorage.KEYSTORE, config.getSecurityPasswordStorage());
  }

  /**
   * Test security password storage default value.
   */
  @Test
  public void testSetSecurityPasswordStorageDefaultValue()
  {
    ControllerConfiguration config = new ControllerConfiguration();

    Assert.assertEquals(SecurityPasswordStorage.KEYSTORE, config.getSecurityPasswordStorage());
  }  
}

