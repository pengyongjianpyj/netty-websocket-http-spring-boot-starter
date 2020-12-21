package org.pyj.demo.handler;

import org.pyj.http.NettyHttpRequest;
import org.pyj.http.annotation.NettyHttpHandler;
import org.pyj.http.handler.IFunctionHandler;

/**
 * @Description: http接口，向对应用户发送消息
 * @Author: pengyongjian
 * @Date: 2020-04-05 10:14
 */
@NettyHttpHandler(path = "/temp/body",method = "POST")
public class TempHandler implements IFunctionHandler<Object> {

    @Override
    public Object execute(NettyHttpRequest request) {
        String contentText = request.contentText();
        return contentText;
    }
}
