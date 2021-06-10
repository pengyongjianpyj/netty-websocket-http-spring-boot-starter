
package org.pyj.demo.http;

import org.pyj.http.NettyHttpRequest;
import org.pyj.http.annotation.NettyHttpHandler;
import org.pyj.http.handler.IFunctionHandler;
import org.pyj.http.handler.Result;
import org.pyj.http.handler.ResultJson;

/**
 * @Description: http接口，参数化路径接口示例
 * @Author: pengyongjian
 * @Date: 2020-04-05 10:14
 */
@NettyHttpHandler(path = "/temp/path/",equal = false)
public class PathVariableHandler implements IFunctionHandler<String> {

    @Override
    public Result<String> execute(NettyHttpRequest request) {
        /**
         * 通过请求uri获取到path参数
         */
        String id = request.getStringPathValue(3);

        return new ResultJson<String>(200, id);
    }
}
