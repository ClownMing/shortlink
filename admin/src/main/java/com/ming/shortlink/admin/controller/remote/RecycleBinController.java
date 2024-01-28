package com.ming.shortlink.admin.controller.remote;

import com.ming.shortlink.admin.common.convention.result.Result;
import com.ming.shortlink.admin.common.convention.result.Results;
import com.ming.shortlink.admin.remote.ShortLinkRemoteService;
import com.ming.shortlink.admin.remote.dto.req.RecycleBinSaveReqDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author clownMing}
 * 回收站管理控制层
 */
@RestController
@RequiredArgsConstructor
public class RecycleBinController {

    /**
     * 回收站添加功能
     */
    @PostMapping("/api/short-link/admin/v1/recycle-bin/save")
    public Result<Void> saveRecycleBin(@RequestBody RecycleBinSaveReqDTO requestParam) {
        ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
        };
        shortLinkRemoteService.saveRecycleBin(requestParam);
        return Results.success();
    }
}
