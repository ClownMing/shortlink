package com.ming.shortlink.project.dao.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ming.shortlink.project.common.database.BaseDO;
import lombok.*;

import java.util.Date;

/**
 * @author clownMing
 * 统计访问设备实体
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LinkDeviceStatsDO extends BaseDO {

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
     * 日期
     */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private Date date;

    /**
     * 访问量
     */
    private Integer cnt;

    /**
     * 访问设备
     */
    private String device;
}
