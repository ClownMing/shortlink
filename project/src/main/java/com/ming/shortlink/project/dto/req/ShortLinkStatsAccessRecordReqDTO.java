package com.ming.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ming.shortlink.project.dao.entity.LinkAccessLogDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author clownMing
 * 短链接监控请求参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ShortLinkStatsAccessRecordReqDTO extends Page<LinkAccessLogDO> {

    /**
     * 完整短链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;
}
