package com.ming.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ming.shortlink.project.dao.entity.LinkAccessLogDO;
import com.ming.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.HashMap;
import java.util.List;

/**
 * @author clownMing
 * 短链接访问日志持久层
 */
@Mapper
public interface LinkAccessLogMapper extends BaseMapper<LinkAccessLogDO> {

    /**
     * 记录短链接访问日志统计
     */
    void shortLinkAccessLogStats(LinkAccessLogDO linkAccessLogDO);

    /**
     * 根据短链接获取指定日期内高频访问IP数据
     */
    List<HashMap<String, Object>> listTopIpByShortLink(ShortLinkStatsReqDTO requestParam);

    /**
     * 根据短链接获取指定日期内新旧访客数据
     */
    HashMap<String, Object> findUvTypeCntByShortLink(ShortLinkStatsReqDTO requestParam);
}
