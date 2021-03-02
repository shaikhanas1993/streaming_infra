package com.example.mysqlProducer.Model;

import javax.persistence.*;

@Entity
@Table(name = "android_logging")
public final class AndroidLogging extends  BaseDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    public String eventId;
    public String value;
    public String localIpAddress;
    public String endPoint;
    public String tag;
    public Integer userId;
}
