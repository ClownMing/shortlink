package com.ming.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ming.shortlink.admin.common.convention.result.Result;
import com.ming.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.ming.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;

/**
 * @author clownMing
 * 回收站接口层
 */
public interface RecycleBinService {

    /**
     * 分页查询回收站短链接
     */
    Result<Page<ShortLinkPageRespDTO>> pageRecycleBin(ShortLinkRecycleBinPageReqDTO requestParam);

}
