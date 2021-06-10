package org.pyj.yeauty.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

/**
 * @author Yeauty
 * @version 1.0
 */
@Component
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ServerPath {

  @AliasFor("path")
  String value() default "/";

  @AliasFor("value")
  String path() default "/";

}
