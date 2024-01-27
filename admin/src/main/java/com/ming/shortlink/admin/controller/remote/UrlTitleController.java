package com.ming.shortlink.admin.controller.remote;

import com.ming.shortlink.admin.common.convention.result.Result;
import com.ming.shortlink.admin.remote.ShortLinkRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author clownMing
 * URL标题控制层
 */
@RestController
@RequiredArgsConstructor
public class UrlTitleController {
    @GetMapping("/api/short-link/admin/v1/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String url) {
        ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
        };
        return shortLinkRemoteService.getTitleByUrl(url);
    }
}
