/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2013, OpenRemote Inc.
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
package org.openremote.controller.protocol.port;

import org.openremote.controller.Constants;
import org.openremote.controller.utils.Logger;

/**
 * A factory for <code>Port</code> instances.
 *
 * @author Olivier Gandit
 * @author <a href="mailto:juha@openremote.org">Juha Lindfors</a>
 */
public class PortFactory
{

   /**
    * A common log category name intended to be used across all classes related to Port implementation.
    */
   public final static String PORT_LOG_CATEGORY = Constants.CONTROLLER_PROTOCOL_LOG_CATEGORY + "port";

   private final static Logger log = Logger.getLogger(PORT_LOG_CATEGORY);

   public static Port createPhysicalBus(String clazz) {
      try {
         //Class<?> c = Class.forName(clazz);

         Class<?> c = Thread.currentThread().getContextClassLoader().loadClass(clazz);

         if (Port.class.isAssignableFrom(c)) {
            return (Port) c.newInstance();
         } else {

           // TODO :
           //         returning null pointer doesn't really make sense here -- if the loaded class
           //         is not assignable to Port then that's an error and should be indicated as
           //         such with an exception that is more precise than the inevitable null pointer
           //         exception that will follow   [JPL]

            return null;
         }
      } catch (Exception e) {
         log.error("Could not instantiate PhysicalBus : {0}", e, e.getMessage());
         return null;
      }
   }
}
