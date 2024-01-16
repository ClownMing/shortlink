package com.ming.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ming.shortlink.admin.dao.entity.UserDO;
import com.ming.shortlink.admin.dto.resp.UserRespDTO;

/**
 * @author clownMing
 */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询用户信息
     * @param username 用户名
     * @return 用户返回实体
     */
    UserRespDTO getUserByUsername(String username);


}
