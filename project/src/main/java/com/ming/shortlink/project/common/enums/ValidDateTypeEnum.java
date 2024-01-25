package com.ming.shortlink.project.common.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * @author clownMing
 * 有效期类型
 */
@Getter
@RequiredArgsConstructor
public enum ValidDateTypeEnum {

    /**
     * 永久有效期
     */
    PERMANENT(0),

    /**
     * 自定义有效期
     */
    CUSTOM(1);

    private final Integer type;

}
