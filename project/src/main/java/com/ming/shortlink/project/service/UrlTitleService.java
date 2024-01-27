package com.ming.shortlink.project.service;

/**
 * @author clownMing
 * URL接口层
 */
public interface UrlTitleService {

    /**
     * 根据url获取网站title
     */
    String getTitleByUrl(String url);

}
