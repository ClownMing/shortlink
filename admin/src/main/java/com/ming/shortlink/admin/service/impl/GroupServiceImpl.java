package com.ming.shortlink.admin.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ming.shortlink.admin.dao.entity.GroupDO;
import com.ming.shortlink.admin.dao.mapper.GroupMapper;
import com.ming.shortlink.admin.service.GroupService;
import org.springframework.stereotype.Service;

/**
 * @author clownMing
 * 短链接分组接口实现层
 */
@Service
public class GroupServiceImpl extends ServiceImpl<GroupMapper, GroupDO> implements GroupService {
}
