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
import org.openremote.controller.Constants;
import org.openremote.controller.utils.Logger;

import javax.net.ssl.SSLException;
import java.net.URISyntaxException;
import java.util.concurrent.TimeUnit;

public class WebSocketClientHandler extends SimpleChannelInboundHandler<Object> {

    private final static Logger log = Logger.getLogger(Constants.BEEHIVE_COMMAND_WEBSOCKET_LOG_CATEGORY);
    private final WebSocketClientHandshaker handshaker;
    private CommandHandler commandHandler;
    private ChannelPromise handshakeFuture;


    public WebSocketClientHandler(WebSocketClientHandshaker handshaker,CommandHandler commandHandler)  {
        this.handshaker = handshaker;
        this.commandHandler = commandHandler;
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
            commandHandler.handleCommand(textFrame, ctx.channel());
        } else if (frame instanceof PongWebSocketFrame) {
           log.info("WebSocket Client received pong");
        } else if (frame instanceof CloseWebSocketFrame) {
           log.info("WebSocket Client received closing");
            ch.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        log.error("Exception on WebsocketClientHandler",cause);
        if (!handshakeFuture.isDone()) {
            handshakeFuture.setFailure(cause);
        }
        ctx.close();
    }

    @Override
    public void channelUnregistered(final ChannelHandlerContext ctx) throws Exception {
        log.info("Sleeping for: " + WebSocketClient.RECONNECT_DELAY + 's');

        final EventLoop loop = ctx.channel().eventLoop();
        loop.schedule(new Runnable() {
            @Override
            public void run() {
                log.info("Reconnecting !");
                try {
                    WebSocketClient.connect(WebSocketClient.configureBootstrap(new Bootstrap(), loop, commandHandler.getDeployer(), commandHandler.getConfig()));
                } catch (URISyntaxException e) {
                    log.error("Error starting WS",e);
                } catch (SSLException e) {
                    log.error("Error starting WS",e);
                } catch (Deployer.PasswordException e) {
                    log.error("Error starting WS",e);
                }
            }
        }, WebSocketClient.RECONNECT_DELAY, TimeUnit.SECONDS);
    }



    public void stop()
    {
        this.handshakeFuture.cancel(true);
    }


}