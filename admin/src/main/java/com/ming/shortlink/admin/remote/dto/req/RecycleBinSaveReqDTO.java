package com.ming.shortlink.admin.remote.dto.req;

import lombok.Data;

/**
 * @author clownMing
 * 回收站添加请求
 */
@Data
public class RecycleBinSaveReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 全部短链接
     */
    private String fullShortUrl;
}
