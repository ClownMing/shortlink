package com.ming.shortlink.project.toolkit;

import cn.hutool.core.util.IdUtil;

/**
 * @author clownMing
 * 封装 hutool 雪花算法
 */
public class SnowUtil {

    // 机器标识
    private static long workerId = 1;

    // 数据中心
    private static long dataCenterId = 1;

    public static long getSnowflakeNextId() {
        return IdUtil.getSnowflake(workerId, dataCenterId).nextId();
    }

    public static String getSnowflakeNextIdStr() {
        return IdUtil.getSnowflake(workerId, dataCenterId).nextIdStr();
    }
}
