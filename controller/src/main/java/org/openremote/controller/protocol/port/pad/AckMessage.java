package org.openremote.controller.protocol.port.pad;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class AckMessage extends PadMessage {
   public static final byte CODE = 'A';
   private int value;

   public AckMessage() {
      this.value = 0;
   }

   public AckMessage(int value) {
      this.value = value;
   }

   public int getValue() {
      return this.value;
   }

   @Override
   protected byte getCode() {
      return CODE;
   }

   @Override
   protected void writeBody(OutputStream os) throws IOException {
      PadMessage.writeUint32(os, this.value);
   }

   @Override
   public void readBody(InputStream is) throws IOException {
      this.value = PadMessage.readUint32(is);
   }
}
