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

import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.openremote.controller.protocol.domintell.DomintellAddress;
import org.openremote.controller.protocol.domintell.DomintellCommandBuilder;
import org.openremote.controller.protocol.domintell.DomintellGateway;
import org.openremote.controller.protocol.domintell.TemperatureMode;

/**
 * @author <a href="mailto:eric@openremote.org">Eric Bariaux</a>
 */
public class TemperatureModule extends DomintellModule implements Temperature {

   /**
    * Domintell logger. Uses a common category for all Domintell related logging.
    */
   private final static Logger log = Logger.getLogger(DomintellCommandBuilder.DOMINTELL_LOG_CATEGORY);

   private float currentTemperature;
   private float setPoint;
   private TemperatureMode mode;
   private float presetSetPoint;
   
   public TemperatureModule(DomintellGateway gateway, String moduleType, DomintellAddress address) {
      super(gateway, moduleType, address);
   }

   @Override
   public void setSetPoint(Float setPoint) {
      NumberFormat temperatureFormat = NumberFormat.getInstance(Locale.US);
      temperatureFormat.setMinimumIntegerDigits(2);
      temperatureFormat.setMaximumIntegerDigits(2);
      temperatureFormat.setMinimumFractionDigits(1);
      temperatureFormat.setMaximumFractionDigits(1);
      gateway.sendCommand(moduleType + address + "%T" + temperatureFormat.format(setPoint));      
   }
   
   @Override
   public void setMode(TemperatureMode mode) {
      gateway.sendCommand(moduleType + address + "%M" + mode.getValue());

   }


   @Override
   public void queryState() {
      gateway.sendCommand(moduleType + address + "%S");
   }

   // Feedback method from HomeWorksDevice ---------------------------------------------------------

   @Override
   public void processUpdate(String info) {
     try {
        // T 0.0 18.0 AUTO 18.0
        StringTokenizer st = new StringTokenizer(info.substring(1));
        currentTemperature = Float.parseFloat(st.nextToken());
        setPoint = Float.parseFloat(st.nextToken());
        mode = TemperatureMode.valueOf(st.nextToken());
        presetSetPoint = Float.parseFloat(st.nextToken());
                
        log.info("Current temperature read as >" + currentTemperature + "<");
     } catch (NumberFormatException e) {
       // Not understood as a scene, do not update ourself
       log.warn("Invalid feedback received " + info, e);
     }
       
     super.processUpdate(info);
   }

   public float getCurrentTemperature() {
      return currentTemperature;
   }

   public float getSetPoint() {
      return setPoint;
   }

   public TemperatureMode getMode() {
      return mode;
   }

   public float getPresetSetPoint() {
      return presetSetPoint;
   }
   
}