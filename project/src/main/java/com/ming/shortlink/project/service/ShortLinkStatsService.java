package com.ming.shortlink.project.service;

import com.ming.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.ming.shortlink.project.dto.resp.ShortLinkStatsRespDTO;

/**
 * @author clownMing
 * 短链接监控接口层
 */
public interface ShortLinkStatsService {

    /**
     * 获取单个短链接监控数据
     */
    ShortLinkStatsRespDTO oneShortLinkStats(ShortLinkStatsReqDTO requestParam);
}
