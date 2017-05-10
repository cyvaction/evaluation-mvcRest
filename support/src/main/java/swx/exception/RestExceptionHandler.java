package swx.exception;


import org.apache.log4j.Logger;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import swx.conmmon.ReturnFormat;

import java.io.IOException;

/**
 * 异常增强，以JSON的形式返回给客服端
 * 异常增强类型：NullPointerException,RunTimeException,ClassCastException,
 * 　　　　　　　　 NoSuchMethodException,IOException,IndexOutOfBoundsException
 * 　　　　　　　　 以及springmvc自定义异常等，如下：
 * Spring自定义异常对应的status code
 * Exception                               HTTP Status Code
 * ConversionNotSupportedException         500 (Internal Server Error)
 * HttpMessageNotWritableException         500 (Internal Server Error)
 * HttpMediaTypeNotSupportedException      415 (Unsupported Media Type)
 * HttpMediaTypeNotAcceptableException     406 (Not Acceptable)
 * HttpRequestMethodNotSupportedException  405 (Method Not Allowed)
 * NoSuchRequestHandlingMethodException    404 (Not Found)
 * TypeMismatchException                   400 (Bad Request)
 * HttpMessageNotReadableException         400 (Bad Request)
 * MissingServletRequestParameterException 400 (Bad Request)
 */

@ControllerAdvice
public class RestExceptionHandler {
    protected static Logger logger = Logger.getLogger(RestExceptionHandler.class);
    //运行时异常
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public String runtimeExceptionHandler(RuntimeException ex) {
        ex.printStackTrace();
        return ReturnFormat.retParam(1000, ex, null);
    }


    //空指针异常
    @ExceptionHandler(NullPointerException.class)
    @ResponseBody
    public String nullPointerExceptionHandler(NullPointerException ex) {
        ex.printStackTrace();
        return ReturnFormat.retParam(1001, ex, null);
    }

    //类型转换异常
    @ExceptionHandler(ClassCastException.class)
    @ResponseBody
    public String classCastExceptionHandler(ClassCastException ex) {
        ex.printStackTrace();
        return ReturnFormat.retParam(1002, ex, null);
    }

    //IO异常
    @ExceptionHandler(IOException.class)
    @ResponseBody
    public String iOExceptionHandler(IOException ex) {
        ex.printStackTrace();
        return ReturnFormat.retParam(1003, ex, null);
    }

    //未知方法异常
    @ExceptionHandler(NoSuchMethodException.class)
    @ResponseBody
    public String noSuchMethodExceptionHandler(NoSuchMethodException ex) {
        ex.printStackTrace();
        return ReturnFormat.retParam(1004, ex, null);
    }

    //数组越界异常
    @ExceptionHandler(IndexOutOfBoundsException.class)
    @ResponseBody
    public String indexOutOfBoundsExceptionHandler(IndexOutOfBoundsException ex) {
        ex.printStackTrace();
        return ReturnFormat.retParam(1005, ex, null);
    }

    //400错误
    @ExceptionHandler({HttpMessageNotReadableException.class})
    @ResponseBody
    public String requestNotReadable(HttpMessageNotReadableException ex) {
       logger.error("400..requestNotReadable");
        ex.printStackTrace();
        return ReturnFormat.retParam(400, ex, null);
    }

    //400错误
    @ExceptionHandler({TypeMismatchException.class})
    @ResponseBody
    public String requestTypeMismatch(TypeMismatchException ex) {
       logger.error("400..TypeMismatchException");
        ex.printStackTrace();
        return ReturnFormat.retParam(400, ex, null);
    }

    //400错误
    @ExceptionHandler({MissingServletRequestParameterException.class})
    @ResponseBody
    public String requestMissingServletRequest(MissingServletRequestParameterException ex) {
       logger.error("400..MissingServletRequest");
        ex.printStackTrace();
        return ReturnFormat.retParam(400, ex, null);
    }

    //404错误
    @ExceptionHandler({NoHandlerFoundException.class})
    @ResponseBody
    public String request404(NoHandlerFoundException ex) {
       logger.error("404..NoHandlerFoundException");
        ex.printStackTrace();
        return ReturnFormat.retParam(404, ex, null);
    }

    //405错误
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    @ResponseBody
    public String request405() {
       logger.error("405...");
        return ReturnFormat.retParam(405, null);
    }

    //406错误
    @ExceptionHandler({HttpMediaTypeNotAcceptableException.class})
    @ResponseBody
    public String request406() {
       logger.error("406...");
        return ReturnFormat.retParam(406, null);
    }

    //415错误
    @ExceptionHandler({HttpMediaTypeNotSupportedException.class})
    @ResponseBody
    public String request415() {
       logger.error("415...");
        return ReturnFormat.retParam(415, null);
    }

    //500错误
    @ExceptionHandler({ConversionNotSupportedException.class, HttpMessageNotWritableException.class,MultipartException.class})
    @ResponseBody
    public String server500(RuntimeException ex) {
       logger.error("500...");
        ex.printStackTrace();
        return ReturnFormat.retParam(500, ex, null);
    }

    //
    @ExceptionHandler({Exception.class})
    @ResponseBody
    public String other(Exception ex) {
        logger.error("415...");
        ex.printStackTrace();
        return ReturnFormat.retParam(-1, ex.getLocalizedMessage(), null);
    }
}