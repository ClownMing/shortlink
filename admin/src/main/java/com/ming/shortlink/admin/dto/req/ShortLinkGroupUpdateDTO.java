package com.ming.shortlink.admin.dto.req;

import lombok.Data;

/**
 * @author clownMing
 * 短链接分组修改参数
 */
@Data
public class ShortLinkGroupUpdateDTO {

    /**
     * 分组标识
     */
    private String gid;


    /**
     * 分组名
     */
    private String name;
}
