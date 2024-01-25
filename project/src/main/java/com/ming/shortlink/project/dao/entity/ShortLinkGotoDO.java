package com.ming.shortlink.project.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author clownMing
 * 短链接跳转实体
 */
@TableName("t_link_goto")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class ShortLinkGotoDO {

    /**
     * id
     */
    private Long id;

    /**
     * 短链接分组标识
     */
    private String gid;

    /**
     * 完整短链接
     */
    private String fullShortUrl;
}
