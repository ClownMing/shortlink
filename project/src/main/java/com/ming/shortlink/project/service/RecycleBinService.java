package com.ming.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ming.shortlink.project.dao.entity.ShortLinkDO;
import com.ming.shortlink.project.dto.req.RecycleBinRecoverReqDTO;
import com.ming.shortlink.project.dto.req.RecycleBinSaveReqDTO;
import com.ming.shortlink.project.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.ming.shortlink.project.dto.resp.ShortLinkPageRespDTO;

/**
 * @author clownMing
 * 回收站接口层
 */
public interface RecycleBinService extends IService<ShortLinkDO> {

    /**
     * 保存回收站
     */
    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);

    /**
     * 分页查询回收站
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkRecycleBinPageReqDTO requestParam);

    /**
     * 恢复回收站短链接
     */
    void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam);
}
