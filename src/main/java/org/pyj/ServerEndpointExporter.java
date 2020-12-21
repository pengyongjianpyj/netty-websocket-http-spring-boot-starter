package org.pyj;

import org.pyj.http.annotation.NettyHttpHandler;
import org.pyj.exception.IllegalPathDuplicatedException;
import org.pyj.http.handler.IFunctionHandler;
import org.pyj.http.path.Path;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.TypeMismatchException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanExpressionContext;
import org.springframework.beans.factory.config.BeanExpressionResolver;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.env.Environment;
import org.yeauty.annotation.ServerEndpoint;
import org.yeauty.exception.DeploymentException;
import org.yeauty.pojo.PojoEndpointServer;
import org.yeauty.pojo.PojoMethodMapping;
import org.yeauty.standard.ServerEndpointConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pengyongjian
 * @Description:
 * @date 2020-03-17 15:52
 */
@Slf4j
public class ServerEndpointExporter extends org.yeauty.standard.ServerEndpointExporter {

    @Autowired
    Environment environment;

    private AbstractBeanFactory beanFactory;

    @Autowired
    private WebsocketServer websocketServer;

    @Override
    public void afterSingletonsInstantiated() {
        registerEndpoint();
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        if (!(beanFactory instanceof AbstractBeanFactory)) {
            throw new IllegalArgumentException("AutowiredAnnotationBeanPostProcessor requires a AbstractBeanFactory: " + beanFactory);
        }
        this.beanFactory = (AbstractBeanFactory) beanFactory;
    }

    private void registerEndpoint() {
        // 获取websocket类，及其注解配置信息
        ApplicationContext context = getApplicationContext();
        String[] endpointBeanNames = context.getBeanNamesForAnnotation(ServerEndpoint.class);
        if(endpointBeanNames.length > 1){
            logger.error("<artifactId>netty-websocket-spring-boot-starter</artifactId> too many @ServerEndpoint class ");
        }
        if(endpointBeanNames.length == 0){
            logger.error("<artifactId>netty-websocket-spring-boot-starter</artifactId> no @ServerEndpoint class ");
        }
        Class<?> endpointClass = context.getType(endpointBeanNames[0]);

        ServerEndpoint annotation = AnnotatedElementUtils.findMergedAnnotation(endpointClass, ServerEndpoint.class);
        if (annotation == null) {
            throw new IllegalStateException("missingAnnotation ServerEndpoint");
        }
        // 获取配置信息并封装到serverEndpointConfig中
        ServerEndpointConfig serverEndpointConfig = buildConfig(annotation);
        // 获取websocket方法并生成映射pojoMethodMapping
        PojoMethodMapping pojoMethodMapping = null;
        try {
            pojoMethodMapping = new PojoMethodMapping(endpointClass, context, beanFactory);
        } catch (DeploymentException e) {
            throw new IllegalStateException("Failed to register ServerEndpointConfig: " + serverEndpointConfig, e);
        }
        // 解析获取websocket的路径path
        String path = resolveAnnotationValue(annotation.value(), String.class, "path");
        // 创建websocket的业务对象
        PojoEndpointServer pojoEndpointServer = new PojoEndpointServer(pojoMethodMapping, serverEndpointConfig, path);
        // 获取http接口的路径接口映射关系
        Map<Path, IFunctionHandler> functionHandlerMap = getFunctionHandlerMap();
        // 创建处理业务类对象
        WebSocketServerHandler webSocketServerHandler = new WebSocketServerHandler(pojoEndpointServer);
        // 创建http处理业务类对象
        HttpServerHandler httpServerHandler = new HttpServerHandler(pojoEndpointServer, serverEndpointConfig,
                functionHandlerMap, webSocketServerHandler);
        // 将http处理业务类对象注入到netty的web容器中
        websocketServer.setHttpServerHandler(httpServerHandler);
        // netty的web容器的启动
        init(serverEndpointConfig.getPort());
    }

    private void init(int port) {
        try {
            websocketServer.init();
            logger.info("=====Netty WebSocket started on port:" + port + " =====");
        } catch (Exception e) {
            logger.error("websocket init fail", e);
        }
    }

