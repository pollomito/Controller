package org.openremote.controller.protocol.port.pad;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class LockMessage extends PadMessage {
   private String portId;
   private String sourceId;

   public LockMessage(String portId, String sourceId) {
      this.portId = portId;
      this.sourceId = sourceId;
   }

   @Override
   protected byte getCode() {
      return 'L';
   }

   @Override
   protected void writeBody(OutputStream os) throws IOException {
      PadMessage.writeString(os, this.portId);
      PadMessage.writeString(os, this.sourceId);
   }

   @Override
   protected void readBody(InputStream is) {
      // Nothing to do
   }
}
