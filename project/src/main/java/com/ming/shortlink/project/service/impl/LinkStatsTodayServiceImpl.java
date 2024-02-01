package com.ming.shortlink.project.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.ming.shortlink.project.dao.entity.LinkStatsTodayDO;
import com.ming.shortlink.project.dao.mapper.LinkStatsTodayMapper;
import com.ming.shortlink.project.service.LinkStatsTodayService;
import org.springframework.stereotype.Service;

/**
 * @author clownMing
 */
@Service
public class LinkStatsTodayServiceImpl extends ServiceImpl<LinkStatsTodayMapper, LinkStatsTodayDO> implements LinkStatsTodayService {
}
