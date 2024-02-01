package com.ming.shortlink.project.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ming.shortlink.project.dao.entity.ShortLinkDO;
import com.ming.shortlink.project.dto.req.ShortLinkBatchCreateReqDTO;
import com.ming.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ming.shortlink.project.dto.req.ShortLinkPageReqDTO;
import com.ming.shortlink.project.dto.req.ShortLinkUpdateReqDTO;
import com.ming.shortlink.project.dto.resp.ShortLinkBatchCreateRespDTO;
import com.ming.shortlink.project.dto.resp.ShortLinkCreateRespDTO;
import com.ming.shortlink.project.dto.resp.ShortLinkGroupCountQueryRespDTO;
import com.ming.shortlink.project.dto.resp.ShortLinkPageRespDTO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;

/**
 * @author clownMing
 * 短链接接口层
 */
public interface ShortLinkService extends IService<ShortLinkDO> {

    /**
     * 创建短链接
     * @param requestParam 创建短连接请求参数
     * @return 短链接创建信息
     */
    ShortLinkCreateRespDTO createShortLink(ShortLinkCreateReqDTO requestParam);

    /**
     * 批量创建短链接
     */
    ShortLinkBatchCreateRespDTO batchCreateShortLink(ShortLinkBatchCreateReqDTO requestParam);

    /**
     * 分页查询短链接
     */
    IPage<ShortLinkPageRespDTO> pageShortLink(ShortLinkPageReqDTO requestParam);

    /**
     * 查询短链接分组内数量
     */
    List<ShortLinkGroupCountQueryRespDTO> listGroupShortLinkCount(List<String> requestParam);

    /**
     * 修改短链接
     */
    Void updateShortLink(ShortLinkUpdateReqDTO requestParam);

    /**
     * 短链接跳转
     */
    void restoreUrl(String shortUri, HttpServletRequest request, HttpServletResponse response);
}
