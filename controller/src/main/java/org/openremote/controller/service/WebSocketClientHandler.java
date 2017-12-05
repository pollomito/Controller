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

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.ScheduledFuture;
import org.openremote.controller.Constants;
import org.openremote.controller.utils.Logger;

import javax.net.ssl.SSLException;
import java.net.URISyntaxException;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private final static Logger log = Logger.getLogger(Constants.BEEHIVE_COMMAND_WEBSOCKET_LOG_CATEGORY);
    private final WebSocketClientHandshaker handshaker;
    private CommandHandler commandHandler;
    private final int timeout;
    private final int reconnectDelay;
    private ChannelPromise handshakeFuture;
    private ScheduledFuture<?> pingPongTaskFuture;
    private PingPongTask pingPongTask;

    public WebSocketClientHandler(WebSocketClientHandshaker handshaker, CommandHandler commandHandler, int timeout, int reconnectDelay) {
        this.handshaker = handshaker;
        this.commandHandler = commandHandler;
        this.timeout = timeout;
        this.reconnectDelay = reconnectDelay;
    }

    public ChannelFuture handshakeFuture() {
        return handshakeFuture;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        handshakeFuture = ctx.newPromise();
    }

    @Override
    public void channelActive(final ChannelHandlerContext ctx) {
        handshaker.handshake(ctx.channel());
        pingPongTask = new PingPongTask(ctx.channel());
        pingPongTaskFuture = ctx.channel().eventLoop().scheduleAtFixedRate(pingPongTask, timeout, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        pingPongTaskFuture.cancel(true);
        log.info("WebSocket Client disconnected!");
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        Channel ch = ctx.channel();
        if (!handshaker.isHandshakeComplete()) {
            handshaker.finishHandshake(ch, (FullHttpResponse) msg);
            log.info("WebSocket Client connected! With OpenSSL");
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
            try {
                commandHandler.handleCommand(textFrame, ctx.channel());
            } catch (Throwable t) {
                log.error("Error handling command " + t.getMessage());
            }
        } else if (frame instanceof PongWebSocketFrame) {
            log.info("WebSocket Client received pong");
            pingPongTask.receivedPong();

        } else if (frame instanceof CloseWebSocketFrame) {
            log.info("WebSocket Client received closing");
            ch.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception on WebsocketClientHandler, class:" + cause.getClass().getCanonicalName()+", message: "+ cause.getMessage() , cause);
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }

    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        if (!ctx.executor().isShuttingDown()) {
            log.info("Sleeping for: " + reconnectDelay + "ms");

            final EventLoop loop = ctx.channel().eventLoop();
            loop.schedule(new Runnable() {
                @Override
                public void run() {
                    log.info("Reconnecting !");
                    try {
                        WebSocketClient.connect(WebSocketClient.configureBootstrap(new Bootstrap(), loop, commandHandler.getDeployer(), commandHandler.getConfig()));
                    } catch (URISyntaxException e) {
                        log.error("Error starting WS, class: "+e.getClass().getCanonicalName()+", message: "+e.getMessage(), e);
                    } catch (SSLException e) {
                        log.error("Error starting WS, class: "+e.getClass().getCanonicalName()+", message: "+e.getMessage(), e);
                    } catch (Deployer.PasswordException e) {
                        log.error("Error starting WS, class: "+e.getClass().getCanonicalName()+", message: "+e.getMessage(), e);
                    }
                }
            }, reconnectDelay, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        super.userEventTriggered(ctx, evt);
        log.info("Websocket Event " + evt);
    }

    public void stop() {
        this.handshakeFuture.cancel(true);
    }

    private class PingPongTask extends TimerTask {
        private final Channel channel;
        private boolean pongReceived;

        PingPongTask(Channel channel) {
            this.channel = channel;
            this.pongReceived = true;
        }

        @Override
        public void run() {
            log.info("Sending Ping to" + channel);
            if (pongReceived) {
                pongReceived = false;
                channel.writeAndFlush(new PingWebSocketFrame());
            } else {
                log.info("Ping timeout closing channel:" + channel);
                channel.disconnect();
            }

        }

        void receivedPong() {
            pongReceived = true;
        }
    }
}