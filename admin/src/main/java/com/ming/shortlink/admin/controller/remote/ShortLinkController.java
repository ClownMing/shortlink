package com.ming.shortlink.admin.controller.remote;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ming.shortlink.admin.common.convention.result.Result;
import com.ming.shortlink.admin.common.convention.result.Results;
import com.ming.shortlink.admin.remote.ShortLinkRemoteService;
import com.ming.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.ming.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.ming.shortlink.admin.remote.dto.req.ShortLinkUpdateReqDTO;
import com.ming.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.ming.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @author clownMing
 * 短链接后管控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/admin/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
        };
        return shortLinkRemoteService.createShortLink(requestParam);
    }


    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/admin/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
        };
        return shortLinkRemoteService.pageShortLink(requestParam);
    }

    /**
     * 修改短链接
     */
    @PostMapping("/api/short-link/admin/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
        };
        shortLinkRemoteService.updateShortLink(requestParam);
        return Results.success();
    }
}
