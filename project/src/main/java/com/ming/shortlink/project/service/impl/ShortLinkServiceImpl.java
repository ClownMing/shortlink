package com.ming.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ming.shortlink.project.dao.entity.ShortLinkDO;
import com.ming.shortlink.project.dao.mapper.ShortLinkMapper;
import com.ming.shortlink.project.service.ShortLinkService;
import org.springframework.stereotype.Service;

/**
 * @author clownMing
 * 短链接接口实现层
 */
@Service
public class ShortLinkServiceImpl extends ServiceImpl<ShortLinkMapper, ShortLinkDO> implements ShortLinkService {
}
