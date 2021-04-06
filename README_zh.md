netty-websocket-http-spring-boot-starter [![License](http://img.shields.io/:license-apache-brightgreen.svg)](http://www.apache.org/licenses/LICENSE-2.0.html)
===================================

[English Docs](https://github.com/pengyongjianpyj/netty-websocket-http-spring-boot-starter/blob/master/README.md)

### 简介
本项目帮助你在spring-boot中使用Netty来开发WebSocket服务器，并像spring-websocket的注解开发一样简单，还可以帮助你在spring-boot中使用Netty来开发简单的Http服务器

WebSocket和Http使用统一端口（默认8080），方便网络方面的管理，另外本项目可用作微服务，只需添加注册发现相关依赖即可

### 要求
- jdk版本为1.8或1.8+


### 快速开始

- 添加依赖:

```xml
    <dependency>
        <groupId>org.pyj</groupId>
        <artifactId>netty-websocket-http-spring-boot-starter</artifactId>
        <version>0.1.0</version>
    </dependency>
```

- spring-boot启动类

```java
import org.pyj.http.annotation.NettyHttpHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;

@SpringBootApplication
@ComponentScan(includeFilters = @Filter(NettyHttpHandler.class))
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

- restful风格的http返回结果实现类

```java
import org.pyj.http.handler.Result;
import java.io.Serializable;

public class ResultJson<T> implements Result<T>, Serializable {

    private static final long serialVersionUID = 1;

    private int code;

    private T t;

    public ResultJson(int code, T t) {
        this.code = code;
        this.t = t;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    @Override
    public String toString() {
        return "ResultJson{" +
                "code=" + code +
                ", t=" + t +
                '}';
    }
}
```

- http表现层接口类

```java
import org.pyj.http.NettyHttpRequest;
import org.pyj.http.annotation.NettyHttpHandler;
import org.pyj.http.handler.IFunctionHandler;
import org.pyj.http.handler.Result;

@NettyHttpHandler(path = "/temp/body",method = "POST")
public class TempHandler implements IFunctionHandler<String> {

    @Override
    public Result<String> execute(NettyHttpRequest request) {
        String contentText = request.contentText();
        return new ResultJson<String>(200, contentText);
    }
}
```


- websocket入口类 在端点类上加上`@ServerEndpoint`注解，并在相应的方法上加上`@BeforeHandshake`、`@OnOpen`、`@OnClose`、`@OnError`、`@OnMessage`、`@OnBinary`、`@OnEvent`注解，样例如下：

```java
@ServerEndpoint(port = "${server.port}", path = "/connect")
public class MyWebSocket {

    @BeforeHandshake
    public void handshake(Session session, HttpHeaders headers, @RequestParam String req, @RequestParam MultiValueMap reqMap, @PathVariable String arg, @PathVariable Map pathMap){
        session.setSubprotocols("stomp");
        if (!"ok".equals(req)){
            System.out.println("Authentication failed!");
            session.close();
        }
    }
    
    @OnOpen
    public void onOpen(Session session, HttpHeaders headers, @RequestParam String req, @RequestParam MultiValueMap reqMap, @PathVariable String arg, @PathVariable Map pathMap){
        System.out.println("new connection");
        System.out.println(req);
    }

    @OnClose
    public void onClose(Session session) throws IOException {
       System.out.println("one connection closed"); 
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        throwable.printStackTrace();
    }

    @OnMessage
    public void onMessage(Session session, String message) {
        System.out.println(message);
        session.sendText("Hello Netty!");
    }

    @OnBinary
    public void onBinary(Session session, byte[] bytes) {
        for (byte b : bytes) {
            System.out.println(b);
        }
        session.sendBinary(bytes); 
    }

    @OnEvent
    public void onEvent(Session session, Object evt) {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent idleStateEvent = (IdleStateEvent) evt;
            switch (idleStateEvent.state()) {
                case READER_IDLE:
                    System.out.println("read idle");
                    break;
                case WRITER_IDLE:
                    System.out.println("write idle");
                    break;
                case ALL_IDLE:
                    System.out.println("all idle");
                    break;
                default:
                    break;
            }
        }
    }

}
```

- 打开WebSocket客户端，连接到`ws://127.0.0.1:8080/connect`


### 注解
###### @ServerEndpoint 
> 当ServerEndpointExporter类通过Spring配置进行声明并被使用，它将会去扫描带有@ServerEndpoint注解的类
> 被注解的类将被注册成为一个WebSocket端点
> 所有的[配置项](#%E9%85%8D%E7%BD%AE)都在这个注解的属性中 ( 如:`@ServerEndpoint(port = "${server.port}", path = "/connect")` )

###### @BeforeHandshake 
> 当有新的连接进入时，对该方法进行回调
> 注入参数的类型:Session、HttpHeaders...

###### @OnOpen 
> 当有新的WebSocket连接完成时，对该方法进行回调
> 注入参数的类型:Session、HttpHeaders...

###### @OnClose
> 当有WebSocket连接关闭时，对该方法进行回调
> 注入参数的类型:Session

###### @OnError
> 当有WebSocket抛出异常时，对该方法进行回调
> 注入参数的类型:Session、Throwable

###### @OnMessage
> 当接收到字符串消息时，对该方法进行回调
> 注入参数的类型:Session、String

###### @OnBinary
> 当接收到二进制消息时，对该方法进行回调
> 注入参数的类型:Session、byte[]

###### @OnEvent
> 当接收到Netty的事件时，对该方法进行回调
> 注入参数的类型:Session、Object
