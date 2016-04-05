package org.openremote.controller.protocol.port.pad;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import org.openremote.controller.Constants;
import org.openremote.controller.protocol.port.PortException;

class PadClient {
   public final static String PAD_LOG_CATEGORY = Constants.CONTROLLER_PROTOCOL_LOG_CATEGORY + "pad";
   private static PadClient INSTANCE = new PadClient();
   public static final int SERVICE_TIMEOUT = 1000;
   private Socket socket;
   private Object serviceLock, listenerLock;
   private AckMessage response;
   private PadServerListener padServerListener;
   private Map<String, PortListener> portListeners;

   static PadClient instance() {
      return INSTANCE;
   }

   private PadClient() {
      this.serviceLock = new Object();
      this.listenerLock = new Object();
      this.portListeners = new HashMap<String, PortListener>();
   }

   // private void disconnect() {
   // if (this.socket != null && this.socket.isConnected()) {
   // try {
   // this.socket.close();
   // } catch (IOException e) {
   // // Nothing to do
   // }
   // }
   // this.socket = null;
   // if (this.padServerListener != null) {
   // if (this.padServerListener.isAlive()) {
   // this.padServerListener.interrupt();
   // try {
   // this.padServerListener.join();
   // } catch (InterruptedException e) {
   // // Nothing to do
   // }
   // }
   // this.padServerListener = null;
   // }
   // }

   private void connect() throws IOException {
      if (this.socket != null && this.socket.isConnected() == false) {
         this.socket = null;
      }
      if (this.socket == null) {
         try {
            this.socket = new Socket("localhost", 7876);
            this.padServerListener = new PadServerListener();
            synchronized (this.listenerLock) {
               this.padServerListener.start();
               try {
                  this.listenerLock.wait();
               } catch (InterruptedException e) {
                  // Nothing to do
               }
            }
         } catch (UnknownHostException e) {
            // Cannot happen
         }
      }
   }

   void service(PadMessage m) throws IOException, PortException {
      this.connect();
      try {
         synchronized (this.serviceLock) {
            this.response = null;
            m.write(this.socket.getOutputStream());
            this.serviceLock.wait(SERVICE_TIMEOUT);
            if (this.response == null) throw new PortException(PortException.SERVICE_TIMEOUT);
            AckMessage r = (AckMessage) this.response;
            int v = r.getValue();
            if (v != 0) throw new PortException(PortException.SERVICE_FAILED, v);
         }
      } catch (InterruptedException e) {
         // Nothing more to do
      }
   }

   synchronized void registerPortListener(String portId, PortListener listener) throws PortException {
      if (this.portListeners.get(portId) == null) {
         this.portListeners.put(portId, listener);
      } else {
         throw new PortException(PortException.ALREADY_LISTENING);
      }
   }

   synchronized void unregisterPortListener(String portId) {
      this.portListeners.remove(portId);
   }

   private PadMessage read() throws Exception {
      return PadMessage.read(this.socket.getInputStream());
   }

   private class PadServerListener extends Thread {
      public PadServerListener() {
         super("PadServerListener");
      }

      @Override
      public void run() {
         synchronized (PadClient.this.listenerLock) {
            PadClient.this.listenerLock.notify();
         }
         while (!this.isInterrupted()) {
            try {
               PadMessage r = PadClient.this.read();
               switch (r.getCode()) {
               case AckMessage.CODE:
                  synchronized (PadClient.this.serviceLock) {
                     PadClient.this.response = (AckMessage) r;
                     PadClient.this.serviceLock.notify();
                  }
                  break;
               case NotifyMessage.CODE:
                  NotifyMessage n = (NotifyMessage) r;
                  for (String portId : PadClient.this.portListeners.keySet()) {
                     if (portId.equals(n.getPortId())) {
                        PadClient.this.portListeners.get(portId).notifyMessage(n);
                     }
                  }

                  // Send ACK
                  new AckMessage(0).write(PadClient.this.socket.getOutputStream());
                  break;
               default:
                  // TODO
               }

               // Handle message
            } catch (IOException x) {
               try {
                  PadClient.this.socket.close();
               } catch (IOException e) {
                  e.printStackTrace();
               }
               this.interrupt();
            } catch (Exception x) {
               System.out.println("Caught " + x + ", " + x.getMessage());
               x.printStackTrace();
            }
         }
         System.out.println("reader stopped");
      }
   }
}
