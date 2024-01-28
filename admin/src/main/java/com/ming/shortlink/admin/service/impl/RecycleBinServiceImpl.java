package com.ming.shortlink.admin.service.impl;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ming.shortlink.admin.common.biz.user.UserContext;
import com.ming.shortlink.admin.common.convention.exception.ServiceException;
import com.ming.shortlink.admin.common.convention.result.Result;
import com.ming.shortlink.admin.dao.entity.GroupDO;
import com.ming.shortlink.admin.dao.mapper.GroupMapper;
import com.ming.shortlink.admin.remote.ShortLinkRemoteService;
import com.ming.shortlink.admin.remote.dto.req.ShortLinkRecycleBinPageReqDTO;
import com.ming.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;
import com.ming.shortlink.admin.service.RecycleBinService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author clownMing
 * 回收站接口实现层
 */
@Service
@RequiredArgsConstructor
public class RecycleBinServiceImpl implements RecycleBinService {

    private final GroupMapper groupMapper;

    @Override
    public Result<IPage<ShortLinkPageRespDTO>> pageRecycleBin(ShortLinkRecycleBinPageReqDTO requestParam) {
        ShortLinkRemoteService shortLinkRemoteService = new ShortLinkRemoteService() {
        };
        LambdaQueryWrapper<GroupDO> queryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getUsername, UserContext.getUsername())
                .eq(GroupDO::getDelFlag, 0);
        List<GroupDO> groupDOList = groupMapper.selectList(queryWrapper);
        if(CollUtil.isEmpty(groupDOList)) {
            throw new ServiceException("用户无分组信息");
        }
        requestParam.setGidList(groupDOList.stream().map(GroupDO::getGid).toList());
        return shortLinkRemoteService.pageRecycleBin(requestParam);
    }
}
