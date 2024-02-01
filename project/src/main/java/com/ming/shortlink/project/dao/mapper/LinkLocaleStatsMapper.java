package com.ming.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ming.shortlink.project.dao.entity.LinkLocaleStatsDO;
import com.ming.shortlink.project.dto.req.ShortLinkGroupStatsReqDTO;
import com.ming.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

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

    /**
     * 根据短链接获取指定日期内地区监控数据
     */
    List<LinkLocaleStatsDO> listLocaleByShortLink(ShortLinkStatsReqDTO requestParam);

    /**
     * 根据分组获取指定日期内地区监控数据
     */
    List<LinkLocaleStatsDO> listLocaleByGroup(ShortLinkGroupStatsReqDTO requestParam);
}
