package com.ming.shortlink.admin.dao.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.ming.shortlink.admin.common.database.BaseDO;
import lombok.*;

/**
 * @author clownMing
 * 短链接分组实体
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("t_group")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GroupDO extends BaseDO {
    /**
     * id
     */
    private Long id;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 创建分组用户名
     */
    private String username;

    /**
     * 分组排序
     */
    private Integer sortOrder;

}
