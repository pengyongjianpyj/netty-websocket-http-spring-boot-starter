package org.pyj.http;

import static io.netty.handler.codec.http.HttpHeaderNames.ACCESS_CONTROL_ALLOW_HEADERS;
import static io.netty.handler.codec.http.HttpHeaderNames.ACCESS_CONTROL_ALLOW_METHODS;
import static io.netty.handler.codec.http.HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaderNames.CONTENT_TYPE;


import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * @Description: http响应信息类
 * @Author: pengyongjian
 * @Date: 2020-04-05 10:14
 */
public class NettyHttpResponse extends DefaultFullHttpResponse {

  private static final PooledByteBufAllocator BYTE_BUF_ALLOCATOR = new PooledByteBufAllocator(false);

  private static final String CONTENT_NORMAL_200 = "{\"code\":200,\"message\":\"OK\"}";
  private static final String CONTENT_ERROR_401 = "{\"code\":401,\"message\":\"UNAUTHORIZED\"}";
  private static final String CONTENT_ERROR_404 = "{\"code\":404,\"message\":\"REQUEST PATH NOT FOUND\"}";
  private static final String CONTENT_ERROR_405 = "{\"code\":405,\"message\":\"METHOD NOT ALLOWED\"}";
  private static final String CONTENT_ERROR_500 = "{\"code\":500,\"message\":\"%s\"}";

  private String content;

  private NettyHttpResponse(HttpResponseStatus status, ByteBuf buffer) {
    super(HttpVersion.HTTP_1_1, status, buffer);
    headers().set(CONTENT_TYPE, "application/json");
    headers().setInt(CONTENT_LENGTH, content().readableBytes());

    /**
     * 支持CORS 跨域访问
     */
    headers().set(ACCESS_CONTROL_ALLOW_ORIGIN, "*");
    headers().set(ACCESS_CONTROL_ALLOW_HEADERS, "Origin, X-Requested-With, Content-Type, Accept, RCS-ACCESS-TOKEN");
    headers().set(ACCESS_CONTROL_ALLOW_METHODS, "GET,POST,PUT,DELETE");
  }

  public static FullHttpResponse make(HttpResponseStatus status) {
    if (HttpResponseStatus.UNAUTHORIZED == status) {
      return NettyHttpResponse.make(HttpResponseStatus.UNAUTHORIZED, CONTENT_ERROR_401);
    }
    if (HttpResponseStatus.NOT_FOUND == status) {
      return NettyHttpResponse.make(HttpResponseStatus.NOT_FOUND, CONTENT_ERROR_404);
    }
    if (HttpResponseStatus.METHOD_NOT_ALLOWED == status) {
      return NettyHttpResponse.make(HttpResponseStatus.METHOD_NOT_ALLOWED, CONTENT_ERROR_405);
    }
    return NettyHttpResponse.make(HttpResponseStatus.OK, CONTENT_NORMAL_200);
  }

  public static FullHttpResponse makeError(Exception exception) {
    String message = exception.getClass().getName() + ":" + exception.getMessage();
    return NettyHttpResponse.make(HttpResponseStatus.INTERNAL_SERVER_ERROR, String.format(CONTENT_ERROR_500, message));
  }

  public static FullHttpResponse ok(String content) {
    return make(HttpResponseStatus.OK, content);
  }

  private static FullHttpResponse make(HttpResponseStatus status, String content) {
    byte[] body = content.getBytes();
    ByteBuf buffer = BYTE_BUF_ALLOCATOR.buffer(body.length);
    buffer.writeBytes(body);
    NettyHttpResponse response = new NettyHttpResponse(status, buffer);
    response.content = content;
    return response;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(protocolVersion().toString()).append(" ").append(status().toString()).append("\n");
    builder.append(CONTENT_TYPE).append(": ").append(headers().get(CONTENT_TYPE)).append("\n");
    builder.append(CONTENT_LENGTH).append(": ").append(headers().get(CONTENT_LENGTH)).append("\n");
    builder.append("content-body").append(": ").append(content).append("\n");
    return builder.toString();
  }
}
