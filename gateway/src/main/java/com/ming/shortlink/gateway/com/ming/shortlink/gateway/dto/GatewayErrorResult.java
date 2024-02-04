package com.ming.shortlink.gateway.com.ming.shortlink.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author clownMing
 * 网关错误返回信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GatewayErrorResult {

    /**
     * HTTP status code
     */
    private Integer status;

    /**
     * result message
     */
    private String message;

}
