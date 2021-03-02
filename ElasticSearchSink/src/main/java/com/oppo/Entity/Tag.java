package com.oppo.Entity;

public class Tag {
    private String SERIAL;
    private String ANDROIDID;
    private String MODEL;
    private String ID;
    private String RELEASE;
    public Tag(){}

    public String getSERIAL() {
        return SERIAL;
    }

    public void setSERIAL(String SERIAL) {
        this.SERIAL = SERIAL;
    }

    public String getANDROIDID() {
        return ANDROIDID;
    }

    public void setANDROIDID(String ANDROIDID) {
        this.ANDROIDID = ANDROIDID;
    }

    public String getMODEL() {
        return MODEL;
    }

    public void setMODEL(String MODEL) {
        this.MODEL = MODEL;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getRELEASE() {
        return RELEASE;
    }

    public void setRELEASE(String RELEASE) {
        this.RELEASE = RELEASE;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "SERIAL='" + SERIAL + '\'' +
                ", ANDROIDID='" + ANDROIDID + '\'' +
                ", MODEL='" + MODEL + '\'' +
                ", ID='" + ID + '\'' +
                ", RELEASE='" + RELEASE + '\'' +
                '}';
    }
}
