package com.ming.shortlink.project.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.ming.shortlink.project.dao.entity.ShortLinkDO;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author clownMing
 * 回收站短链接分页请求参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ShortLinkRecycleBinPageReqDTO extends Page<ShortLinkDO> {

    /**
     * 分组标识
     */
    private List<String> gidList;
}
