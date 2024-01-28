package com.ming.shortlink.admin.remote.dto.req;

import lombok.Data;

/**
 * @author clownMing
 * 回收站删除请求
 */
@Data
public class RecycleBinRemoveReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 全部短链接
     */
    private String fullShortUrl;
}
