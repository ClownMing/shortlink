package com.ming.shortlink.admin.controller.remote;

import com.ming.shortlink.admin.common.convention.result.Result;
import com.ming.shortlink.admin.remote.ShortLinkActualRemoteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author clownMing
 * URL标题控制层
 */
@RestController(value = "uriTitleControllerByAdmin")
@RequiredArgsConstructor
public class UrlTitleController {

    private final ShortLinkActualRemoteService shortLinkActualRemoteService;

    @GetMapping("/api/short-link/admin/v1/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String url) {
        return shortLinkActualRemoteService.getTitleByUrl(url);
    }
}
