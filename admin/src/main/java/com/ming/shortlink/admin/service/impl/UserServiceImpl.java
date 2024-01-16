package com.ming.shortlink.admin.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ming.shortlink.admin.common.convention.exception.ClientException;
import com.ming.shortlink.admin.common.enums.UserErrorCodeEnum;
import com.ming.shortlink.admin.dao.entity.UserDO;
import com.ming.shortlink.admin.dao.mapper.UserMapper;
import com.ming.shortlink.admin.dto.resp.UserRespDTO;
import com.ming.shortlink.admin.service.UserService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

/**
 * @author clownMing
 * 用户接口实现层
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {
    @Override
    public UserRespDTO getUserByUsername(String username) {
        LambdaQueryWrapper<UserDO> wrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(UserDO::getUsername, username);
        UserDO dbUser = baseMapper.selectOne(wrapper);
        if (dbUser == null) {
            throw new ClientException(UserErrorCodeEnum.USER_NULL);
        }
        UserRespDTO result = new UserRespDTO();
        BeanUtils.copyProperties(dbUser, result);
        return result;
    }

    @Override
    public Boolean hasUsername(String username) {
        LambdaQueryWrapper<UserDO> lambdaQueryWrapper = Wrappers.lambdaQuery(UserDO.class)
                .eq(StrUtil.isNotBlank(username), UserDO::getUsername, username);
        UserDO userDO = baseMapper.selectOne(lambdaQueryWrapper);
        return userDO == null;
    }
}
