/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2015, OpenRemote Inc.
 *
 * See the contributors.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.controller;

import org.openremote.controller.service.ServiceContext;

/**
 * Class that is used to bring together Velbus communication configuration parameters from
 * the properties file velbus.properties and the deployed controller configuration file
 * controller.xml.
 *
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class VelbusConfiguration extends Configuration {

   // Constants ------------------------------------------------------------------------------------

   /**
    * Name of the Velbus property that is used to configure the hostname(s) or IP address(es) of the server.
    */
   public static final String VELBUS_SERVER_HOSTNAMES = "velbus.server.hostnames";

   /**
    * Name of the Velbus property that configures the port(s) of the server.
    */
   public static final String VELBUS_SERVER_PORTS = "velbus.server.ports";

   // Class Members --------------------------------------------------------------------------------

   /**
    * Updates the Velbus configuration with Velbus configuration settings from the designer
    * (controller.xml) and returns the updated Velbus configuration.
    *
    * @return  updated Z-Wave configuration
    */
   public static VelbusConfiguration readXML()
   {
      VelbusConfiguration config = ServiceContext.getVelbusConfiguration();

     return (VelbusConfiguration)Configuration.updateWithControllerXMLConfiguration(config);
   }


   // Private Instance Fields ----------------------------------------------------------------------

   /**
    * Hostname(s) or IP address(es)
    */
   private String serverHostnames;

   /**
    * Port(s)
    */
   private String serverPorts;

   // Public Instance Methods ----------------------------------------------------------------------

   public String getServerHostnames()
   {
     return preferAttrCustomValue(VELBUS_SERVER_HOSTNAMES, serverHostnames);
   }

   public void setServerHostnames(String serverHostnames)
   {
     this.serverHostnames = serverHostnames;
   }

   public String getServerPorts()
   {
     return preferAttrCustomValue(VELBUS_SERVER_PORTS, serverPorts);
   }

   public void setServerPorts(String serverPorts)
   {
     this.serverPorts = serverPorts;
   }
}