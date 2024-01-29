package com.ming.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ming.shortlink.project.dao.entity.LinkLocaleStatsDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author clownMing
 * 地区统计访问持久层
 */
@Mapper
public interface LinkLocaleStatsMapper extends BaseMapper<LinkLocaleStatsDO> {

    /**
     * 地区统计
     */
    void shortLinkLocalStats(LinkLocaleStatsDO linkLocaleStatsDO);
}
