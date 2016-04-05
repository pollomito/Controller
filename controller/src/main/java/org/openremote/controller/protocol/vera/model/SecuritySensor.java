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


public class SecuritySensor extends VeraDevice {

   protected Boolean armed;
   protected Boolean tripped;
   
   public SecuritySensor(VeraCategory category, int id, String name, VeraClient client) {
      super(category, id, name, client);
   }

   @Override
   protected void updateDeviceSpecificSensors() {
      if ((attachedSensors.get(VeraCmd.GET_ARMED) != null) && (armed != null)) {
         attachedSensors.get(VeraCmd.GET_ARMED).update(armed?"on":"off");
      }
      if ((attachedSensors.get(VeraCmd.GET_TRIPPED) != null) && (tripped != null)) {
         attachedSensors.get(VeraCmd.GET_TRIPPED).update(tripped?"on":"off");
      }
   }

   @Override
   protected void updateDeviceSpecificStatus(Element element) {
      if (element.getAttributeValue("armed") != null) {
         this.armed = (element.getAttributeValue("armed").equals("1"))?true:false;
      }
      if (element.getAttributeValue("tripped") != null) {
         this.tripped = (element.getAttributeValue("tripped").equals("1"))?true:false;
      }
   }

}
