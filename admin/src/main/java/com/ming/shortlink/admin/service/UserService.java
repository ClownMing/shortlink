package com.ming.shortlink.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.ming.shortlink.admin.dao.entity.UserDO;
import com.ming.shortlink.admin.dto.req.UserLoginReqDTO;
import com.ming.shortlink.admin.dto.req.UserRegisterReqDTO;
import com.ming.shortlink.admin.dto.req.UserUpdateReqDTO;
import com.ming.shortlink.admin.dto.resp.UserLoginRespDTO;
import com.ming.shortlink.admin.dto.resp.UserRespDTO;

/**
 * @author clownMing
 */
public interface UserService extends IService<UserDO> {

    /**
     * 根据用户名查询用户信息
     *
     * @param username 用户名
     * @return 用户返回实体
     */
    UserRespDTO getUserByUsername(String username);

    /**
     * 查询用户名是否存在
     *
     * @param username 用户名
     * @return 用户名存在返回 true, 否则返回 false
     */
    Boolean hasUsername(String username);

    /**
     * 用户注册
     *
     * @param requestParam 用户注册请求参数
     */
    void register(UserRegisterReqDTO requestParam);

    /**
     * 根据用户名修改用户
     */
    void update(UserUpdateReqDTO requestParam);

    /**
     * 用户登录
     */
    UserLoginRespDTO login(UserLoginReqDTO requestParam);

    /**
     * 通过token，检验用户是否登录
     * @param username 用户名
     * @param token 用户Token
     */
    Boolean checkLogin(String username, String token);

    /**
     * 用户退出登录
     * @param username 用户名
     * @param token 用户Token
     */
    void logout(String username, String token);
}
