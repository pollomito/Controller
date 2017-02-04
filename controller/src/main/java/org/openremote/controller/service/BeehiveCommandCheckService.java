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
package org.openremote.controller.service;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InterfaceAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.ProtocolException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.openremote.controller.Constants;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.exception.ConfigurationException;
import org.openremote.controller.exception.ConnectionException;
import org.openremote.controller.exception.OpenRemoteException;
import org.openremote.controller.proxy.ControllerProxy;
import org.openremote.controller.service.Deployer.PasswordException;
import org.openremote.controller.utils.HttpUtils;
import org.openremote.controller.utils.Logger;
import org.openremote.controllercommand.domain.ControllerCommandDTO;
import org.openremote.rest.GenericResourceResultWithErrorMessage;
import org.openremote.useraccount.domain.ControllerDTO;
import org.restlet.data.ChallengeScheme;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import flexjson.JSONDeserializer;

import javax.net.ssl.HttpsURLConnection;

/**
 * TODO
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 * @author Marcus Redeker
 */
public class BeehiveCommandCheckService
{

  // TODO : Remove this enclosing class, it has no purpose, all the functionality is in the nested class

  // TODO :
  //        look into making http.keepAlive and http.maxConnections  properties externally
  //        configurable, possibly also provider specific configurations such as :
  //          - sun.net.http.errorstream.enableBuffering
  //          - sun.net.http.errorstream.timeout
  //          - sun.net.http.errorstream.bufferSize



  // Constants ------------------------------------------------------------------------------------

  /**
   * Name for this thread: {@value}
   */
  public final static String REMOTE_COMMAND_THREAD_NAME = "Remote Command Thread";


  // Class Members --------------------------------------------------------------------------------

  /**
   * Log for this service.
   */
  private final static Logger log = Logger.getLogger(Constants.BEEHIVE_COMMAND_CHECKER_LOG_CATEGORY);


  public static String getMACAddresses() throws Exception
  {
    StringBuilder macs = new StringBuilder();
    Enumeration<NetworkInterface> enum1 = NetworkInterface.getNetworkInterfaces();

    while (enum1.hasMoreElements())
    {
      NetworkInterface networkInterface = enum1.nextElement();

      if (!networkInterface.isLoopback())
      {
        boolean onlyLinkLocal = true;

        for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses())
        {
          if (!interfaceAddress.getAddress().isLinkLocalAddress())
          {
            onlyLinkLocal = false;
          }
        }

        if (onlyLinkLocal)
        {
          continue;
        }

        byte[] mac = networkInterface.getHardwareAddress();

        if (mac != null)
        {
          macs.append(getMACString(networkInterface.getHardwareAddress()));
          macs.append(",");
        }
      }
    }

    if (macs.length() == 0)
    {
      return "no-mac-address-found";
    }

    macs.deleteCharAt(macs.length()-1);

