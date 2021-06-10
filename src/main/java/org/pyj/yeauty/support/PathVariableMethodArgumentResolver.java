package org.pyj.yeauty.support;

import io.netty.channel.Channel;
import java.util.Map;
import org.pyj.yeauty.annotation.PathVariable;
import org.pyj.yeauty.pojo.PojoEndpointServer;
import org.springframework.beans.TypeConverter;
import org.springframework.beans.factory.support.AbstractBeanFactory;
import org.springframework.core.MethodParameter;

public class PathVariableMethodArgumentResolver implements MethodArgumentResolver {

  private final AbstractBeanFactory beanFactory;

  public PathVariableMethodArgumentResolver(AbstractBeanFactory beanFactory) {
    this.beanFactory = beanFactory;
  }

  @Override
  public boolean supportsParameter(MethodParameter parameter) {
    return parameter.hasParameterAnnotation(org.pyj.yeauty.annotation.PathVariable.class);
  }

  @Override
  public Object resolveArgument(MethodParameter parameter, Channel channel, Object object) throws Exception {
    org.pyj.yeauty.annotation.PathVariable ann = parameter.getParameterAnnotation(PathVariable.class);
    String name = ann.name();
    if (name.isEmpty()) {
      name = parameter.getParameterName();
      if (name == null) {
        throw new IllegalArgumentException(
            "Name for argument type [" + parameter.getNestedParameterType().getName() +
                "] not available, and parameter name information not found in class file either.");
      }
    }
    Map<String, String> uriTemplateVars = channel.attr(PojoEndpointServer.URI_TEMPLATE).get();
    Object arg = (uriTemplateVars != null ? uriTemplateVars.get(name) : null);
    TypeConverter typeConverter = beanFactory.getTypeConverter();
    return typeConverter.convertIfNecessary(arg, parameter.getParameterType());
  }
}
