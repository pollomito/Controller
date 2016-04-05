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

import java.util.ArrayList;
import java.util.List;

import org.openremote.controller.protocol.domintell.DomintellAddress;
import org.openremote.controller.protocol.domintell.DomintellCommand;
import org.openremote.controller.protocol.domintell.DomintellGateway;

/**
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class DomintellModule {

   // Instance Fields
   // ----------------------------------------------------------------------

   /**
    * Gateway we're associated with. This is the gateway we'll use to send the commands.
    */
   protected DomintellGateway gateway;
   
   protected String moduleType;
   
   /**
    * Address of this module.
    */
   protected DomintellAddress address;

   /**
    * Commands we should update when our status changes
    */
   private List<DomintellCommand> commands = new ArrayList<DomintellCommand>();

   // Constructors ---------------------------------------------------------------------------------

   public DomintellModule(DomintellGateway gateway, String moduleType, DomintellAddress address) {
      super();
      this.gateway = gateway;
      this.moduleType = moduleType;
      this.address = address;
   }

   // Public methods -------------------------------------------------------------------------------
   
   /**
    * Called when a feedback information is received from the Domintell system in order for this device to update its status.
    * This is implemented by each specific device to process the feedback received as appropriate for it.
    * Subclasses must then call this implementation to make sure value change is propagated to registered commands.
    * 
    * @param info String as received from the Domintell after the device address
    */
   public void processUpdate(String info) {
      updateCommands();
   }
   
   /**
    * Add a command to update on value change.
    * 
    * @param command DomintellCommand to add
    */
   public void addCommand(DomintellCommand command) {
      commands.add(command);
   }
   
   /**
    * Remove a command to update on value change.
    * 
    * @param command DomintellCommand to remove
    */
   public void removeCommand(DomintellCommand command) {
      commands.remove(command);
   }

   /**
    * Update all registered commands (because of a status change received from bus)
    */
   protected void updateCommands() {
      for (DomintellCommand command : commands) {
         command.updateSensors(this);
      }
   }}
