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
package org.openremote.controller.protocol.telnet;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.openremote.controller.command.Command;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.model.sensor.SwitchSensor;
import org.openremote.controller.protocol.ReadCommand;
import org.openremote.controller.statuscache.ChangedStatusTable;
import org.openremote.controller.statuscache.EventProcessorChain;
import org.openremote.controller.statuscache.StatusCache;
import org.openremote.controller.utils.Logger;

/**
 * Tests for {@link org.openremote.controller.protocol.telnet.TelnetCommandBuilder} and
 * {@link org.openremote.controller.protocol.telnet.TelnetCommand} classes
 * 
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class TelnetCommandBuilderTest
{
  private TelnetCommandBuilder builder = null;
  private StatusCache cache = null;


  // Test Lifecycle -------------------------------------------------------------------------------

  @Before public void setUp()
  {
    // sets up a shared telnet command builder and a status cache for sensor testing

    builder = new TelnetCommandBuilder();

    ChangedStatusTable cst = new ChangedStatusTable();
    EventProcessorChain echain = new EventProcessorChain();

    cache = new StatusCache(cst, echain);
  }

  /**
   * This is a regression test for ORCJAVA-326 (http://jira.openremote.org/browse/ORCJAVA-326).
   *
   * Simple telnet (TCP really) server is started which responds with 'on', should map the
   * configured switch sensor to on state.
   *
   * @throws Exception  if test fails
   */
  @Test public void testSwitchOnResponse_ORCJAVA_326() throws Exception
  {
    Server server = null;
    SwitchSensor s1 = null;

    final int SENSOR_ID = 1;
    final int SERVER_PORT = 4444;

    try
    {
      server = new Server(SERVER_PORT);
      Map<String, String> requestResponse = new HashMap<String, String>();
      requestResponse.put("sendtest1", "on");

      server.start(requestResponse);

      TelnetCommand cmd = getCommand("localhost", SERVER_PORT, "null|sendtest1");

      s1 = new SwitchSensor("switch on", SENSOR_ID, cache, cmd, 1);

      cache.registerSensor(s1);
      s1.start();

      Assert.assertTrue(
          "Expected 'on', got '" + getSensorValueFromCache(SENSOR_ID) + "'",
          getSensorValueFromCache(SENSOR_ID).equals("on")
      );

    }

    finally
    {
      stop(s1);
      stop(server);
    }
  }

  /**
   * Tests a telnet command that expects an initial response *before* sending a command
   * that is supposed to indicate an 'on' state.
   *
   * @throws Exception if test fails
   */
  @Test public void testWaitForMessage() throws Exception
  {
    Server server = null;
    SwitchSensor s2 = null;

    final int SENSOR_ID = 2;
    final Integer SERVER_PORT = 4445;

    try
    {
      server = new Server(SERVER_PORT);
      Map<String, String> requestResponse = new HashMap<String, String>();
      requestResponse.put("sendtest2", "on");

      server.start(requestResponse, "Foo", 0);

      TelnetCommand cmd = getCommand("localhost", SERVER_PORT, "Foo|sendtest2");

      s2 = new SwitchSensor("switch 2 on", SENSOR_ID, cache, cmd, 1);

      cache.registerSensor(s2);
      s2.start();


      Assert.fail(
          "This test is currently failing due to what seems like a bug in the telnet " +
          "implementation. Telnet command 'Foo|sendtest2' is configured which correctly " +
          "waits to receive 'Foo' from test telnet server in 'WaitForString' implementation. " +
          "The 'sendtest2' is then sent for which the response is 'on'. This is not handled " +
          "correctly since the telnet implementation is still waiting for 'Foo' as response " +
          "which eventually times out with an exception."
      );

      Assert.assertTrue(
          "Expected 'on', got '" + getSensorValueFromCache(SENSOR_ID) + "'",
          getSensorValueFromCache(SENSOR_ID).equals("on")
      );
    }

    finally
    {
      stop(s2);
      stop(server);
    }
  }


  /**
   * Make sure the telnet server response to switch sensor polling read is case insensitive.
   *
   * @throws Exception if the test fails
   */
  @Test public void testSwitchOnOffCaseInsensitivity() throws Exception
  {
    Server server = null;
    SwitchSensor s2 = null;

    final int SENSOR_ID = 51;
    final Integer SERVER_PORT = 4544;

    try
    {
      server = new Server(SERVER_PORT);
      Map<String, String> requestResponse = new HashMap<String, String>();
      requestResponse.put("insensitive", "ON");

      server.start(requestResponse);

      TelnetCommand cmd = getCommand("localhost", SERVER_PORT, "null|insensitive");

      s2 = new SwitchSensor("switch case insensitive", SENSOR_ID, cache, cmd, 1);

      cache.registerSensor(s2);
      s2.start();

      Assert.assertTrue(
          "Expected 'on', got '" + getSensorValueFromCache(SENSOR_ID) + "'",
          getSensorValueFromCache(SENSOR_ID).equals("on")
      );
    }

    finally
    {
      stop(s2);
      stop(server);
    }
  }

  /**
   * Tests telnet polling switch sensor zero-one response mapping to on/off.
   *
   * @throws Exception if the test fails
   */
  @Test public void testSwitchZeroOneResponse() throws Exception
  {
    Server server = null;
    SwitchSensor s1 = null;
    SwitchSensor s2 = null;

    final int SENSOR_ID1 = 52;
    final int SENSOR_ID2 = 53;
    final Integer SERVER_PORT = 4554;

    try
    {
      server = new Server(SERVER_PORT);
      Map<String, String> requestResponse = new HashMap<String, String>();
      requestResponse.put("one", "1");
      requestResponse.put("zero", "0");

      server.start(requestResponse);

      TelnetCommand cmd = getCommand("localhost", SERVER_PORT, "null|one");

      s2 = new SwitchSensor("switch one", SENSOR_ID2, cache, cmd, 1);

      cache.registerSensor(s2);
      s2.start();

      Assert.assertTrue(
          "Expected 'on', got '" + getSensorValueFromCache(SENSOR_ID2) + "'",
          getSensorValueFromCache(SENSOR_ID2).equals("on")
      );


      cmd = getCommand("localhost", SERVER_PORT, "null|zero");

      s1 = new SwitchSensor("switch zero", SENSOR_ID1, cache, cmd, 1);

      cache.registerSensor(s1);
      s1.start();

      Assert.assertTrue(
          "Expected 'off', got '" + getSensorValueFromCache(SENSOR_ID1) + "'",
          getSensorValueFromCache(SENSOR_ID1).equals("off")
      );

    }

    finally
    {
      stop(s2);
      stop(s1);
      stop(server);
    }
  }


  /**
   * Tests wait for message timeout behavior.
   *
   * Given all the timings, the test is a bit iffy. But it does configure the initial response
   * on the telnet server to double the default timeout configured on the telnet protocol (not
   * setting timeout explicitly so relying on the default value). Then waits for double the
   * telnet server's response delay value before checking if response to message has been sent
   * back ('on').
   *
   * Given that the wait-for-reply timeout in telnet protocol implementation should kick in
   * at TelnetCommand.DEFAULT_TIMEOUT, not expecting the 'on' response to ever make it back
   * to the controller's cache.
   *
   * @throws Exception if test fails
   */
  @Test public void testWaitForMessageTimeout() throws Exception
  {
    Server server = null;
    SwitchSensor s3 = null;

    final int SENSOR_ID = 3;
    final Integer SERVER_PORT = 4446;
    final int RESPONSE_DELAY = TelnetCommand.DEFAULT_TIMEOUT * 1000 * 2;

    try
    {
      server = new Server(SERVER_PORT);
      Map<String, String> requestResponse = new HashMap<String, String>();
      requestResponse.put("sendtest3", "on");

      server.start(requestResponse, "Na-na", RESPONSE_DELAY);

      TelnetCommand cmd = getCommand("localhost", SERVER_PORT, "Na-na|sendtest3");

      s3 = new SwitchSensor("switch 3 on", SENSOR_ID, cache, cmd, 1);

      cache.registerSensor(s3);
      s3.start();

      // give it time to come back... although we know the timeout should trigger earlier

      Thread.sleep(RESPONSE_DELAY * 2);

      // expecting N/A since timeout should have kicked in...

      Assert.assertTrue(
          "Expected 'N/A', got '" + getSensorValueFromCache(SENSOR_ID) + "'",
          getSensorValueFromCache(SENSOR_ID).equals("N/A")
      );
    }

    finally
    {
      stop(s3);
      stop(server);
    }
  }


  /**
   * Increases telnet wait for timeout to 3 seconds and adds a response delay to test telnet
   * server to two seconds (these values are based on Telnet default timeout being one second). <p>
   *
   * Everything should work as expected without timeouts, although the delays are longer. The
   * dependencies to timeouts to test behavior is a bit iffy though, making this test not 100%
   * to execute correctly on all systems.
   *
   * @throws Exception if test fails
   */
  @Test public void testIncreasedWaitForMessageTimeout() throws Exception
  {
    Server server = null;
    SwitchSensor s4 = null;

    final int SENSOR_ID = 4;
    final Integer SERVER_PORT = 4447;
    final int RESPONSE_DELAY = TelnetCommand.DEFAULT_TIMEOUT * 1000 * 2;
    final int WAIT_FOR_TIME_OUT = TelnetCommand.DEFAULT_TIMEOUT * 3;

    try
    {
      server = new Server(SERVER_PORT);
      Map<String, String> requestResponse = new HashMap<String, String>();
      requestResponse.put("sendtest4", "on");

      server.start(requestResponse, "LongDelay", RESPONSE_DELAY);

      TelnetCommand cmd = getCommand("localhost", SERVER_PORT, "LongDelay|sendtest4", WAIT_FOR_TIME_OUT);

      s4 = new SwitchSensor("switch 4 on", SENSOR_ID, cache, cmd, 1);

      cache.registerSensor(s4);
      s4.start();

      // give the response some time to return...

      Thread.sleep(RESPONSE_DELAY + 1000);

      // expecting 'on' ...

      Assert.fail(
          "This test is currently failing due to what seems like a bug in the telnet " +
          "implementation. Telnet command 'LongDelay|sendtest4' is configured which correctly " +
          "waits to receive 'LongDelay' from test telnet server in 'WaitForString' implementation. " +
          "The 'sendtest4' is then sent for which the response is 'on'. This is not handled " +
          "correctly since the telnet implementation is still waiting for 'LongDelay' as response " +
          "which eventually times out with an exception."
      );

      Assert.assertTrue(
          "Expected 'on', got '" + getSensorValueFromCache(SENSOR_ID) + "'",
          getSensorValueFromCache(SENSOR_ID).equals("on")
      );
    }

    finally
    {
      stop(s4);
      stop(server);
    }
  }

  /**
   * Similar to the other timeout test expect without the initial welcome/waitfor message which
   * is currently causing buggy/unexpected behavior. <p>
   *
   * Increases telnet wait for timeout to 3 seconds and adds a response delay to test telnet
   * server to two seconds (these values are based on Telnet default timeout being one second). <p>
   *
   * Everything should work as expected without timeouts, although the delays are longer. The
   * dependencies to timeouts to test behavior is a bit iffy though, making this test not 100%
   * to execute correctly on all systems.
   *
   * @throws Exception if test fails
   */
  @Test public void testIncreasedWaitForMessageTimeout2() throws Exception
  {
    Server server = null;
    SwitchSensor s5 = null;

    final int SENSOR_ID = 5;
    final Integer SERVER_PORT = 4448;
    final int RESPONSE_DELAY = TelnetCommand.DEFAULT_TIMEOUT * 1000 * 2;
    final int WAIT_FOR_TIME_OUT = TelnetCommand.DEFAULT_TIMEOUT * 3;

    try
    {
      server = new Server(SERVER_PORT);
      Map<String, String> requestResponse = new HashMap<String, String>();
      requestResponse.put("sendtest5", "on");

      server.start(requestResponse, null /* no welcome message */, RESPONSE_DELAY);

      TelnetCommand cmd = getCommand("localhost", SERVER_PORT, "null|sendtest5", WAIT_FOR_TIME_OUT);

      s5 = new SwitchSensor("switch 5 on", SENSOR_ID, cache, cmd, 1);

      cache.registerSensor(s5);
      s5.start();

      // give the response some time to return...

      Thread.sleep(RESPONSE_DELAY + 1000);

      // expecting 'on' ...

      Assert.assertTrue(
          "Expected 'on', got '" + getSensorValueFromCache(SENSOR_ID) + "'",
          getSensorValueFromCache(SENSOR_ID).equals("on")
      );
    }

    finally
    {
      stop(s5);
      stop(server);
    }
  }




   @Test public void testTelnet()
   {
     TelnetCommand cmd = getCommand("192.168.1.1", 23, "test");
     Assert.assertEquals("192.168.1.1", cmd.getIp());
     Assert.assertEquals(23, cmd.getPort().intValue());
     Assert.assertEquals("test", cmd.getCommand());
   }
   
   @Test public void testTelnetWithParam()
   {
     TelnetCommand cmd = getCommand("192.168.1.1", 23, "light1_${param}");
     Assert.assertEquals("192.168.1.1", cmd.getIp());
     Assert.assertEquals(23, cmd.getPort().intValue());
     Assert.assertEquals("light1_255", cmd.getCommand());
   }


  // Helpers --------------------------------------------------------------------------------------

  private String getSensorValueFromCache(int sensorID) throws Exception
  {
    // sleep here to give the polling mechanism enough time to push the event value to cache...

    Thread.sleep(ReadCommand.POLLING_INTERVAL * 2);

    return cache.queryStatus(sensorID);
  }

  private void stop(Server server)
  {
    if (server != null)
    {
      try
      {
        server.stop();
      }

      catch (Exception e)
      {
        System.err.println("test server failed: " + e);
      }
    }
  }

  private void stop(Sensor s)
  {
    if (s != null)
    {
      s.stop();
    }
  }

  private TelnetCommand getCommand(String address, int port, String cmd, int timeout)
  {
    Element ele = new Element("command");
    ele.setAttribute("id", "test");
    ele.setAttribute("protocol", "telnet");
    ele.setAttribute(Command.DYNAMIC_VALUE_ATTR_NAME, "255");

    Element propName = new Element("property");
    propName.setAttribute("name", "name");
    propName.setAttribute("value", "testName");

    Element propAddr = new Element("property");
    propAddr.setAttribute("name", "ipAddress");
    propAddr.setAttribute("value", address);

    Element propPort = new Element("property");
    propPort.setAttribute("name", "port");
    propPort.setAttribute("value", "" + port);

    Element propCommand = new Element("property");
    propCommand.setAttribute("name", "command");
    propCommand.setAttribute("value", cmd);

    ele.addContent(propName);
    ele.addContent(propAddr);
    ele.addContent(propPort);
    ele.addContent(propCommand);


    if (timeout >= 0)
    {
      Element propTimeout = new Element("property");
      propTimeout.setAttribute("name", "timeout");
      propTimeout.setAttribute("value", "" + timeout);

      ele.addContent(propTimeout);
    }

    return (TelnetCommand) builder.build(ele);
  }

  private TelnetCommand getCommand(String address, int port, String cmd)
  {
    return getCommand(address, port, cmd, -1);
  }


  // Nested Classes -------------------------------------------------------------------------------


  /**
   * A simple TCP server that can be used for telnet testing.
   */
  private static class Server
  {
    private final static Logger log =
        Logger.getLogger(TelnetCommandBuilder.TELNET_PROTOCOL_LOG_CATEGORY);


    private int port;
    private ServerSocket server;
    private ServerThread serverListeningThread = null;
    private Thread t;

    private Server(int port)
    {
      this.port = port;
    }


    /**
     * Starts the server on configured port.
     *
     * @param requestResponse mapping of incoming requests to responses.
     * @param responseDelay   if it is desired to add artificial response delay to socket reader
     *                        responses in this server for testing purposes
     *
     * @throws IOException  if things break
     */
    private void start(Map<String, String> requestResponse, String welcomeMessage, int responseDelay)
        throws IOException
    {
      server = new ServerSocket(port);

      log.info("Telnet server at port {0} starting...", port);

      serverListeningThread = new ServerThread(server, requestResponse, welcomeMessage, responseDelay);
      t = new Thread(serverListeningThread);
      t.start();

      log.info("Telnet server at port {0} started.", port);
    }

    private void start(Map<String, String> requestResponse) throws IOException
    {
      start(requestResponse, null, 0);
    }


    /**
     * Try to do a clean stop of the server.
     *
     * @throws Exception  if things break
     */
    private void stop() throws Exception
    {
      Logger log = Logger.getLogger(TelnetCommandBuilder.TELNET_PROTOCOL_LOG_CATEGORY);

      log.info("Telnet server at port {0} closing.", port);

      serverListeningThread.stop();

      // give server listening thread a little while to clean up before attempting to interrupt...

      Thread.sleep(250);

      // interrupt the server listening thread...

      t.interrupt();

      // give the server listening thread a little while to manage interrupt before forcing
      // server socket close...

      Thread.sleep(250);

      server.close();
    }
  }


  /**
   * The server reader thread that listens on the port, accepts sockets and spawns readers
   * for the sockets.
   */
  private static class ServerThread implements Runnable
  {
    volatile boolean serverRunning = true;
    private ServerSocket server;
    private Map<String, String> requestResponse = null;
    private Map<SocketReader, Thread> socketThreads = new HashMap<SocketReader, Thread>();
    private Set<Socket> socketReaders = new HashSet<Socket>();
    private int responseDelay;
    private String welcomeMessage;

    ServerThread(ServerSocket server, Map<String, String> requestResponse, String welcomeMessage,
                 int socketResponseDelay)
    {
      this.server = server;
      this.requestResponse = requestResponse;
      this.responseDelay = socketResponseDelay;
      this.welcomeMessage = welcomeMessage;
    }

    /**
     * Spawn a socket reader for incoming connections.
     */
    public void run()
    {
      try
      {
        while (serverRunning)
        {
          Socket socket = server.accept();

          SocketReader socketReader = new SocketReader(
              this, socket, requestResponse, welcomeMessage, responseDelay
          );

          Thread t = new Thread(socketReader);
          t.start();

          socketThreads.put(socketReader, t);
          socketReaders.add(socket);
        }
      }

      catch (Exception e)
      {
        System.err.println("Server at port " + server.getLocalPort() + " failed: " + e.getMessage());
      }
    }

    /**
     * Try to do a clean shutdown on existing socket reader threads.
     *
     * @throws Exception  if things break
     */
    public void stop() throws Exception
    {
      serverRunning = false;

      for (SocketReader reader : socketThreads.keySet())
      {
        reader.stop();
      }

      Thread.sleep(250);

      for (SocketReader reader : socketThreads.keySet())
      {
        Thread t = socketThreads.get(reader);

        t.interrupt();
      }

      Thread.sleep(250);

      for (Socket s : socketReaders)
      {
        close(s);
      }
    }

    private void close(Socket s)
    {
      try
      {
        s.close();
      }

      catch (Exception e)
      {
        System.err.println("Can't close socket: " + e.getMessage());
      }
    }

    private void cleanUp(SocketReader reader, Socket s)
    {
      close(s);

      socketReaders.remove(s);

      socketThreads.remove(reader);
    }

  }


  /**
   * Socket reader and response thread.
   */
  private static class SocketReader implements Runnable
  {
    private final static Logger log = Logger.getLogger(TelnetCommandBuilder.TELNET_PROTOCOL_LOG_CATEGORY);

    volatile boolean socketReading = true;
    private Socket socket = null;
    private ServerThread server = null;
    private Map<String, String> requestResponse = null;
    private int responseDelay = 0;
    private String welcomeMessage = null;

    SocketReader(ServerThread server, Socket socket, Map<String, String> requestResponse,
                 String welcomeMessage, int responseDelay)
    {
      this.socket = socket;
      this.server = server;
      this.requestResponse = requestResponse;
      this.responseDelay = responseDelay;
      this.welcomeMessage = welcomeMessage;
    }


    @Override public void run()
    {
      try
      {
        BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
        BufferedInputStream in = new BufferedInputStream(socket.getInputStream());

        if (hasWelcomeMessage())
        {
          send(out, welcomeMessage);
        }

        while (socketReading)
        {
          BufferedReader bin = new BufferedReader(new InputStreamReader(in));
          String cmd = bin.readLine();

          if (cmd == null)
          {
            socketReading = false;
            break;
          }

          System.out.println(
              "Telnet server at port " + server.server.getLocalPort() + " received: '" + cmd + "'"
          );

          respond(out, cmd);
        }
      }

      catch (InterruptedException e)
      {
        System.out.println("Socket reader has been interrupted. Shutting down...");
      }

      catch (Exception e)
      {
        System.err.println("Socket reader failed: " + e.getMessage());
      }

      finally
      {
        server.cleanUp(this, socket);
      }
    }

    public void stop()
    {
      socketReading = false;
    }

    public void send(OutputStream out, String msg) throws InterruptedException, IOException
    {
      if (msg == null || msg.equals(""))
      {
        System.err.println("Telnet server attempted to send an empty message.");

        return;
      }

      if (hasResponseDelay())
      {
        Thread.sleep(responseDelay);
      }

      PrintStream print = new PrintStream(new BufferedOutputStream(out));

      print.write(msg.getBytes());
      print.flush();

      System.out.println(
          "Telnet server at port " + server.server.getLocalPort() + " sent: '" + msg + "'"
      );
    }

    public void respond(OutputStream out, String cmd) throws InterruptedException
    {
      String response = requestResponse.get(cmd.trim());


      if (response != null)
      {
        try
        {
          send(out, response);
        }

        catch (IOException e)
        {
          System.err.println("Socket reader response I/O failed: " + e.getMessage());
        }
      }

      else
      {
        System.err.println("\n\n*** Telnet server did not find a matching response to : '" + cmd + "'\n");
      }
    }

    private boolean hasWelcomeMessage()
    {
      return welcomeMessage != null && !welcomeMessage.equals("");
    }

    private boolean hasResponseDelay()
    {
      return responseDelay > 0;
    }
  }




}
