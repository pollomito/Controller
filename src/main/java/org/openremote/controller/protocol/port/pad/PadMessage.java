package org.openremote.controller.protocol.port.pad;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.openremote.controller.protocol.port.PortException;

public abstract class PadMessage {
   private byte VERSION = 'a';

   public void write(OutputStream os) throws IOException {
      // Using a temporary buffer to make sure all data is sent in a single TCP packet
      // to optimize exchanges and ensure data interpretation
      ByteArrayOutputStream o = new ByteArrayOutputStream();
      this.writeHeader(o);
      this.writeBody(o);
      os.write(o.toByteArray());
   }

   protected void writeHeader(OutputStream os) throws IOException {
      os.write(VERSION);
      os.write(this.getCode());
   }

   protected abstract byte getCode();

   protected abstract void writeBody(OutputStream os) throws IOException;

   protected abstract void readBody(InputStream is) throws IOException;

   public static void writeString(OutputStream os, String s) throws IOException {
      PadMessage.writeUint16(os, s.length());
      os.write(s.getBytes());
   }

   public static String readString(InputStream is) throws IOException {
      int len = readUint16(is);
      byte buf[] = new byte[len];
      is.read(buf);
      return new String(buf);
   }

   public static void writeUint16(OutputStream os, int i) throws IOException {
      os.write(String.format("%1$04X", i & 0xFFFF).getBytes());
   }

   public static int readUint16(InputStream is) throws IOException {
      byte buf[] = new byte[4];
      is.read(buf);
      return Integer.parseInt(new String(buf), 16);
   }

   public static void writeUint32(OutputStream os, int i) throws IOException {
      os.write(String.format("%1$04X", (i >> 16) & 0xFFFF).getBytes());
      os.write(String.format("%1$04X", i & 0xFFFF).getBytes());
   }

   public static int readUint32(InputStream is) throws IOException {
      byte buf[] = new byte[8];
      is.read(buf);
      return (int) Long.parseLong(new String(buf), 16);
   }

   public static void writeOctetString(OutputStream os, byte[] s) throws IOException {
      PadMessage.writeUint16(os, s.length * 2);
      for (int i = 0; i < s.length; ++i) {
         os.write(String.format("%1$02X", s[i] & 0xFF).getBytes());
      }
   }

   public static byte[] readOctetString(InputStream is) throws IOException {
      int len = readUint16(is);
      byte buf[] = new byte[2];
      byte out[] = new byte[len / 2];
      for (int i = 0; i < len / 2; ++i) {
         is.read(buf);
         out[i] = (byte) Integer.parseInt(new String(buf), 16);
      }
      return out;
   }

   public static PadMessage read(InputStream is) throws IOException, PortException {
      int v = is.read();
      if (v < 0) throw new IOException();
      if (v != 'a') throw new PortException(PortException.INVALID_MESSAGE);
      int c = is.read();
      PadMessage m = PadMessageFactory.create((byte) c);
      m.readBody(is);
      return m;
   }
}
