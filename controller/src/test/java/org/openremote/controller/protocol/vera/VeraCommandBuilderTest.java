/*
 * OpenRemote, the Home of the Digital Home. Copyright 2008-2013, OpenRemote Inc.
 * 
 * See the contributors.txt file in the distribution for a full listing of individual contributors.
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>.
 */
package org.openremote.controller.protocol.vera;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.jdom.Element;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mortbay.jetty.Request;
import org.mortbay.jetty.Server;
import org.mortbay.jetty.handler.AbstractHandler;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.command.Command;
import org.openremote.controller.command.CommandBuilder;
import org.openremote.controller.command.CommandFactory;
import org.openremote.controller.deployer.ModelBuilder;
import org.openremote.controller.deployer.Version20CommandBuilder;
import org.openremote.controller.deployer.Version20ModelBuilder;
import org.openremote.controller.exception.InitializationException;
import org.openremote.controller.exception.NoSuchCommandException;
import org.openremote.controller.model.xml.Version20SensorBuilder;
import org.openremote.controller.protocol.virtual.VirtualCommandBuilder;
import org.openremote.controller.service.Deployer;
import org.openremote.controller.statuscache.StatusCache;
import org.openremote.controller.suite.AllTests;
import org.openremote.devicediscovery.domain.DiscoveredDeviceDTO;

/**
 * Tests the implementation of Vera protocol class.
 * Along goes the runtime creation of the VeraCommandBuilder based on controller.xml properties.
 * Also the device discovery is tested.
 * 
 * @author <a href="mailto:marcus@openremote.org">Marcus Redeker</a>
 * 
 */
public class VeraCommandBuilderTest {

   // Constants ------------------------------------------------------------------------------------

   /**
    * Port we are using for the HTTP server during tests.
    */
   private final static int HTTP_SERVER_PORT = 3480; // Hardcoded vera port in VeraClient

   // Class Fields ------------------------------------------------------------------------------

   /**
    * Reference to the deployer
    */
   private static Deployer deployer;
   
   /**
    * Reference to the VeraCommandBuilder which was created based on the controller.xml given to the deployer
    */
   private static VeraCommandBuilder veraCommandBuilder;
   
   /**
    * HTTP server that can be used to provide responses to Vera commands. This emulates our Vera box.
    */
   private static Server httpServer;

   protected static boolean dimmerStatus = false;
   protected static int dimmerValue = 0;

   
   // Test Setup and Tear Down ---------------------------------------------------------------------

   @BeforeClass
   public static void setUpBeforeClass() throws Exception {
      httpServer = new Server(HTTP_SERVER_PORT);
      httpServer.setHandler(new HttpServerResponse());
      httpServer.start();
      deployer = createDeployer();
      deployer.softRestart();
      veraCommandBuilder = new VeraCommandBuilder(deployer);
   }


   // Tests ----------------------------------------------------------------------------

   /**
    * Tests if the command builder gave the detected devices to the deployer for device announcement 
    */
   @Test
   public void testDeviceDiscovery() throws MalformedURLException {
      List<DiscoveredDeviceDTO> devices = ((TestDeployer)deployer).getDiscoveredDevicesToAnnounce();
      Assert.assertEquals(4, devices.size());
   }
   
   /**
    * Tests turn dimmer on 
    */
   @Test
   public void testTurnDimmerOn() {
      VeraCommand cmd = (VeraCommand) getCommand("5", "ON", null);
      Assert.assertNotNull(cmd);
      cmd.send();
      Assert.assertEquals(true, dimmerStatus);
   }

   /**
    * Tests turn dimmer off 
    */
   @Test
   public void testTurnDimmerOff() {
      VeraCommand cmd = (VeraCommand) getCommand("5", "OFF", null);
      Assert.assertNotNull(cmd);
      cmd.send();
      Assert.assertEquals(false, dimmerStatus);
   }
   
