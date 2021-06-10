package org.pyj.yeauty.support;

import io.netty.channel.Channel;
import org.pyj.yeauty.annotation.OnError;
import org.springframework.core.MethodParameter;

public class ThrowableMethodArgumentResolver implements MethodArgumentResolver {
  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.getMethod().isAnnotationPresent(OnError.class) &&
        Throwable.class.isAssignableFrom(parameter.getParameterType());
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
    if (object instanceof Throwable) {
      return object;
    }
    return null;
  }
}
