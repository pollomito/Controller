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
package org.openremote.controller.utils;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.openremote.controller.ControllerConfiguration;

/**
 * This class is used to provide utility method about network.
 *
 * @author Javen
 */
public class NetworkUtil
{
  public static final Logger logger = Logger.getLogger(NetworkUtil.class);
  private static ControllerConfiguration configuration = null;   

  private NetworkUtil(){}
   
  /**
   * This method resolves local host IP address. If resolution fails , "localhost" will be returned.
   *
   * @return IP address string of local host
   */
  public static String getLocalhostIP()
  {
    String ip = "localhost";

    if (Strings.toUpperCase(System.getProperty("os.name")).contains("WINDOWS"))
    {
      ip = getLocalHostFromWindows();
    }

    else
    {
      ip = getLocalHostFromLinux();
    }

    return ip;
  }
   
   private static String getLocalHostFromWindows(){
      String ip = getConfiguration().getWebappIp();
      if ((ip != null) && (!ip.isEmpty())) {
          return ip;
      }
      InetAddress addr = null;
      try {
         addr = InetAddress.getLocalHost();
      } catch (UnknownHostException e) {
         logger.error("Can't get Network Interfaces", e);
      }
      byte[] ipAddr = addr.getAddress();
      StringBuffer ipAddrStr = new StringBuffer();
      for (int i = 0; i < ipAddr.length; i++) {
         if (i > 0) {
            ipAddrStr.append(".");
         }
         ipAddrStr.append(ipAddr[i] & 0xFF);
      }
      return ipAddrStr.toString();
   }
   
   private synchronized static ControllerConfiguration getConfiguration() {
      if (configuration == null) {
         configuration = ControllerConfiguration.readXML();
      }
      return configuration;
   }

   private static String getLocalHostFromLinux(){
      String ip = getConfiguration().getWebappIp();
      if ((ip != null) && (!ip.isEmpty())) {
          return ip;
      }
      try {
         Enumeration<?> interfaces = (Enumeration<?>) NetworkInterface.getNetworkInterfaces();
         while (interfaces.hasMoreElements()) {
            NetworkInterface ni = (NetworkInterface) interfaces.nextElement();
            if (ni.isUp() && ni.supportsMulticast() && !ni.isLoopback()) {
               Enumeration<?> addresses = ni.getInetAddresses();
               while (addresses.hasMoreElements()) {
                  InetAddress ia = (InetAddress) addresses.nextElement();
                  if (ia instanceof Inet6Address) {
                     continue;
                  }
                  ip = ia.getHostAddress();
               }
               if(!ip.isEmpty()){
                  break;
               }
            }
         }
      } catch (SocketException e) {
         logger.error("Can't get Network Interfaces", e);
      }
      return ip; 
   }

}
