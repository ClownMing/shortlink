package com.ming.shortlink.project;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.env.Environment;

/**
 * @author clownMing
 */
@SpringBootApplication
@MapperScan("com.ming.shortlink.project.dao.mapper")
@EnableDiscoveryClient
public class ShortLinkApplication {

    private static final Logger LOG = LoggerFactory.getLogger(ShortLinkApplication.class);
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ShortLinkApplication.class);
        Environment env = application.run(args).getEnvironment();
        LOG.info("---------- admin module start success!!!, port > {} ----------", env.getProperty("server.port"));
    }
}
