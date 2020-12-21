
package org.pyj.exception;

/**
 * @Description: 不合法的请求路径异常
 * @Author: pengyongjian
 * @Date: 2020-04-05 10:14
 */
public class IllegalPathNotFoundException extends Exception {
    public IllegalPathNotFoundException() {
        super("PATH NOT FOUND");
    }
}
