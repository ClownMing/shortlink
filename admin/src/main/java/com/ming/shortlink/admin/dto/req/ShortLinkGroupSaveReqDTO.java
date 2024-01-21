package com.ming.shortlink.admin.dto.req;

import lombok.Data;

/**
 * @author clownMing
 * 短链接分组创建参数
 */
@Data
public class ShortLinkGroupSaveReqDTO {

    /**
     * 分组名
     */
    private String name;
}
