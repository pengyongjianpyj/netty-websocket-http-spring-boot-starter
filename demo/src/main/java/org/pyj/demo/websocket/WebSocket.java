package org.pyj.demo.websocket;

import io.netty.handler.codec.http.HttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.pyj.yeauty.annotation.OnClose;
import org.pyj.yeauty.annotation.ServerPath;
import org.springframework.stereotype.Component;
import org.pyj.yeauty.pojo.Session;

import java.util.Map;

/**
 * @author pengyongjian
 * @Description:
 * @date 2020/12/21 下午2:58
 */
@Component
@Slf4j
@ServerPath(path = "/connect")
public class WebSocket {

    @org.pyj.yeauty.annotation.OnOpen
    public void onOpen(Session session, @org.pyj.yeauty.annotation.RequestParam Map<String, Object> map, HttpHeaders headers) {
        System.out.println(session.id());
    }

    @OnClose
    public void onClose(Session session) {
    }

    @org.pyj.yeauty.annotation.OnError
    public void onError(Session session, Throwable throwable) {
    }

    @org.pyj.yeauty.annotation.OnMessage
    public void OnMessage(Session session, String message) {
        System.out.println(message);
    }
}
