package org.pyj.http.handler;

/**
 * @author pengyongjian
 * @Description:
 * @date 2020/12/21 下午3:51
 */
public class ResultJson<T> implements Result<T> {

    private static final long serialVersionUID = 1;

    private int code;

    private T t;

    public ResultJson(int code, T t) {
        this.code = code;
        this.t = t;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getT() {
        return t;
    }

    public void setT(T t) {
        this.t = t;
    }

    @Override
    public String toString() {
        return "ResultJson{" +
                "code=" + code +
                ", t=" + t +
                '}';
    }
}
