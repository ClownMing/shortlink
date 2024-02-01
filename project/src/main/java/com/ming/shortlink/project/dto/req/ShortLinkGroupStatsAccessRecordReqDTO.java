package com.ming.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ming.shortlink.project.dao.entity.LinkAccessLogDO;
import lombok.Data;

@Data
public class ShortLinkGroupStatsAccessRecordReqDTO extends Page<LinkAccessLogDO> {

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
