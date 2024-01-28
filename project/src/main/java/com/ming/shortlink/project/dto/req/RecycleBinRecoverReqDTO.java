package com.ming.shortlink.project.dto.req;

import lombok.Data;

/**
 * @author clownMing
 * 回收站恢复请求
 */
@Data
public class RecycleBinRecoverReqDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 全部短链接
     */
    private String fullShortUrl;
}
