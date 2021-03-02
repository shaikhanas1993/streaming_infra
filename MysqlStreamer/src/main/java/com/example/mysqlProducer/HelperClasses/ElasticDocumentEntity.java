package com.example.mysqlProducer.HelperClasses;

public class ElasticDocumentEntity {
    private Integer userId;
    private Tag tag;
    public String localIpAddress;
    public String endPoint;
    private String eventId;
    private String value;

    public ElasticDocumentEntity(){}



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

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Tag getTag() {
        return tag;
    }

    public void setTag(Tag tag) {
        this.tag = tag;
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

    @Override
    public String toString() {
        return "ElasticDocumentEntity{" +
                "userId=" + userId +
                ", tag=" + tag +
                ", localIpAddress='" + localIpAddress + '\'' +
                ", endPoint='" + endPoint + '\'' +
                ", eventId='" + eventId + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
