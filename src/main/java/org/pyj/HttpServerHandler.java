package org.pyj;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.pyj.exception.IllegalMethodNotAllowedException;
import org.pyj.exception.IllegalPathNotFoundException;
import org.pyj.http.handler.IFunctionHandler;
import org.pyj.http.NettyHttpRequest;
import org.pyj.http.NettyHttpResponse;
import org.pyj.http.path.Path;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.AttributeKey;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;
import org.springframework.beans.TypeMismatchException;
import org.springframework.util.StringUtils;
import org.yeauty.pojo.PojoEndpointServer;
import org.yeauty.standard.ServerEndpointConfig;
import org.yeauty.support.WsPathMatcher;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static io.netty.handler.codec.http.HttpHeaderNames.*;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.*;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * @author pengyongjian
 * @Description: 管道（信息）处理类，处理websocket/http请求
 * @date 2020-03-17 15:52
 */
@ChannelHandler.Sharable
@Slf4j
public class HttpServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private PojoEndpointServer pojoEndpointServer;

    private ServerEndpointConfig config;

    private Map<Path, IFunctionHandler> functionHandlerMap;

    private WebSocketServerHandler webSocketServerHandler;

    public HttpServerHandler(PojoEndpointServer pojoEndpointServer, ServerEndpointConfig config,
                             Map<Path, IFunctionHandler> functionHandlerMap, WebSocketServerHandler webSocketServerHandler) {
        this.pojoEndpointServer = pojoEndpointServer;
        this.config = config;
        this.functionHandlerMap = functionHandlerMap;
        this.webSocketServerHandler = webSocketServerHandler;
    }

    public ServerEndpointConfig getConfig() {
        return config;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
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
        if(pattern != null){
            channelReadWebSocket(ctx, request, pattern);
        }
        // 否则用http请求路径来处理
        else {
            channelReadHttp(ctx, request);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        pojoEndpointServer.doOnError(ctx.channel(), cause);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        pojoEndpointServer.doOnClose(ctx.channel());
        super.channelInactive(ctx);
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
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
        if (!StringUtils.isEmpty(pojoEndpointServer.getHost()) && !pojoEndpointServer.getHost().equals("0.0.0.0") && !pojoEndpointServer.getHost().equals(host.split(":")[0])) {
            res = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
            sendWebSocketResponse(ctx, req, res);
            return;
        }

        // check wesocket's header msg
        if (!req.headers().contains(UPGRADE) || !req.headers().contains(SEC_WEBSOCKET_KEY) || !req.headers().contains(SEC_WEBSOCKET_VERSION)) {
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
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(fullUrl, subprotocols, true, config.getmaxFramePayloadLength());
        WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(channel);
        } else {
            ChannelPipeline pipeline = ctx.pipeline();
            pipeline.remove(ctx.name());
            if (config.getReaderIdleTimeSeconds() != 0 || config.getWriterIdleTimeSeconds() != 0 || config.getAllIdleTimeSeconds() != 0) {
                pipeline.addLast(new IdleStateHandler(config.getReaderIdleTimeSeconds(), config.getWriterIdleTimeSeconds(), config.getAllIdleTimeSeconds()));
            }
            if (config.isUseCompressionHandler()) {
                pipeline.addLast(new WebSocketServerCompressionHandler());
            }
            // 管道添加WebSocketServerHandler
            pipeline.addLast(webSocketServerHandler);
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

    /**
     * @Description: 处理http请求
     * @Author: pengyongjian
     * @Date: 2020-04-11 12:08
     * @param: ctx
     * @param: request
     * @return: void
     */
    private void channelReadHttp(ChannelHandlerContext ctx, FullHttpRequest request) {
        FullHttpRequest copyRequest = request.copy();
        ctx.executor().execute(() -> onReceivedRequest(ctx,new NettyHttpRequest(copyRequest)));
    }

    private void onReceivedRequest(ChannelHandlerContext context, NettyHttpRequest request){
        FullHttpResponse response = handleHttpRequest(request);
        context.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        ReferenceCountUtil.release(request);
    }

    private FullHttpResponse handleHttpRequest(NettyHttpRequest request) {
        IFunctionHandler functionHandler = null;
        try {
            functionHandler = matchFunctionHandler(request);
            Object response =  functionHandler.execute(request);
            return NettyHttpResponse.ok(response.toString());
        }
        catch (IllegalMethodNotAllowedException error){
            return NettyHttpResponse.make(HttpResponseStatus.METHOD_NOT_ALLOWED);
        }
        catch (IllegalPathNotFoundException error){
            return NettyHttpResponse.make(HttpResponseStatus.NOT_FOUND);
        }
        catch (Exception error){
            logger.error(functionHandler.getClass().getSimpleName() + " Error",error);
            return NettyHttpResponse.makeError(error);
        }
    }

    private IFunctionHandler matchFunctionHandler(NettyHttpRequest request) throws IllegalPathNotFoundException, IllegalMethodNotAllowedException {

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

        if (!optional.isPresent() && !matched.get()){
            throw  new IllegalPathNotFoundException();
        }

        if (!optional.isPresent() && matched.get()){
            throw  new IllegalMethodNotAllowedException();
        }

        return functionHandlerMap.get(optional.get());
    }
}