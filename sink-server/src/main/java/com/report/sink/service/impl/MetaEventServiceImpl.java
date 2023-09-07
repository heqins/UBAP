package com.report.sink.service.impl;

import com.api.common.bo.MetaEvent;
import com.baomidou.dynamic.datasource.annotation.DS;
import com.report.sink.dao.MetaEventDao;
import com.report.sink.service.ICacheService;
import com.report.sink.service.IMetaEventService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Service
@DS("mysqlDataSource")
public class MetaEventServiceImpl implements IMetaEventService {

    @Resource
    private MetaEventDao metaEventDao;

    @Resource(name = "redisCacheService")
    private ICacheService redisCacheService;

    @Override
    public MetaEvent getMetaEvent(String appId, String eventName) {
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(eventName)) {
            return null;
        }

        MetaEvent metaEventsCache = redisCacheService.getMetaEventCache(appId, eventName);
        if (Objects.nonNull(metaEventsCache)) {
            return metaEventsCache;
        }

        MetaEvent event = metaEventDao.getByName(appId, eventName);
        redisCacheService.setMetaEventCache(appId, eventName, event);

        return event;
    }


}