    return macs.toString();
  }

  private static String getMACString(byte[] mac)
  {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < mac.length; i++)
    {
      sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
    }

    return sb.toString();
  }


  // Instance Fields ------------------------------------------------------------------------------

  private BeehiveCommandChecker commandChecker;
  private ControllerConfiguration config;


  // Constructors ---------------------------------------------------------------------------------

  public BeehiveCommandCheckService(ControllerConfiguration config)
  {
    this.config = config;
    // TODO: This is a global switch for _ALL_ HttpsURLConnections. We must switch to a proper HTTP client library!
      if ("true".equals(System.getenv("SSL_DISABLE_HOSTNAME_VERIFICATION"))) {
          log.warn("Disabling SSL hostname verification for all connections to Beehive command checking service!");
          HttpUtils.disableHostnameVerification();
      }
  }


  // Instance Methods -----------------------------------------------------------------------------

  public void start(Deployer deployer)
  {
    this.commandChecker = new BeehiveCommandChecker(deployer, config);
    this.commandChecker.start();
  }
   
  public void stop()
  {
    this.commandChecker.stop();
  }
   


  // Nested Classes -------------------------------------------------------------------------------

  private static class BeehiveCommandChecker implements Runnable
  {

    // Instance Variables -------------------------------------------------------------------------

    /**
     * Shared thread variable to indicate this thread's running status.
     */
    private volatile boolean running = true;

    /**
     * Shared thread field for the HTTPS connection.
     */
    private volatile HttpsURLConnection https = null;

    /**
     * Deployer service reference.
     */
    private Deployer deployer;

    /**
     * Controller's current configuration
     */
    private ControllerConfiguration config;

    /**
     * The host thread for this runnable implementation.
     */
    private Thread remoteCommandThread = null;


    // Constructors -------------------------------------------------------------------------------

    BeehiveCommandChecker(Deployer deployer, ControllerConfiguration config)
    {
      this.deployer = deployer;
      this.config = config;
    }


    // Implements Runnable ------------------------------------------------------------------------

    @Override public void run()
    {
      int sleepTime = config.getRemoteCommandRequestIntervalMillis();

      try
      {
        URL serviceURL = getRemoteCommandService(deployer.getUserName());

        log.info("Starting remote command service to {0}...", serviceURL);

        while (running)
        {
          boolean isFastPolling = false;

          try
          {
            String str = httpGet(serviceURL, deployer.getUserName() /* TODO : dont fetch for each conn. */);

            GenericResourceResultWithErrorMessage res = null;

            try
            {
              res = new JSONDeserializer<GenericResourceResultWithErrorMessage>()
                        .use(null, GenericResourceResultWithErrorMessage.class)
                        .use("result", ArrayList.class)
                        .use("result.values", ControllerCommandDTO.class).deserialize(str);
            }

            catch(RuntimeException e)
            {
               log.error("Failed to deserialize commands from remote command service : ''{0}''.", e, str);
            }


            if (res != null)
            {
              if (res.getErrorMessage() != null)
              {
                log.warn("Remote command service returned an error : {0}", res.getErrorMessage());
              }

              else
              {
                List<ControllerCommandDTO> commands = resolveResult(res.getResult());

                if (commands != null && !commands.isEmpty())
                {
                  executeCommand(commands.get(0));

                  isFastPolling = true;
                }
              }
            }
          }

          catch (ConnectionException e)
          {
            log.warn(
                "Unable to connect to remote command service, retrying in {0} milliseconds. " +
                "Connection error: {1}",
                e, sleepTime, e.getMessage()
            );
          }

          log.trace(
              "Remote command check waiting for next connection attempt in {0} ms...", sleepTime
          );

          try
          {
            if (!isFastPolling)
            {
              Thread.sleep(sleepTime);
            }
          }

          catch (InterruptedException e)
          {
            running = false;

            Thread.currentThread().interrupt();

            log.info("Remote command thread was interrupted, shutting down...");
          }
        }
      }

      catch (ConfigurationException e)
      {
        running = false;

        log.error(
            "Could not start remote access service due to configuration error: ",
            e, e.getMessage()
        );
      }

      catch (ServiceClosedException e)
      {
        log.info("Shutting down {0}: {1}", Thread.currentThread().getName(), e.getMessage());
      }

      catch (Throwable t)
      {
        log.error("{0} : {1}", t, Thread.currentThread().getName(), t.getMessage());
      }

      log.info("Thread ''{0}'' stopped.", Thread.currentThread().getName());
    }


    // Private Instance Methods -------------------------------------------------------------------


    private void start()
    {
      remoteCommandThread = new Thread(this);

      remoteCommandThread.setName(REMOTE_COMMAND_THREAD_NAME);
      remoteCommandThread.setDaemon(true);

      remoteCommandThread.start();
    }

    private void stop()
    {
      running = false;

      release(https);

      remoteCommandThread.interrupt();
    }

    /**
     * Executes HTTP GET to given URL and reads the response document for the request.
     *
     * @param url
     *          URL to connect to
     *
     * @param username
     *          username to authenticate with
     *
     * @return  HTTP response document
     *
     * @throws ConfigurationException
     *            If HTTP GET request could not be executed. Typically indicates an error
     *            that cannot be resolved without a re-start and re-configuration and
     *            attempting to retry the request usually doesn't help.
     *
     * @throws ConnectionException
     *            If the HTTP connection failed because of request time out, unresponsive
     *            or unavailable server, connection was closed by the other side, or other
     *            types of network errors. These types of errors can resolve themselves
     *            sometimes by waiting and then re-attempting the request.
     *
     * @throws ServiceClosedException
     *            If we were interrupted while the running flag were set to false. Should
     *            proceed with an orderly shut-down.
     */
    private String httpGet(URL url, String username) throws ConfigurationException,
                                                            ConnectionException,
                                                            ServiceClosedException
    {
      return httpRequest(url, HttpMethod.GET, username);
    }

    /**
     * Executes HTTP POST to given URL.
     *
     * @param url
     *          URL to connect to
     *
     * @param username
     *          username to authenticate with
     *
     * @return  HTTP response document
     *
     * @throws ConfigurationException
     *            If HTTP POST request could not be executed. Typically indicates an error
     *            that cannot be resolved without a re-start and re-configuration and
     *            attempting to retry the request usually doesn't help.
     *
     * @throws ConnectionException
     *            If the HTTP connection failed because of request time out, unresponsive
     *            or unavailable server, connection was closed by the other side, or other
     *            types of network errors. These types of errors can resolve themselves
     *            some times by waiting and then re-attempting the request.
     *
     * @throws ServiceClosedException
     *            If we were interrupted while the running flag were set to false. Should
     *            proceed with an orderly shut-down.
     */
    private String httpPost(URL url, String username) throws ConfigurationException,
                                                            ConnectionException,
                                                            ServiceClosedException
    {
      return httpRequest(url, HttpMethod.POST, username);
    }


    /**
     * Executes HTTP request to given URL and reads the response document for the request.
     *
     * @param url
     *          URL to connect to
     *
     * @param method
     *          HTTP method to use for the request
     *
     * @param username
     *          username to authenticate with
     *
     * @return  HTTP response document
     *
     * @throws ConfigurationException
     *            If HTTP GET request could not be executed. Typically indicates an error
     *            that cannot be resolved without a re-start and re-configuration and
     *            attempting to retry the request usually doesn't help.
     *
     * @throws ConnectionException
     *            If the HTTP connection failed because of request time out, unresponsive
     *            or unavailable server, connection was closed by the other side, or other
     *            types of network errors. These types of errors can resolve themselves
     *            some times by waiting and then re-attempting the request.
     *
     * @throws ServiceClosedException
     *            If we were interrupted while the running flag were set to false. Should
     *            proceed with an orderly shut-down.
     */
    private String httpRequest(URL url, HttpMethod method, String username)
        throws ConfigurationException, ConnectionException, ServiceClosedException
    {
      HttpsURLConnection connection = connect(url, method, username);

      int responseCode = getResponseCode(connection);

      switch (responseCode)
      {
        case HttpURLConnection.HTTP_OK:

          return readResponse(connection);

        case HttpURLConnection.HTTP_UNAUTHORIZED:

          release(connection);

          throw new ConnectionException(
              "Unrecognized username ''{0}'' or incorrect password connecting to ''{1}''",
              username, url
          );

        case HttpURLConnection.HTTP_NOT_FOUND:

          release(connection);

          throw new ConnectionException(
              "Remote command service at ''{0}'' was not available or not found.", url
          );

        default:

          try
          {
            throw new ConnectionException(
                "Connection to ''{0}'' failed, HTTP error code {1} - {2}",
                url, responseCode, connection.getResponseMessage()
            );
          }

          catch (IOException e)
          {
            throw new ConnectionException(
                "Connection to ''{0}'' failed, HTTP error code {1}", url, responseCode
            );
          }

          finally
          {
            release(connection);
          }
      }
    }


    /**
     * Reads an HTTPS response document from the connection input stream.
     *
     * @return  HTTP response document
     *
     * @throws ConnectionException
     *            If the HTTP connection failed because of request time out, unresponsive
     *            or unavailable server, connection was closed by the other side, or other
     *            types of network errors. These types of errors can resolve themselves
     *            some times by waiting and then re-attempting the request.
     *
     * @throws ServiceClosedException
     *            If we were interrupted while the running flag were set to false. Should
     *            proceed with an orderly shut-down.
     */
    private String readResponse(HttpsURLConnection connection) throws ConnectionException,
                                                                      ServiceClosedException
    {
      try
      {
        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder builder = new StringBuilder(1024);

        String line = "";

        while (line != null)
        {
          line = reader.readLine();

          builder.append(line);
        }

        return builder.toString();
      }

      catch (SocketTimeoutException e)
      {
        release(connection);

        // in case of time-out, translate to connection exception that allows connection
        // retries...

        throw new ConnectionException(
            "Connection read timeout while reading response document : {0}",
            e, e.getMessage()
        );
      }

      catch (InterruptedIOException e)
      {
        release(connection);

        // We were interrupted -- trying to resolve by what reason. If running flag has
        // been set to false, it's an interrupt for an orderly shutdown, so convert
        // to specific close service exception. Otherwise treat it as a regular I/O
        // exception and allow retries via connection exception type...

        if (!running)
        {
          throw new ServiceClosedException(
            "Remote command service was interrupted while reading a response document : {0}",
            e, e.getMessage()
          );
        }

        else
        {
          throw new ConnectionException(
              "Reading response document was interrupted: {0}", e, e.getMessage()
          );
        }
      }

      catch (IOException e)
      {
        release(connection);

        // Generic I/O exception. Allow retries by re-throwing it as a connection exception...

        throw new ConnectionException(
            "I/O error while reading a response document : {0}",
            e, e.getMessage()
        );
      }
    }


    /**
     * Creates an HTTPS connection to a given URL with a HTTP Basic authentication.
     *
     * @param url
     *          URL to connect to
     *
     * @param method
     *          HTTP method to use for the request
     *
     * @param username
     *          username to authenticate with
     *
     * @return  HTTPS connection
     *
     * @throws ConfigurationException
     *            If HTTPS request could not be constructed. Typically indicates an error
     *            that cannot be resolved without a re-start and re-configuration and
     *            attempting to retry the request usually doesn't help.
     *
     * @throws ConnectionException
     *            If the HTTP connection failed because of request time out, unresponsive
     *            or unavailable server, connection was closed by the other side, or other
     *            types of network errors. These types of errors can resolve themselves
     *            some times by waiting and then re-attempting the request.
     *
     * @throws ServiceClosedException
     *            If we were interrupted while the running flag were set to false. Should
     *            proceed with an orderly shut-down.
     */
    private HttpsURLConnection connect(URL url, HttpMethod method, String username)
        throws ConnectionException, ConfigurationException, ServiceClosedException
    {
      HttpsURLConnection connection = createConnection(url);

      try
      {
        connection.setDoInput(true);
        connection.setRequestMethod(method.name());
        connection.setReadTimeout(config.getRemoteCommandResponseTimeoutMillis());
        connection.setConnectTimeout(config.getRemoteCommandConnectionTimeoutMillis());
        connection.setRequestProperty("User-Agent", "OpenRemote Controller");  // TODO : add version
        connection.setRequestProperty("Accept", "application/json");

        connection.addRequestProperty(
            Constants.HTTP_AUTHORIZATION_HEADER,
            HttpUtils.generateHttpBasicAuthorizationHeader(username, deployer.getPassword(username)));

        log.debug(
            "Connecting user ''{0}'' to ''{1}'' \n  Connection Timeout : {2} \n  Response Timeout : {3}",
            username, url, config.getRemoteCommandConnectionTimeoutMillis(),
            config.getRemoteCommandResponseTimeoutMillis()
        );

        connection.connect();

        return connection;
      }

      catch (SocketTimeoutException e)
      {
        release(connection);

        // Socket timeout, convert to connection exception to allow retries...

        throw new ConnectionException(
            "Remote access proxy service at ''{0}'' did not respond within {1} milliseconds.",
            url, config.getRemoteCommandConnectionTimeoutMillis()
        );
      }

      catch (InterruptedIOException e)
      {
        release(connection);

        // Other generic I/O interrupts. If running flag has been set to false, most likely
        // asked to perform an orderly shutdown. Otherwise convert to a connection exception
        // type that will allow retries...

        if (!running)
        {
          throw new ServiceClosedException("Interrupted while connecting to ''{0}''", url);
        }

        else
        {
          throw new ConnectionException("Interrupted while connecting to ''{0}''", url);
        }
      }

      catch (ConnectException e)
      {
        release(connection);

        // Regular connection exception will return to the connection loop and retry later...

        throw new ConnectionException("Could not connect to ''{0}'' : {1}", e, url, e.getMessage());
      }

      catch (Deployer.PasswordException e)
      {
        release(connection);

        // If we can't find the credentials, keep re-trying... the user might not yet have
        // entered his credentials.

        throw new ConnectionException(
            "The required password for user ''{0}'' was not found. Password manager error : {1}",
            e, username, e.getMessage()
        );
      }

      catch (ProtocolException e)
      {
        release(connection);

        // Things are pretty bad if we can't even construct a HTTP GET request. Shut down the
        // service with a configuration exception...

        throw new ConfigurationException(
            "Could not construct a HTTP GET request : {0}", e, e.getMessage()
        );
      }

      catch (IOException e)
      {
        release(connection);

        // I/O error during the connect. Connection exception will allow us to retry...

        throw new ConnectionException(
            "I/O error while connecting to ''{0}'' : {1}",
            e, url, e.getMessage()
        );
      }
    }


    /**
     * Attempts to release a connection by cleaning up (by consuming all input) so that it would
     * be returned immediately to the cache for re-use (HTTP keep-alive). <p>
     *
     * See http://docs.oracle.com/javase/6/docs/technotes/guides/net/http-keepalive.html
     *
     * @param connection
     *          connection to release
     */
    private void release(HttpsURLConnection connection)
    {
      if (connection == null)
      {
        return;
      }

      // try reading return code if it's still in the stream...

      try
      {
        getResponseCode(connection);
      }

      catch (Throwable throwable)
      {
        log.debug(
            "Error in attempting to retrieve the return code while cleaning up connection : {0}",
            throwable, throwable.getMessage()
        );
      }


      // Attempt to consume all of the input stream...

      try
      {
        InputStream is = null;

        try
        {
          is = connection.getInputStream();

          if (is != null)
          {
            BufferedInputStream bin = new BufferedInputStream(is);

            int bytes = 0;

            while ((bytes = bin.read(new byte[1024])) > 0);
          }
        }

        catch (IOException exception)
        {
          log.debug(
              "Error while attempting to release HTTPS connection input stream : {0}",
              exception, exception.getMessage()
          );
        }

        finally
        {
          if (is != null)
          {
            try
            {
              is.close();
            }

            catch (IOException ioe)
            {
              log.debug(
                  "Failed to close an input stream on connection release: {0}",
                  ioe, ioe.getMessage()
              );
            }
          }
        }


        // Check and consume the error stream if any (may be the same as input stream depending
        // on implementation)...

        InputStream errorStream = connection.getErrorStream();

        if (errorStream != null)
        {
          BufferedInputStream bin = new BufferedInputStream(errorStream);

          try
          {
            int bytes = 0;

            while ((bytes = bin.read(new byte[1024])) > 0);
          }

          catch (IOException exception)
          {
            log.debug(
                "Error while attempting to release HTTPS connection error stream : {0}",
                exception, exception.getMessage()
            );
          }

          finally
          {
            try
            {
              errorStream.close();
            }

            catch (IOException ioe)
            {
              log.debug(
                  "Failed to close an error stream on connection release: {0}",
                  ioe, ioe.getMessage()
              );
            }
          }
        }

        connection.disconnect();
      }

      // catch all error handler -- we don't want any of the release functionality errors to
      // propagate further... worst case scenario is a connection needs to be rebuilt instead
      // of re-used from the cache.

      catch (Throwable throwable)
      {
        log.debug("Error in releasing connection: {0}", throwable, throwable.getMessage());
      }
    }


    /**
     * Reads an HTTPS response code from the connection input stream.
     *
     * @param   connection
     *            https connection to use
     *
     * @return  HTTP response code
     *
     * @throws ConnectionException
     *            If the HTTP connection failed because of request time out, unresponsive
     *            or unavailable server, connection was closed by the other side, or other
     *            types of network errors. These types of errors can resolve themselves
     *            some times by waiting and then re-attempting the request.
     *
     * @throws ServiceClosedException
     *            If we were interrupted while the running flag were set to false. Should
     *            proceed with an orderly shut-down.
     */
    private int getResponseCode(HttpsURLConnection connection) throws ConnectionException,
                                                                      ServiceClosedException
    {
      try
      {
        return connection.getResponseCode();
      }

      catch (NullPointerException e)
      {
        throw new ConnectionException("Null connection.");
      }

      catch (SocketTimeoutException e)
      {
        // Socket time out, allow retry by translating to connection exception...

        throw new ConnectionException(
            "Read timeout while waiting for HTTP response message : {0}", e, e.getMessage()
        );
      }

      catch (InterruptedIOException e)
      {
        // Other generic I/O interruptions. Attempt to resolve the reason. If running
        // flag has been set to false, we've been interrupted to perform an orderly shutdown.
        // Otherwise allow retries via connection exception type...

        if (!running)
        {
          throw new ServiceClosedException("Service interrupted and requested to close...");
        }

        else
        {
          throw new ConnectionException(
              "Interrupted while reading HTTPS response code : {0}", e, e.getMessage()
          );
        }
      }

      catch (IOException e)
      {
        // Generic I/O errors. Allow retries via connection exception type...

        throw new ConnectionException(
            "I/O error while reading HTTPS response code : {0}", e, e.getMessage()
        );
      }
    }


    private HttpsURLConnection createConnection(URL url) throws ConnectionException
    {
      try
      {
        URLConnection connection = url.openConnection();

        // store connection reference to a shared thread variable -- used by an external thread
        // to release the connection if this service is stopped...

        this.https = (HttpsURLConnection)connection;

        return https;
      }

      catch (ClassCastException e)
      {
        throw new ConnectionException(
            "Configuration error: the ''{0}'' property ''{1}'' must be a URI with https:// schema.",
            ControllerConfiguration.REMOTE_COMMAND_SERVICE_URI, url
        );
      }

      catch (IOException e)
      {
        throw new ConnectionException(
            "Failed to create a connection to ''{0}'' : {1}", e, url, e.getMessage()
        );
      }
    }


    //
    // TODO
    //
    private void executeCommand(ControllerCommandDTO controllerCommand)
    {
      switch (controllerCommand.getCommandTypeEnum())
      {
        case INITIATE_PROXY:
            initiateProxy(controllerCommand);
            break;

        case UNLINK_CONTROLLER:

            stop();
            deployer.unlinkController();

            break;
            
        case DOWNLOAD_DESIGN:
        {
          try {
            String username = deployer.getUserName();
            if (username == null || username.equals(""))
            {
              log.error("Unable to retrieve username for beehive command service API call. Skipped...");
              break;
            }

            String password = deployer.getPassword(username);
            deployer.deployFromOnline(username, password);
            ackCommand(controllerCommand.getOid());
          } catch (PasswordException e) {
             log.error("Unable to retrieve password for beehive command service API call. Skipped...", e);
          } catch (ConfigurationException e) {
             log.error("Synchronizing controller with online account failed : {0}", e, e.getMessage());
          } catch (ConnectionException e) {
            log.error("Synchronizing controller with online account failed : {0}", e, e.getMessage());
          }
          break;
        }

        default:
            log.error("ControllerCommand not implemented yet: " + controllerCommand.getCommandType());
      }
    }


    //
    // TODO
    //
    private void initiateProxy(ControllerCommandDTO command)
    {
      Long id = command.getOid();
      String url = command.getCommandParameter().get("url");
      String token = command.getCommandParameter().get("token");

      Socket beehiveSocket = null;

      boolean needsAck = true;

      try
      {
        log.info("Connecting to beehive at "+url+" for proxy");
        beehiveSocket = ControllerProxy.makeClientSocket(url, token, config.getProxyTimeout());

        // at this point the command should already have been marked as ack by the listening end at beehive

        log.info("Connected to beehive");
        needsAck = false;

        // try to connect to it, see if it's still valid

        String ip = config.getWebappIp();
        int port = config.getWebappPort();

        if (ip == null || ip.trim().length() == 0)
        {
          ip = "localhost";
        }

        if (port == 0)
        {
          port = 8080;
        }

        ControllerProxy proxy = new ControllerProxy(beehiveSocket, ip, port, config.getProxyTimeout());
        log.info("Starting proxy");
        proxy.start();
      }

      catch (IOException e)
      {
        log.info("Got exception while connecting to beehive", e);

        if(beehiveSocket != null)
        {
          try
          {
            beehiveSocket.close();
          }

          catch (IOException e1)
          {
            // ignore
          }
        }

        // the server should have closed it, but let's help him to make sure

        if(needsAck)
        {
          ackCommand(id);
        }
      }
    }


    //
    // TODO
    //
    private void ackCommand(Long id)
    {
      log.debug("Acking command "+id) ;
      ClientResource cr = null;

      try
      {
        // TODO : multiple URIs possible... need to ack to same server

        URI base = config.getRemoteCommandURIs() [0];

        URI uri = base.resolve(base.getPath() + "/command/" + id);

        cr = new ClientResource(uri);

        String username = deployer.getUserName();

        cr.setChallengeResponse(ChallengeScheme.HTTP_BASIC, username, deployer.getPassword(username));

        Representation r = cr.delete();
        String str;
        str = r.getText();

        GenericResourceResultWithErrorMessage res =
            new JSONDeserializer<GenericResourceResultWithErrorMessage>()
                .use(null, GenericResourceResultWithErrorMessage.class)
                .use("result", String.class).deserialize(str);

        if (res.getErrorMessage() != null)
        {
              throw new RuntimeException(res.getErrorMessage());
        }
      }

      catch (Exception e)
      {
        log.error("!!! Unable to ACK controller command with id: " + id, e);
      }

      finally
      {
        if (cr != null)
        {
          cr.release();
        }
      }
    }



    /**
     * Resolves the URL to request remote commands based on controller's configuration
     * and controller's identity.
     */
    private URL getRemoteCommandService(String username) throws ConfigurationException
    {
      //
      // IMPLEMENTATION NOTES :
      //
      //    - username parameter is required when controller's ID is resolved from
      //      a remote server, not otherwise
      //    - if remote ID resolution has been configured (no ::loopback::) this method
      //      blocks indefinitely until such a remote controller ID is returned
      //
      // See other notes why the remote controller ID resolution should be removed.


      String uriPath = "";
      URI base = null;
      URI uri = null;

      try
      {
        // TODO : implement handling for multiple URIs for client side fail-over...

        base = config.getRemoteCommandURIs() [0];

        if (base == null)
        {
          throw new ConfigurationException(
              "Remote command service property ''{0}'' has not been set.",
              ControllerConfiguration.REMOTE_COMMAND_SERVICE_URI
          );
        }

        uriPath = base.getPath() + "/commands/" + getControllerIdentity(username);

        uri = base.resolve(uriPath);

        URL url = uri.toURL();

        if (!url.getProtocol().equalsIgnoreCase("https"))
        {
          throw new ConfigurationException(
              "The ''{0}'' property ''{1}'' must be a URI with https:// schema.",
              ControllerConfiguration.REMOTE_COMMAND_SERVICE_URI, url
          );
        }

        return url;
      }

      catch (IllegalArgumentException e)
      {
        throw new ConfigurationException(
            "Cannot resolve remote command service URI ''{0}'' : {1}",
            e, base.toString() + "/" + uriPath, e.getMessage()
        );
      }

      catch (MalformedURLException e)
      {
        throw new ConfigurationException(
            "Unable to convert URI ''{0}'' to URL : {1}",
            e, uri, e.getMessage()
        );
      }
    }


    @SuppressWarnings("unchecked")
    private List<ControllerCommandDTO> resolveResult(Object result) throws ConnectionException
    {
      // TODO : GenericResource... class needs a rethink to avoid requiring suppress warning annotations

      try
      {
        return (ArrayList<ControllerCommandDTO>)result;
      }

      catch (ClassCastException e)
      {
        throw new ConnectionException(e.toString());
      }
    }









    // ----- 8< -----------------------------------------------------------------------------------


    // TODO :
    //        This is a temporary implementation to abstract away the controller
    //        identity construction -- currently done as a remote call to the
    //        Beehive back-end but should be ideally an identifier the controller
    //        can construct locally.
    private Long getControllerIdentity(String username) throws ConfigurationException
    {
      String url = config.getBeehiveAccountServiceRESTRootUrl();

      // If acct management hasn't been configured or it has been configured with a local
      // loopback controller identity value...

      if (url == null || (url.startsWith("::loopback") && url.endsWith("::")))
      {
        return localControllerID(url);
      }

      else
      {
        try
        {
          url = config.getBeehiveAccountServiceRESTRootUrl() + "controller/announce/" + getMACAddresses();
        }

        catch (Exception e)
        {
          throw new ConfigurationException(e.toString());
        }
      }

      final int DELAY = 1000;

      // Because controller id is remote, we need to wait for an actual response before
      // we can continue...

      try
      {
        while (running)
        {
          try
          {
            String str = httpPost(new URL(url), username);

            GenericResourceResultWithErrorMessage res =
                new JSONDeserializer<GenericResourceResultWithErrorMessage>()
                    .use(null, GenericResourceResultWithErrorMessage.class)
                    .use("result", ControllerDTO.class)
                    .deserialize(str);

            if (res.getErrorMessage() != null)
            {
              throw new ConfigurationException("Controller identity error : {0}", res.getErrorMessage());
            }

            else
            {
              return ((ControllerDTO)res.getResult()).getOid();
            }
          }

          catch (ConnectionException e)
          {
            log.debug("Unable to retrieve controller identity, retrying in {0} milliseconds...", e, DELAY);
          }

          catch (ServiceClosedException e)
          {
            throw new ConfigurationException("Service closed.", e);
          }

          try
          {
            Thread.sleep(DELAY);
          }

          catch (InterruptedException e)
          {
            Thread.currentThread().interrupt();

            throw new ConfigurationException("Controller identity process interrupted.");
          }
        }
      }

      catch (MalformedURLException e)
      {
        throw new ConfigurationException(
            "Configuration property ''{0}'' value ''{1}'' is not a valid URL: {2}",
            ControllerConfiguration.BEEHIVE_ACCOUNT_SERVICE_REST_ROOT_URL, url, e.getMessage()
        );
      }

      throw new ConfigurationException("Controller identity: SNAFU");
    }

    private Long localControllerID(String url) throws ConfigurationException
    {
      // No remote acct mgmt, just use a fixed value for controller's identity..

      if (url == null)
      {
        return 1L;
      }

      url = url.trim();
      url = url.substring(2, url.length() - 2);
      String[] elements = url.split(",");

      if (elements.length == 1)
      {
        return 1L;
      }

      else if (elements.length >= 2)
      {
        try
        {
          return Long.parseLong(elements[1]);
        }

        catch (NumberFormatException e)
        {
          throw new ConfigurationException(
              "Unable to parse loopback value from ''{0}'' : {1}", e, url, e.getMessage()
          );
        }
      }

      throw new ConfigurationException("LocalControllerID SNAFU");
    }
  }


  public enum HttpMethod
  {
    GET, POST, PUT, DELETE, HEAD, OPTIONS, TRACE
  }

  public static class ServiceClosedException extends OpenRemoteException
  {
    public ServiceClosedException(String msg, Object... params)
    {
      super(msg, params);
    }
  }

}

