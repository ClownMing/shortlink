package com.ming.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ming.shortlink.project.dao.entity.LinkOsStatsDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author clownMing
 */
@Mapper
public interface LinkOsStatsMapper extends BaseMapper<LinkOsStatsDO> {

    void shortLinkOsStats(LinkOsStatsDO linkOsStatsDO);
}
