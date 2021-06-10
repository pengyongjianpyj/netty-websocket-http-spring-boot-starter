package org.pyj.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * @author pengyongjian
 * @Description:
 * @date 2021/6/10 10:22 上午
 */
@Data
@Configuration
@ConfigurationProperties("web.pyj")
public class WebProperties {

  private String host = "0.0.0.0";

  private Integer bossLoopGroupThreads = 1;

  private Integer workerLoopGroupThreads = 0;

  private Boolean useCompressionHandler = false;

  //------------------------- option -------------------------

  private Integer optionConnectTimeoutMillis = 30000;

  private Integer optionSoBacklog = 128;

  //------------------------- childOption -------------------------

  private Integer childOptionWriteSpinCount = 16;

  private Integer childOptionWriteBufferHighWaterMark = 65536;

  private Integer childOptionWriteBufferLowWaterMark = 32768;

  private Integer childOptionSoRcvbuf = -1;

  private Integer childOptionSoSndbuf = -1;

  private Boolean childOptionTcpNodelay = true;

  private Boolean childOptionSoKeepalive = false;

  private Integer childOptionSoLinger = -1;

  private Boolean childOptionAllowHalfClosure = false;

  //------------------------- idleEvent -------------------------

  private Integer readerIdleTimeSeconds = 0;

  private Integer writerIdleTimeSeconds = 0;

  private Integer allIdleTimeSeconds = 0;

  //------------------------- handshake -------------------------

  private Integer maxFramePayloadLength = 65536;

  //------------------------- eventExecutorGroup -------------------------

  private Boolean useEventExecutorGroup = true;
      //use EventExecutorGroup(another thread pool) to perform time-consuming synchronous business logic

  private Integer eventExecutorGroupThreads = 16;

  //------------------------- ssl (refer to spring Ssl) -------------------------

  /**
   * {@link org.springframework.boot.web.server.Ssl}
   */

  private String sslKeyPassword = "";

  private String sslKeyStore = "";            //e.g. classpath:server.jks

  private String sslKeyStorePassword = "";

  private String sslKeyStoreType = "";        //e.g. JKS

  private String sslTrustStore = "";

  private String sslTrustStorePassword = "";

  private String sslTrustStoreType = "";

  //------------------------- cors (refer to spring CrossOrigin) -------------------------

  /**
   * {@link org.springframework.web.bind.annotation.CrossOrigin}
   */

  private String[] corsOrigins = {};

  private Boolean corsAllowCredentials = true;

}
