package com.ming.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ming.shortlink.project.common.database.BaseDO;
import lombok.*;

/**
 * @author clownMing
 * 短链接访问日志实体
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName("t_link_access_logs")
public class LinkAccessLogDO extends BaseDO {
    /**
     * id
     */
    private Long id;

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 用户信息
     */
    private String user;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 操作系统
     */
    private String os;

    /**
     * ip
     */
    private String ip;

    /**
     * 访问设备
     */
    private String device;

    /**
     * 访问网络
     */
    private String network;

    /**
     * 地区
     */
    private String locale;
}
