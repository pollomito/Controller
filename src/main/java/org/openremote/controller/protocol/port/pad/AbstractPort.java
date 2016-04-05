package org.openremote.controller.protocol.port.pad;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;

import org.openremote.controller.protocol.port.Message;
import org.openremote.controller.protocol.port.Port;
import org.openremote.controller.protocol.port.PortException;
import org.openremote.controller.utils.Logger;

public class AbstractPort implements Port, PortListener {
   private final static Logger log = Logger.getLogger(PadClient.PAD_LOG_CATEGORY);
   public final static String PORT_ID = "portId";
   public final static String PORT_TYPE = "portType";
   public final static String PORT_SPEED = "speed";
   public final static String PORT_NB_BITS = "nbBits";
   public final static String PORT_PARITY = "parity";
   public final static int PORT_RX_DATA_HAND_OFF_TIMEOUT = 500;
   private PadClient padClient;
   private SynchronousQueue<NotifyMessage> queue = new SynchronousQueue<NotifyMessage>();
   private String portId;
   private String portType;

   public AbstractPort() {
      this.padClient = PadClient.instance();
      this.portId = null;
      this.portType = null;
   }

   @Override
   public void configure(Map<String, Object> configuration) throws IOException, PortException {
      Object o1 = configuration.get(PORT_ID);
      Object o2 = configuration.get(PORT_TYPE);
      if (!(o1 instanceof String) || !(o2 instanceof String)) {
         throw new PortException(PortException.INVALID_CONFIGURATION);
      }
      if (this.portId != null) this.padClient.unregisterPortListener(this.portId);
      this.portId = (String) o1;
      this.portType = (String) o2;
      if (this.portId == null || this.portId.equals("") || this.portType == null || this.portType.equals("")) {
         throw new PortException(PortException.INVALID_CONFIGURATION);
      }
      this.padClient.registerPortListener(this.portId, this);
      try {
         this.padClient.service(new CreatePortMessage(this.portId, this.portType));
      } catch (PortException x) {
         if (!(x.getCode() == PortException.SERVICE_FAILED && x.getRootCode() == -5)) throw x;
      }

      // Configure port
      ConfigureMessage m = new ConfigureMessage(portId);
      for (String key : configuration.keySet()) {
         m.addConfig(key, configuration.get(key).toString());
      }
      this.padClient.service(m);
   }

   @Override
   public void start() throws IOException, PortException {
      this.padClient.service(new LockMessage(this.portId, "t"));
   }

   @Override
   public void stop() throws IOException, PortException {
      this.padClient.service(new UnlockMessage(this.portId, "t"));
   }

   @Override
   public void send(Message message) throws IOException, PortException {
      this.padClient.service(new NotifyMessage(this.portId, message.getContent()));
   }

   @Override
   public Message receive() throws IOException {
      NotifyMessage notification = null;

      try
      {
        notification = queue.take();
      }

      catch (InterruptedException e)
      {
        Thread.currentThread().interrupt();
      }

      return (notification != null ? notification.getMessage() : null);
   }

   @Override
   public void notifyMessage(NotifyMessage notification) {
      if (notification == null)
      {
        return;
      }

      try
      {
        boolean handOff = queue.offer(
            notification, PORT_RX_DATA_HAND_OFF_TIMEOUT,
            TimeUnit.MILLISECONDS
        );

        if (!handOff)
        {
          log.error(
              "Discarded received data from port ''{0}'' " +
              "because of internal timeout error.", this.portId
          );
        }
      }

      catch (InterruptedException e)
      {
        Thread.currentThread().interrupt();
      }
   }
}
