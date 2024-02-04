package com.ming.shortlink.gateway.com.ming.shortlink.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;

/**
 * @author clownMing
 * 网关服务应用启动类
 */
@SpringBootApplication
public class GatewayApplication {
    private static final Logger LOG = LoggerFactory.getLogger(GatewayApplication.class);
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(GatewayApplication.class);
        Environment env = application.run(args).getEnvironment();
        LOG.info("---------- gateway module start success!!!, port > {} ----------", env.getProperty("server.port"));
    }

}
