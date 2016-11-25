/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2014, OpenRemote Inc.
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
package org.openremote.controller.service;

import io.netty.bootstrap.Bootstrap;
import org.openremote.controller.Constants;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.utils.Logger;

import javax.net.ssl.SSLException;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.URISyntaxException;
import java.util.Enumeration;


public class BeehiveCommandCheckService {

   public static String getMACAddresses() throws Exception {
      StringBuilder macs = new StringBuilder();
      Enumeration<NetworkInterface> enum1 = NetworkInterface.getNetworkInterfaces();

      while (enum1.hasMoreElements()) {
         NetworkInterface networkInterface = enum1.nextElement();

         if (!networkInterface.isLoopback()) {
            boolean onlyLinkLocal = true;

            for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
               if (!interfaceAddress.getAddress().isLinkLocalAddress()) {
                  onlyLinkLocal = false;
               }
            }

            if (onlyLinkLocal) {
               continue;
            }

            byte[] mac = networkInterface.getHardwareAddress();

            if (mac != null) {
               macs.append(getMACString(networkInterface.getHardwareAddress()));
               macs.append(",");
            }
         }
      }

      if (macs.length() == 0) {
         return "no-mac-address-found";
      }

      macs.deleteCharAt(macs.length() - 1);

      return macs.toString();
   }

   private static String getMACString(byte[] mac) {
      StringBuilder sb = new StringBuilder();

      for (int i = 0; i < mac.length; i++) {
         sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
      }

      return sb.toString();
   }

   // Class Members --------------------------------------------------------------------------------

   /**
    * Log for this service.
    */
   private final static Logger log = Logger.getLogger(Constants.BEEHIVE_COMMAND_CHECKER_LOG_CATEGORY);


   public static void start(Deployer deployer, ControllerConfiguration config) {
      try {
         Bootstrap bootstrap = WebSocketClient.configureBootstrap(new Bootstrap(), deployer, config);
         bootstrap.connect();
      } catch (URISyntaxException e) {
         log.error("Error starting WS",e);
      } catch (SSLException e) {
         log.error("Error starting WS",e);
      } catch (Deployer.PasswordException e) {
         log.error("Error starting WS",e);
      }
   }

}




