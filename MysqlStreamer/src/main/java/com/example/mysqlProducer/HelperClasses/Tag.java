package com.example.mysqlProducer.HelperClasses;

public class Tag {
    private String Serial;
    private String AndroidId;
    private String Model;
    private String ID;
    private String Release;

    public Tag(){}

    public String getSerial() {
        return Serial;
    }

    public void setSerial(String serial) {
        Serial = serial;
    }

    public String getAndroidId() {
        return AndroidId;
    }

    public void setAndroidId(String androidId) {
        AndroidId = androidId;
    }

    public String getModel() {
        return Model;
    }

    public void setModel(String model) {
        Model = model;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public String getRelease() {
        return Release;
    }

    public void setRelease(String release) {
        Release = release;
    }

    @Override
    public String toString() {
        return "Tag{" +
                "Serial='" + Serial + '\'' +
                ", AndroidId='" + AndroidId + '\'' +
                ", Model='" + Model + '\'' +
                ", ID='" + ID + '\'' +
                ", Release='" + Release + '\'' +
                '}';
    }
}
