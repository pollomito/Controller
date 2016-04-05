/*
 * OpenRemote, the Home of the Digital Home. Copyright 2008-2011, OpenRemote Inc.
 * 
 * See the contributors.txt file in the distribution for a full listing of individual contributors.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.openremote.controller.protocol.knx.ip;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import org.openremote.controller.Constants;
import org.openremote.controller.protocol.knx.ip.KnxIpException.Code;
import org.openremote.controller.protocol.knx.ip.message.IpConnectResp;
import org.openremote.controller.protocol.knx.ip.message.IpConnectionStateResp;
import org.openremote.controller.protocol.knx.ip.message.IpDisconnectResp;
import org.openremote.controller.protocol.knx.ip.message.IpDiscoverResp;
import org.openremote.controller.protocol.knx.ip.message.IpMessage;
import org.openremote.controller.protocol.knx.ip.message.IpMessage.Primitive;
import org.openremote.controller.protocol.knx.ip.message.IpTunnelingAck;
import org.openremote.controller.protocol.knx.ip.message.IpTunnelingReq;
import org.openremote.controller.protocol.port.DatagramSocketMessage;
import org.openremote.controller.protocol.port.Message;
import org.openremote.controller.protocol.port.Port;
import org.openremote.controller.protocol.port.PortException;
import org.openremote.controller.protocol.port.PortFactory;
import org.openremote.controller.utils.Logger;

/**
 * IP message processor, able to :
 * <ul>
 * <li>send requests,</li>
 * <li>synchronize requests and responses,</li>
 * <li>handle incoming requests.</li>
 * </ul>
 */
class IpProcessor {
   /**
    * A common log category name intended to be used across all classes related to KNX implementation.
    */
   public final static String KNXIP_LOG_CATEGORY = Constants.CONTROLLER_PROTOCOL_LOG_CATEGORY + "knx.ip";

   private final static Logger log = Logger.getLogger(KNXIP_LOG_CATEGORY);
   private Object syncLock;
   private IpMessage con;
   private PhysicalBusListener busListener;
   private IpProcessorListener listener;
   private Port port;
   private DatagramSocket inSocket;
   private String physicalBusClazz;

   private class PhysicalBusListener extends Thread {

      public PhysicalBusListener(String name) {
         super("PhysicalBusListener for " + name);
      }

      @Override
      public void run() {
         synchronized (this) {
            this.notify();
         }
         while (!this.isInterrupted()) {
            try {
               Message b = IpProcessor.this.port.receive();
               // TODO Check sender address?

               // Create an IpMessage from received data
               IpMessage m = IpProcessor.this.create(new ByteArrayInputStream(b.getContent()));

               // Handle Discovery responses specifically
               if (m instanceof IpDiscoverResp) {
                  IpProcessor.this.listener.notifyMessage(m);
               } else {
                  // Handle other messages
                  if (m.getPrimitive() == Primitive.RESP) {

                     // Handle responses
                     synchronized (IpProcessor.this.syncLock) {
                        IpProcessor.this.con = m;
                        IpProcessor.this.syncLock.notify();
                     }
                  } else {
                     // Handle requests
                     IpProcessor.this.listener.notifyMessage(m);
                  }
               }
            } catch (IOException x) {
               // Socket read error, stop thread
               log.info("KNX-IP socket listener IOException", x);
               Thread.currentThread().interrupt();
            } catch (PortException x) {
               // Port error, stop thread
               log.info("KNX-IP socket listener PortException", x);
               Thread.currentThread().interrupt();
            }catch (KnxIpException x) {
               // Invalid message, stop thread
               log.warn("KNX-IP socket listener KnxIpException", x);
               Thread.currentThread().interrupt();
            }
         }
         log.warn("KNX-IP socket listener stopped");
      }
   }

   IpProcessor(IpProcessorListener listener, String physicalBusClazz) {
      this.syncLock = new Object();
      this.listener = listener;
      this.physicalBusClazz = physicalBusClazz;
   }

  void start(String src, InetAddress srcAddr, DatagramSocket outSocket) throws KnxIpException,
                                                                               IOException,
                                                                               PortException,
                                                                               InterruptedException
  {
    log.debug("Creating KNX bus with " + this.physicalBusClazz);

    this.port = PortFactory.createPhysicalBus(this.physicalBusClazz);
    this.inSocket = new DatagramSocket(new InetSocketAddress(srcAddr, 0));

    // Start bus
    Map<String, Object> cfg = new HashMap<String, Object>();
    cfg.put("inSocket", this.inSocket);
    cfg.put("outSocket", outSocket == null ? this.inSocket : outSocket);

    this.port.configure(cfg);
    this.port.start();
      
    // Start bus listener
    this.busListener = new PhysicalBusListener(src);

     //PortFactory.createPhysicalBus(this.physicalBusClazz);
    synchronized (this.busListener) {
         this.busListener.start();
         this.busListener.wait();
    }
  }

   InetSocketAddress getSrcSocketAddr() {
      return (InetSocketAddress) this.inSocket.getLocalSocketAddress();
   }

   void stop() throws PortException, IOException, InterruptedException {
      // Stop bus
      this.port.stop();

      // Stop IpListener
      this.busListener.interrupt();
      this.busListener.join();
   }

   synchronized IpMessage service(IpMessage message, InetSocketAddress destAddr) throws InterruptedException,
         IOException, PortException, KnxIpException {
      IpMessage out = null;
      synchronized (this.syncLock) {
         this.con = null;
         this.send(message, destAddr);
         long dt = 0;
         while (out == null && dt < message.getSyncSendTimeout()) {
            long st = System.currentTimeMillis();
            this.syncLock.wait(message.getSyncSendTimeout() - dt);
            dt += System.currentTimeMillis() - st;
            if (this.con != null && !(con instanceof IpDiscoverResp)) out = this.con;
         }
      }
      return out;
   }

   void send(IpMessage message, InetSocketAddress destAddr) throws IOException, PortException {
      this.send(message, destAddr, this.port);
   }

   private void send(IpMessage message, InetSocketAddress destAddr, Port bus) throws IOException, PortException {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      message.write(os);
      byte[] b = os.toByteArray();
      bus.send(new DatagramSocketMessage(destAddr, b));
   }

   private IpMessage create(InputStream is) throws IOException, KnxIpException {
      IpMessage out = null;

      // Check header is 0x06 0x10
      if ((is.read() != 0x06) || (is.read() != 0x10)) throw new KnxIpException(Code.invalidHeader,
            "Create message failed");

      // Extract Service Type Identifier
      int sti = (is.read() << 8) + is.read();

      // Extract message length
      int l = ((is.read() << 8) + is.read()) - 6;

      // Instantiate message
      switch (sti) {
      case IpConnectResp.STI:
         out = new IpConnectResp(is, l);
         break;
      case IpDisconnectResp.STI:
         out = new IpDisconnectResp(is, l);
         break;
      case IpDiscoverResp.STI:
         out = new IpDiscoverResp(is, l);
         break;
      case IpTunnelingAck.STI:
         out = new IpTunnelingAck(is, l);
         break;
      case IpTunnelingReq.STI:
         out = new IpTunnelingReq(is, l);
         break;
      case IpConnectionStateResp.STI:
         out = new IpConnectionStateResp(is, l);
         break;
      default:
         throw new KnxIpException(Code.unexpectedServiceType, "Could not create message");
      }
      return out;
   }
}
