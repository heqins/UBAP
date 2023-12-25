package com.report.sink.helper;

import cn.hutool.json.JSONUtil;
import com.api.common.enums.AttributeDataTypeEnum;
import com.api.common.model.dto.sink.EventLogDTO;
import com.api.common.model.dto.sink.TableColumnDTO;
import com.report.sink.service.ICacheService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * @author heqin
 */
@Component
public class DorisHelper {

    private final Logger log = LoggerFactory.getLogger(DorisHelper.class);
    
    private static final String COLUMN_QUERY_SQL = "select column_name, column_type, is_nullable\n" +
            "FROM information_schema.columns\n" +
            "WHERE table_schema = ? AND table_name = ?;";

    private static final String ALTER_ADD_COLUMN_SQL = "ALTER TABLE `%s`.`%s` ADD COLUMN %s %s;";

    @Resource(name = "redisCacheService")
    private ICacheService redisCacheService;

    @Resource(name = "localCacheServiceImpl")
    private ICacheService localCacheService;

    @Resource(name = "dorisDataSource")
    private DataSource dataSource;

    public List<TableColumnDTO> getTableColumnInfos(String dbName, String tableName) {
        if (StringUtils.isBlank(dbName) || StringUtils.isBlank(tableName)) {
            return null;
        }

        List<TableColumnDTO> columns = localCacheService.getColumnCache(dbName, tableName);
        if (columns != null) {
            return columns;
        }

        columns = redisCacheService.getColumnCache(dbName, tableName);
        if (columns != null) {
            return columns;
        }

        columns = new ArrayList<>();
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(COLUMN_QUERY_SQL)) {
                statement.setString(1, dbName);
                statement.setString(2, tableName);

                ResultSet resultSet = statement.executeQuery();
                while (resultSet.next()) {
                    String columnName = resultSet.getString("column_name");
                    String columnType = resultSet.getString("column_type");
                    String isNullable = resultSet.getString("is_nullable");

                    String columnJavaType = transferColumnTypeFromDorisToJava(columnType.toLowerCase(Locale.ROOT));
                    if (StringUtils.isBlank(columnJavaType)) {
                        throw new IllegalStateException("column java type not found");
                    }

                    TableColumnDTO columnDTO = new TableColumnDTO();
                    columnDTO.setColumnName(columnName);
                    columnDTO.setStatus(1);
                    columnDTO.setNullable(Objects.equals(isNullable.toLowerCase(Locale.ROOT), "yes"));
                    columnDTO.setTableName(tableName);
                    columnDTO.setColumnType(columnJavaType);

                    columns.add(columnDTO);
                }
            } catch (SQLException e) {
                log.error("DorisHelper getTableColumnInfos sql error", e);
            }
        }catch (SQLException e) {
            log.error("DorisHelper getTableColumnInfos sql error", e);
        }

        localCacheService.setColumnCache(dbName, tableName, columns);
        redisCacheService.setColumnCache(dbName, tableName, columns);

        return columns;
    }

    public String transferColumnTypeFromDorisToJava(String columnType) {
        if (columnType == null) {
            return "";
        }

        if (columnType.startsWith("varchar")) {
            return "java.lang.String";
        }

        if (columnType.startsWith("int")) {
            return "java.lang.Integer";
        }

        if (columnType.startsWith("bigint")) {
            return "java.lang.Long";
        }

        if (columnType.startsWith("decimal")) {
            return "java.math.BigDecimal";
        }

        if (columnType.startsWith("date")) {
            return "java.util.Date";
        }

        if (columnType.startsWith("tinyint")) {
            return "java.lang.Byte";
        }

        if (columnType.startsWith("smallint")) {
            return "java.lang.Short";
        }

        if (columnType.startsWith("tinyint(0)") || columnType.startsWith("tinyint(1)")) {
            return "java.lang.Boolean";
        }

        return "";
    }

    public void addTableColumn(EventLogDTO eventLogDTO, Set<String> jsonFields) {
        List<String> alterQueries = new ArrayList<>(jsonFields.size());
        for (String jsonField: jsonFields) {
            // todo:
            Object obj = eventLogDTO.getFieldValueMap().get(jsonField);
            if (obj == null) {
                throw new IllegalStateException("DorisHelper changeTableSchema column obj is null");
            }

            String className = obj.getClass().getCanonicalName();
            String type = AttributeDataTypeEnum.getDefaultDataTypeByClass(className);

            if (StringUtils.isBlank(type)) {
//                log.warn("DorisHelper type not found className:{} field:{}", className, jsonField);
                continue;
            }

            String query = String.format(ALTER_ADD_COLUMN_SQL, eventLogDTO.getDbName(), eventLogDTO.getTableName(), jsonField, type);
            alterQueries.add(query);
        }

        if (!CollectionUtils.isEmpty(alterQueries)) {
            try (Connection connection = dataSource.getConnection()) {
                connection.setAutoCommit(false);

                for (String sql: alterQueries) {
                    try (PreparedStatement statement = connection.prepareStatement(sql)){
                        statement.execute();
                    } catch (SQLException e) {
                        connection.rollback();
                        log.error("DorisHelper changeTableSchema execute error", e);
                    }
                }

                connection.commit();
            }catch (SQLException e) {
                log.error("DorisHelper changeTableSchema alter column commit error", e);
            }
        }

        localCacheService.removeColumnCache(eventLogDTO.getDbName(), eventLogDTO.getTableName());
        redisCacheService.removeColumnCache(eventLogDTO.getDbName(), eventLogDTO.getTableName());
    }

    public void tableInsertData(String sql, List<TableColumnDTO> columnDTOList, List<Map<String, Object>> jsonDataList) {
        if (sql == null || jsonDataList == null) {
            return;
        }

        Connection insertConnection;
        try {
            insertConnection = dataSource.getConnection();
        }catch (SQLException e) {
            throw new IllegalStateException(e.getMessage());
        }

        try {
            insertConnection.setAutoCommit(false);
            PreparedStatement statement = insertConnection.prepareStatement(sql);
            for (Map<String, Object> jsonObject: jsonDataList) {
                if (columnDTOList.size() < jsonObject.size()) {
                    log.error("DorisHelper tableInsertData columnDTOList size < jsonObject size");
                    return;
                }

                for (int i = 0; i < columnDTOList.size(); i++) {
                    String columnName = columnDTOList.get(i).getColumnName();
                    String columnType = columnDTOList.get(i).getColumnType();

                    Object value = jsonObject.get(columnName);
                    if (value == null) {
                        if (!columnDTOList.get(i).getNullable()) {
                            log.warn("DorisHelper tableInsertData column is not nullable but yet null jsonObject:{}", JSONUtil.toJsonStr(jsonObject));
                            return;
                        }

                        switch (columnType) {
                            case "java.lang.Byte":
                                value = 0;
                                break;
                            case "java.lang.Short":
                                value = 0;
                                break;
                            case "java.lang.String":
                                value = "";
                                break;
                            case "java.lang.Integer":
                                value = 0;
                                break;
                            case "java.lang.Float":
                                value = 0.0f;
                                break;
                            case "java.lang.Double":
                                value = 0.0d;
                                break;
                            case "java.lang.Long":
                                value = 0L;
                                break;
                            case "java.util.Date":
                                value = new Date();
                                break;
                            default:
                        }
                    }

                    statement.setObject(i + 1, value);
                }

                statement.addBatch();
                statement.executeBatch();
            }

            insertConnection.commit();
        }catch (SQLException e) {
            try {
                insertConnection.rollback();
            }catch (SQLException e1) {
            }

            log.error("DorisHelper tableInsertData insert execute error", e);
            throw new IllegalStateException("插入失败");
        }finally {
            try {
                insertConnection.close();
            }catch (SQLException e) {
                log.error("DorisHelper tableInsertData insert close error", e);
            }
        }
    }
}
