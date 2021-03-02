package com.opp.deduplicationAndroidLogs.document;

import lombok.Data;

@Data
public class AndroidLogging {

    private Long mysqlId;
    private String eventId;
    private String value_of_event;
    private String localIpAddress;
    private String endPoint;
    private Integer userId;
    private String serial;
    private String androidId;
    private String model;
    private String release;
    private String tagId;
}
