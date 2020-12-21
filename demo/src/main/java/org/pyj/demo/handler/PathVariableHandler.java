
package org.pyj.demo.handler;

import org.pyj.http.NettyHttpRequest;
import org.pyj.http.annotation.NettyHttpHandler;
import org.pyj.http.handler.IFunctionHandler;

/**
 * @Description: http接口，参数化路径接口示例
 * @Author: pengyongjian
 * @Date: 2020-04-05 10:14
 */
@NettyHttpHandler(path = "/temp/path/",equal = false)
public class PathVariableHandler implements IFunctionHandler<Object> {

    @Override
    public Object execute(NettyHttpRequest request) {
        /**
         * 通过请求uri获取到path参数
         */
        String id = request.getStringPathValue(3);

        return id;
    }
}
