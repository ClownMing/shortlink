package com.ming.shortlink.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ming.shortlink.project.dao.entity.ShortLinkDO;
import com.ming.shortlink.project.dto.req.RecycleBinSaveReqDTO;

/**
 * @author clownMing
 * 回收站接口层
 */
public interface RecycleBinService extends IService<ShortLinkDO> {

    /**
     * 保存回收站
     */
    void saveRecycleBin(RecycleBinSaveReqDTO requestParam);

}
