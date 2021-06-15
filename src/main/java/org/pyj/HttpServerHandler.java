package org.pyj;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pyj.exception.IllegalMethodNotAllowedException;
import org.pyj.exception.IllegalPathNotFoundException;
import org.pyj.http.NettyHttpRequest;
import org.pyj.http.NettyHttpResponse;
import org.pyj.http.handler.IFunctionHandler;
import org.pyj.http.path.Path;

/**
 * @author pengyongjian
 * @Description: 管道（信息）处理类，处理websocket/http请求
 * @date 2020-03-17 15:52
 */
@ChannelHandler.Sharable
@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

  protected final Log logger = LogFactory.getLog(this.getClass());

  private final WebSocketServerHandler webSocketServerHandler;

  private final Map<Path, IFunctionHandler> functionHandlerMap;

  public HttpServerHandler(WebSocketServerHandler webSocketServerHandler, Map<Path, IFunctionHandler> functionHandlerMap) {
    this.webSocketServerHandler = webSocketServerHandler;
    this.functionHandlerMap = functionHandlerMap;
  }

  @Override
  protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
    // 如果是websocket的请求路径,当做websocket请求来处
    if (!webSocketServerHandler.isWebSocket(ctx, request)) {
      // 否则用http请求路径来处
      channelReadHttp(ctx, request);
    }
  }

  /**
   * @Description: 处理http请求
   * @Author: pengyongjian
   * @Date: 2020-04-11 12:08
   * @param: ctx
   * @param: request
   * @return: void
   */
  public void channelReadHttp(ChannelHandlerContext ctx, FullHttpRequest request) {
    ctx.executor().parent().execute(() -> {
      FullHttpResponse response = handleHttpRequest(new NettyHttpRequest(request));
      ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    });
  }

  private FullHttpResponse handleHttpRequest(NettyHttpRequest request) {
    IFunctionHandler functionHandler = null;
    try {
      functionHandler = matchFunctionHandler(request);
      Object response = functionHandler.execute(request);
      return NettyHttpResponse.ok(response.toString());
    } catch (IllegalMethodNotAllowedException error) {
      return NettyHttpResponse.make(HttpResponseStatus.METHOD_NOT_ALLOWED);
    } catch (IllegalPathNotFoundException error) {
      return NettyHttpResponse.make(HttpResponseStatus.NOT_FOUND);
    } catch (Exception error) {
      logger.error(functionHandler.getClass().getSimpleName() + " Error", error);
      return NettyHttpResponse.makeError(error);
    }
  }

  private IFunctionHandler matchFunctionHandler(NettyHttpRequest request)
      throws IllegalPathNotFoundException, IllegalMethodNotAllowedException {

    AtomicBoolean matched = new AtomicBoolean(false);

    Stream<Path> stream = functionHandlerMap.keySet().stream()
        .filter(((Predicate<Path>) path -> {
          //过滤 Path URI 不匹配的
          if (request.matched(path.getUri(), path.isEqual())) {
            matched.set(true);
            return matched.get();
          }
          return false;

        }).and(path -> {
          //过滤 Method 匹配的
          return request.isAllowed(path.getMethod());
        }));

    Optional<Path> optional = stream.findFirst();

    stream.close();

    if (!optional.isPresent() && !matched.get()) {
      throw new IllegalPathNotFoundException();
    }

    if (!optional.isPresent() && matched.get()) {
      throw new IllegalMethodNotAllowedException();
    }

    return functionHandlerMap.get(optional.get());
  }

}
