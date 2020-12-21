package org.pyj.demo;

import org.pyj.http.annotation.NettyHttpHandler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;

@SpringBootApplication
@ComponentScan(includeFilters = @Filter(NettyHttpHandler.class))
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}