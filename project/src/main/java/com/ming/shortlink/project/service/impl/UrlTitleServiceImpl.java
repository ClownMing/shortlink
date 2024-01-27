package com.ming.shortlink.project.service.impl;

import com.ming.shortlink.project.common.enums.UserAgentEnum;
import com.ming.shortlink.project.service.UrlTitleService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * @author clownMing
 * URL接口实现层
 */
@Service
public class UrlTitleServiceImpl implements UrlTitleService {

    private static final int timeout = 5 * 1000;

    @Override
    public String getTitleByUrl(String url) {
        // 请求返回的内容
        Document doc;
        try {
            doc = Jsoup.connect(url).
                    userAgent(UserAgentEnum.EDGE_USER_AGENT.getAgent())
                    .timeout(timeout)
                    .get();
        } catch (IOException e) {
            return "获取标题时出错";
        }
        String title = doc.title();
        if(title.isEmpty()) {
            return "--";
        }
        return title;
    }
}
