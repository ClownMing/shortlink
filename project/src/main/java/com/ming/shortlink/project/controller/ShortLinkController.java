package com.ming.shortlink.project.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ming.shortlink.project.common.convention.result.Result;
import com.ming.shortlink.project.common.convention.result.Results;
import com.ming.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ming.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ming.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.ming.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.ming.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.ming.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import com.ming.shortlink.project.service.ShortLinkService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author clownMing
 * 短链接控制层
 */
@RestController
@RequiredArgsConstructor
public class ShortLinkController {

    private final ShortLinkService shortLinkService;


    /**
     * 创建短链接
     */
    @PostMapping("/api/short-link/v1/create")
    public Result<ShortLinkCreateRespDTO> createShortLink(@RequestBody ShortLinkCreateReqDTO requestParam) {
        return Results.success(shortLinkService.createShortLink(requestParam));
    }

    /**
     * 分页查询短链接
     */
    @GetMapping("/api/short-link/v1/page")
    public Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        return Results.success(shortLinkService.pageShortLink(requestParam));
    }
    /**
     * 查询短链接分组内数量
     */
    @GetMapping("/api/short-link/v1/count")
    public Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(@RequestParam List<String> requestParam) {
        return Results.success(shortLinkService.listGroupShortLinkCount(requestParam));
    }

    /**
     * 修改短链接
     */
    @PutMapping("/api/short-link/v1/update")
    public Result<Void> updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        return Results.success(shortLinkService.updateShortLink(requestParam));
    }

}
