package com.report.sink.service;


import com.api.common.dto.TableColumnDTO;
import com.report.sink.helper.RedisHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author heqin
 */
@Service
@Slf4j
public class RedisCacheServiceImpl implements ICacheService{

    @Resource
    private RedisHelper redisHelper;

    @Override
    public List<TableColumnDTO> getColumnCache(String dbName, String tableName) {
        return null;
    }

    @Override
    public void setColumnCache(String dbName, String tableName, List<TableColumnDTO> columns) {

    }
}