    private ServerEndpointConfig buildConfig(ServerEndpoint annotation) {
        String host = resolveAnnotationValue(annotation.host(), String.class, "host");
        int port = resolveAnnotationValue(annotation.port(), Integer.class, "port");
        String path = resolveAnnotationValue(annotation.value(), String.class, "value");
        int bossLoopGroupThreads = resolveAnnotationValue(annotation.bossLoopGroupThreads(), Integer.class, "bossLoopGroupThreads");
        int workerLoopGroupThreads = resolveAnnotationValue(annotation.workerLoopGroupThreads(), Integer.class, "workerLoopGroupThreads");
        boolean useCompressionHandler = resolveAnnotationValue(annotation.useCompressionHandler(), Boolean.class, "useCompressionHandler");

        int optionConnectTimeoutMillis = resolveAnnotationValue(annotation.optionConnectTimeoutMillis(), Integer.class, "optionConnectTimeoutMillis");
        int optionSoBacklog = resolveAnnotationValue(annotation.optionSoBacklog(), Integer.class, "optionSoBacklog");

        int childOptionWriteSpinCount = resolveAnnotationValue(annotation.childOptionWriteSpinCount(), Integer.class, "childOptionWriteSpinCount");
        int childOptionWriteBufferHighWaterMark = resolveAnnotationValue(annotation.childOptionWriteBufferHighWaterMark(), Integer.class, "childOptionWriteBufferHighWaterMark");
        int childOptionWriteBufferLowWaterMark = resolveAnnotationValue(annotation.childOptionWriteBufferLowWaterMark(), Integer.class, "childOptionWriteBufferLowWaterMark");
        int childOptionSoRcvbuf = resolveAnnotationValue(annotation.childOptionSoRcvbuf(), Integer.class, "childOptionSoRcvbuf");
        int childOptionSoSndbuf = resolveAnnotationValue(annotation.childOptionSoSndbuf(), Integer.class, "childOptionSoSndbuf");
        boolean childOptionTcpNodelay = resolveAnnotationValue(annotation.childOptionTcpNodelay(), Boolean.class, "childOptionTcpNodelay");
        boolean childOptionSoKeepalive = resolveAnnotationValue(annotation.childOptionSoKeepalive(), Boolean.class, "childOptionSoKeepalive");
        int childOptionSoLinger = resolveAnnotationValue(annotation.childOptionSoLinger(), Integer.class, "childOptionSoLinger");
        boolean childOptionAllowHalfClosure = resolveAnnotationValue(annotation.childOptionAllowHalfClosure(), Boolean.class, "childOptionAllowHalfClosure");

        int readerIdleTimeSeconds = resolveAnnotationValue(annotation.readerIdleTimeSeconds(), Integer.class, "readerIdleTimeSeconds");
        int writerIdleTimeSeconds = resolveAnnotationValue(annotation.writerIdleTimeSeconds(), Integer.class, "writerIdleTimeSeconds");
        int allIdleTimeSeconds = resolveAnnotationValue(annotation.allIdleTimeSeconds(), Integer.class, "allIdleTimeSeconds");

        int maxFramePayloadLength = resolveAnnotationValue(annotation.maxFramePayloadLength(), Integer.class, "maxFramePayloadLength");

        ServerEndpointConfig serverEndpointConfig = new ServerEndpointConfig(host, port, path, bossLoopGroupThreads, workerLoopGroupThreads, useCompressionHandler, optionConnectTimeoutMillis, optionSoBacklog, childOptionWriteSpinCount, childOptionWriteBufferHighWaterMark, childOptionWriteBufferLowWaterMark, childOptionSoRcvbuf, childOptionSoSndbuf, childOptionTcpNodelay, childOptionSoKeepalive, childOptionSoLinger, childOptionAllowHalfClosure, readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds, maxFramePayloadLength);

        return serverEndpointConfig;
    }

    private <T> T resolveAnnotationValue(Object value, Class<T> requiredType, String paramName) {
        if (value == null) {
            return null;
        }
        TypeConverter typeConverter = beanFactory.getTypeConverter();
        if (typeConverter == null) {
            throw new IllegalArgumentException(
                    "TypeConverter of AbstractBeanFactory is null: " + beanFactory);
        }
        if (value instanceof String) {
            String strVal = beanFactory.resolveEmbeddedValue((String) value);
            BeanExpressionResolver beanExpressionResolver = beanFactory.getBeanExpressionResolver();
            if (beanExpressionResolver != null) {
                value = beanExpressionResolver.evaluate(strVal, new BeanExpressionContext(beanFactory, null));
            } else {
                value = strVal;
            }
        }
        try {
            return typeConverter.convertIfNecessary(value, requiredType);
        } catch (TypeMismatchException e) {
            throw new IllegalArgumentException("Failed to convert value of parameter '" + paramName + "' to required type '" + requiredType.getName() + "'");
        }
    }

    public Map<Path, IFunctionHandler> getFunctionHandlerMap() {
        HashMap<Path, IFunctionHandler> functionHandlerMap = new HashMap<>();
        ApplicationContext applicationContext = getApplicationContext();
        Map<String, Object> handlers =  applicationContext.getBeansWithAnnotation(NettyHttpHandler.class);
        for (Map.Entry<String, Object> entry : handlers.entrySet()) {
            Object handler = entry.getValue();
            Path path = Path.make(handler.getClass().getAnnotation(NettyHttpHandler.class));
            if (functionHandlerMap.containsKey(path)){
                logger.error("IFunctionHandler has duplicated :" + path.toString(),new IllegalPathDuplicatedException());
                System.exit(0);
            }
            logger.info(path.toString());
            functionHandlerMap.put(path, (IFunctionHandler) handler);
        }
        return functionHandlerMap;
    }

}
