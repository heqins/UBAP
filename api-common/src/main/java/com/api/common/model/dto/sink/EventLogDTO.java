package com.api.common.model.dto.sink;

import java.util.Set;

public class EventLogDTO {

    private String appId;

    private String eventName;

    private String dbName;

    private String tableName;

    private Long eventTime;

    private String dataJson;

    private String errorReason;

    private String errorHandling;

    private String eventType;

    private Integer status;

    private Set<String> fields;

    public EventLogDTO(String appId, String eventName, Long eventTime, String dataJson,
                       String errorReason, String errorHandling, String eventType, Integer status,
                       Set<String> fields, String dbName, String tableName) {
        this.appId = appId;
        this.eventName = eventName;
        this.eventTime = eventTime;
        this.dataJson = dataJson;
        this.errorReason = errorReason;
        this.errorHandling = errorHandling;
        this.eventType = eventType;
        this.status = status;
        this.fields = fields;
        this.tableName = tableName;
        this.dbName = dbName;
    }

    public EventLogDTO() {
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Long getEventTime() {
        return eventTime;
    }

    public void setEventTime(Long createTime) {
        this.eventTime = createTime;
    }

    public String getDataJson() {
        return dataJson;
    }

    public void setDataJson(String dataJson) {
        this.dataJson = dataJson;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public void setErrorReason(String errorReason) {
        this.errorReason = errorReason;
    }

    public String getErrorHandling() {
        return errorHandling;
    }

    public void setErrorHandling(String errorHandling) {
        this.errorHandling = errorHandling;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Set<String> getFields() {
        return fields;
    }

    public void setFields(Set<String> fields) {
        this.fields = fields;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    @Override
    public String toString() {
        return "EventLog{" +
                "appId='" + appId + '\'' +
                ", eventName='" + eventName + '\'' +
                ", eventTime=" + eventTime +
                ", dataJson='" + dataJson + '\'' +
                ", errorReason='" + errorReason + '\'' +
                ", errorHandling='" + errorHandling + '\'' +
                ", eventType='" + eventType + '\'' +
                ", status=" + status +
                '}';
    }
}
