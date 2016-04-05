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
package org.openremote.controller.protocol.domintell.model;

import org.apache.log4j.Logger;
import org.openremote.controller.protocol.domintell.DomintellAddress;
import org.openremote.controller.protocol.domintell.DomintellCommandBuilder;
import org.openremote.controller.protocol.domintell.DomintellGateway;

/**
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class DimmerModule extends DomintellModule implements Dimmer {

   /**
    * Domintell logger. Uses a common category for all Domintell related logging.
    */
   private final static Logger log = Logger.getLogger(DomintellCommandBuilder.DOMINTELL_LOG_CATEGORY);

   private int[] levels = new int[8];
   
   public DimmerModule(DomintellGateway gateway, String moduleType, DomintellAddress address) {
      super(gateway, moduleType, address);
   }

   @Override
   public void on(Integer output) {
      gateway.sendCommand(moduleType + address + "-" + Integer.toString(output) + "%I");
   }

   @Override
   public void off(Integer output) {
      gateway.sendCommand(moduleType + address + "-" + Integer.toString(output) + "%O");
   }

   @Override
   public void toggle(Integer output) {
      gateway.sendCommand(moduleType + address + "-" + Integer.toString(output));
   }

   @Override
   public void queryState(Integer output) {
      gateway.sendCommand(moduleType + address + "-" + Integer.toString(output) + "%S");
   }

   @Override
   public void setLevel(Integer output, int level) {
      gateway.sendCommand(moduleType + address + "-" + Integer.toString(output) + "%D" + Integer.toString(level));
   }
   
   // Feedback method from HomeWorksDevice ---------------------------------------------------------

   @Override
   public void processUpdate(String info) {
     try {
        // D 064 0 0 0 0 0 0
        for (int i = 0; i < 8; i++) {
           levels[i] = Integer.parseInt(info.substring(1 + i * 2, 3 + i *2).trim(), 16);
        }
     } catch (NumberFormatException e) {
       // Not understood as a scene, do not update ourself
       log.warn("Invalid feedback received " + info, e);
     }
       
     super.processUpdate(info);
   }

   public int getLevel(int output) {
      return levels[output - 1];
   }
}
