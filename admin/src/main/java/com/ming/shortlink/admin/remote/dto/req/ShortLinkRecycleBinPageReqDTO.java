package com.ming.shortlink.admin.remote.dto.req;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author clownMing
 * 回收站短链接分页请求参数
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ShortLinkRecycleBinPageReqDTO extends Page {

    /**
     * 分组标识
     */
    private List<String> gidList;
}
