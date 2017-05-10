package swx.conmmon;

import com.google.common.collect.Maps;

import java.util.Map;

/**
 * 格式化返回客户端数据格式（json）
 * @author zgx
 * @Description:
 * @date 2017/4/28
 */
public class ReturnFormat {
    private static Map<String, String> messageMap = Maps.newHashMap();

    //初始化状态码与文字说明
    static {
        messageMap.put("0", "");

        messageMap.put("400", "Bad Request!");
        messageMap.put("401", "NotAuthorization");
        messageMap.put("404", "NoHandlerFoundException");
        messageMap.put("405", "Method Not Allowed");
        messageMap.put("406", "Not Acceptable");
        messageMap.put("415", "Unsupported Media Type");
        messageMap.put("500", "Internal Server Error");

        messageMap.put("1000", "[服务器]运行时异常");
        messageMap.put("1001", "[服务器]空值异常");
        messageMap.put("1002", "[服务器]数据类型转换异常");
        messageMap.put("1003", "[服务器]IO异常");
        messageMap.put("1004", "[服务器]未知方法异常");
        messageMap.put("1005", "[服务器]数组越界异常");
        messageMap.put("1006", "[服务器]网络异常");

    }

    public static String retParam(int status, Object data) {
        RespInfo json = new RespInfo(status, data, messageMap.get(String.valueOf(status)));
        return json.toString();
    }
    public static String retParam(int status,Exception ex, Object data) {
        RespInfo json = new RespInfo(status, data, messageMap.get(String.valueOf(status))+ex.getLocalizedMessage());
        return json.toString();
    }
    public static String retParam(int status,String msg, Object data) {
        RespInfo json = new RespInfo(status, data, msg);
        return json.toString();
    }
}