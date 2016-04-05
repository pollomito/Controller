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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.openremote.controller.protocol.dscit100.Packet.PacketCallback;
import org.openremote.controller.protocol.dscit100.PanelState.State;
import org.openremote.controller.utils.Logger;

/**
 * Implements a DSC Connection over IP.
 *
 * @author Greg Rapp
 * @author Phil Taylor
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class IpConnection implements DSCIT100Connection
{
  // Class Members --------------------------------------------------------------------------------

  /**
   * DSC logger. Uses a common category for all DSC related logging.
   */
  private final Logger log = Logger.getLogger(DSCIT100CommandBuilder.DSC_LOG_CATEGORY);


  // Instance Fields ------------------------------------------------------------------------------

  private Socket socket = null;
  private PrintWriter out = null;
  private IpListener listener;
  protected PacketCallback packetCallback;

  /**
   * Connection credentials. Currently used by EnvisaLink gateway only. Set to null for IT-100.
   */
  private String credentials;


  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new DSC IP gateway connection with given connection credentials and Java socket.
   * Note that credentials are needed for EnvisaLink gateway only, IT-100 should use a null
   * credential.
   *
   * @param connectionCredentials
   *          connection password for EnvisaLink gateway, null for IT-100 gateway
   *
   * @param socket
   *          An IP socket
   */
  public IpConnection(String connectionCredentials, Socket socket)
  {
    this.credentials = connectionCredentials;
    this.socket = socket;

    listener = new IpListener(socket);

    try
    {
      this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    catch (IOException e)
    {
      log.error(
          "Error creating socket output stream for " + socket.getInetAddress().getHostAddress(), e
      );
    }
  }


  // Implements DSCIT100 Connection ---------------------------------------------------------------

  @Override public void send(ExecuteCommand command)
  {
    sendInternal(command.getPacket());
  }

  @Override public void send(Packet packet)
  {
    sendInternal(packet);
  }


  @Override public boolean isConnected()
  {
    // isConnected() will ALWAYS return true IF you have connected the socket.
    // isClosed() will return false ONLY if YOU have disconnected the socket.
    return socket.isConnected() & !socket.isClosed();
  }

  @Override public void close()
  {
    log.debug("Closing connection to " + socket.getInetAddress().getHostAddress());

    try
    {
      socket.close();
    }

    catch (IOException e)
    {
      log.warn("Error closing connection to " + socket.getInetAddress().getHostAddress(), e);
    }
  }

  @Override public String getAddress()
  {
    StringBuilder sb = new StringBuilder();

    sb.append(socket.getInetAddress().getHostAddress());
    sb.append(":");
    sb.append(socket.getPort());

    return sb.toString();
  }

  @Override public State getState(StateDefinition stateDefinition)
  {
    if (listener != null && listener.state != null)
    {
      return listener.state.getState(stateDefinition);
    }

    else
    {
      log.warn(
          "Unable to get connection listener or listener state database is unavailable for " +
          "connection to " + socket.getInetAddress().getHostAddress()
      );

      return null;
    }
  }



  // Private Methods ------------------------------------------------------------------------------

  private void sendInternal(Packet packet)
  {
    if (isConnected())
    {
      log.debug(
          "Sending data to address " + socket.getInetAddress().getHostAddress() + " : " +
          packet.toPacket()
      );

      if (packet.getCallback() != null)
      {
        this.packetCallback = packet.getCallback();
      }

      /* Replace out.println() with out.print() as println appends a "platform dependant" newline
        character which causes the EnvisaLink to generate a 502020 (API Format) error.
        Also need to flush the buffer (I suspect that println does that itself) ? */

      out.print(packet.toPacket());
      out.flush();

      if (out.checkError())
      {
        log.warn("Error in the underlying DSC socket print stream.");
      }
    }

    else
    {
      log.warn(
          "Could not send data to address " + socket.getInetAddress().getHostAddress() + " : " +
          packet.toPacket()
      );
    }
  }


  // Inner Classes --------------------------------------------------------------------------------

  /**
   * A listener Thread for an IpConnection
   */
  private class IpListener implements Runnable
  {
    private Thread thread;
    private Socket socket;
    private PanelState state;

    public IpListener(Socket socket)
    {
      this.socket = socket;
      this.state = new PanelState();

      thread = new Thread(this);
      thread.start();
    }

    @Override public void run()
    {
      log.info("Starting connection listener thread for " + socket.getInetAddress().getHostAddress());

      BufferedReader in = null;

      try
      {
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      }

      catch (IOException e)
      {
        log.error("I/O error creating reader socket for " + socket.getInetAddress().getHostAddress(), e);

        try
        {
          socket.close();
        }

        catch (Throwable t)
        {
          log.debug("Cannot close socket: {0}", t, t.getMessage());
        }

        return;
      }

      log.info("Starting read loop for " + socket.getInetAddress().getHostAddress());

      // Send IT100 state discovery packet to get current system state (not used for EnvisaLink)...

      if (credentials == null || credentials.equals(""))
      {
        // The First ever command seems to get lost by
        // my Ethernet->Serial adaptor!
        sendInternal(new Packet("000", ""));
        sendInternal(new Packet("001", ""));

        // Send IT100 labels request packet to get system labels...

        sendInternal(new Packet("002", ""));
      }

      boolean isConnected = true;
      String rawData;

      while (isConnected)
      {
        Packet packet = null;

        try
        {
          if ((rawData = in.readLine()) == null)
          {
            log.debug("Socket has disconnected.");

            isConnected = false;
          }

          else
          {
            log.debug(
                "Received data from {0}: ''{1}''.", socket.getInetAddress().getHostAddress(), rawData
            );

            packet = new Packet(rawData);
          }
        }

        catch (IOException e)
        {
          log.warn("Error parsing packet: {0}", e, e.getMessage());

          isConnected = false;
        }

        if (!isConnected)
        {
          try
          {
            socket.close();
          }

          catch (Throwable t)
          {
            log.debug("Unable to close socket: {0}", t, t.getMessage());
          }

          break;
        }

        try
        {
          if (packet != null)
          {
            /* 505 commands are sent by the EnvisaLink to request authentication
               As this isn't relevant to the panel state, process it here */

            if (packet.getCommand().equals("505"))
            {
              // Login interaction
              String num = packet.getData().substring(0, 1);

              if (num.equals("3"))
              {
                log.info("EnvisaLink: Login required");

                sendInternal(new Packet("005", credentials));
              }

              else if (num.equals("1"))
              {
                log.info("EnvisaLink: Login successful");

                sendInternal(new Packet("001", "")); // Ask for panel status.
              }

              else if (num.equals("0"))
              {
                log.error("EnvisaLink: Invalid password");

                isConnected = false;

                IpConnection.this.close();
              }

              else if (num.equals("2"))
              {
                log.error("EnvisaLink: connection timeout");

                isConnected = false;
              }
            }

            else
            {
              state.processPacket(packet);
            }

            if (!isConnected)
            {
              break;
            }

            if (packetCallback != null)
            {
              log.debug("Executing callback method for packet " + packet);

              packetCallback.receive(IpConnection.this, packet);
              packetCallback = null;
            }
          }
        }
        catch (Exception e)
        {
          log.warn("Error in packet", e);

          isConnected = false;
        }

      } // End of while loop

      // Connection has failed, close the socket so it can be recreated later...

      IpConnection.this.close();

      log.info(
          "Ending DSC connection listener thread for {0}", socket.getInetAddress().getHostAddress()
      );

    }
  }
}
