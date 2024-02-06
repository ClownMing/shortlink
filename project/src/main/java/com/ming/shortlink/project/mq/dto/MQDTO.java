package com.ming.shortlink.project.mq.dto;

import com.ming.shortlink.project.common.database.BaseDO;
import com.ming.shortlink.project.dto.biz.ShortLinkStatsRecordDTO;
import lombok.*;

/**
 * @author clownMing
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MQDTO extends BaseDO {

    /**
     * 完整链接
     */
    private String fullShortUrl;

    /**
     * 分组标识
     */
    private String gid;

    /**
     * 短链接统计实体
     */
    ShortLinkStatsRecordDTO shortLinkStatsRecordDTO;
}
