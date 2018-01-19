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

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.net.URI;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;
import java.util.regex.Pattern;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import junit.framework.Assert;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import flexjson.JSONSerializer;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.HttpReceiver;
import org.openremote.controller.SecureTCPTestServer;
import org.openremote.controller.TCPTestServer;
import org.openremote.controllercommand.domain.ControllerCommandDTO;
import org.openremote.rest.GenericResourceResultWithErrorMessage;
import org.openremote.security.PasswordManager;
import org.openremote.security.X509CertificateBuilder;
import org.openremote.security.provider.BouncyCastleX509CertificateBuilder;
import org.openremote.useraccount.domain.ControllerDTO;


/**
 * Unit tests for class {@link org.openremote.controller.service.BeehiveCommandCheckService}.
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class BeehiveCommandCheckServiceTest
{

  private static PrivateKey privateKey;
  private static Certificate certificate;
  private static HostnameVerifier oldVerifier;
  private static URI controllerBaseDir;

  // Test Lifecycle -------------------------------------------------------------------------------

  /**
   * Set up the encryption related infrastructure: install bouncycastle security provider,
   * create a key pair and corresponding certificate for the tests, configure the client trust
   * store in the JVM, and set up a host name verifier for 'localhost' because of the non-CA
   * certified key pair.
   *
   * @throws Exception    if anything fails
   */
  @BeforeClass public static void setup() throws Exception
  {
    // Set up BC provider, create a key pair and a self-signed certificate for the keypair,
    // store the certificate in a trust store and establish a hostname verifier that accepts
    // localhost connections regardless of the (self-signed) certificate used...

    controllerBaseDir = setControllerBaseDir();

    Security.addProvider(new BouncyCastleProvider());

    KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();

    certificate = createCertificate(keyPair);
    privateKey = keyPair.getPrivate();

    setSystemTrustStore(certificate);

    oldVerifier = HttpsURLConnection.getDefaultHostnameVerifier();

    HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier()
    {
      @Override public boolean verify(String hostname, SSLSession sslSession)
      {
        return hostname.equalsIgnoreCase("localhost");
      }
    });


  }

  /**
   * Attempt to clean up the modifications we've made after the tests: remove truststore
   * property configuration, remove BouncyCastle security provider and remove our relaxed
   * host name verification.
   */
  @AfterClass public static void cleanup()
  {
    clearSystemTrustStore();

    Security.removeProvider("BC");

    HttpsURLConnection.setDefaultHostnameVerifier(oldVerifier);
  }

  /**
   * Establish a base directory for test controller resources...
   */
  private static URI setControllerBaseDir()
  {
    URI baseDir = new File(System.getProperty("java.io.tmpdir"), "or-controller-test").toURI();

    File controllerBase = new File(baseDir);

    if (!controllerBase.exists())
    {
      boolean success = controllerBase.mkdirs();

      if (!success)
      {
        throw new RuntimeException("Could not create controller base " + controllerBaseDir);
      }
    }

    return baseDir;
  }

  /**
   * Create a self-signed certificate for a key pair.
   */
  private static Certificate createCertificate(KeyPair keyPair)
      throws X509CertificateBuilder.CertificateBuilderException
  {
    X509CertificateBuilder builder = new BouncyCastleX509CertificateBuilder();

    return builder.createSelfSignedCertificate(
        keyPair,
        new X509CertificateBuilder.Configuration(
            X509CertificateBuilder.SignatureAlgorithm.SHA512_WITH_RSA,
            "testissuer"
        )
    );
  }

  /**
   * Clear the trust store properties.
   */
  private static void clearSystemTrustStore()
  {
    System.clearProperty("javax.net.ssl.trustStore");
    System.clearProperty("javax.net.ssl.trustStorePassword");
  }

  private static File setSystemTrustStore(Certificate cert) throws Exception
  {
    File dir = new File(controllerBaseDir);
    File truststore = new File(dir, ".truststore");

    truststore.deleteOnExit();

    KeyStore trust = KeyStore.getInstance("JKS");

    trust.load(null, null);
    trust.setCertificateEntry("cert", cert);
    trust.store(new FileOutputStream(truststore), "foo".toCharArray());

    Assert.assertTrue(truststore.exists());

    System.setProperty("javax.net.ssl.trustStore", truststore.getAbsoluteFile().toString());
    System.setProperty("javax.net.ssl.trustStorePassword", "foo");

    return truststore;
  }


  // Unit Tests -----------------------------------------------------------------------------------


  /**
   * Basic test to check that the controller thread responsible for checking remote commands
   * starts and generates the expected HTTP GET request. <p>
   *
   * This test uses locally configured controller ID.
   *
   * @throws Exception    if test fails
   */
  @Test(timeout=10000)
  public void testRemoteCommandRequest() throws Exception
  {
    SecureTCPTestServer s = null;

    final Long CONTROLLER_ID = 1L;
    final String HOSTNAME = "localhost";
    final int PORT = 18888;
    final String USERNAME = "randomname";

    BeehiveCommandCheckService cs = null;

    try
    {
      // create a HTTP response...

      ArrayList<ControllerCommandDTO> list = new ArrayList<ControllerCommandDTO>();
      GenericResourceResultWithErrorMessage garbage = new GenericResourceResultWithErrorMessage(null, list);
      String response = new JSONSerializer().include("result").serialize(garbage);

      // create a secure TCP server with a HTTP receiver component...

      Semaphore completed = new Semaphore(1);
      completed.acquire();
      HttpReceiver receiver = new NotifyingReceiver(completed);

      // respond to HTTP GET to Host: https://[HOSTNAME]:[PORT]/commands/[CONTROLLER_ID]...

      receiver.addResponse(HttpReceiver.Method.GET, "/commands/" + CONTROLLER_ID, response);
      s = new SecureTCPTestServer(PORT, privateKey, certificate, receiver);

      s.start();


      // Create a deployer configuration for the CCS service...

      ControllerConfiguration config = new ControllerConfiguration();
      config.setRemoteCommandRequestInterval("60s");
      config.setBeehiveAccountServiceRESTRootUrl("::loopback," + CONTROLLER_ID + "::");
      config.setRemoteCommandServiceURI("https://" + HOSTNAME + ":" + PORT);
      config.setResourcePath(
          new File(new File(controllerBaseDir), "remote-command-request-test").getAbsolutePath()
      );

      // We need a resource dir for the controller's keystore...

      createUserResourceDir(config, USERNAME);


      // Start the deployer and wait for first remote command check request...

      cs = new BeehiveCommandCheckService(config);
      cs.start(DeployerTest.createDeployer(config));

      completed.acquire();


      // Check what the server receives...

      Assert.assertTrue(receiver.getMethod() == HttpReceiver.Method.GET);
      Assert.assertTrue(receiver.getPath().equalsIgnoreCase("/commands/" + CONTROLLER_ID));
      Assert.assertTrue(receiver.getHost(), receiver.getHost().equalsIgnoreCase(HOSTNAME));
      Assert.assertTrue(receiver.getPort().toString(), receiver.getPort() == PORT);
      Assert.assertTrue(receiver.getUserName().equals(USERNAME));

      Assert.assertTrue(receiver.getHeader("User-Agent").toLowerCase().contains("openremote"));
      Assert.assertTrue(receiver.getHeader("Accept").toLowerCase().contains("application/json"));

      Assert.assertTrue(receiver.getRequestMessageBody() == null);
    }

    finally
    {
      if (cs != null)
      {
        cs.stop();
      }

      if (s != null)
      {
        s.stop();
      }
    }
  }


  /**
   * Basic test to check that the controller continuously checks for remote commands,
   * despite receiving unexpected/erroneous response documents. Also tests remote command
   * loop frequency configuration.<p>
   *
   * This test uses locally configured controller ID.
   *
   * @throws Exception    if test fails
   */
  @Test(timeout=10000)
  public void testRemoteCommandRequestLoop() throws Exception
  {
    SecureTCPTestServer s = null;

    final Long CONTROLLER_ID = 1L;
    final String HOSTNAME = "localhost";
    final int PORT = 19999;
    final String USERNAME = "randomname";

    BeehiveCommandCheckService cs = null;

    try
    {
      // create a HTTP response...

      ArrayList<ControllerCommandDTO> list = new ArrayList<ControllerCommandDTO>();
      GenericResourceResultWithErrorMessage garbage = new GenericResourceResultWithErrorMessage(null, list);
      String response = new JSONSerializer().include("result").serialize(garbage);

      // Create a modified TCP server HTTP receiver component that counts the number of requests
      // we are making from client to server...

      CountingHttpReceiver receiver = new CountingHttpReceiver(4);


      // respond to HTTP GET to Host: https://[HOSTNAME]:[PORT]/commands/[CONTROLLER_ID]...

      receiver.addResponse(HttpReceiver.Method.GET, "/commands/" + CONTROLLER_ID, response);
      s = new SecureTCPTestServer(PORT, privateKey, certificate, receiver);

      s.start();


      // Create a deployer configuration for the CCS service, tuned up to one remote command
      // request every 250 milliseconds...

      ControllerConfiguration config = new ControllerConfiguration();
      config.setRemoteCommandRequestInterval("250ms");
      config.setRemoteCommandServiceURI("https://" + HOSTNAME + ":" + PORT);
      config.setBeehiveAccountServiceRESTRootUrl("::loopback," + CONTROLLER_ID + "::");
      config.setResourcePath(
          new File(new File(controllerBaseDir), "remote-command-request-loop-test").getAbsolutePath()
      );


      // We need a resource dir for the controller's keystore...

      createUserResourceDir(config, USERNAME);


      // Start the deployer and give it time to accumulate 4 remote command
      // check requests...

      cs = new BeehiveCommandCheckService(config);
      cs.start(DeployerTest.createDeployer(config));

      receiver.sharedSemaphore.acquire();

      // Should have accumulated 4 requests within time period of 1 to 1.25 seconds...

      Assert.assertTrue("Got " + receiver.currentCount, receiver.currentCount >= 4);
      Assert.assertTrue(
          "Took " + receiver.executionTime.toString() + " ms, is config broken or ur puter slow?",
          receiver.executionTime + 250 >= 1000 && receiver.executionTime + 250 <= 1250
      );
    }

    finally
    {
      if (cs != null)
      {
        cs.stop();
      }

      if (s != null)
      {
        s.stop();
      }
    }
  }


  /**
   * This test ensures the remote command check thread stays alive despite attempting to
   * connect to a non-existing server...
   *
   * @throws Exception if test fails
   */
  @Test public void testRemoteCommandRequestNoServer() throws Exception
  {
    final Long CONTROLLER_ID = 1L;
    final String HOSTNAME = "localhost";
    final int PORT = 19888;
    final String USERNAME = "randomname";


    // Create a deployer configuration for the CCS service. There's nothing running at
    // port [PORT]

    ControllerConfiguration config = new ControllerConfiguration();
    config.setRemoteCommandRequestInterval("250ms");
    config.setRemoteCommandServiceURI("https://" + HOSTNAME + ":" + PORT);
    config.setBeehiveAccountServiceRESTRootUrl("::loopback," + CONTROLLER_ID + "::");
    config.setResourcePath(
        new File(new File(controllerBaseDir), "remote-command-request-noserver-test").getAbsolutePath()
    );


    // We need a resource dir for the controller's keystore...

    createUserResourceDir(config, USERNAME);


    BeehiveCommandCheckService cs = new BeehiveCommandCheckService(config);

    try
    {
      cs.start(DeployerTest.createDeployer(config));

      Thread.sleep(1000);

      // Check that the thread is still alive and spinning....

      Thread t = getRemoteCommandThread();

      if (t == null)
      {
        Assert.fail("Test failed, thread has been erased.");
      }

      Assert.assertTrue(t.isAlive());
      Assert.assertTrue(t.getState() != Thread.State.TERMINATED);
      Assert.assertTrue(!t.isInterrupted());
    }

    finally
    {
      cs.stop();
    }
  }



  /**
   * Tests thread for remote commands when server is responding slowly (or not at all) and
   * the response timeout kicks in.<p>
   *
   * This test uses locally configured controller ID.
   *
   * @throws Exception    if test fails
   */
  @Test(timeout=10000)
  public void testRemoteCommandRequestNoResponse() throws Exception
  {
    SecureTCPTestServer s = null;

    final Long CONTROLLER_ID = 1L;
    final String HOSTNAME = "localhost";
    final int PORT = 19199;
    final String USERNAME = "randomname";

    BeehiveCommandCheckService cs = null;

    try
    {
      // create a HTTP response...

      ArrayList<ControllerCommandDTO> list = new ArrayList<ControllerCommandDTO>();
      GenericResourceResultWithErrorMessage garbage = new GenericResourceResultWithErrorMessage(null, list);
      String response = new JSONSerializer().include("result").serialize(garbage);

      // Create a modified TCP server receiver component that will never send the response
      // but counts the number of times it receives a valid requests and *should* have responded...

      MuteHttpReceiver receiver = new MuteHttpReceiver(4);


      receiver.addResponse(HttpReceiver.Method.GET, "/commands/" + CONTROLLER_ID, response);
      s = new SecureTCPTestServer(PORT, privateKey, certificate, receiver);

      s.start();


      // Create a deployer configuration for the CCS service. This is tuned to send a request
      // every 250 milliseconds but only to a shorter response timeout, waiting only 200 ms
      // for the server to respond before discarding the request...

      ControllerConfiguration config = new ControllerConfiguration();
      config.setRemoteCommandRequestInterval("250ms");
      config.setRemoteCommandResponseTimeout("200ms");
      config.setBeehiveAccountServiceRESTRootUrl("::loopback," + CONTROLLER_ID + "::");
      config.setRemoteCommandServiceURI("https://" + HOSTNAME + ":" + PORT);
      config.setResourcePath(
          new File(new File(controllerBaseDir), "remote-command-request-noresponse-test").getAbsolutePath()
      );


      // We need a resource dir for the controller's keystore...

      createUserResourceDir(config, USERNAME);


      // Start the deployer and give it time to accumulate 4 remote command
      // check requests...

      cs = new BeehiveCommandCheckService(config);
      cs.start(DeployerTest.createDeployer(config));

      receiver.sharedSemaphore.acquire();

      // Should have accumulated at least four requests within a second, despite there being
      // no responses from the server since the response timeout is low (200 milliseconds)...

      Assert.assertTrue("" + receiver.currentCount, receiver.currentCount >= 4);
      Assert.assertTrue(
          "Took " + receiver.executionTime.toString() + "ms, is config broken or your puter slow?",
          receiver.executionTime >= 800 && receiver.executionTime <= 1200
      );
    }

    finally
    {
      if (cs != null)
      {
        cs.stop();
      }

      if (s != null)
      {
        s.stop();
      }
    }
  }



  /**
   * Basic test for checking remote commands with remotely retrieved controller ID.
   *
   * @throws Exception    if test fails
   */
  @Test(timeout=10000)
  public void testRemoteControllerID() throws Exception
  {
    SecureTCPTestServer s1 = null;
    SecureTCPTestServer s2 = null;

    final Long CONTROLLER_ID = 123L;
    final String HOSTNAME = "localhost";
    final int PORT_S1 = 19299;
    final int PORT_S2 = 19399;
    final String USERNAME = "randomname";
    final String macAddressList = BeehiveCommandCheckService.getMACAddresses();

    BeehiveCommandCheckService cs = null;

    try
    {
      // create a HTTP response for controller announcement in account service...

      ControllerDTO controller = new ControllerDTO();
      controller.setOid(CONTROLLER_ID);
      GenericResourceResultWithErrorMessage result = new GenericResourceResultWithErrorMessage(null, controller);
      String response = new JSONSerializer().include("result").serialize(result);


      // Set up a receiver for the account service URL path /controller/announce/[MAC ADDRESS LIST]

      HttpReceiver receiver = new HttpReceiver();
      receiver.addResponse(HttpReceiver.Method.POST, "/controller/announce/" + macAddressList, response);
      s1 = new SecureTCPTestServer(PORT_S1, privateKey, certificate, receiver);

      s1.start();

      // Create an HTTP response for remote command check

      ArrayList<ControllerCommandDTO> list = new ArrayList<ControllerCommandDTO>();
      GenericResourceResultWithErrorMessage garbage = new GenericResourceResultWithErrorMessage(null, list);
      String response2 = new JSONSerializer().include("result").serialize(garbage);

      // Set up a receiver for the remote command service URL path /commands/[CONTROLLER_ID]...

      Semaphore completed = new Semaphore(1);
      completed.acquire();

      HttpReceiver receiver2 = new NotifyingReceiver(completed);
      receiver2.addResponse(HttpReceiver.Method.GET, "/commands/" + CONTROLLER_ID, response2);
      s2 = new SecureTCPTestServer(PORT_S2, privateKey, certificate, receiver2);

      s2.start();


      // Create a deployer with a configuration for both account service and
      // remote command service...

      ControllerConfiguration config = new ControllerConfiguration();
      config.setBeehiveAccountServiceRESTRootUrl("https://" + HOSTNAME + ":" + PORT_S1 + "/");
      config.setRemoteCommandServiceURI("https://" + HOSTNAME + ":" + PORT_S2);
      config.setResourcePath(
          new File(new File(controllerBaseDir), "remote-command-remote-id-test").getAbsolutePath()
      );


      // We need a resource dir for the controller's keystore...

      createUserResourceDir(config, USERNAME);


      cs = new BeehiveCommandCheckService(config);
      cs.start(DeployerTest.createDeployer(config));

      completed.acquire();


      // Assert remote controller ID HTTP POST request...

      Assert.assertTrue(receiver.getMethod().equals(HttpReceiver.Method.POST));
      Assert.assertTrue(
          "Got '" + receiver.getPath() +  "'",
          receiver.getPath().equals("/controller/announce/" + macAddressList)
      );

      // Assert remote command url...

      Assert.assertTrue(
          receiver2.getPath(),
          receiver2.getPath().equalsIgnoreCase("/commands/" + CONTROLLER_ID)
      );
    }

    finally
    {
      if (cs != null)
      {
        cs.stop();
      }

      if (s1 != null)
      {
        s1.stop();
      }

      if (s2 != null)
      {
        s2.stop();
      }
    }
  }


  /**
   * Tests remote command service that returns INITIATE_PROXY command and tests a socket
   * connection is made from controller to the given URL with a given token.
   *
   * @throws Exception  if test fails
   */
  @Test(timeout=70000)
  public void testInitiateProxy() throws Exception
  {
    SecureTCPTestServer s = null;
    SecureTCPTestServer s2 = null;

    final Long CONTROLLER_ID = 1L;
    final String HOSTNAME = "localhost";
    final int PORT = 19988;
    final int PORT_2 = 19998;

    final String USERNAME = "randomname";
    final String PROXY_TOKEN = "random-token";

    BeehiveCommandCheckService cs = null;

    try
    {
      // Create a remote command service response to initiate proxy to server...

      ArrayList<ControllerCommandDTO> list = new ArrayList<ControllerCommandDTO>();
      ControllerCommandDTO cmd = new ControllerCommandDTO();

      cmd.setCommandTypeEnum(ControllerCommandDTO.Type.INITIATE_PROXY);
      Map<String, String> params = new HashMap<String, String>();
      params.put("url", "https://" + HOSTNAME + ":" + PORT_2);
      params.put("token", PROXY_TOKEN);
      cmd.setCommandParameter(params);
      list.add(cmd);

      GenericResourceResultWithErrorMessage garbage = new GenericResourceResultWithErrorMessage(null, list);
      String response = new JSONSerializer().include("result").include("result.commandParameter").serialize(garbage);


      // Attach a HTTP receiver that responds to https://localhost:[PORT]/commands/[CONTROLLER_ID]...

      HttpReceiver receiver = new HttpReceiver();
      receiver.addResponse(HttpReceiver.Method.GET, "/commands/" + CONTROLLER_ID, response);
      s = new SecureTCPTestServer(PORT, privateKey, certificate, receiver);

      s.start();


      // Attach a TCP receiver that listens on the configured INITIATE_PROXY 'url' parameter...

      final Semaphore complete = new Semaphore(1);
      complete.acquire();

      s2 = new SecureTCPTestServer(PORT_2, privateKey, certificate, new TCPTestServer.Receiver()
      {
        @Override public void received(String tcpString, TCPTestServer.Response response)
        {
          // client initiating a proxy should send back a token we gave them...

          Assert.assertTrue(tcpString.equals(PROXY_TOKEN));

          complete.release();
        }
      });

      s2.start();


      // Deployer config for remote command service...

      ControllerConfiguration config = new ControllerConfiguration();
      config.setRemoteCommandRequestInterval("60s");
      config.setBeehiveAccountServiceRESTRootUrl("::loopback," + CONTROLLER_ID + "::");
      config.setRemoteCommandServiceURI("https://" + HOSTNAME + ":" + PORT);
      config.setResourcePath(
          new File(new File(controllerBaseDir), "remote-command-initiate-proxy-test").getAbsolutePath()
      );


      // We need a resource dir for the controller's keystore...

      createUserResourceDir(config, USERNAME);


      cs = new BeehiveCommandCheckService(config);
      cs.start(DeployerTest.createDeployer(config));

      complete.acquire();
    }

    finally
    {
      if (cs != null)
      {
        cs.stop();
      }

      if (s != null)
      {
        s.stop();
      }

      if (s2 != null)
      {
        s2.stop();
      }
    }
  }



  // Helper Methods -------------------------------------------------------------------------------


  private void createUserResourceDir(ControllerConfiguration config, String username) throws Exception
  {
    File dir = new File(config.getResourcePath());

    if (!dir.exists())
    {
      boolean success = dir.mkdirs();

      if (!success)
      {
        throw new RuntimeException("Cannot create controller resource dir " + dir);
      }
    }

    // Where controller expects to find the currently verified user name...

    File f = new File(dir, ".user");

    f.deleteOnExit();

    FileWriter out = new FileWriter(f);
    out.write(username);
    out.close();

    // And the keystore...

    File keyfile = new File(dir, ".keystore");
    keyfile.deleteOnExit();

    // And the credentials convention used to access the store...

    String storeKey = System.getProperty("user.name") + ".key";

    PasswordManager pw = new PasswordManager(keyfile.toURI(), storeKey.toCharArray());
    pw.addPassword("randomname", "foo".getBytes(), storeKey.toCharArray());
  }

  private Thread getRemoteCommandThread()
  {
    Thread[] threads = new Thread[Thread.activeCount() * 2];
    Thread.enumerate(threads);

    for (Thread t : threads)
    {
      if (t == null || t.getName() == null)
      {
        continue;
      }

      else if (t.getName().equals(BeehiveCommandCheckService.REMOTE_COMMAND_THREAD_NAME))
      {
        return t;
      }
    }

    return null;
  }



  // Nested Classes -------------------------------------------------------------------------------

  /**
   * Test TCP server receiver that counts the number of incoming requests (assuming they're
   * a valid requests mapping to a response).
   */
  private static class CountingHttpReceiver extends NotifyingReceiver
  {
    protected int currentCount = 0;
    protected int countToComplete = 0;
    protected Long executionTime = 0L;

    private Long firstExecutionTime = 0L;

    private CountingHttpReceiver(int countToComplete) throws InterruptedException
    {
      super(initSemaphore(new Semaphore(1)));

      this.countToComplete = countToComplete;
    }

    @Override protected void respond(TCPTestServer.Response response)
    {
      startTimer();

      super.respond(response);

      count();
    }

    @Override protected void semaphoreRelease()
    {
      // override to no-op -- release semaphore only after count() condition
    }

    protected void startTimer()
    {
      if (currentCount == 0)
      {
        firstExecutionTime = System.currentTimeMillis();
      }
    }

    protected void count()
    {
      currentCount++;

      if (currentCount >= countToComplete)
      {
        executionTime = System.currentTimeMillis() - firstExecutionTime;

        sharedSemaphore.release();
      }
    }
  }


  /**
   * A notifying receiver that can be used via a semaphore to notify the test whenever an
   * expected condition has been fulfilled and test can continue.  <p>
   *
   * By default counts down by one the semaphore permits after the response has been sent.
   */
  private static class NotifyingReceiver extends HttpReceiver
  {
    protected Semaphore sharedSemaphore;

    protected static Semaphore initSemaphore(Semaphore semaphore) throws InterruptedException
    {
      semaphore.acquire();

      return semaphore;
    }

    private NotifyingReceiver(Semaphore sharedSemaphore)
    {
      this.sharedSemaphore = sharedSemaphore;
    }

    @Override protected void respond(TCPTestServer.Response response)
    {
      super.respond(response);

      semaphoreRelease();
    }

    protected void semaphoreRelease()
    {
      sharedSemaphore.release();
    }
  }

  /**
   * Test TCP server receiver which won't send responses back to the client -- just counts
   * the number of responses it should've sent.
   */
  private static class MuteHttpReceiver extends CountingHttpReceiver
  {

    private MuteHttpReceiver(int countToComplete) throws InterruptedException
    {
      super(countToComplete);
    }

    @Override protected void respond(TCPTestServer.Response response)
    {
      // don't respond...

      startTimer();

      super.count();
    }
  }
}

