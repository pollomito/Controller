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

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

/**
 * An utility for tests that make or require secure (TLS) TCP based connections to remote servers
 * or TCP/IP devices.
 *
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class SecureTCPTestServer extends TCPTestServer
{

  // Class Members --------------------------------------------------------------------------------

  /**
   * Logger for test runs.
   */
  protected final static Logger log = Logger.getLogger(
      Constants.CONTROLLER_ROOT_LOG_CATEGORY + ".test.tcpserver.secure"
  );


  // Instance Fields ------------------------------------------------------------------------------

  private PrivateKey privateKey;

  private Certificate certificate;



  // Constructors ---------------------------------------------------------------------------------

  /**
   * Constructs a new SSL TCP server listening on given port and handling incoming string requests
   * with the given receiver implementation.
   *
   * @param port
   *          listening port
   *
   * @param receiver
   *          receiver implementation
   */
  public SecureTCPTestServer(int port, PrivateKey key, Certificate cert, Receiver receiver)
  {
    this(port, key, cert, receiver, DEFAULT_SERVER_NAME);
  }

  /**
   * Constructs a new SSL TCP server listening on given port and handling incoming string requests
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
  public SecureTCPTestServer(int port, PrivateKey key, Certificate cert, Receiver receiver, String serverName)
  {
    super(port, receiver, serverName);

    this.privateKey = key;
    this.certificate = cert;
  }


  // Methods --------------------------------------------------------------------------------------

  @Override protected ServerSocket createServerSocket() throws IOException
  {
    try
    {
      KeyManager[] keyManagers = createKeyManager();

      SSLContext ssl = SSLContext.getInstance("TLS");

      ssl.init(
          keyManagers,
          null,         // no client cert authentication, no trust stores needed
          null          // use default secure random implementation
      );

      SSLServerSocketFactory sslSocketFactory = ssl.getServerSocketFactory();

      return sslSocketFactory.createServerSocket(port);
    }

    catch (Throwable t)
    {
      throw new IOException(t);
    }
  }


  private KeyManager[] createKeyManager() throws NoSuchAlgorithmException, KeyStoreException,
                                                 CertificateException, IOException,
                                                 UnrecoverableKeyException
  {
    KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");

    KeyStore ks = KeyStore.getInstance("JCEKS");
    ks.load(null, "testkey".toCharArray());

    ks.setKeyEntry("testkey", privateKey, "testkey".toCharArray(), new Certificate[] { certificate });

    kmf.init(ks, "testkey".toCharArray());

    return kmf.getKeyManagers();
  }

}
