package com.ming.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ming.shortlink.project.dto.req.ShortLinkGroupStatsAccessRecordReqDTO;
import com.ming.shortlink.project.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.ming.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import com.ming.shortlink.project.dto.resp.ShortLinkStatsAccessRecordRespDTO;
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

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    IPage<ShortLinkStatsAccessRecordRespDTO> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam);

    /**
     * 访问分组短链接指定时间内访问记录监控数据
     */
    IPage<ShortLinkStatsAccessRecordRespDTO> groupShortLinkStatsAccessRecord(ShortLinkGroupStatsAccessRecordReqDTO requestParam);
}
