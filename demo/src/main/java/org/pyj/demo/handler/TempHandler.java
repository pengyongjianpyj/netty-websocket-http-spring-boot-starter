package org.pyj.demo.handler;

import org.pyj.http.NettyHttpRequest;
import org.pyj.http.annotation.NettyHttpHandler;
import org.pyj.http.handler.IFunctionHandler;
import org.pyj.http.handler.Result;
import org.pyj.http.handler.ResultJson;

/**
 * @Description: http接口，向对应用户发送消息
 * @Author: pengyongjian
 * @Date: 2020-04-05 10:14
 */
@NettyHttpHandler(path = "/temp/body",method = "POST")
public class TempHandler implements IFunctionHandler<String> {

    @Override
    public Result<String> execute(NettyHttpRequest request) {
        String contentText = request.contentText();
        return new ResultJson<String>(200, contentText);
    }
}
