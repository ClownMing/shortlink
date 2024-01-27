package com.ming.shortlink.admin.common.enums;

/**
 * @author clownMing
 */
public enum UserAgentEnum {

    EDGE_USER_AGENT("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0", "edge浏览器");


    final String agent;

    final String desc;

    UserAgentEnum(String agent, String desc) {
        this.agent = agent;
        this.desc = desc;
    }

    public String getAgent() {
        return agent;
    }

    public String getDesc() {
        return desc;
    }
}
