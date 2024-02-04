package com.ming.shortlink.gateway.com.ming.shortlink.gateway.config;

import lombok.Data;

import java.util.List;

/**
 * @author clownMing
 * 过滤器配置
 */
@Data
public class Config {

    /**
     * 白名单前置路径
     */
    private List<String> whitePathList;
}
