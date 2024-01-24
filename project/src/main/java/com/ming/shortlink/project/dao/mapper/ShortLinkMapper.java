package com.ming.shortlink.project.dao.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ming.shortlink.project.dao.entity.ShortLinkDO;
import com.ming.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author clownMing
 * 短链接持久层
 */
public interface ShortLinkMapper extends BaseMapper<ShortLinkDO> {

    List<ShortLinkGroupCountQueryRespDTO>  listGroupShortLinkCount(@Param("gids")List<String> gids);

}
