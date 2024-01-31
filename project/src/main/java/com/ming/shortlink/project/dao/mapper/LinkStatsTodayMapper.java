package com.ming.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ming.shortlink.project.dao.entity.LinkStatsTodayDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author clownMing
 * 短链接今日统计持久层
 */
@Mapper
public interface LinkStatsTodayMapper extends BaseMapper<LinkStatsTodayDO> {

    void shortLinkTodayStats(LinkStatsTodayDO linkStatsTodayDO);
}
