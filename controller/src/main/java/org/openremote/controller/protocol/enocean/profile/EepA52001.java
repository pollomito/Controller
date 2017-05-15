package org.openremote.controller.protocol.enocean.profile;

import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.protocol.enocean.ConfigurationException;
import org.openremote.controller.protocol.enocean.ConnectionException;
import org.openremote.controller.protocol.enocean.DeviceID;
import org.openremote.controller.protocol.enocean.RadioInterface;
import org.openremote.controller.protocol.enocean.packet.radio.EspRadioTelegram;

public class EepA52001 implements EepTransceive {

   public EepA52001(DeviceID deviceID, String command) {
      // TODO Auto-generated constructor stub
   }

   @Override
   public boolean update(EspRadioTelegram telegram) {
      // TODO Auto-generated method stub
      return false;
   }

   @Override
   public void updateSensor(Sensor sensor) throws ConfigurationException {
      // TODO Auto-generated method stub

   }

   @Override
   public void send(RadioInterface radioInterface) throws ConfigurationException, ConnectionException {
      // TODO Auto-generated method stub

   }

   @Override
   public EepType getType() {
      // TODO Auto-generated method stub
      return null;
   }

}
