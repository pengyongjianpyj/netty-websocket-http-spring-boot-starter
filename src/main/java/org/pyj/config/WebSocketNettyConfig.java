package org.pyj.config;

import org.pyj.ServerEndpointExporter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author pengyongjian
 * @Description: websocket配置类，用于启动websocket
 * @date 2020-03-16 14:04
 */
@Configuration
public class WebSocketNettyConfig {
  @Bean
  public ServerEndpointExporter serverEndpointExporter() {
    return new ServerEndpointExporter();
  }
}
