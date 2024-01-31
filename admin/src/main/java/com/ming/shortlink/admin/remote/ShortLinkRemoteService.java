package com.ming.shortlink.admin.remote;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.ming.shortlink.admin.common.convention.result.Result;
import com.ming.shortlink.admin.common.convention.result.Results;
import com.ming.shortlink.admin.remote.dto.req.*;
import com.ming.shortlink.admin.remote.dto.resp.*;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.HashMap;
import java.util.List;

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
        return JSON.parseObject(resultStr, new TypeReference<>() {
        });
    }


    /**
     * 分页查询短链接
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageShortLink(ShortLinkPageReqDTO requestParam) {
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("gid", requestParam.getGid());
        requestMap.put("orderTag", requestParam.getOrderTag());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/page", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }

    /**
     * 查询短链接分组内数量
     */
    default Result<List<ShortLinkGroupCountQueryRespDTO>> listGroupShortLinkCount(List<String> requestParam) {
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("requestParam", requestParam);
        String resultPageStr = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/count", requestMap);
        return JSON.parseObject(resultPageStr, new TypeReference<>() {
        });
    }

    /**
     * 修改短链接
     */
    default void updateShortLink(@RequestBody ShortLinkUpdateReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/update", JSONUtil.toJsonStr(requestParam));
    }

    /**
     * 根据url获取目标网站title
     */
    default Result<String> getTitleByUrl(String url) {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("url", url);
        String result = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/title", hashMap);
        return JSON.parseObject(result, new TypeReference<>() {
        });
    }

    /**
     * 回收站添加功能
     */
    default void saveRecycleBin(RecycleBinSaveReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/save", JSONUtil.toJsonStr(requestParam));
    }

    /**
     * 分页查询回收站
     */
    default Result<IPage<ShortLinkPageRespDTO>> pageRecycleBin(ShortLinkRecycleBinPageReqDTO requestParam) {
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("gidList", requestParam.getGidList());
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        String result = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/page", requestMap);
        return JSON.parseObject(result, new TypeReference<>() {
        });
    }

    /**
     * 恢复回收站短链接
     */
    default void recoverRecycleBin(RecycleBinRecoverReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/recover", JSONUtil.toJsonStr(requestParam));

    }

    /**
     * 回收站移除功能
     */
    default Result<Void> removeRecycleBin(RecycleBinRemoveReqDTO requestParam) {
        HttpUtil.post("http://127.0.0.1:8001/api/short-link/v1/recycle-bin/remove", JSONUtil.toJsonStr(requestParam));
        return Results.success();
    }

    /**
     * 访问单个短链接指定时间内监控数据
     */
    default Result<ShortLinkStatsRespDTO> oneShortLinkStats(String fullShortUrl, String gid, String startDate, String endDate) {
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("fullShortUrl", fullShortUrl);
        requestMap.put("gid", gid);
        requestMap.put("startDate", startDate);
        requestMap.put("endDate", endDate);
        String result = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats", requestMap);
        return JSON.parseObject(result, new TypeReference<>() {
        });
    }

    /**
     * 访问单个短链接指定时间内访问记录监控数据
     */
    default Result<IPage<ShortLinkStatsAccessRecordRespDTO>> shortLinkStatsAccessRecord(ShortLinkStatsAccessRecordReqDTO requestParam) {
        HashMap<String, Object> requestMap = new HashMap<>();
        requestMap.put("current", requestParam.getCurrent());
        requestMap.put("size", requestParam.getSize());
        requestMap.put("fullShortUrl", requestParam.getFullShortUrl());
        requestMap.put("gid", requestParam.getGid());
        requestMap.put("startDate", requestParam.getStartDate());
        requestMap.put("endDate", requestParam.getEndDate());
        String result = HttpUtil.get("http://127.0.0.1:8001/api/short-link/v1/stats/access-record", requestMap);
        return JSON.parseObject(result, new TypeReference<>() {
        });
    }
}
