/*
 * OpenRemote, the Home of the Digital Home. Copyright 2008-2015, OpenRemote Inc.
 * 
 * See the contributors.txt file in the distribution for a full listing of individual contributors.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.openremote.controller.protocol.vera.model;

import org.jdom.Element;
import org.openremote.controller.protocol.vera.VeraClient;
import org.openremote.controller.protocol.vera.VeraCmd;


public class Switch extends VeraDevice {

   protected Boolean status;
   
   public Switch(VeraCategory category, int id, String name, VeraClient client) {
      super(category, id, name, client);
   }

   public void turnOn() {
      StringBuffer cmdUrl = new StringBuffer();
      cmdUrl.append("http://");
      cmdUrl.append(client.getAddress());
      cmdUrl.append(":3480/data_request?id=lu_action&output_format=xml&DeviceNum=");
      cmdUrl.append(id);
      cmdUrl.append("&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=1");
      getClient().sendCommand(cmdUrl.toString());
   }

   public void turnOff() {
      StringBuffer cmdUrl = new StringBuffer();
      cmdUrl.append("http://");
      cmdUrl.append(client.getAddress());
      cmdUrl.append(":3480/data_request?id=lu_action&output_format=xml&DeviceNum=");
      cmdUrl.append(id);
      cmdUrl.append("&serviceId=urn:upnp-org:serviceId:SwitchPower1&action=SetTarget&newTargetValue=0");
      getClient().sendCommand(cmdUrl.toString());
   }

   @Override
   protected void updateDeviceSpecificSensors() {
      if ((attachedSensors.get(VeraCmd.GET_STATUS) != null) && (status != null)) {
         attachedSensors.get(VeraCmd.GET_STATUS).update(status?"on":"off");
      }
   }

   @Override
   protected void updateDeviceSpecificStatus(Element element) {
      if (element.getAttributeValue("status") != null) {
         this.status = (element.getAttributeValue("status").equals("1"))?true:false;
      }
   }

}
