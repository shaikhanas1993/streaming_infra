package com.example.mysqlProducer.HelperClasses;

public class MysqlOffSet {
    private long epocTime;
    private Long id;

    public  MysqlOffSet(){}

    public long getEpocTime() {
        return epocTime;
    }

    public void setEpocTime(long epocTime) {
        this.epocTime = epocTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
