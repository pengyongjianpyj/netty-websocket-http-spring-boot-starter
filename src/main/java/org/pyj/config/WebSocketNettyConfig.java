package org.pyj.config;

import org.pyj.ServerEndpointExporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author pengyongjian
 * @Description: 配置类，用于启动服务
 * @date 2020-03-16 14:04
 */
@Configuration
public class WebSocketNettyConfig {
  @Bean
  public ServerEndpointExporter serverEndpointExporter() {
    return new ServerEndpointExporter();
  }

  @Bean
  public WebProperties webProperties() {
    return new WebProperties();
  }
}
