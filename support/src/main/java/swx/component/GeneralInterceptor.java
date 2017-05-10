package swx.component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;


/**
 * 拦截器 token验证
 *
 * @author zgx
 * @Description:
 * @date 2017/4/28
 */

public class GeneralInterceptor extends HandlerInterceptorAdapter {
    @Value("${jwt.secret}")
    private String secret;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //根据请求头的token进行验证
        boolean isSuccess;
        final String authHeader = request.getHeader("token");

        if (request.getHeader("token") != null)
            if (authHeader == null) {
                throw new ServletException("无效的请求头");
            }

        try {
            final Claims claims = Jwts.parser().setSigningKey("cdswxzlpc").parseClaimsJws(authHeader).getBody();
//            if (claims.getExpiration().before(new Date()))
//                throw new ExpiredJwtException("token超时失效");

            //将解析出来的信息放在Request Attribute里 供controller中调用
            request.setAttribute("user", claims);
            isSuccess = true;
        } catch (ExpiredJwtException expir) {
            throw new ServletException("登录超时" + expir.getLocalizedMessage());
        } catch (Exception e) {
            throw new ServletException("token校验无效," + e.getLocalizedMessage());
        }

        if (!isSuccess) throw new Exception("未登录");

        return isSuccess;
    }

}