package org.pyj.demo;

import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.yeauty.annotation.*;
import org.yeauty.pojo.Session;

import java.io.IOException;
import java.util.Map;

/**
 * @author pengyongjian
 * @Description:
 * @date 2020/12/21 下午2:58
 */
@Component
@Slf4j
@ServerEndpoint(port = "${server.port}", path = "/connect")
public class WebSocket {
    @OnOpen
    public void onOpen(Session session, @RequestParam Map<String, Object> map, HttpHeaders headers) {
        System.out.println(session.id());
    }

    @OnClose
    public void onClose(Session session) throws IOException {
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
    }

    @OnMessage
    public void OnMessage(Session session, String message) {
    }
}
