package com.oppo.Helper;

import java.util.Date;

public class AndroidLoggingRdbmsEntity {

    private Long id;
    private String eventId;
    private String value;
    private String localIpAddress;
    private String endPoint;
    private String tag;
    private Integer userId;
    private Date dateCreated;
    private Date lastUpdated;

    public AndroidLoggingRdbmsEntity(){}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLocalIpAddress() {
        return localIpAddress;
    }

    public void setLocalIpAddress(String localIpAddress) {
        this.localIpAddress = localIpAddress;
    }

    public String getEndPoint() {
        return endPoint;
    }

    public void setEndPoint(String endPoint) {
        this.endPoint = endPoint;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "AndroidLoggingRdbmsEntity{" +
                "id=" + id +
                ", eventId='" + eventId + '\'' +
                ", value='" + value + '\'' +
                ", localIpAddress='" + localIpAddress + '\'' +
                ", endPoint='" + endPoint + '\'' +
                ", tag='" + tag + '\'' +
                ", userId=" + userId +
                ", dateCreated=" + dateCreated +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
