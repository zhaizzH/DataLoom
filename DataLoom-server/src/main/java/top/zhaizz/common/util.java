package top.zhaizz.common;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class util {
    /**
     * 安全解析 JSON 对象，null / 空串 / 解析异常 → 返回空 JSONObject
     */
    public static JSONObject parseObjectOrEmpty(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new JSONObject();
        }
        try {
            JSONObject parsed = JSONObject.parseObject(json);
            return parsed == null ? new JSONObject() : parsed;
        } catch (Exception ignored) {
            return new JSONObject();
        }
    }

    /**
     * 安全解析 JSON 数组，null / 空串 / 解析异常 → 返回空 JSONArray
     */
    public static JSONArray parseArrayOrEmpty(String json) {
        if (json == null || json.trim().isEmpty()) {
            return new JSONArray();
        }
        try {
            JSONArray parsed = JSONArray.parseArray(json);
            return parsed == null ? new JSONArray() : parsed;
        } catch (Exception ignored) {
            return new JSONArray();
        }
    }
}
