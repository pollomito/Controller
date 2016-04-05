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

import org.openremote.controller.utils.Logger;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * An utility for tests that make or require TCP based connections to remote servers or TCP/IP
 * devices. Apart from simulating existing services, this class can implement typical error
 * behaviors that the client side should be able to manage.
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class TCPTestServer
{

  // Constants ------------------------------------------------------------------------------------

  /**
   * Default server name if nothing is specified.
   */
  protected final static String DEFAULT_SERVER_NAME = "test server";

  /**
   * Server socket timeout (in milliseconds). How long the server blocks on accept() before
   * a SocketTimeoutException is raised.
   */
  protected final static int SERVER_SOCKET_TIMEOUT = 10000;

  /**
   * Client socket timeout (in milliseconds). How long the reader waits for client input
   * before a SocketTimeoutException is raised.
   */
  protected final static int SOCKET_READER_TIMEOUT = 1000;


  // Class Members --------------------------------------------------------------------------------

  /**
   * Logger for test runs.
   */
  protected final static Logger log = Logger.getLogger(
      Constants.CONTROLLER_ROOT_LOG_CATEGORY + ".test.tcpserver"
  );


  // Instance Fields ------------------------------------------------------------------------------

  /**
   * The TCP port opened by this test server.
   */
  protected int port;

  /**
   * The listening socket at {@link #port}.
   */
  protected ServerSocket server;

  /**
   * The receiver implementation that can handle the incoming string requests from the client.
   */
  protected Receiver receiver;

  /**
   * Server name can be used to distinguish multiple server instances in log output.
   */
  protected String serverName = DEFAULT_SERVER_NAME;

  /**
   * The listening socket thread implementation.
   */
  private ServerThread serverListeningThread = null;

  /**
   * The listening thread at {@link #port}.
   */
  private Thread t;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new TCP server listening on given port and handling incoming string requests
   * with the given receiver implementation.
   *
   * @param port
   *          listening port
   *
   * @param receiver
   *          receiver implementation
   */
  public TCPTestServer(int port, Receiver receiver)
  {
    this.port = port;
    this.receiver = receiver;
  }

  /**
   * Constructs a new TCP server listening on given port and handling incoming string requests
   * with the given receiver implementation.
   *
   * @param port
   *          listening port
   *
   * @param receiver
   *          receiver implementation
   *
   * @param serverName
   *          human readable name for this test server
   */
  public TCPTestServer(int port, Receiver receiver, String serverName)
  {
    this.port = port;
    this.receiver = receiver;
    this.serverName = serverName;
  }


  // Methods --------------------------------------------------------------------------------------

  /**
   * Starts the server on configured port.
   *
   * @throws java.io.IOException  if things break
   */
  public void start() throws IOException
  {
    server = createServerSocket();

    log.info("{0} at port {1} starting...", serverName, port);

    serverListeningThread = new ServerThread(server, receiver);
    t = new Thread(serverListeningThread);
    t.setName(serverName + " listening thread on port " + port);

    t.start();
  }


  /**
   * Try to do a clean stop of the server.
   */
  public void stop()
  {
    if (serverListeningThread == null)
    {
      // Apparently the server listening thread was never created, so we can just leave...

      return;
    }

    serverListeningThread.stop();

    log.info("Closed {0} at port {1}...", serverName, port);
  }


  /**
   * Socket factory method for subclasses to override to create different kinds of socket
   * implementations.
   *
   * @return  server socket for accepting connections
   *
   * @throws IOException  if anything breaks
   */
  protected ServerSocket createServerSocket() throws IOException
  {
    return new ServerSocket(port);
  }

  /**
   * Closes the server socket.
   */
  private void closeServer()
  {
    try
    {
      server.close();
    }

    catch (IOException ioe)
    {
      log.info("Error in closing server socket at port {0} : {1}", port, ioe.getMessage());
    }
  }


  // Inner Classes --------------------------------------------------------------------------------

  /**
   * The server reader thread that listens on the port, accepts sockets and spawns readers
   * for the sockets.
   */
  private class ServerThread implements Runnable
  {
    /**
     * Indicates that this thread is running...
     */
    private volatile boolean serverRunning = true;

    /**
     * The server socket this thread implementation is associated with.
     */
    private ServerSocket server;

    /**
     * The running socket reader threads. The server expects the reader to properly clean up via
     * {@link #finishReader(org.openremote.controller.TCPTestServer.SocketReader)}
     * call when the reader is finished.
     */
    private Map<SocketReader, Thread> socketThreads = new ConcurrentHashMap<SocketReader, Thread>();

    /**
     * The receiver implementation to manage incoming client requests.
     */
    private Receiver receiver;


    // Constructors -------------------------------------------------------------------------------

    /**
     * Implementation for the thread listening on the server socket.
     *
     * @param server    the server socket this thread implementation is associated with
     */
    private ServerThread(ServerSocket server, Receiver receiver)
    {
      this.server = server;
      this.receiver = receiver;

      try
      {
        server.setSoTimeout(SERVER_SOCKET_TIMEOUT);
      }

      catch (SocketException e)
      {
        log.warning("Cannot set listening server socket timeout, will block indefinitely...");
      }
    }


    /**
     * Spawn a socket reader for incoming connections.
     */
    public void run()
    {
      while (serverRunning)
      {
        Socket socket = null;

        try
        {
          // Spawn a new thread for each incoming connection attempt...

          socket = server.accept();
        }

        catch (SocketTimeoutException e)
        {
          if (serverRunning)
          {
            // We were interrupted by the SO_SOCKET_TIMEOUT -- checking if anything needs to be
            // done but since we are still 'running', just go ahead and continue...

            continue;
          }

          else
          {
            log.info(
                "{0} has been stopped, no longer accepting client sockets at port {1}",
                TCPTestServer.this.serverName, TCPTestServer.this.port
            );

            stop();

            break;
          }
        }

        catch (InterruptedIOException e)
        {
          // We've been interrupted by closing the server socket... time to clean up and get out...

          serverRunning = false;

          log.info("{0} has been stopped, closing client sockets...", serverName);

          stop();

          break;
        }

        catch (IOException e)
        {
          if (serverRunning)
          {
            log.info("Accepting client socket failed: {0}", e, e.getMessage());
          }

          else
          {
            break;
          }
        }


        log.info(
            "Accepting a new client connection on port {0}",
            (socket == null) ? "null" : socket.getLocalPort()
        );

        SocketReader socketReader = new SocketReader(this, socket, receiver);

        Thread t = new Thread(socketReader);
        t.setName("Socket Reader Port " + ((socket == null) ? "<unknown>" : socket.getPort()));

        // Keep track of running socket reader threads and open sockets for an orderly clean-up...

        socketThreads.put(socketReader, t);

        // Start the thread...

        t.start();
      }
    }

    /**
     * Try to do a clean shutdown on existing socket reader threads.
     */
    public void stop()
    {
      // Mark the server listening socket as stopped. The serverRunning flag is polled on a
      // frequency set via SO_SOCKET_TIMEOUT variable (or never if socket has been configured
      // to wait indefinitely) or if the socket accept loop is interrupted by closing the socket.

      serverRunning = false;

      // The thread interrupt in all likelihood will have no impact since Java doesn't allow
      // interrupts on blocking I/O. It would only have impact on synchronization blocking
      // which the current implementation doesn't do but you can always be prepared for
      // future changes...

      t.interrupt();

      // should the interrupt have an impact, give the server a little while to manage the
      // clean-up...

      try
      {
        Thread.sleep(100);
      }

      catch (InterruptedException e)
      {
        // If we get interrupted it means this calling thread itself is being asked to shut down,
        // so better just to bail out...

        Thread.currentThread().interrupt();

        log.info("Calling thread has been asked to shut down, no time to wait for clean up...");

        closeServer();

        closeClients();

        return;
      }

      // Close the server to raise a interrupted I/O exception that will allow us to clean up...

      closeServer();

      // Tell the client socket readers to stop as well -- these will also be checked as long as
      // the SOCKET_SO_TIMEOUT has been set...

      closeClients();
    }

    private void closeClients()
    {
      for (SocketReader reader : socketThreads.keySet())
      {
        reader.stop();
      }
    }


    /**
     * Callback from reader thread to server to let it know it can clean up the thread and socket.
     * Call when the reader thread is finished.
     *
     * @param reader
     *          the reader thread
     */
    private void finishReader(SocketReader reader)
    {
      // In case the thread was blocking on a non I/O related operations, interrupt it to
      // let it finish...

      Thread t = socketThreads.remove(reader);

      t.interrupt();
    }
  }


  // Nested Classes -------------------------------------------------------------------------------

  /**
   * Socket reader and response thread.
   */
  public static class SocketReader implements Runnable
  {

    /**
     * Indicates if this client socket thread should continue reading input from client.
     */
    private volatile boolean socketReading = true;

    /**
     * Client socket.
     */
    private Socket socket = null;

    /**
     * Reference to the listening server socket that spawned this client socket reader.
     */
    private ServerThread server = null;

    /**
     * Receiver to handle client's input strings.
     */
    private Receiver receiver = null;


    // Constructors -------------------------------------------------------------------------------

    /**
     * Constructs a client socket reader thread.
     *
     * @param server
     *          the server socket that spawned this client reader
     *
     * @param socket
     *          the client socket used for reading input
     *
     * @param receiver
     *          the receiver used to process the incoming client input
     */
    private SocketReader(ServerThread server, Socket socket, Receiver receiver)
    {
      this.socket = socket;
      this.server = server;
      this.receiver = receiver;

      try
      {
        this.socket.setSoTimeout(SOCKET_READER_TIMEOUT);
      }

      catch (SocketException e)
      {
        log.info("Cannot set socket timeout to a client socket, will block indefinitely...");
      }

    }


    // Implements Runnable ------------------------------------------------------------------------

    @Override public void run()
    {
      try
      {
        final BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
        final BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
        final BufferedReader bin = new BufferedReader(new InputStreamReader(in));
        final BufferedWriter bout = new BufferedWriter(new OutputStreamWriter(out));

        while (socketReading)
        {
          try
          {
            log.debug(Thread.currentThread().getName() + " waiting for next line...");

            String incoming = bin.readLine();

            if (incoming == null)
            {
              log.info("End of stream reached. Closing...");

              socketReading = false;
            }

            if (receiver != null && socketReading)
            {
              receiver.received(incoming, new Response()
              {
                @Override public void respond(String response)
                {
                  socketReading = false;

                  try
                  {
                    bout.write(response);
                    bout.flush();
                    bout.close();

                    log.info("RESPONSE: \n" + response);
                  }

                  catch (IOException e)
                  {
                    log.info("Writing response failed: {0}", e.getMessage());
                  }
                }
              });
            }
          }

          catch (SocketTimeoutException e)
          {
            // We were interrupted by SO_SOCKET_TIMEOUT so we can check if anything needs
            // to be done

            if (!socketReading)
            {
              log.info("Client socket reader closing...");

              break;
            }
          }

          catch (InterruptedIOException e)
          {
            // Socket or connection was closed, that's a hint for us to stop (we were interrupted)...

            socketReading = false;

            log.info("Client socket reader stopped...");

            break;
          }
        }
      }

      catch (IOException e)
      {
        if (socketReading)
        {
          log.error("Socket reader failed: {0}", e, e.getMessage());
        }
      }

      finally
      {
        stop();

        server.finishReader(this);
      }
    }

    public void stop()
    {
      // Mark the thread to be stopped...

      socketReading = false;

      // Give it a moment to finish in case we were not in a blocking wait loop on read() operation...

      try
      {
        Thread.sleep(100);
      }

      catch (InterruptedException e)
      {
        // If we get interrupted it means the calling thread has been asked to stop, so better
        // just to bail out...

        Thread.currentThread().interrupt();

        log.info("Shutting down in a hurry, no time to wait for a client socket to finish cleanly...");

        closeSocket();

        return;
      }

      // Close the socket to raise an I/O exception on the blocking read() call...

      closeSocket();
    }

    private void closeSocket()
    {
      try
      {
        socket.close();
      }

      catch (IOException e)
      {
        log.error("Failed to close client socket : {0}", e.getMessage());
      }
    }
  }

  public static interface Receiver
  {
    void received(String tcpString, Response response);
  }

  public static interface Response
  {
    void respond(String response);
  }

}
