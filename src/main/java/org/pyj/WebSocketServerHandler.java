package org.pyj;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.pyj.yeauty.pojo.PojoEndpointServer;

/**
 * @author pengyongjian
 * @Description: websocket服务处理类
 * @date 2020-03-16 14:07
 */
@ChannelHandler.Sharable
class WebSocketServerHandler extends SimpleChannelInboundHandler<WebSocketFrame> {

  private final PojoEndpointServer pojoEndpointServer;

  public WebSocketServerHandler(PojoEndpointServer pojoEndpointServer) {
    this.pojoEndpointServer = pojoEndpointServer;
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

}
