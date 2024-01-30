package com.ming.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ming.shortlink.project.dao.entity.LinkAccessStatsDO;
import com.ming.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author clownMing
 * 短链接基础访问监控持久层
 */
@Mapper
public interface LinkAccessStatsMapper extends BaseMapper<LinkAccessStatsDO> {

    /**
     * 记录基础访问监控数据
     */
    void shortLinkStats(LinkAccessStatsDO linkAccessStatsDO);

    /**
     * 根据短链接获取指定日期内基础监控数据
     */
    List<LinkAccessStatsDO> listStatsByShortLink(ShortLinkStatsReqDTO shortLinkStatsReqDTO);

    /**
     * 根据短链接获取指定日期内小时基础监控数据
     */
    List<LinkAccessStatsDO> listHourStatsByShortLink(ShortLinkStatsReqDTO requestParam);


    List<LinkAccessStatsDO> listWeekdayStatsByShortLink(ShortLinkStatsReqDTO requestParam);
}
