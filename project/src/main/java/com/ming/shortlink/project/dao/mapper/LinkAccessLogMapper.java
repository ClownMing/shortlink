package com.ming.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ming.shortlink.project.dao.entity.LinkAccessLogDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author clownMing
 * 短链接访问日志持久层
 */
@Mapper
public interface LinkAccessLogMapper extends BaseMapper<LinkAccessLogDO> {

    void shortLinkAccessLogStats(LinkAccessLogDO linkAccessLogDO);
}
