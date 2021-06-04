package org.pyj.http.handler;


import org.pyj.http.NettyHttpRequest;

/**
 * @Description: http接口，所有对外http接口都需实现该接口
 * @Author: pengyongjian
 * @Date: 2020-04-05 10:14
 */
public interface IFunctionHandler<T> {
  Result<T> execute(NettyHttpRequest request);
}
