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

import java.net.URI;
import java.net.URISyntaxException;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

import org.openremote.controller.exception.ConfigurationException;
import org.openremote.controller.service.ServiceContext;
import org.openremote.controller.utils.Logger;
import org.openremote.controller.utils.Strings;


/**
 * TODO:
 *     - ORCJAVA-183 (http://jira.openremote.org/browse/ORCJAVA-183)
 *     - ORCJAVA-170 (http://jira.openremote.org/browse/ORCJAVA-170)
 *
 *
 * This class provides the Java bindings from config.properties file found in
 * <tt>WEB-INF/classes</tt> directory of the web archive.
 *
 * The actual value injection is currently configured via Spring frameworks'
 * <tt>applicationContext.xml</tt> configuration file also found in the web archive.
 * Each additional configuration property must be added to the XML definition
 * as well as a Java accessor method implementation in this class.
 *
 * @author Dan Cong
 * @author <a href="mailto:juha@openremote.org>Juha Lindfors</a>
 * @author Jerome Velociter
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class ControllerConfiguration extends Configuration
{

  // Constants ------------------------------------------------------------------------------------

  public final static String DEFAULT_LINE_SEPARATOR = "\n";

  public final static String LINE_SEPARATOR = getLineSeparator();


  /**
   * Configuration property name which indicates whether uploading a new controller
   * definition from a controller local admin interface is allowed.
   */
  public static final String RESOURCE_UPLOAD_ALLOWED = "resource.upload.allowed";

  /**
   * Configuration property name which is used to locate controller's artifact location.
   */
  public static final String RESOURCE_PATH = "resource.path";

  /**
   * Configuration property name used for locating controller backend services.
   *
   * TODO : See ORCJAVA-191 (http://jira.openremote.org/browse/ORCJAVA-191)
   */
  public static final String BEEHIVE_REST_ROOT_URL = "beehive.REST.Root.Url";

  /**
   * Configuration property name used for listing the remote command service URIs: {@value}
   */
  public static final String REMOTE_COMMAND_SERVICE_URI = "remote.command.service.uri";

  /**
   * Configuration property name used for connecting to the remote websocket command service URIs: {@value}
   */
  public static final String REMOTE_COMMAND_SERVICE_WS_URI = "remote.command.service.ws.uri";

  /**
   * Configuration property name used for setting the request interval to check incoming
   * remote commands to the controller.
   */
  public final static String REMOTE_COMMAND_REQUEST_INTERVAL = "remote.command.request.interval";

  /**
   * Configuration property name used for setting the connection timeout value for remote command
   * service : {@value}
   */
  public final static String REMOTE_COMMAND_CONNECTION_TIMEOUT = "remote.command.connection.timeout";

  /**
   * Configuration property name used for setting the response response timeout for remote command
   * service : {@value}
   */
  public final static String REMOTE_COMMAND_RESPONSE_TIMEOUT = "remote.command.response.timeout";


  public static final String BEEHIVE_ACCOUNT_SERVICE_REST_ROOT_URL = "beehiveAccountService.REST.Root.Url";
  public static final String BEEHIVE_DEVICE_DISCOVERY_SERVICE_REST_ROOT_URL = "beehiveDeviceDiscoveryService.REST.Root.Url";
  public static final String BEEHIVE_SYNCING = "controller.performBeehiveSyncing";

  public static final String IRSEND_PATH = "irsend.path";
  public static final String MULTICAST_PORT = "multicast.port";
  public static final String MULTICAST_ADDRESS = "multicast.address";
  public static final String WEBAPP_PORT = "webapp.port";
  public static final String COPY_LIRCD_CONF_ON = "copy.lircd.conf.on";
  public static final String LIRCD_CONF_PATH = "lircd.conf.path";
  public static final String WEBAPP_IP = "webapp.ip";
  public static final String PROXY_TIMEOUT = "proxy.timeout";

  public static final String LAGARTO_BROADCAST_ADDRESS = "lagarto_network.broadcast";


  public static final String CONTROLLER_APPLICATIONNAME = "controller.applicationname";
  
  public static final String SECURITY_PASSWORD_STORAGE = "security.passwordStorage";


  /**
   * Default value to wait between requests when checking for available remote commands
   * for this controller. Milliseconds : {@value}
   */
  public final static int DEFAULT_REMOTE_COMMAND_REQUEST_INTERVAL = 30000;

  /**
   * Default value to establish remote command service connection before timing out.
   * Milliseconds : {@value}
   */
  public final static int DEFAULT_REMOTE_COMMAND_CONNECTION_TIMEOUT = 10000;

  /**
   * Default value to wait for remote command service response before timing out.
   * Milliseconds : {@value}
   */
  public final static int DEFAULT_REMOTE_COMMAND_RESPONSE_TIMEOUT = 15000;

  public final static int DEFAULT_WS_TIMEOUT = 30000;
  public final static int DEFAULT_WS_RECONNECT_DELAY = 30000;


  // Class Members --------------------------------------------------------------------------------

  private static String getLineSeparator()
  {
    return AccessController.doPrivileged(new PrivilegedAction<String>()
    {
      public String run()
      {
        try
        {
          return System.getProperty("line.separator", DEFAULT_LINE_SEPARATOR);
        }

        catch (SecurityException e)
        {
          String msg = "Falling back to default line separator. Cannot access system " +
                       "line separator property due to security restrictions: " + e.getMessage();

          Logger.getLogger(Constants.RUNTIME_CONFIGURATION_LOG_CATEGORY).log(Level.SEVERE, msg, e);

          return DEFAULT_LINE_SEPARATOR;
        }
      }
    });
  }

  public static ControllerConfiguration readXML()
  {
    ControllerConfiguration config = ServiceContext.getControllerConfiguration();

    return (ControllerConfiguration)Configuration.updateWithControllerXMLConfiguration(config);
  }
  


  
  // Private Instance Variables -------------------------------------------------------------------
  

  private int webappPort;
  private String multicastAddress;
  private int multicastPort;
  private String resourcePath;
  private long macroIRExecutionDelay = 500;
  private String webappIp;
  private String beehiveRESTRootUrl;
  private String beehiveAccountServiceRESTRootUrl;
  private String beehiveDeviceDiscoveryServiceRESTRootUrl;
  private boolean beehiveSyncing;
  private String webappName;
  private String irsendPath;
  private String lircdconfPath;
  private int proxyTimeout;
  private String lagartoBroadcastAddr;
  private SecurityPasswordStorage securityPasswordStorage;
  private int reconnectionDelay;
  private int WSTimeout;
  
  /**
   * The set of URIs used to retrieve remote commands. Note that this can contain a comma
   * separated list of URIs. Therefore for programmatic access, the method
   * {@link #getRemoteCommandURIs()} should be used to retrieve the parsed array of
   * actual URI instance that can be used.
   */
  private String remoteCommandServiceURI = "<undefined>";

  /**
   * The set of URIs used to connect to websocket remote commands.
   */
  private String remoteCommandServiceWsURI = "<undefined>";

  /**
   * The request interval used to check if remote commands are available for this controller.
   * In milliseconds: {@link #DEFAULT_REMOTE_COMMAND_REQUEST_INTERVAL}
   */
  private int remoteCommandRequestInterval;

  /**
   * The connection timeout value used to wait for the remote command service to establish
   * a connection with controller, until timed out. In milliseconds :
   * {@link #DEFAULT_REMOTE_COMMAND_CONNECTION_TIMEOUT}
   */
  private int remoteCommandConnectionTimeout = DEFAULT_REMOTE_COMMAND_CONNECTION_TIMEOUT;

  /**
   * The connection timeout value used for waiting a response from remote command service
   * before timing out. In milliseconds : {@link #DEFAULT_REMOTE_COMMAND_RESPONSE_TIMEOUT}
   */
  private int remoteCommandResponseTimeout = DEFAULT_REMOTE_COMMAND_RESPONSE_TIMEOUT;

  /** Whether copy lircd.conf for user. */
  private boolean copyLircdconf;

  /** The resource upload switch. */
  private boolean allowResourceUpload;

  /**
   * The COM (Serial) port the ORC should use (for example, to send X10 events)
   */
  private String comPort;

  /**
   * The transmitter to use for X10
   */
  private String x10transmitter;


  // Public Methods -------------------------------------------------------------------------------

  /**
   * Returns a string containing an operating system specific filesystem path to
   * a executable LIRC 'irsend' command.
   *
   * @see #setIrsendPath(String)
   *
   * @return operating system specific filesystem path
   */
  public String getIrsendPath() {
    return preferAttrCustomValue(IRSEND_PATH, irsendPath);
  }

  /**
   * An operating system specific filesystem path as a string to a LIRC 'irsend'
   * executable command.
   *
   * @see #getIrsendPath()
   *
   * @param irsendPath operating system specific filesystem path
   */
  public void setIrsendPath(String irsendPath) {
    // TODO :
    //  could attempt to convert the string to a valid URI to support
    //  a system neutral path format ?
    //
    // TODO :
    //  should be explicit about (how-to) the use of relative paths

    this.irsendPath = irsendPath.trim();
  }

  /**
   * Returns a string containing an operating system specific filesystem path to
   * LIRC daemon configuration file.
   *
   * @see #setLircdconfPath(String)
   *
   * @return operating system specific filesystem path
   */
  public String getLircdconfPath() {
    return preferAttrCustomValue(LIRCD_CONF_PATH, lircdconfPath);
  }

  /**
   * An operating system specific filesystem path as a string to a LIRC 'lircd.conf' file
   * (containing all the infrared remote codes).
   *
   * @see #getLircdconfPath()
   *
   * @param lircdconfPath operating system specific path
   */
  public void setLircdconfPath(String lircdconfPath) {
    // TODO :
    //  could attempt to convert the string to a valid URI to support
    //  a system neutral path format ?
    //
    // TODO :
    //  should be explicit about (how-to) the use of relative paths

    this.lircdconfPath = lircdconfPath.trim();
  }

  /**
   * Indicates whether an existing 'lircd.conf' file is overriden on the system
   * when a new controller configuration is deployed through the web interface.
   *
   * @see #setCopyLircdconf(boolean)
   *
   * @return true if copy-over on configuration deployment is enabled; false otherwise
   */
  public boolean isCopyLircdconf() {
    return preferAttrCustomValue(COPY_LIRCD_CONF_ON, copyLircdconf);
  }

  /**
   * Determines whether a 'lircd.conf' file in a deployed controller configuration should
   * be copied over an existing LIRC configuration file found in the path returned by
   * {@link #getLircdconfPath()} method.  <p>
   *
   * <b>Important Note:</b> This is potentially disruptive to the user's system if an existing,
   * unrelated LIRC daemon configuration was already in use.
   *
   * @see #isCopyLircdconf()
   *
   * @param copyLircdconf  If true, enables copy-over behavior. If false, requires manual
   *                       infrared configuration in 'lircd.conf' file.
   */
  public void setCopyLircdconf(boolean copyLircdconf) {
    this.copyLircdconf = copyLircdconf;
  }

  /**
   * Returns the port number used by controller discovery service.
   *
   * @see #setWebappPort(int)
   *
   * @return port number
   */
  public int getWebappPort() {
    return preferAttrCustomValue(WEBAPP_PORT, webappPort);
  }

  /**
   * The port number the controller returns on a panel discovery request. It should
   * match the port number configured on the hosting web container or otherwise
   * things just don't work right.  <p>
   *
   * TODO:
   *   This is a duplicate configuration and should be consolidated with a proper service
   *   container around the web container. It should only be necessary to define the
   *   port number once.
   *
   * @see #getWebappPort()
   *
   * @param webappPort  the HTTP listening port of the web container (e.g. Tomcat 8080)
   */
  public void setWebappPort(int webappPort) {
    this.webappPort = webappPort;
  }

  /**
   * Returns the configured controller discovery multicast address
   *
   * @see #setMulticastAddress(String)
   *
   * @return IP multicast address as a string
   */
  public String getMulticastAddress() {
    return preferAttrCustomValue(MULTICAST_ADDRESS, multicastAddress);
  }

  /**
   * Sets the multicast address used for controller discovery by panels.
   *
   * @see #getMulticastAddress()
   *
   * @param multicastAddress IP multicast address as a string
   */
  public void setMulticastAddress(String multicastAddress) {

    // TODO : actually validate a correctly configured IP address

    this.multicastAddress = multicastAddress.trim();
  }

  /**
   * Returns the configured multicast port used for controller discovery.
   *
   * @return  multicast port
   */
  public int getMulticastPort() {
    return preferAttrCustomValue(MULTICAST_PORT, multicastPort);
  }

  /**
   * Sets the multicast port used for controller discovery.
   *
   * @see #getMulticastPort()
   *
   * @param multicastPort port number
   */
  public void setMulticastPort(int multicastPort) {
    this.multicastPort = multicastPort;
  }

  /**
   * Returns an operating system specific string containing a filesystem path
   * to a directory with the resource files of a controller
   *
   * @see #setResourcePath(String)
   *
   * @return operating system specific filesystem path as a string
   */
  public String getResourcePath() {
//      return preferAttrCustomValue(RESOURCE_PATH, resourcePath);

    // TODO : convert to absolute path or URI

     return resourcePath;
  }

  /**
   * The resource path points to a directory where the controller looks for all
   * configuration files, images and other resources for panels and controllers.
   *
   * @see #getResourcePath()
   *
   * @param resourcePath  system specific filesystem path as a string to a directory containing
   *                      the resource files of a controller
   */
  public void setResourcePath(String resourcePath) {

    // TODO :
    //  could attempt to convert the string to a valid URI to support
    //  a system neutral path format ?
    //
    // TODO :
    //  should be explicit about (how-to) the use of relative paths

    this.resourcePath = resourcePath.trim();
  }

  /**
   * Indicates if controller allows new deployments through the web admin interface.
   *
   * @see #setResourceUploadAllowed
   *
   * @return true if controller configuration can be uploaded through
   *         web interface; false otherwise
   */
  public boolean isResourceUploadAllowed()
  {
    return preferAttrCustomValue(RESOURCE_UPLOAD_ALLOWED, allowResourceUpload);
  }

  /**
   * Enables the ability to upload new controller definitions
   * directly from the web admin interface.  <p>
   *
   * This is a convenience feature for home users for easy deployment of
   * the controller configuration in cases where all users of the network
   * are trusted, or the controller is still being configured and tested. <p>
   *
   * As a security consideration, when deploying the controller on a public
   * network, this setting should always be disabled. <p>
   *
   * When disabled, configuration must be handled manually by copying all
   * the controller configuration files manually to a directory pointed by
   * {@link #setResourcePath} property. Access to this directory in the
   * filesystem should be properly secured with access restrictions. <p>
   *
   * @param resourceUpload  true to enable controller configuration upload via
   *                        web interface; false otherwise
   *
   * @see #setResourcePath
   * @see #isResourceUploadAllowed
   */
  public void setResourceUploadAllowed(boolean resourceUpload)
  {
    this.allowResourceUpload = resourceUpload;
  }


  /**
   * Returns the name of the serial port configured for X10 serial PLM unit
   *
   * @see #setComPort(String)
   *
   * @return the name of the COM (serial) port or device
   */
  public String getComPort() {
    return comPort;
  }

  /**
   * Serial port or device to use with X10 serial power-line modules
   * (e.g. CM11A or CM17A "FireCracker" modules).
   *
   * Serial port configuration is specific to the operating system in use.
   * On Linux '/dev/ttyS0' or similar device name should be used. For
   * Microsoft Windows systems use port names such as COM1, COM2, etc.
   *
   * @see #getComPort()
   *
   * @param comPort the name of the COM (serial) port or device
   */
  public void setComPort(String comPort) {
    this.comPort = comPort.trim();
  }

  /**
   * TODO
   *
   * @see #setX10transmitter(String)
   *
   * @return
   */
  public String getX10transmitter() {
    return x10transmitter;
  }

  /**
   * TODO
   *
   * @see #getX10transmitter()
   *
   * @param x10transmitter
   */
  public void setX10transmitter(String x10transmitter) {
    this.x10transmitter = x10transmitter.trim();
  }

   public long getMacroIRExecutionDelay() {
      return preferAttrCustomValue("Macro.IR.Execution.Delay", macroIRExecutionDelay);
   }

   public void setMacroIRExecutionDelay(long macroIRExecutionDelay) {
      this.macroIRExecutionDelay = macroIRExecutionDelay;
   }

   public String getWebappIp()
   {
     return preferAttrCustomValue(WEBAPP_IP, webappIp);
   }

   public void setWebappIp(String webappIp)
   {
     this.webappIp = webappIp.trim();
   }

   public String getBeehiveRESTRootUrl()
   {
     // TODO : see ORCJAVA-191 (http://jira.openremote.org/browse/ORCJAVA-191)
     return preferAttrCustomValue(BEEHIVE_REST_ROOT_URL, beehiveRESTRootUrl);
   }

   public void setBeehiveRESTRootUrl(String beehiveRESTRootUrl)
   {
     this.beehiveRESTRootUrl = beehiveRESTRootUrl.trim();
   }
   
   public String getBeehiveAccountServiceRESTRootUrl()
   {
     return preferAttrCustomValue(BEEHIVE_ACCOUNT_SERVICE_REST_ROOT_URL, beehiveAccountServiceRESTRootUrl);
   }

   public void setBeehiveAccountServiceRESTRootUrl(String beehiveAccountServiceRESTRootUrl)
   {
     this.beehiveAccountServiceRESTRootUrl = beehiveAccountServiceRESTRootUrl.trim();
   }
   
   public String getBeehiveDeviceDiscoveryServiceRESTRootUrl()
   {
     return preferAttrCustomValue(BEEHIVE_DEVICE_DISCOVERY_SERVICE_REST_ROOT_URL, beehiveDeviceDiscoveryServiceRESTRootUrl);
   }

   public void setBeehiveDeviceDiscoveryServiceRESTRootUrl(String beehiveDeviceDiscoveryServiceRESTRootUrl)
   {
     this.beehiveDeviceDiscoveryServiceRESTRootUrl = beehiveDeviceDiscoveryServiceRESTRootUrl.trim();
   }




   public String getWebappName()
   {
     return preferAttrCustomValue(CONTROLLER_APPLICATIONNAME, webappName);
   }
   
   public void setWebappName(String webappName)
   {
     this.webappName = webappName;
   }

   public int getProxyTimeout() {
      return preferAttrCustomValue(PROXY_TIMEOUT, proxyTimeout);
   }
   
   public void setProxyTimeout(int proxyTimeout) {
      this.proxyTimeout = proxyTimeout;
   }

  /**
   * Returns the broadcast address used to publish network events (ZeroMQ)
   * from Lagarto servers
   *
   * @see #setLagartoBroadcastAddr(String)
   *
   * @return IP broadcast address as a string
   */
  public String getLagartoBroadcastAddr()
  {
    return preferAttrCustomValue(LAGARTO_BROADCAST_ADDRESS, lagartoBroadcastAddr);
  }

  /**
   * Sets the broadcast address used to receive network events (ZeroMQ) from
   * Lagarto servers
   *
   * @see #getLagartoBroadcastAddr()
   */
  public void setLagartoBroadcastAddr(String broadcastAddress)
  {
    this.lagartoBroadcastAddr = broadcastAddress.trim();
  }


  // Remote Command Service Configuration ---------------------------------------------------------


  /**
   * Returns the configured value of remote command service URIs. Note that this is the original
   * configured value of string based URIs, as a comma separated list. For programatic API
   * usage you will want to use {@link #getRemoteCommandURIs()} instead.
   *
   * @return  configuration string of a comma separated list of remote command service URIs
   */
  public String getRemoteCommandServiceURI()
  {
    // NOTE: this mainly exist to satisfy Spring requirements for configuration, otherwise
    //       it is not really useful, use URL[] getRemoteCommandURIs() instead

    return preferAttrCustomValue(
       REMOTE_COMMAND_SERVICE_URI,
       remoteCommandServiceURI
    ).trim();
  }

  public String getRemoteCommandServiceWsURI()
  {
    // NOTE: this mainly exist to satisfy Spring requirements for configuration, otherwise
    //       it is not really useful, use URL[] getRemoteCommandURIs() instead

    return preferAttrCustomValue(
          REMOTE_COMMAND_SERVICE_WS_URI,
          remoteCommandServiceWsURI
    ).trim();
  }

  /**
   * Returns a list of configured URIs that are used to connect to a remote command service.
   * Note that the list will not contain any values returned by {@link #getRemoteCommandServiceURI()}
   * that cannot be parsed into a proper URI syntax.
   *
   * @return  an array of remote command service URIs
   */
  public URI[] getRemoteCommandURIs()
  {
    String value = preferAttrCustomValue(
       REMOTE_COMMAND_SERVICE_URI,
       remoteCommandServiceURI
    ).trim();

    String[] uriStrings = value.split(",");
    Set<URI> uris = new HashSet<URI>(1);

    for (String uriString : uriStrings)
    {
      try
      {
        uris.add(new URI(uriString.trim()));
      }

      catch (URISyntaxException e)
      {
        log.warn(
            "Unable to parse ''{0}'' in property ''{1}'' to a valid URI. Value ignored.",
            uriString, REMOTE_COMMAND_SERVICE_URI
        );
      }
    }

    return uris.toArray(new URI[uris.size()]);
  }

  /**
   * Sets remote command service URIs. The string must parse to a proper URI syntax. Multiple
   * URIs may be added with as a comma-separated list.
   *
   * @param remoteCommandServiceURI   remote command service URI(s) as a string
   */
  public void setRemoteCommandServiceURI(String remoteCommandServiceURI)
  {
    this.remoteCommandServiceURI = remoteCommandServiceURI.trim();
  }


  public void setRemoteCommandServiceWsURI(String remoteCommandServiceWsURI)
  {
    this.remoteCommandServiceWsURI = remoteCommandServiceWsURI.trim();
  }


  /**
   * Returns the configured remote command connection timeout value in milliseconds.
   *
   * @return  remote command service client connection timeout value in milliseconds
   */
  public int getRemoteCommandConnectionTimeoutMillis()
  {
    return preferAttrCustomValue(REMOTE_COMMAND_CONNECTION_TIMEOUT, remoteCommandConnectionTimeout);
  }

  /**
   * This method returns the configured string value (uninterpreted, including potential suffices
   * for minutes, seconds or milliseconds) for remote command service response timeout. <p>
   *
   * It is included to satisfy Spring framework configuration requirements but it is not used
   * otherwise. See {@link #getRemoteCommandResponseTimeoutMillis()} instead for the actual
   * timeout value in milliseconds.
   *
   * @return    configuration string for remote command service response timeout, uninterpreted
   *            including user convenience suffices for minutes, seconds or milliseconds
   */
  public String getRemoteCommandConnectionTimeout()
  {
    return preferAttrCustomValue(REMOTE_COMMAND_CONNECTION_TIMEOUT, "" + remoteCommandConnectionTimeout);
  }

  /**
   * Sets the remote command connection timeout value. The string value must be parseable to
   * an integer value and is interpreted as seconds. For smaller millisecond values, postfix the
   * integer string with 'ms', e.g. '100ms'. For longer minute values postfix the integer
   * string with 'm', e.g. '1m' for one minute (60 seconds, 60,000 milliseconds). An explicit
   * 's' suffix is also accepted for seconds but is not required; any number value without
   * a suffix is interpreted as a second value.
   *
   * @param timeout   number string such as '10' for ten seconds or a number string with
   *                  time-unit qualifier such as '2m' for two minutes, '1500ms' for 1,500
   *                  milliseconds or '10s' for ten seconds
   */
  public void setRemoteCommandConnectionTimeout(String timeout)
  {
    try
    {
      remoteCommandConnectionTimeout = timeStringToMillis(timeout);
    }

    catch (InvalidTimeException e)
    {
      log.info(
          "Remote command connection timeout was configured to an invalid value {0}. " +
          "Will wait for connection indefinitely.", timeout
      );

      remoteCommandConnectionTimeout = 0;
    }

    catch (ConfigurationException e)
    {
      log.warn(
          "Unable to parse remote command connection timeout value ''{0}'', " +
          "using default {1} milliseconds...",
          timeout, DEFAULT_REMOTE_COMMAND_CONNECTION_TIMEOUT);
    }
  }


  /**
   * Returns the configured remote command response timeout value in milliseconds.
   *
   * @return  remote command service client response timeout value in milliseconds
   */
  public int getRemoteCommandResponseTimeoutMillis()
  {
    return preferAttrCustomValue(REMOTE_COMMAND_RESPONSE_TIMEOUT, remoteCommandResponseTimeout);
  }

  /**
   * This method returns the configured string value (uninterpreted, including potential suffices
   * for minutes, seconds or milliseconds) for remote command service response timeout. <p>
   *
   * It is included to satisfy Spring framework configuration requirements but it is not used
   * otherwise. See {@link #getRemoteCommandResponseTimeoutMillis()} instead for the actual
   * timeout value in milliseconds.
   *
   * @return    configuration string for remote command service response timeout, uninterpreted
   *            including user convenience suffices for minutes, seconds or milliseconds
   */
  public String getRemoteCommandResponseTimeout()
  {
    return preferAttrCustomValue(
        REMOTE_COMMAND_RESPONSE_TIMEOUT,
        Integer.toString(remoteCommandResponseTimeout)
    );
  }


  /**
   * Sets the remote command response timeout value. The string value must be parseable to
   * an integer value and is interpreted as seconds. For smaller millisecond values, postfix the
   * integer string with 'ms', e.g. '100ms'. For longer minute values postfix the integer
   * string with 'm', e.g. '1m' for one minute (60 seconds, 60,000 milliseconds). An explicit
   * 's' suffix is also accepted for seconds but is not required; any number value without
   * a suffix is interpreted as a second value.
   *
   * @param timeout   number string such as '10' for ten seconds or a number string with
   *                  time-unit qualifier such as '2m' for two minutes, '1500ms' for
   *                  1,500 milliseconds or '10s' for ten seconds
   */
  public void setRemoteCommandResponseTimeout(String timeout)
  {
    try
    {
      remoteCommandResponseTimeout = timeStringToMillis(timeout);
    }

    catch (InvalidTimeException e)
    {
      log.info(
          "Remote command response timeout was set to {0}, will wait for response indefinitely.",
          timeout
      );

      remoteCommandResponseTimeout = 0;
    }

    catch (ConfigurationException e)
    {
      log.warn(
          "Unable to parse remote command response timeout value ''{0}'', " +
          "using default {1} milliseconds...",
          timeout, DEFAULT_REMOTE_COMMAND_RESPONSE_TIMEOUT);
    }
  }

  /**
   * Returns the configured remote command request interval value in milliseconds.
   *
   * @return  remote command request interval value in milliseconds
   */
  public int getRemoteCommandRequestIntervalMillis()
  {
    return preferAttrCustomValue(REMOTE_COMMAND_REQUEST_INTERVAL, remoteCommandRequestInterval);
  }

  /**
   * This method returns the configured string value (uninterpreted, including potential suffices
   * for minutes, seconds or milliseconds) for remote command request interval. <p>
   *
   * It is included to satisfy Spring framework configuration requirements but it is not used
   * otherwise. See {@link #getRemoteCommandRequestIntervalMillis()} instead for the actual
   * timeout value in milliseconds.
   *
   * @return    configuration string for remote command request interval, uninterpreted
   *            including user convenience suffices for minutes, seconds or milliseconds
   */
  public String getRemoteCommandRequestInterval()
  {
    return preferAttrCustomValue(
        REMOTE_COMMAND_REQUEST_INTERVAL,
        Integer.toString(remoteCommandRequestInterval)
    );
  }

  /**
   * Sets the remote command request interval value. The string value must be parseable to
   * an integer value and is interpreted as seconds. For smaller millisecond values, postfix the
   * integer string with 'ms', e.g. '100ms'. For longer minute values postfix the integer
   * string with 'm', e.g. '1m' for one minute (60 seconds, 60,000 milliseconds). An explicit
   * 's' suffix is also accepted for seconds but is not required; any number value without
   * a suffix is interpreted as a second value.
   *
   * @param interval  number string such as '10' for ten seconds or a number string with
   *                  time-unit qualifier such as '2m' for two minutes, '1500ms' for
   *                  1,500 milliseconds or '10s' for ten seconds
   */
  public void setRemoteCommandRequestInterval(String interval)
  {
    try
    {
      this.remoteCommandRequestInterval = timeStringToMillis(interval);
    }

    catch (Exception e)
    {
      log.info(
          "Remote command request interval was set to {0}, using default value {1} instead. ",
          interval, DEFAULT_REMOTE_COMMAND_REQUEST_INTERVAL
      );

      remoteCommandRequestInterval = DEFAULT_REMOTE_COMMAND_REQUEST_INTERVAL;
    }
  }

  public boolean getBeehiveSyncing()
  {
    return preferAttrCustomValue(BEEHIVE_SYNCING, beehiveSyncing);
  }

  public void setBeehiveSyncing(boolean beehiveSyncing)
  {
    this.beehiveSyncing = beehiveSyncing;
  }
  
  public void setSecurityPasswordStorageAsString(String securityPasswordStorageAsString)
  {
     try {
        this.securityPasswordStorage = SecurityPasswordStorage.valueOf(
              (securityPasswordStorageAsString != null)?Strings.toUpperCase(securityPasswordStorageAsString.trim()):"");
      } catch (IllegalArgumentException e) {
         log.info("Security password storage was set to invalid value ''{0}'', using {1} instead.",
               securityPasswordStorageAsString, SecurityPasswordStorage.KEYSTORE.name());
         this.securityPasswordStorage = SecurityPasswordStorage.KEYSTORE;
      }
  }
  
  public void setSecurityPasswordStorage(SecurityPasswordStorage securityPasswordStorage)
  {
     this.securityPasswordStorage = securityPasswordStorage;
  }
  
  public SecurityPasswordStorage getSecurityPasswordStorage()
  {
    String defaultSecurityPasswordStorage = SecurityPasswordStorage.KEYSTORE.name();
    if (this.securityPasswordStorage != null) {
       defaultSecurityPasswordStorage = this.securityPasswordStorage.name();
    }
    String securityPasswordStorageAsString = preferAttrCustomValue(SECURITY_PASSWORD_STORAGE, defaultSecurityPasswordStorage);
    try {
      return SecurityPasswordStorage.valueOf(
            (securityPasswordStorageAsString != null)?Strings.toUpperCase(securityPasswordStorageAsString.trim()):"");

    } catch (IllegalArgumentException e) {
       log.info("Security password storage was set to invalid value ''{0}'', using {1} instead.",
             securityPasswordStorageAsString, SecurityPasswordStorage.KEYSTORE.name());
       return SecurityPasswordStorage.KEYSTORE;
    }
  }

  public void setWSTimeoutAsString(String timeout)
  {
    try
    {
      this.WSTimeout= timeStringToMillis(timeout);
    }
    catch (Exception e)
    {
      log.warn(
              "Unable to parse websocket timeout value ''{0}'', " +
                      "using default {1} milliseconds...",
              timeout, DEFAULT_WS_TIMEOUT);
      this.WSTimeout = DEFAULT_WS_TIMEOUT;
    }
  }

    public int getWSTimeout() {
        return WSTimeout;
    }

  public void setReconnectionDelayAsString(String reconnectionDelay)
  {
    try
    {
      this.reconnectionDelay = timeStringToMillis(reconnectionDelay);
    }
    catch (Exception e)
    {
      log.warn(
              "Unable to parse websocket reconnection delay value ''{0}'', " +
                      "using default {1} milliseconds...",
              reconnectionDelay, DEFAULT_WS_RECONNECT_DELAY);
      this.reconnectionDelay = DEFAULT_WS_RECONNECT_DELAY;
    }
  }

  public int getReconnectionDelay() {
    return this.reconnectionDelay;
  }
}
