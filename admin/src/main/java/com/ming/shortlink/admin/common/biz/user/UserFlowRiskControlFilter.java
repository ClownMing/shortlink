package com.ming.shortlink.admin.common.biz.user;

import com.alibaba.fastjson2.JSON;
import com.google.common.collect.Lists;
import com.ming.shortlink.admin.common.convention.exception.ClientException;
import com.ming.shortlink.admin.common.convention.result.Results;
import com.ming.shortlink.admin.config.UserFlowRiskControlConfiguration;
import com.ming.shortlink.admin.toolkit.SnowUtil;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scripting.support.ResourceScriptSource;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import static com.ming.shortlink.admin.common.convention.errorcode.BaseErrorCode.FLOW_LIMIT_ERROR;

/**
 * @author clownMing
 */
@RequiredArgsConstructor
public class UserFlowRiskControlFilter implements Filter {

    private static final Logger LOG = LoggerFactory.getLogger(UserFlowRiskControlFilter.class);

    private static final String USER_FLOW_RISK_CONTROL_LUA_SCRIPT_PATH = "lua/user_flow_risk_control.lua";

    private final StringRedisTemplate stringRedisTemplate;

    private final UserFlowRiskControlConfiguration userFlowRiskControlConfiguration;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource(USER_FLOW_RISK_CONTROL_LUA_SCRIPT_PATH)));
        redisScript.setResultType(Long.class);
        String username = Optional.ofNullable(UserContext.getUsername()).orElse("other_" + StringUtils.substring(SnowUtil.getSnowflakeNextIdStr(), 0, 6));
        Long result = null;
        try{
            result = stringRedisTemplate.execute(redisScript, Lists.newArrayList(username), userFlowRiskControlConfiguration.getTimeWindow());
        }catch (Throwable ex) {
            LOG.error("执行用户请求流量限制lua脚本出错", ex);
            returnJson((HttpServletResponse) response, JSON.toJSONString(Results.failure(new ClientException(FLOW_LIMIT_ERROR))));
        }
        if(result == null || result > userFlowRiskControlConfiguration.getMaxAccessCount()) {
            returnJson((HttpServletResponse) response, JSON.toJSONString(Results.failure(new ClientException(FLOW_LIMIT_ERROR))));
        }
        chain.doFilter(request, response);
    }

    private void returnJson(HttpServletResponse response, String json){
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        PrintWriter writer = null;
        try  {
            writer = response.getWriter();
            writer.print(json);
        }catch (Exception e) {

        }finally {
            writer.close();
        }
    }
}
