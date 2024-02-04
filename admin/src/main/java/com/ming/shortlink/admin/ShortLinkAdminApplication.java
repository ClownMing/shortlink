package com.ming.shortlink.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.env.Environment;

/**
 * @author clownMing
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients("com.ming.shortlink.admin.remote")
@MapperScan("com.ming.shortlink.admin.dao.mapper")
public class ShortLinkAdminApplication {
    private static final Logger LOG = LoggerFactory.getLogger(ShortLinkAdminApplication.class);

    /**
     * 根据用户名查询用户信息
     */
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(ShortLinkAdminApplication.class);
        Environment env = application.run(args).getEnvironment();
        LOG.info("---------- admin module start success!!!, port > {} ----------", env.getProperty("server.port"));
    }
}
