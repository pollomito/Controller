package org.openremote.controller.service;

import flexjson.JSONSerializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.exception.ControllerRESTAPIException;
import org.openremote.controllercommand.domain.ControllerCommandResponseDTO;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest(ServiceContext.class)
public class CommandHandlerTest {

   private CommandHandler commandHandler;
   private Deployer deployer;
   private ChannelFuture channelFuture;
   private Channel channel;
   private ControllerConfiguration config;
   private CommandService commandService;

   @Before
   public void setUp() throws Exception {
      deployer = mock(Deployer.class);
      mockStatic(ServiceContext.class);
      commandService = mock(CommandService.class);
      PowerMockito.when(ServiceContext.getCommandService()).thenReturn(commandService);
      channel = mock(Channel.class);
      channelFuture = mock(ChannelFuture.class);
      when(channel.writeAndFlush(isA(TextWebSocketFrame.class))).thenReturn(channelFuture);
      config = new ControllerConfiguration();
      commandHandler = new CommandHandler(deployer, config);
   }

   @Test
   public void handleCommand_invalidUser() throws JSONException {
      TextWebSocketFrame textFrame = new TextWebSocketFrame("{ 'result' : {'commandType' : 'DOWNLOAD_DESIGN' }}");

      commandHandler.handleCommand(textFrame, channel);
      ControllerCommandResponseDTO responseDTO = new ControllerCommandResponseDTO();
      responseDTO.setOid(null);
      responseDTO.setCommandTypeEnum(ControllerCommandResponseDTO.Type.ERROR);
      JSONObject response = new JSONObject(new JSONSerializer().deepSerialize(responseDTO));
      verify(channel).writeAndFlush(new TextWebSocketFrame(response.toString()));
   }

   @Test
   public void handleCommand_downloadDesign() throws JSONException {
      TextWebSocketFrame textFrame = new TextWebSocketFrame("{ 'result' : {'commandType' : 'DOWNLOAD_DESIGN' }}");
      //ArgumentCaptor<TextWebSocketFrame> captor = ArgumentCaptor.forClass(TextWebSocketFrame.class);
      when(deployer.getUserName()).thenReturn("MockUser");
      Channel channel = mock(Channel.class);
      channelFuture = mock(ChannelFuture.class);
      when(channel.writeAndFlush(isA(TextWebSocketFrame.class))).thenReturn(channelFuture);
      commandHandler.handleCommand(textFrame, channel);
      ControllerCommandResponseDTO responseDTO = new ControllerCommandResponseDTO();
      responseDTO.setOid(null);
      responseDTO.setCommandTypeEnum(ControllerCommandResponseDTO.Type.SUCCESS);
      JSONObject response = new JSONObject(new JSONSerializer().deepSerialize(responseDTO));
      verify(channel).writeAndFlush(new TextWebSocketFrame(response.toString()));
   }

   @Test
   public void handleCommand_initiateProxyFailed() throws JSONException {
      TextWebSocketFrame textFrame = new TextWebSocketFrame("{ 'result' : {'commandType' : 'INITIATE_PROXY', 'commandParameter': { 'url':'wrong', 'token':'wrong' }}}");
      //ArgumentCaptor<TextWebSocketFrame> captor = ArgumentCaptor.forClass(TextWebSocketFrame.class);
      when(deployer.getUserName()).thenReturn("MockUser");
      Channel channel = mock(Channel.class);
      channelFuture = mock(ChannelFuture.class);
      when(channel.writeAndFlush(isA(TextWebSocketFrame.class))).thenReturn(channelFuture);
      commandHandler.handleCommand(textFrame, channel);
      ControllerCommandResponseDTO responseDTO = new ControllerCommandResponseDTO();
      responseDTO.setOid(null);
      responseDTO.setCommandTypeEnum(ControllerCommandResponseDTO.Type.ERROR);
      JSONObject response = new JSONObject(new JSONSerializer().deepSerialize(responseDTO));
      verify(channel).writeAndFlush(new TextWebSocketFrame(response.toString()));
   }

   @Test
   public void handleCommand_unlinkController() throws JSONException {
      TextWebSocketFrame textFrame = new TextWebSocketFrame("{ 'result' : {'commandType' : 'UNLINK_CONTROLLER'}}");
      //ArgumentCaptor<TextWebSocketFrame> captor = ArgumentCaptor.forClass(TextWebSocketFrame.class);
      when(deployer.getUserName()).thenReturn("MockUser");
      Channel channel = mock(Channel.class);
      channelFuture = mock(ChannelFuture.class);
      when(channel.writeAndFlush(isA(TextWebSocketFrame.class))).thenReturn(channelFuture);
      commandHandler.handleCommand(textFrame, channel);
      ControllerCommandResponseDTO responseDTO = new ControllerCommandResponseDTO();
      responseDTO.setOid(null);
      responseDTO.setCommandTypeEnum(ControllerCommandResponseDTO.Type.SUCCESS);
      JSONObject response = new JSONObject(new JSONSerializer().deepSerialize(responseDTO));
      verify(channel).writeAndFlush(new TextWebSocketFrame(response.toString()));
   }

   @Test
   public void handleCommand_executeDeviceCommand() throws JSONException, ControllerRESTAPIException {
      TextWebSocketFrame textFrame = new TextWebSocketFrame("{ 'result' : {'commandType' : 'EXECUTE_DEVICE_COMMAND'}}");
      //ArgumentCaptor<TextWebSocketFrame> captor = ArgumentCaptor.forClass(TextWebSocketFrame.class);
      when(deployer.getUserName()).thenReturn("MockUser");
      Channel channel = mock(Channel.class);
      channelFuture = mock(ChannelFuture.class);
      when(channel.writeAndFlush(isA(TextWebSocketFrame.class))).thenReturn(channelFuture);
      commandHandler.handleCommand(textFrame, channel);
      ControllerCommandResponseDTO responseDTO = new ControllerCommandResponseDTO();
      responseDTO.setOid(null);
      responseDTO.setCommandTypeEnum(ControllerCommandResponseDTO.Type.SUCCESS);
      JSONObject response = new JSONObject(new JSONSerializer().deepSerialize(responseDTO));
      verify(commandService).execute(null,null,null);
      verify(channel).writeAndFlush(new TextWebSocketFrame(response.toString()));
   }

}