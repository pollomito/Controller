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
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
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
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Enumeration;

/**
 * @author Mica Hallin
 */
public class BeehiveCommandCheckService {

  public static String getMACAddresses() throws Exception
  {
    StringBuilder macs = new StringBuilder();
    Enumeration<NetworkInterface> enum1 = NetworkInterface.getNetworkInterfaces();

    while (enum1.hasMoreElements())
    {
      NetworkInterface networkInterface = enum1.nextElement();

      if (!networkInterface.isLoopback())
      {
        boolean onlyLinkLocal = true;

        for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses())
        {
          if (!interfaceAddress.getAddress().isLinkLocalAddress())
          {
            onlyLinkLocal = false;
          }
        }

        if (onlyLinkLocal)
        {
          continue;
        }

        byte[] mac = networkInterface.getHardwareAddress();

        if (mac != null)
        {
          macs.append(getMACString(networkInterface.getHardwareAddress()));
          macs.append(",");
        }
      }
    }

    if (macs.length() == 0)
    {
      return "no-mac-address-found";
    }

    macs.deleteCharAt(macs.length()-1);

    return macs.toString();
  }

  private static String getMACString(byte[] mac)
  {
    StringBuilder sb = new StringBuilder();

    for (int i = 0; i < mac.length; i++)
    {
      sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
    }

    return sb.toString();
  }

  // Class Members --------------------------------------------------------------------------------

  /**
   * Log for this service.
   */
  private final static Logger log = Logger.getLogger(Constants.BEEHIVE_COMMAND_CHECKER_LOG_CATEGORY);
  private EventLoopGroup group;

  public void start(Deployer deployer, ControllerConfiguration config) {
    try {
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

      if (!"ws".equalsIgnoreCase(scheme) && !"wss".equalsIgnoreCase(scheme)) {
        System.err.println("Only WS(S) is supported.");
        return;
      }

      final boolean ssl = "wss".equalsIgnoreCase(scheme);
      final SslContext sslCtx;
      if (ssl) {
        sslCtx = SslContextBuilder.forClient().startTls(true).build();
      } else {
        sslCtx = null;
      }

      group = new NioEventLoopGroup();
      try {
        DefaultHttpHeaders customHeaders = new DefaultHttpHeaders();

        ByteBuf authz = Unpooled.copiedBuffer("user" + ":" + "password", CharsetUtil.UTF_8);
        ByteBuf authzBase64 = Base64.encode(authz);

        customHeaders.set(HttpHeaderNames.AUTHORIZATION, "Basic " + authzBase64.toString(CharsetUtil.US_ASCII));
        final WebSocketClientHandler handler =
              new WebSocketClientHandler(
                    WebSocketClientHandshakerFactory.newHandshaker(
                          uri, WebSocketVersion.V13, null, true, customHeaders), deployer, config);

        Bootstrap b = new Bootstrap();
        b.group(group)
              .channel(NioSocketChannel.class)
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

        Channel ch = b.connect(uri.getHost(), port).sync().channel();
        handler.handshakeFuture().sync();


      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    } catch (SSLException e) {
      e.printStackTrace();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }


  public void stop() {
    if (group != null) {
      group.shutdownGracefully();
    }
  }

}




