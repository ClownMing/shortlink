package com.ming.shortlink.project.controller;

import com.ming.shortlink.project.common.convention.result.Result;
import com.ming.shortlink.project.common.convention.result.Results;
import com.ming.shortlink.project.service.UrlTitleService;
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

    private final UrlTitleService urlTitleService;

    @GetMapping("/api/short-link/v1/title")
    public Result<String> getTitleByUrl(@RequestParam("url") String url) {
        return Results.success(urlTitleService.getTitleByUrl(url));
    }
}
