package org.openremote.controller.service;

import flexjson.JSONDeserializer;
import flexjson.JSONSerializer;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.json.JSONException;
import org.json.JSONObject;
import org.openremote.controller.Constants;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.exception.ConfigurationException;
import org.openremote.controller.exception.ConnectionException;
import org.openremote.controller.proxy.ControllerProxy;
import org.openremote.controller.utils.Logger;
import org.openremote.controllercommand.domain.ControllerCommandDTO;
import org.openremote.controllercommand.domain.ControllerCommandResponseDTO;
import org.openremote.rest.GenericResourceResultWithErrorMessage;

import java.io.IOException;
import java.net.Socket;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private final static Logger log = Logger.getLogger(Constants.BEEHIVE_COMMAND_CHECKER_LOG_CATEGORY);
    private final WebSocketClientHandshaker handshaker;
    private ChannelPromise handshakeFuture;
    private final Deployer deployer;
    private final ControllerConfiguration config;


    public WebSocketClientHandler(WebSocketClientHandshaker handshaker, Deployer deployer, ControllerConfiguration config) {
        this.handshaker = handshaker;
        this.deployer = deployer;
        this.config = config;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
       log.info("WebSocket Client disconnected!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
           log.info("WebSocket Client connected!");
            handshakeFuture.setSuccess();
            return;
        }

        if (msg instanceof FullHttpResponse) {
            FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }


        WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
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
                     executeCommand((ControllerCommandDTO) res.getResult());
                }
            }

        } else if (frame instanceof PongWebSocketFrame) {
           log.info("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
           log.info("WebSocket Client received closing");
            ch.close();
        }
    }


    //
    // TODO
    //
    private void executeCommand(ControllerCommandDTO controllerCommand)
    {
        switch (controllerCommand.getCommandTypeEnum())
        {
            case INITIATE_PROXY:
                if (initiateProxy(controllerCommand)) {
                   ackResponse(controllerCommand.getOid());
                } else {
                   ackResponse(controllerCommand.getOid(),"Error trying to connect on beehive");
                }
                break;

            case UNLINK_CONTROLLER:

                stop();
                deployer.unlinkController();
                ackResponse(controllerCommand.getOid());
                break;

            case DOWNLOAD_DESIGN:
            {
                try {
                    String username = deployer.getUserName();
                    if (username == null || username.equals(""))
                    {
                        ackResponse(controllerCommand.getOid(),"Unable to retrieve username for beehive command service API call. Skipped...");
                        break;
                    }

                    String password = deployer.getPassword(username);
                    deployer.deployFromOnline(username, password);
                    ackResponse(controllerCommand.getOid());

                } catch (Deployer.PasswordException e) {
                    ackResponse(controllerCommand.getOid(),"Unable to retrieve password for beehive command service API call. Skipped...", e);
                } catch (ConfigurationException e) {
                    ackResponse(controllerCommand.getOid(),"Synchronizing controller with online account failed : "+e.getMessage(), e);
                } catch (ConnectionException e) {
                    ackResponse(controllerCommand.getOid(),"Synchronizing controller with online account failed : "+e.getMessage(), e);
                } catch (Exception e) {
                    ackResponse(controllerCommand.getOid(),"Other Exception", e);
                }
                break;
            }

            default:
                ackResponse(controllerCommand.getOid(),"ControllerCommand not implemented yet: " + controllerCommand.getCommandType());
        }
       }

    private void ackResponse(Long oid) {
        ackResponse(oid,null);
    }

    private void ackResponse(Long oid, String errorMessage) {
        ackResponse(oid, errorMessage,null);
    }

    private void ackResponse(Long oid, String errorMessage, Throwable e) {
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
          handshakeFuture.channel().writeAndFlush(new TextWebSocketFrame(response.toString()));
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


    public void stop()
    {
        this.handshakeFuture.cancel(true);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception on WebsocketClientHandler",cause);
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}