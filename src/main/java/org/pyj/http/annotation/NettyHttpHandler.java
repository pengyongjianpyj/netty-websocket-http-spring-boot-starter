package org.pyj.http.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.stereotype.Component;

/**
 * @Description: 用于注解接收http请求的类
 * @Author: pengyongjian
 * @Date: 2020-04-05 10:11
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface NettyHttpHandler {
  /**
   * 请求路径
   *
   * @return
   */
  String path() default "";

  /**
   * 支持的提交方式
   *
   * @return
   */
  String method() default "GET";

  /**
   * path和请求路径是否需要完全匹配。 如果是PathVariable传参数，设置为false
   *
   * @return
   */
  boolean equal() default true;
}
