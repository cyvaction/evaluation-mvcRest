package swx.conmmon;



import com.google.gson.Gson;


/**
 * Rest返回信息
 *
 * @author zgx
 * @Description:
 * @date 2017/4/28
 */
public class RespInfo<T> {
    //状态码
    private int status;
    //业务数据
    private T data;
    //提示信息
    private String message;

    public RespInfo(int status, T data, String message) {
        this.status = status;
        this.data = data;
        this.message = message;
    }

    public RespInfo(int status, T data) {
        this.status = status;
        this.data = data;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {

        return "{" +
                "\"code\":" + status +
                ",\"data\":" + (data instanceof String ? data : new Gson().toJson(data)) +
                ",\"message\":" + (null == message ? "\"\"" : "\"" + message + "\"") + "}";
    }


}
