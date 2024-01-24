package com.ming.shortlink.admin.remote;

import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ming.shortlink.admin.common.convention.result.Result;
import com.ming.shortlink.admin.remote.dto.req.ShortLinkCreateReqDTO;
import com.ming.shortlink.admin.remote.dto.req.ShortLinkPageReqDTO;
import com.ming.shortlink.admin.remote.dto.resp.ShortLinkCreateRespDTO;
import com.ming.shortlink.admin.remote.dto.resp.ShortLinkPageRespDTO;

import java.util.HashMap;

/**
 * @author clownMing
 * // todo 后续重构为 SpringCloud Feign调用
 * 短链接中台远程调用服务
 */
public interface ShortLinkRemoteService {

    /**
     * 创建短链接
     */
    default Result<ShortLinkCreateRespDTO> createShortLink(ShortLinkCreateReqDTO requestParam) {
        String resultStr = HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/create", JSON.toJSONString(requestParam));
        return JSON.parseObject(resultStr, new TypeReference<>(){});
    }


    /**
     * 分页查询短链接
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("gid", requestParam.getGid());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/page", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>(){});
    }


}
