/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2016, OpenRemote Inc.
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

package org.openremote.controller.statuscache.rules;

import static org.junit.Assert.*;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.conf.EqualityBehaviorOption;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.internal.io.ResourceFactory;
import org.openremote.controller.RuleListener;
import org.openremote.controller.model.event.CustomState;

public class RuleListenerTest
{
   private KieSession ksession;

   public static String TEST_SENSOR_NAME = "SENSOR_NAME";
   
   @Before
   public void setUp() throws Exception
   {
     KieServices kieServices = KieServices.Factory.get();

     KieModuleModel kieModuleModel = kieServices.newKieModuleModel();

     KieBaseModel kieBaseModel = kieModuleModel.newKieBaseModel("OpenRemoteKBase")
             .setDefault(true)
             .setEqualsBehavior(EqualityBehaviorOption.EQUALITY);
     KieSessionModel kieSessionModel = kieBaseModel.newKieSessionModel("OpenRemoteKSession")
             .setDefault(true)
             .setType(KieSessionModel.KieSessionType.STATEFUL);

     KieFileSystem kfs = kieServices.newKieFileSystem();
     kfs.writeKModuleXML(kieModuleModel.toXML());

     kfs.write("src/main/resources/TestRuleFiring.drl", ResourceFactory.newClassPathResource("org/openremote/controller/statuscache/rules/TestRuleFiring.drl"));
     KieBuilder kieBuilder = kieServices.newKieBuilder(kfs).buildAll();

     assertFalse (kieBuilder.getResults().hasMessages(Message.Level.ERROR));

     KieContainer kieContainer = kieServices.newKieContainer(kieServices.getRepository().getDefaultReleaseId());
     KieBase kb = kieContainer.getKieBase();
     ksession = kb.newKieSession();
   }

   @After
   public void tearDown() throws Exception
   {
      ksession.dispose();
   }

   /**
    * This test confirms the plumbing for the event listener is working.
    * This test is testing the following behavior:
    *    The event listener detects rule activations.
    *    The listener fires "BeforeMatchFired" in response to rule activations.
    *    The listener is properly detecting and logging information about the rule (name, declarations, LHS, etc.).
    */
   @Test
   public void testBeforeMatchFired()
   {
      CustomState newState = new CustomState(1, TEST_SENSOR_NAME, "ON" );

      ksession.insert(newState);
      
      RuleListener ruleListener = new RuleListener();
      ksession.addEventListener(ruleListener);
      
      //add a handler so logging output can be stored in active memory
      Logger ruleLogger = ruleListener.getLogger();
      TestLogHandler handler = new TestLogHandler();
      ruleLogger.addHandler(handler);
      ruleLogger.setLevel(Level.ALL);
      
      
      ksession.fireAllRules();
 
      String lastLog = String.format("rule \"%s\" // (package org.openremote.controller.statuscache.rules)\n" +
            "\tDeclarations \n\t\tDeclaration: \"$e\"\n\t\tValue:\n\t\t\tSensor Name: \"%s\"\n\t\t\tSensor Value: \"ON\"\n" +
            "\tLHS objects(antecedents)\n\t\tClass: \"CustomState\"\n\t\tFields: \n\t\t\tEvent Name: \t\"%s\"\n\t\t\tEvent Value: \t\"ON\"\n", "TestRuleFiring", TEST_SENSOR_NAME, TEST_SENSOR_NAME);

      handler.assertLastLog(Level.FINE, lastLog);
   }

  private static class TestLogHandler extends Handler
  {
     private Level lastLevel;
     private String lastMessage;

     public TestLogHandler(){
       this.setLevel(Level.ALL);
       this.setFormatter(new SimpleFormatter());
     }
     
     @Override public void publish(LogRecord record)
     {
       lastLevel = record.getLevel();
       lastMessage = record.getMessage();
     }

     @Override public void flush()
     {

     }

     @Override public void close()
     {

     }

     void assertLastLog(Level level, String msg)
     {
       Assert.assertTrue(
           "Expected log message '" + msg + "', got '" + lastMessage + "'.",
           msg.equals(lastMessage)
       );

       Assert.assertTrue(
           "Expected level " + level + ", got " + lastLevel,
           level.equals(lastLevel)
       );
     }
   }
}
