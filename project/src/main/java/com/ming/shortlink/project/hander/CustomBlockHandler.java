package com.ming.shortlink.project.hander;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.ming.shortlink.project.common.convention.result.Result;
import com.ming.shortlink.project.dto.req.ShortLinkCreateReqDTO;
import com.ming.shortlink.project.dto.resp.ShortLinkCreateRespDTO;

/**
 * @author clownMing
 */
public class CustomBlockHandler {

    public static Result<ShortLinkCreateRespDTO> createShortLinkBlockHandlerMethod(ShortLinkCreateReqDTO requestParam, BlockException exception) {
        return new  Result<ShortLinkCreateRespDTO>().setCode("B100000").setMessage("当前访问网站人数过多，请稍后再试...");
    }

}
