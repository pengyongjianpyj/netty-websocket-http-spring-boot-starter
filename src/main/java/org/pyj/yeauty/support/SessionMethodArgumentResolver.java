package org.pyj.yeauty.support;

import io.netty.channel.Channel;
import org.pyj.yeauty.pojo.PojoEndpointServer;
import org.pyj.yeauty.pojo.Session;
import org.springframework.core.MethodParameter;

public class SessionMethodArgumentResolver implements MethodArgumentResolver {
  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return org.pyj.yeauty.pojo.Session.class.isAssignableFrom(parameter.getParameterType());
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
    Session session = channel.attr(PojoEndpointServer.SESSION_KEY).get();
    return session;
  }
}
