package org.pyj.demo;

import io.netty.handler.codec.http.HttpHeaders;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.pyj.yeauty.annotation.OnClose;
import org.pyj.yeauty.annotation.OnError;
import org.pyj.yeauty.annotation.OnMessage;
import org.pyj.yeauty.annotation.OnOpen;
import org.pyj.yeauty.annotation.RequestParam;
import org.pyj.yeauty.annotation.ServerPath;
import org.pyj.yeauty.pojo.Session;

/**
 * @author pengyongjian
 * @Description:
 * @date 2020/12/21 下午2:58
 */
@Component
@Slf4j
@ServerPath(path = "/connect2")
public class WebSocket2 {

    @OnOpen
    public void onOpen(Session session, @RequestParam Map<String, Object> map, HttpHeaders headers) {
        System.out.println(session.id());
    }

    @OnClose
    public void onClose(Session session) {
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
    }

    @OnMessage
    public void OnMessage(Session session, String message) {
        System.out.println(message);
    }
}
