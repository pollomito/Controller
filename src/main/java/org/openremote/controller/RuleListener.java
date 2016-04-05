/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2012, OpenRemote Inc.
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
 *
 */

package org.openremote.controller;

import java.util.List;
import java.util.Map;

import org.drools.definition.rule.Rule;
import org.drools.event.rule.BeforeActivationFiredEvent;
import org.drools.event.rule.DefaultAgendaEventListener;
import org.drools.runtime.rule.Activation;
import org.openremote.controller.model.sensor.Sensor;
import org.openremote.controller.protocol.Event;
import org.openremote.controller.utils.Logger;

/**
 * This class logs Drools rule execution. It is intended to assist the user in debugging Drools
 * behavior.
 * 
 * @author Isaac Martin
 */
public class RuleListener extends DefaultAgendaEventListener
{

   private Logger log;
    
   public RuleListener()
   {
      super();
      log = Logger.getLogger(Constants.RUNTIME_EVENTPROCESSOR_LOG_CATEGORY + ".drools");
   }
   
   @Override
   public void beforeActivationFired(BeforeActivationFiredEvent ruleEvent)
   {
      final Rule rule = ruleEvent.getActivation().getRule();
      String ruleName = rule.getName();
      
      if (ruleName.startsWith("--"))
      {
         return;
      }

      if (ruleName.startsWith("-"))
      {
         log.debug("Rule: " + ruleName + ": SKIPPED LOGGER");
         return;
      }
      
      String rulePackage = rule.getPackageName();
      ruleName = "\"" + ruleName + "\" // (package " + rulePackage + ")";
      
      Activation activationEvent = ruleEvent.getActivation();
      List<String> declarationIDs = activationEvent.getDeclarationIDs();
      List<Object> antecedents = activationEvent.getObjects();
      
      
      String declarationLog = "";
      for (String declarationID : declarationIDs)
      {
         Object declarationValue = null;
         String declarationValueString = "";
         declarationValue = activationEvent.getDeclarationValue(declarationID);
         declarationValueString = this.declarationValueToString(declarationValue);
         if (declarationValue instanceof Sensor || declarationValue instanceof Event)
         {
            declarationLog = String.format("%s\t\tDeclaration: \"%s\"\n\t\tValue:\n\t\t\t%s\n", declarationLog, declarationID, declarationValueString);
         }
         else
         {
            declarationLog = String.format("%s\t\tDeclaration: \"%s: %s\"\n", declarationLog, declarationID, declarationValueString);
         }
      }
      
      String objectLog = "";
      for (Object antecedent : antecedents)
      {
         String theClass = "";
         String theValue = "";
         theClass = antecedent.getClass().getSimpleName();
         theValue = this.antecedentValueToString(antecedent);
         objectLog = String.format("%s\t\tClass: \"%s\"\n\t\tFields: \n\t\t\t%s\n", objectLog, theClass, theValue);
      }
      
      log.debug(String.format("rule %s\n" +
      		                  "\tDeclarations \n%s" +
      		                  "\tLHS objects(antecedents)\n%s", ruleName, declarationLog, objectLog));
      
   }
   
   public Logger getLogger()
   {
      return log;
   }
   
   /**
    * This method converts a declarationValue into a string. 
    * The need for this method would be obviated if all our facts descended from 
    * a Fact class with a method to return the most salient value of a fact.
    * @param antecedent - An object referenced by a drools LHS
    * @return
    */
   private String antecedentValueToString(Object antecedent)
   {
      String theValue = null;
      if (antecedent != null)
      {
         theValue = antecedent.toString();
      }
      
      if (antecedent instanceof Sensor) //may be unnecessary if we never have raw sensor objects in WM
      {
         Sensor theSensor = (Sensor) antecedent;
         String sensorName = theSensor.getName();
         theValue = String.format("Sensor: %s\n", sensorName);
    
         theValue = String.format("%s\t\tSensor Properties\n", theValue);
         Map<String,String> sensorValues = theSensor.getProperties();
         
         for (Map.Entry<String, String> entry : sensorValues.entrySet())
         {
            String entryName = entry.getKey();
            String entryValue = entry.getValue();
            theValue = String.format("%sName: \t\"%s\"\n\t\t\tValue: \t\"%s\"", theValue, entryName, entryValue);           
         }   
      }
      if (antecedent instanceof Event)
      {
         Event theEvent = (Event) antecedent;
         String sourceName = theEvent.getSource();
         String eventValue = theEvent.getValue().toString(); //assumes all values can directly cast to String      
         theValue = String.format("Event Name: \t\"%s\"\n\t\t\tEvent Value: \t\"%s\"", sourceName, eventValue);
      }

      return theValue;
   }
   
   /**
    * This method converts a declarationValue into a string. 
    * The need for this method would be obviated if all our facts descended from 
    * a Fact class with a method to return the unique identifier as a string.
    * @param declarationValue - The object associated with a drools LHS declaration
    * @return
    */
   private String declarationValueToString(Object declarationValue)
   {
      String convertedDeclarationValue = null;
      if (declarationValue != null)
      {
         convertedDeclarationValue = declarationValue.toString();
      }

      if (declarationValue instanceof Sensor) //may be unnecessary if we never have raw sensor objects in WM
      {
         convertedDeclarationValue = ((Sensor) declarationValue).getName();
      }
      if (declarationValue instanceof Event)
      {
         String sensorName = ((Event) declarationValue).getSource();
         String sensorValue = ((Event) declarationValue).getValue().toString();
         convertedDeclarationValue = String.format("Sensor Name: \"%s\"\n\t\t\tSensor Value: \"%s\"", sensorName, sensorValue);
      }
      
      return convertedDeclarationValue;
   }
   
}
