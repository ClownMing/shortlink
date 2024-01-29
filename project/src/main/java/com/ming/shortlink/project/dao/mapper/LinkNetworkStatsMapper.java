package com.ming.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ming.shortlink.project.dao.entity.LinkNetworkStatsDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author clownMing
 * 统计监控网络持久层
 */
@Mapper
public interface LinkNetworkStatsMapper extends BaseMapper<LinkNetworkStatsDO> {

    void shortLinkNetwork(LinkNetworkStatsDO LinkNetworkStatsDO);
}
