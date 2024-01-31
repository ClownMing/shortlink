package com.ming.shortlink.admin.controller.remote;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ming.shortlink.admin.common.convention.result.Result;
import com.ming.shortlink.admin.remote.ShortLinkRemoteService;
import com.ming.shortlink.admin.remote.dto.req.ShortLinkStatsAccessRecordReqDTO;
import com.ming.shortlink.admin.remote.dto.req.ShortLinkStatsReqDTO;
import com.ming.shortlink.admin.remote.dto.resp.ShortLinkStatsAccessRecordRespDTO;
import com.ming.shortlink.admin.remote.dto.resp.ShortLinkStatsRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author clownMing
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkStatsController {

    /**
     * 访问单个短链接指定时间内监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats")
    public Result<ShortLinkStatsRespDTO> shortLinkStats(ShortLinkStatsReqDTO requestParam) {
        ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
        };
        return shortLinkRemoteService.oneShortLinkStats(requestParam.getFullShortUrl(), requestParam.getGid(), requestParam.getStartDate(), requestParam.getEndDate());
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    @GetMapping("/api/short-link/admin/v1/stats/access-record")
    public Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
        };
        return shortLinkRemoteService.shortLinkStatsAccessRecord(requestParam);
    }
}