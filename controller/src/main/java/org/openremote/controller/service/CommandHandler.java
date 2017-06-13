/*
 * OpenRemote, the Home of the Digital Home.
 * Copyright 2008-2014, OpenRemote Inc.
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
package org.openremote.controller.service;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.json.JSONException;
import org.json.JSONObject;
import org.openremote.controller.Constants;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.exception.ConfigurationException;
import org.openremote.controller.exception.ConnectionException;
import org.openremote.controller.exception.ControllerRESTAPIException;
import org.openremote.controller.proxy.ControllerProxy;
import org.openremote.controller.utils.Logger;
import org.openremote.controllercommand.domain.ControllerCommandDTO;
import org.openremote.controllercommand.domain.ControllerCommandResponseDTO;
import org.openremote.rest.GenericResourceResultWithErrorMessage;

import java.io.IOException;
import java.net.Socket;
import java.util.Map;


public class CommandHandler {
   private final Deployer deployer;
   private final ControllerConfiguration config;

   private final static Logger log = Logger.getLogger(Constants.BEEHIVE_COMMAND_CHECKER_LOG_CATEGORY);
   private CommandService commandService = ServiceContext.getCommandService();

   public CommandHandler(Deployer deployer, ControllerConfiguration config) {
      this.deployer = deployer;
      this.config = config;
   }

   public Deployer getDeployer() {
      return deployer;
   }

   public ControllerConfiguration getConfig() {
      return config;
   }

   public void handleCommand(TextWebSocketFrame textFrame, Channel channel) {
      GenericResourceResultWithErrorMessage res = null;
      String str = textFrame.text();
      try
      {
         res = new JSONDeserializer<GenericResourceResultWithErrorMessage>()
               .use(null, GenericResourceResultWithErrorMessage.class)
               .use("result", ControllerCommandDTO.class).deserialize(str);
      }

      catch(RuntimeException e)
      {
         log.error("Failed to deserialize commands from remote command service : ''{0}''.", e, str);
      }


      if (res != null)
      {
         if (res.getErrorMessage() != null)
         {
            log.warn("Remote command service returned an error : {0}", res.getErrorMessage());
         }

         else
         {
            executeCommand((ControllerCommandDTO) res.getResult(), channel);
         }
      }
   }

   //
   // TODO
   //
   private void executeCommand(ControllerCommandDTO controllerCommand, Channel channel)
   {
      switch (controllerCommand.getCommandTypeEnum())
      {
         case INITIATE_PROXY:
            if (initiateProxy(controllerCommand)) {
               ackResponse(controllerCommand.getOid(), channel);
            } else {
               ackResponse(controllerCommand.getOid(),"Error trying to connect on beehive", channel);
            }
            break;

         case UNLINK_CONTROLLER:

            deployer.unlinkController();
            ackResponse(controllerCommand.getOid(), channel);
            break;

         case DOWNLOAD_DESIGN:
         {
            try {
               String username = deployer.getUserName();
               if (username == null || username.equals(""))
               {
                  ackResponse(controllerCommand.getOid(),"Unable to retrieve username for beehive command service API call. Skipped...", channel);
                  break;
               }

               String password = deployer.getPassword(username);
               deployer.deployFromOnline(username, password);
               ackResponse(controllerCommand.getOid(), channel);

            } catch (Deployer.PasswordException e) {
               ackResponse(controllerCommand.getOid(),"Unable to retrieve password for beehive command service API call. Skipped...", e, channel);
            } catch (ConfigurationException e) {
               ackResponse(controllerCommand.getOid(),"Synchronizing controller with online account failed : "+e.getMessage(), e, channel);
            } catch (ConnectionException e) {
               ackResponse(controllerCommand.getOid(),"Synchronizing controller with online account failed : "+e.getMessage(), e, channel);
            } catch (Exception e) {
               ackResponse(controllerCommand.getOid(),"Other Exception", e, channel);
            }
            break;
         }
         case EXECUTE_DEVICE_COMMAND:
         {
            try {
               Map<String, String> param = controllerCommand.getCommandParameter();
               commandService.execute(param.get("deviceName"),param.get("commandName") , param.get("param"));
               ackResponse(controllerCommand.getOid(), channel);
            } catch (ControllerRESTAPIException e) {
               ackResponse(controllerCommand.getOid(),"Other Exception", e, channel);
            }
            break;
         }

         default:
            ackResponse(controllerCommand.getOid(),"ControllerCommand not implemented yet: " + controllerCommand.getCommandType(), channel);
      }
   }

   private void ackResponse(Long oid, Channel channel) {
      ackResponse(oid,null, channel );
   }

   private void ackResponse(Long oid, String errorMessage, Channel channel) {
      ackResponse(oid, errorMessage,null, channel);
   }

   private void ackResponse(Long oid, String errorMessage, Throwable e, Channel channel) {
      ControllerCommandResponseDTO responseDTO = new ControllerCommandResponseDTO();
      responseDTO.setOid(oid);
      if (errorMessage != null) {
         log.error(errorMessage, e);
         responseDTO.setCommandTypeEnum(ControllerCommandResponseDTO.Type.ERROR);
      } else {
         responseDTO.setCommandTypeEnum(ControllerCommandResponseDTO.Type.SUCCESS);
      }
      try {
         JSONObject response = new JSONObject(new JSONSerializer().deepSerialize(responseDTO));
         channel.writeAndFlush(new TextWebSocketFrame(response.toString()));
      } catch (JSONException e1) {
         log.error("Error serialising command json",e1);
      }
   }

   //
   // TODO
   //
   private boolean initiateProxy(ControllerCommandDTO command)
   {
      Long id = command.getOid();
      String url = command.getCommandParameter().get("url");
      String token = command.getCommandParameter().get("token");

      Socket beehiveSocket = null;

      boolean isSuccess = true;

      try
      {
         log.info("Connecting to beehive at "+url+" for proxy");
         beehiveSocket = ControllerProxy.makeClientSocket(url, token, config.getProxyTimeout());

         // at this point the command should already have been marked as ack by the listening end at beehive

         log.info("Connected to beehive");

         // try to connect to it, see if it's still valid

         String ip = config.getWebappIp();
         int port = config.getWebappPort();

         if (ip == null || ip.trim().length() == 0)
         {
            ip = "localhost";
         }

         if (port == 0)
         {
            port = 8080;
         }

         ControllerProxy proxy = new ControllerProxy(beehiveSocket, ip, port, config.getProxyTimeout());
         log.info("Starting proxy");
         proxy.start();
      }

      catch (IOException e)
      {
         log.info("Got exception while connecting to beehive", e);

         if(beehiveSocket != null)
         {
            try
            {
               beehiveSocket.close();
            }

            catch (IOException e1)
            {
               // ignore
            }
         }

         // the server should have closed it, but let's help him to make sure
         isSuccess = false;
      }
      return isSuccess;
   }

}