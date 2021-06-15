package org.pyj;

import static io.netty.handler.codec.http.HttpHeaderNames.SEC_WEBSOCKET_KEY;
import static io.netty.handler.codec.http.HttpHeaderNames.SEC_WEBSOCKET_VERSION;
import static io.netty.handler.codec.http.HttpHeaderNames.UPGRADE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.cors.CorsHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrameAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import java.util.Set;
import org.pyj.config.ServerEndpointConfig;
import org.pyj.yeauty.pojo.PojoEndpointServer;
import org.pyj.yeauty.support.WsPathMatcher;
import org.springframework.beans.TypeMismatchException;
import org.springframework.util.StringUtils;

/**
 * @author pengyongjian
 * @Description: websocket服务处理类
 * @date 2020-03-16 14:07
 */
@ChannelHandler.Sharable
public class WebSocketServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

  private static PojoEndpointServer pojoEndpointServer;

  private final ServerEndpointConfig config;

  public WebSocketServerHandler(PojoEndpointServer pojoEndpointServer, ServerEndpointConfig config) {
    WebSocketServerHandler.pojoEndpointServer = pojoEndpointServer;
    this.config = config;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, WebSocketFrame msg) throws Exception {
    handleWebSocketFrame(ctx, msg);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
    cause.printStackTrace();
    pojoEndpointServer.doOnError(ctx.channel(), cause);
  }

  @Override
  public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    pojoEndpointServer.doOnClose(ctx.channel());
  }

  @Override
  public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
    pojoEndpointServer.doOnEvent(ctx.channel(), evt);
  }

  private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
    if (frame instanceof TextWebSocketFrame) {
      pojoEndpointServer.doOnMessage(ctx.channel(), frame);
      return;
    }
    if (frame instanceof PingWebSocketFrame) {
      ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
      return;
    }
    if (frame instanceof CloseWebSocketFrame) {
      ctx.writeAndFlush(frame.retainedDuplicate()).addListener(ChannelFutureListener.CLOSE);
      return;
    }
    if (frame instanceof BinaryWebSocketFrame) {
      pojoEndpointServer.doOnBinary(ctx.channel(), frame);
      return;
    }
    if (frame instanceof PongWebSocketFrame) {
      return;
    }
  }

  public boolean isWebSocket(ChannelHandlerContext ctx, FullHttpRequest request){
    //path match 是否是websocket的路径
    Channel channel = ctx.channel();
    QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
    String pattern = null;
    Set<WsPathMatcher> pathMatcherSet = pojoEndpointServer.getPathMatcherSet();
    for (WsPathMatcher pathMatcher : pathMatcherSet) {
      if (pathMatcher.matchAndExtract(decoder, channel)) {
        pattern = pathMatcher.getPattern();
        break;
      }
    }

    // 如果是websocket的请求路径,当做websocket请求来处理
    if (pattern != null) {
      channelReadWebSocket(ctx, request, pattern);
      return true;
    }
    // 否则用http请求路径来处理
    else {
      return false;
    }
  }

  /**
   * @Description: 处理websocket请求
   * @Author: pengyongjian
   * @Date: 2020-04-11 12:08
   * @param: ctx
   * @param: req
   * @return: void
   */
  private void channelReadWebSocket(ChannelHandlerContext ctx, FullHttpRequest req, String pattern) {
    FullHttpResponse res;
    // Handle a bad request.
    if (!req.decoderResult().isSuccess()) {
      res = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
      sendWebSocketResponse(ctx, req, res);
      return;
    }

    // Allow only GET methods.
    if (req.method() != GET) {
      res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
      sendWebSocketResponse(ctx, req, res);
      return;
    }

    // check request host
    String host = req.headers().get(HttpHeaderNames.HOST);
    if (StringUtils.isEmpty(host)) {
      res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
      sendWebSocketResponse(ctx, req, res);
      return;
    }
    if (!StringUtils.isEmpty(pojoEndpointServer.getHost()) && !pojoEndpointServer.getHost().equals("0.0.0.0")
        && !pojoEndpointServer.getHost().equals(host.split(":")[0])) {
      res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
      sendWebSocketResponse(ctx, req, res);
      return;
    }

    // check wesocket's header msg
    if (!req.headers().contains(UPGRADE) || !req.headers().contains(SEC_WEBSOCKET_KEY)
        || !req.headers().contains(SEC_WEBSOCKET_VERSION)) {
      res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
      sendWebSocketResponse(ctx, req, res);
      return;
    }

    // 处理请求
    try {
      handleWebSocketRequest(ctx, req, pattern);
    } catch (TypeMismatchException e) {
      res = new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST);
      sendWebSocketResponse(ctx, req, res);
      e.printStackTrace();
    } catch (Exception e) {
      res = new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR);
      sendWebSocketResponse(ctx, req, res);
      e.printStackTrace();
    }
  }

  private static void sendWebSocketResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
    // Generate an error page if response getStatus code is not OK (200).
    int statusCode = res.status().code();
    if (statusCode != OK.code() && res.content().readableBytes() == 0) {
      ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
      res.content().writeBytes(buf);
      buf.release();
    }
    HttpUtil.setContentLength(res, res.content().readableBytes());

    // Send the response and close the connection if necessary.
    ChannelFuture f = ctx.channel().writeAndFlush(res);
    if (!HttpUtil.isKeepAlive(req) || statusCode != 200) {
      f.addListener(ChannelFutureListener.CLOSE);
    }
  }

  private void handleWebSocketRequest(ChannelHandlerContext ctx, FullHttpRequest req, String pattern) {

    String subprotocols = null;
    Channel channel = ctx.channel();
    if (pojoEndpointServer.hasBeforeHandshake(channel, pattern)) {
      pojoEndpointServer.doBeforeHandshake(channel, req, pattern);
      if (!channel.isActive()) {
        return;
      }

      AttributeKey<String> subprotocolsAttrKey = AttributeKey.valueOf("subprotocols");
      if (channel.hasAttr(subprotocolsAttrKey)) {
        subprotocols = ctx.channel().attr(subprotocolsAttrKey).get();
      }
    }

    // Handshake
    String fullUrl = "ws://" + req.headers().get(HttpHeaderNames.HOST) + req.uri();
    WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(fullUrl, subprotocols,
        true, config.getmaxFramePayloadLength());
    WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
    if (handshaker == null) {
      WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channel);
    } else {
      ChannelPipeline pipeline = ctx.pipeline();
      pipeline.remove(ctx.name());
      if (config.getReaderIdleTimeSeconds() != 0
          || config.getWriterIdleTimeSeconds() != 0
          || config.getAllIdleTimeSeconds() != 0) {
        pipeline.addLast(new IdleStateHandler(config.getReaderIdleTimeSeconds(),
            config.getWriterIdleTimeSeconds(), config.getAllIdleTimeSeconds()));
      }
      if (config.isUseCompressionHandler()) {
        pipeline.addLast(new WebSocketServerCompressionHandler());
      }
      // 管道添加WebSocketServerHandler
      pipeline.addLast(this);
      String finalPattern = pattern;
      handshaker.handshake(channel, req).addListener(future -> {
        if (future.isSuccess()) {
          pojoEndpointServer.doOnOpen(channel, req, finalPattern);
        } else {
          handshaker.close(channel, new CloseWebSocketFrame());
        }
      });
    }
  }

}
