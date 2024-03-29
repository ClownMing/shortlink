package com.ming.shortlink.admin.remote.dto.resp;

import lombok.Data;

/**
 * @author clownMing
 * 短链接分组查询返回参数
 */
@Data
public class ShortLinkGroupCountQueryRespDTO {

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 短链接数量
     */
    private Integer shortLinkCount;

}
