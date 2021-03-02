package com.oppo.Helper;
import com.google.gson.Gson;
import com.oppo.Entity.Tag;

public class GsonHelper {
    public static  String getJsonStringObject(Object object)
    {
        Gson gson = new Gson();
        String json = gson.toJson(object);
        return json;
    }

    public static AndroidLoggingRdbmsEntity getAndoridLoggingRdbmsEntityFromString(String jsonString) throws Exception
    {
        Gson gson = new Gson();
        AndroidLoggingRdbmsEntity entity = gson.fromJson(jsonString, AndroidLoggingRdbmsEntity.class);
        return entity;
    }

    public static Tag getTagObjectFromString(String jsonString) throws Exception
    {
        Gson gson = new Gson();
        jsonString = jsonString.replace(" ","");
        Tag tagObject = gson.fromJson(jsonString, Tag.class);
        return tagObject;
    }
    /*
    public static AndroidLogging getAndroidLoggingObjectFromString(String jsonString) throws Exception
    {
        Gson gson = new Gson();
        AndroidLogging androidLoggingObject = gson.fromJson(jsonString, AndroidLogging.class);
        return androidLoggingObject;
    }*/
}
