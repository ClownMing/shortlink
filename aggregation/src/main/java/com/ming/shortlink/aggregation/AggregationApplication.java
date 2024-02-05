package com.ming.shortlink.aggregation;

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
@EnableDiscoveryClient
@SpringBootApplication(scanBasePackages = {
        "com.ming.shortlink.admin",
        "com.ming.shortlink.project",
        "com.ming.shortlink.aggregation"
})
@MapperScan(value = {
        "com.ming.shortlink.admin.dao.mapper",
        "com.ming.shortlink.project.dao.mapper"
})
public class AggregationApplication {

    private static final Logger LOG = LoggerFactory.getLogger(AggregationApplication.class);
    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(AggregationApplication.class);
        Environment env = application.run(args).getEnvironment();
        LOG.info("---------- admin module start success!!!, port > {} ----------", env.getProperty("server.port"));
    }
}
