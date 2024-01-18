package com.ming.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ming.shortlink.admin.dao.entity.GroupDO;
import com.ming.shortlink.admin.dao.mapper.GroupMapper;
import com.ming.shortlink.admin.service.GroupService;
import com.ming.shortlink.admin.toolkit.RandomGenerator;
import org.springframework.stereotype.Service;

/**
 * @author clownMing
 * 短链接分组接口实现层
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {
    @Override
    public void saveGroup(String groupName) {
        String gid;
        while(true){
            gid = RandomGenerator.generateRandom();
            if(!hasGid(gid)) {
                break;
            }
        }
        GroupDO groupDO = GroupDO.builder()
                .gid(gid)
                .sortOrder(0)
                .name(groupName)
                .build();
        baseMapper.insert(groupDO);
    }


    private boolean hasGid(String gid) {
        LambdaQueryWrapper<GroupDO> lambdaQueryWrapper = Wrappers.lambdaQuery(GroupDO.class)
                .eq(GroupDO::getGid, gid)
                // todo 设置用户名
                .eq(GroupDO::getUsername, null);
        GroupDO hasGroupFlag = baseMapper.selectOne(lambdaQueryWrapper);
        return hasGroupFlag != null;
    }
}
