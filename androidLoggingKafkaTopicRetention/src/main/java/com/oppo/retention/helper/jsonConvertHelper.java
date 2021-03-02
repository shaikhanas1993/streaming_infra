package com.oppo.retention.helper;

import com.google.gson.Gson;
import com.oppo.retention.pojo.MysqlOffSet;

public class jsonConvertHelper {

    public static  String getJsonStringObject(Object object)
    {
        Gson gson = new Gson();
        String json = gson.toJson(object);
        return json;
    }

    public static MysqlOffSet getMysqlObjectFromString(String jsonString) throws Exception
    {
        Gson gson = new Gson();
        MysqlOffSet entity = gson.fromJson(jsonString, MysqlOffSet.class);
        return entity;
    }

}
