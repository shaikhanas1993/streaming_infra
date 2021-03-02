package com.example.mysqlProducer.HelperClasses;

import com.google.gson.Gson;
public class GsonHelper {

    public static  String getJsonStringObject(Object object)
    {
            Gson gson = new Gson();
            String json = gson.toJson(object);
            return json;
    }

    public static Tag getTagObjectFromString(String jsonString) throws Exception
    {
        Gson gson = new Gson();
        Tag tagObject = gson.fromJson(jsonString, Tag.class);
        return tagObject;
    }

    public static MysqlOffSet getMysqlOffsetObjectFromString(String jsonString) throws Exception
    {
        Gson gson = new Gson();
        MysqlOffSet mysqlOffSetObject = gson.fromJson(jsonString, MysqlOffSet.class);
        return mysqlOffSetObject;
    }
}
