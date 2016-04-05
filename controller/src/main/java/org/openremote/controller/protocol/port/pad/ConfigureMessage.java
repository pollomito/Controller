package org.openremote.controller.protocol.port.pad;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class ConfigureMessage extends PadMessage {
   public static final byte CODE = 'C';
   private String portId;
   private Map<String, String> config;

   public ConfigureMessage() {
      this.config = new HashMap<String, String>();
   }

   public ConfigureMessage(String portId) {
      this();
      this.portId = portId;
   }

   @Override
   protected byte getCode() {
      return CODE;
   }

   @Override
   protected void writeBody(OutputStream os) throws IOException {
      PadMessage.writeString(os, this.portId);
      PadMessage.writeUint32(os, this.config.size());
      for (String key : this.config.keySet()) {
         PadMessage.writeString(os, key);
         PadMessage.writeString(os, this.config.get(key));
      }
   }

   @Override
   protected void readBody(InputStream is) throws IOException {
      // Nothing to do
   }

   public String getPortId() {
      return this.portId;
   }

   public void addConfig(String key, String value) {
      this.config.put(key, value);
   }
}
