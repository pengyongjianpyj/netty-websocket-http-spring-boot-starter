
package org.pyj.exception;

/**
 * @Description: 不合法的请求方法异常
 * @Author: pengyongjian
 * @Date: 2020-04-05 10:14
 */
public class IllegalMethodNotAllowedException extends Exception {
    public IllegalMethodNotAllowedException() {
        super("METHOD NOT ALLOWED");
    }
}