   /**
    * Tests turn dimmer 50% 
    */
   @Test
   public void testTurnDimmer50() {
      VeraCommand cmd = (VeraCommand) getCommand("5", "SET_LEVEL", "50");
      Assert.assertNotNull(cmd);
      cmd.send();
      Assert.assertEquals(50, dimmerValue);
   }
   
   /**
    * Tests unknown command 
    */
   @Test (expected=NoSuchCommandException.class)
   public void testUnknownCommand() {
      getCommand("5", "DUMMY", "50");
   }
   
   /**
    * Tests invalid id
    */
   @Test (expected=NoSuchCommandException.class)
   public void testInvalidId() {
      getCommand("dummy", "ON", "50");
   }
   
   // Helper methods -------------------------------------------------------------------------------
   
   private Command getCommand(String device, String command, String commandValue) throws NoSuchCommandException
   {
     Element ele = new Element("command");
     ele.setAttribute("id", "test");
     ele.setAttribute("protocol", "vera");

     if (commandValue != null)
     {
       ele.setAttribute(Command.DYNAMIC_VALUE_ATTR_NAME, commandValue);
     }
     
     Element propName = new Element("property");
     propName.setAttribute("name", "command");
     propName.setAttribute("value", command);

     Element propUrl = new Element("property");
     propUrl.setAttribute("name", "device");
     propUrl.setAttribute("value", device);

     
     ele.addContent(propName);
     ele.addContent(propUrl);

     return veraCommandBuilder.build(ele);
   }
   
   
   private static Deployer createDeployer() throws InitializationException {

      StatusCache cache = new StatusCache();
      ControllerConfiguration config = new ControllerConfiguration();
      config.setBeehiveSyncing(false);
      HashMap<String, String> props = new HashMap<String, String>();
      config.setConfigurationProperties(props);
      URI deploymentURI = AllTests.getAbsoluteFixturePath().resolve("vera");
      config.setResourcePath(deploymentURI.getPath());
      Map<String, CommandBuilder> builders = new HashMap<String, CommandBuilder>();
      builders.put("virtual", new VirtualCommandBuilder());
      CommandFactory cf = new CommandFactory(builders);
      Version20SensorBuilder sensorBuilder = new Version20SensorBuilder();
      sensorBuilder.setCommandFactory(cf);

      Version20ModelBuilder builder = new Version20ModelBuilder(cache, config, sensorBuilder, new Version20CommandBuilder(cf), cf);

      Map<String, ModelBuilder> modelBuilders = new HashMap<String, ModelBuilder>();
      modelBuilders.put(ModelBuilder.SchemaVersion.VERSION_2_0.toString(), builder);

      Deployer deployer = new TestDeployer("test", cache, config, modelBuilders);
      return deployer;
   }

   // Nested Classes -------------------------------------------------------------------------------

   private static class HttpServerResponse extends AbstractHandler {

      private String status1;
      
      public HttpServerResponse() {
         URI uri = AllTests.getAbsoluteFixturePath().resolve("vera/veraStatus1.xml");
         try {
            status1= FileUtils.readFileToString(new File(uri));
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }
      
      public void handle(String target, HttpServletRequest request, HttpServletResponse response, int dispatch)
            throws IOException, ServletException {
         
         String a = request.getQueryString();
         if (a.indexOf("SwitchPower1&action=SetTarget&newTargetValue=1") != -1) {
            dimmerStatus = true;
         }
         if (a.indexOf("SwitchPower1&action=SetTarget&newTargetValue=0") != -1) {
            dimmerStatus = false;
         }
         if (a.indexOf("Dimming1&action=SetLoadLevelTarget&newLoadlevelTarget=50") != -1) {
            dimmerValue = 50;
         }

         response.setContentType("text/html");
         response.setStatus(HttpServletResponse.SC_OK);

         response.getWriter().print(status1);
         response.getWriter().flush();

         ((Request) request).setHandled(true);
      }

   }

}
