package com.ming.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ming.shortlink.project.dao.entity.LinkNetworkStatsDO;
import com.ming.shortlink.project.dto.req.ShortLinkStatsReqDTO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author clownMing
 * 统计监控网络持久层
 */
@Mapper
public interface LinkNetworkStatsMapper extends BaseMapper<LinkNetworkStatsDO> {

    /**
     * 记录访问设备监控数据
     */
    void shortLinkNetwork(LinkNetworkStatsDO LinkNetworkStatsDO);

    /**
     * 根据短链接获取指定日期内访问网络监控数据
     */
    List<LinkNetworkStatsDO> listNetworkStatsByShortLink(ShortLinkStatsReqDTO requestParam);
}
