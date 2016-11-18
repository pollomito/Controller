package org.openremote.controller.service;

import flexjson.JSONDeserializer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;
import org.openremote.controller.Constants;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.exception.ConfigurationException;
import org.openremote.controller.exception.ConnectionException;
import org.openremote.controller.proxy.ControllerProxy;
import org.openremote.controller.utils.Logger;
import org.openremote.controllercommand.domain.ControllerCommandDTO;
import org.openremote.rest.GenericResourceResultWithErrorMessage;
import org.restlet.data.ChallengeScheme;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;

import java.io.IOException;
import java.net.Socket;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

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
        System.out.println("WebSocket Client disconnected!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            System.out.println("WebSocket Client connected!");
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
                    ControllerCommandDTO command = (ControllerCommandDTO) res.getResult();
                     executeCommand(command);
                }
            }
            
        } else if (frame instanceof PongWebSocketFrame) {
            System.out.println("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
            System.out.println("WebSocket Client received closing");
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
                initiateProxy(controllerCommand);
                break;

            case UNLINK_CONTROLLER:

                stop();
                deployer.unlinkController();

                break;

            case DOWNLOAD_DESIGN:
            {
                try {
                    String username = deployer.getUserName();
                    if (username == null || username.equals(""))
                    {
                        log.error("Unable to retrieve username for beehive command service API call. Skipped...");
                        break;
                    }

                    String password = deployer.getPassword(username);
                    deployer.deployFromOnline(username, password);
                    ackCommand(controllerCommand.getOid());
                } catch (Deployer.PasswordException e) {
                    log.error("Unable to retrieve password for beehive command service API call. Skipped...", e);
                } catch (ConfigurationException e) {
                    log.error("Synchronizing controller with online account failed : {0}", e, e.getMessage());
                } catch (ConnectionException e) {
                    log.error("Synchronizing controller with online account failed : {0}", e, e.getMessage());
                }
                break;
            }

            default:
                log.error("ControllerCommand not implemented yet: " + controllerCommand.getCommandType());
        }
    }

    private void ackCommand(Long id)
    {
        //TODO send back ack
    }

    //
    // TODO
    //
    private void initiateProxy(ControllerCommandDTO command)
    {
        Long id = command.getOid();
        String url = command.getCommandParameter().get("url");
        String token = command.getCommandParameter().get("token");

        Socket beehiveSocket = null;

        boolean needsAck = true;

        try
        {
            log.info("Connecting to beehive at "+url+" for proxy");
            beehiveSocket = ControllerProxy.makeClientSocket(url, token, config.getProxyTimeout());

            // at this point the command should already have been marked as ack by the listening end at beehive

            log.info("Connected to beehive");
            needsAck = false;

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

            if(needsAck)
            {
                ackCommand(id);
            }
        }
    }


    public void stop()
    {
        this.handshakeFuture.cancel(true);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }
}