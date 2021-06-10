package org.pyj.yeauty.support;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.QueryStringDecoder;
import java.util.LinkedHashMap;
import java.util.Map;
import org.pyj.yeauty.pojo.PojoEndpointServer;
import org.springframework.util.AntPathMatcher;

public class AntPathMatcherWrapper extends AntPathMatcher implements WsPathMatcher {

  private final String pattern;

  public AntPathMatcherWrapper(String pattern) {
    this.pattern = pattern;
  }

  @Override
  public String getPattern() {
    return this.pattern;
  }

  @Override
  public boolean matchAndExtract(QueryStringDecoder decoder, Channel channel) {
    Map<String, String> variables = new LinkedHashMap<>();
    boolean result = doMatch(pattern, decoder.path(), true, variables);
    if (result) {
      channel.attr(PojoEndpointServer.URI_TEMPLATE).set(variables);
      return true;
    }
    return false;
  }
}
