package org.openremote.controller.protocol.port.pad;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class PingMessage extends PadMessage {
   public final static byte CODE = 'P';

   public PingMessage() {
   }

   @Override
   protected byte getCode() {
      return CODE;
   }

   @Override
   protected void writeBody(OutputStream os) throws IOException {
   }

   @Override
   protected void readBody(InputStream is) {
      // TODO
   }
}
