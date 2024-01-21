package com.ming.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ming.shortlink.admin.dao.entity.GroupDO;
import com.ming.shortlink.admin.dto.req.ShortLinkGroupUpdateReqDTO;
import com.ming.shortlink.admin.dto.resp.ShortLinkGroupRespDTO;

import java.util.List;

/**
 * @author clownMing
 * 短链接分组接口层
 */
public interface GroupService extends IService<GroupDO> {

    /**
     * 新增短链接分组
     * @param groupName 短链接分组名
     */
    void saveGroup(String groupName);

    /**
     * 查询用户短链接分组集合
     */
    List<ShortLinkGroupRespDTO> listGroup();

    /**
     * 修改短链接分组
     */
    void updateGroup(ShortLinkGroupUpdateReqDTO requestParam);

    /**
     * 删除短链接分组
     */
    void deleteGroup(String gid);
}
