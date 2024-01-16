package com.ming.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ming.shortlink.admin.dao.entity.UserDO;
import com.ming.shortlink.admin.dto.req.UserRegisterReqDTO;
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

    /**
     * 查询用户名是否存在
     * @param username 用户名
     * @return 用户名存在返回 true, 否则返回 false
     */
    Boolean hasUsername(String username);

    void register(UserRegisterReqDTO requestParam);

}
