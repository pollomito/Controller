/*
 * OpenRemote, the Home of the Digital Home. Copyright 2008-2011, OpenRemote Inc.
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
package org.openremote.controller;

import org.openremote.controller.service.ServiceContext;

/**
 * Note that password authentication is not supported at this time (as the password encryption algorithm is proprietary)
 * 
 * @author Eric Bariaux
 */
public class DomintellConfig extends Configuration {

   // Constants ------------------------------------------------------------------------------------

   public final static String DOMINTELL_ADDRESS = "domintell.address";
   public final static String DOMINTELL_PORT = "domintell.port";

   // Class Members --------------------------------------------------------------------------------

   public static DomintellConfig readXML() {
      DomintellConfig config = ServiceContext.getDomintellConfiguration();

      return (DomintellConfig) Configuration.updateWithControllerXMLConfiguration(config);
   }

   // Instance Fields ------------------------------------------------------------------------------

   private String address;
   private int port;

   // Public Instance Methods ----------------------------------------------------------------------

   public String getAddress() {
      return preferAttrCustomValue(DOMINTELL_ADDRESS, address);
   }

   public void setAddress(String address) {
      this.address = address;
   }

   public int getPort() {
      return preferAttrCustomValue(DOMINTELL_PORT, port);
   }

   public void setPort(int port) {
      this.port = port;
   }

}
