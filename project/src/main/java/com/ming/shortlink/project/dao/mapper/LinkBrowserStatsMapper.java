package com.ming.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ming.shortlink.project.dao.entity.LinkBrowserStatsDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author clownMing
 * 浏览器统计持久层
 */
@Mapper
public interface LinkBrowserStatsMapper extends BaseMapper<LinkBrowserStatsDO> {

    void shortLinkBrowserStats(LinkBrowserStatsDO linkBrowserStatsDO);
}
