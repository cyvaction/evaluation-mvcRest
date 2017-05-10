package swx.conmmon;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author zgx
 * @Description:
 * @date 2017/4/28
 */
public abstract class BaseController {
    @Autowired
    protected HttpServletRequest request;
    @Autowired
    protected HttpServletResponse response;

    protected static Logger logger = Logger.getLogger(BaseController.class);

    /**
     * 成功无错误信息返回
     *
     * @param status
     * @param data
     * @return
     */
    protected String retData(int status, Object data) {
        return ReturnFormat.retParam(status, data);
    }

    /**
     * 失败有错误信息返回
     *
     * @param status
     * @param msg
     * @return
     */
    protected String retMsg(int status, String msg) {
        return ReturnFormat.retParam(status, msg, null);
    }


}