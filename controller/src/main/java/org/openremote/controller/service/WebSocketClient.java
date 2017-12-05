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
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.base64.Base64;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.util.CharsetUtil;
import org.openremote.controller.Constants;
import org.openremote.controller.ControllerConfiguration;
import org.openremote.controller.utils.Logger;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketClient {

   private final static Logger log = Logger.getLogger(Constants.BEEHIVE_COMMAND_WEBSOCKET_LOG_CATEGORY);


   static void connect(Bootstrap b) {

      b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000).
              connect().addListener(new ChannelFutureListener() {
         @Override
         public void operationComplete(ChannelFuture future) throws Exception {
            if (future.cause() != null) {
               log.error("Error connecting to CCS WS:"+ future.cause().getMessage() );
            } else if (! future.channel().isActive()) {
               log.error("Error Connect operation done without channel active");
            } else {
               log.info("WS Connection established");
            }
         }
      });
      log.info("WS Connection in progress");
   }

   /* create new EventLoop on first start */
   static Bootstrap configureBootstrap(Bootstrap b, Deployer deployer, ControllerConfiguration config) throws URISyntaxException, SSLException, Deployer.PasswordException {

      return configureBootstrap(b, new NioEventLoopGroup(), deployer, config);
   }

   static Bootstrap configureBootstrap(Bootstrap b, EventLoopGroup g, Deployer deployer, ControllerConfiguration config) throws URISyntaxException, SSLException, Deployer.PasswordException {

      URI uri = new URI(config.getRemoteCommandServiceWsURI());

      String scheme = uri.getScheme() == null ? "ws" : uri.getScheme();
      final String host = uri.getHost() == null ? "127.0.0.1" : uri.getHost();
      final int port;
      if (uri.getPort() == -1) {
         if ("ws".equalsIgnoreCase(scheme)) {
            port = 80;
         } else if ("wss".equalsIgnoreCase(scheme)) {
            port = 443;
         } else {
            port = -1;
         }
      } else {
         port = uri.getPort();
      }

      final boolean ssl = "wss".equalsIgnoreCase(scheme);
      final SslContext sslCtx;
      if (ssl) {
         sslCtx = SslContextBuilder.forClient().startTls(true).build();
      } else {
         sslCtx = null;
      }

      DefaultHttpHeaders customHeaders = new DefaultHttpHeaders();

      ByteBuf authz = Unpooled.copiedBuffer(deployer.getUserName()+":" + deployer.getPassword(deployer.getUserName()), CharsetUtil.UTF_8);
      ByteBuf authzBase64 = Base64.encode(authz);

      customHeaders.set(HttpHeaderNames.AUTHORIZATION, "Basic " + authzBase64.toString(CharsetUtil.US_ASCII));
      final WebSocketClientHandler handler =
            new WebSocketClientHandler(
                  WebSocketClientHandshakerFactory.newHandshaker(
                        uri, WebSocketVersion.V13, null, true, customHeaders),new CommandHandler(deployer, config), config.getWSTimeout(), config.getReconnectionDelay());
      b.group(g)
            .channel(NioSocketChannel.class)
            .remoteAddress(host, port)
            .handler(new ChannelInitializer<SocketChannel>() {
               @Override
               protected void initChannel(SocketChannel ch) {
                  ChannelPipeline p = ch.pipeline();
                  if (sslCtx != null) {
                     p.addLast(sslCtx.newHandler(ch.alloc(), host, port));
                  }
                  p.addLast(
                        new HttpClientCodec(),
                        new HttpObjectAggregator(8192),
                        WebSocketClientCompressionHandler.INSTANCE,
                        handler);
               }
            });
      return b;
   }

}
