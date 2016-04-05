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
package org.openremote.controller.protocol.domintell;

import org.apache.commons.lang.StringUtils;

/**
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class DomintellAddress {

   private int address;
   
   public DomintellAddress(String address) throws InvalidDomintellAddressException {
      if (address.startsWith("0x")) {
         if (address.length() > 2) {
            this.address = Integer.parseInt(address.substring(2).trim(), 16);
         } else {
            throw new InvalidDomintellAddressException("Could not parse Domintell address", address);
         }
      } else {
         this.address = Integer.parseInt(address);
      }
   }
   
   public DomintellAddress(int address) {
      this.address = address;
   }
   
   public String toString() {
      return StringUtils.right("      " + Integer.toString(address, 16).toUpperCase(), 6);
   }
}
