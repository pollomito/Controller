package org.openremote.controller.protocol.port.pad;

import org.openremote.controller.protocol.port.PortException;

public class PadMessageFactory {
   public static PadMessage create(byte code) throws PortException {
      switch (code) {
      case AckMessage.CODE:
         return new AckMessage();
      case NotifyMessage.CODE:
         return new NotifyMessage();
      }
      
      throw new PortException(PortException.INVALID_MESSAGE);
   }
}
