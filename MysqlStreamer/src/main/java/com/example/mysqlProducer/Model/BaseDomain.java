package com.example.mysqlProducer.Model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@MappedSuperclass
public abstract class BaseDomain implements Serializable {
    @Temporal(TemporalType.TIMESTAMP)
    protected Date dateCreated = null;
    @Temporal(TemporalType.TIMESTAMP)
    protected Date lastUpdated;
    @PrePersist
    public void prePersist() {
        this.dateCreated = new Date();
        this.lastUpdated = new Date();
    }
    @PreUpdate
    public void preUpdate() {
        this.lastUpdated = new Date();
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
}
