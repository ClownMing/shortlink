package com.ming.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ming.shortlink.project.dao.entity.LinkDeviceStatsDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author clownMing
 * 统计访问设备持久层
 */
@Mapper
public interface LinkDeviceStatsMapper extends BaseMapper<LinkDeviceStatsDO> {

    void shortLinkDeviceStats(LinkDeviceStatsDO linkDeviceStatsDO);
}
