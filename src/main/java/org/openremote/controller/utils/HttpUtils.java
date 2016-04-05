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

import org.apache.commons.codec.binary.Base64;
import org.openremote.controller.Constants;

public class HttpUtils {

   /**
    * Generates the appropriate string to use as HTTP Authorization header for basic authentication.
    * 
    * @param username User name for authentication
    * @param password Credentials for authentication
    * @return Header for HTTP Authorization
    */
   public static String generateHttpBasicAuthorizationHeader(String username, String password)
   {
      return Constants.HTTP_BASIC_AUTHORIZATION + new String(Base64.encodeBase64((username + ":" + password).getBytes()));
   }
   
}
