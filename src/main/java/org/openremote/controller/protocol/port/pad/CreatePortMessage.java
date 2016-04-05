package org.openremote.controller.protocol.port.pad;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CreatePortMessage extends PadMessage {
   private String portId;
   private String portType;

   public CreatePortMessage(String portId, String portType) {
      this.portId = portId;
      this.portType = portType;
   }

   @Override
   protected byte getCode() {
      return 'O';
   }

   @Override
   protected void writeBody(OutputStream os) throws IOException {
      PadMessage.writeString(os, this.portId);
      PadMessage.writeString(os, this.portType);
   }

   @Override
   protected void readBody(InputStream is) {
      // TODO 
   }
}
