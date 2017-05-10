package swx.controller;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import swx.conmmon.BaseController;
import swx.conmmon.Consts;
import swx.domain.User;
import swx.service.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.ExecutionException;

import static swx.conmmon.Consts.*;

/**
 * 登录验证
 * Created on 2017/4/27
 */
@RestController
public class LoginController extends BaseController {
    @Value("${jwt.secret}")
    private String secret;//配置文件中的密钥

    @Value("${jwt.expiration}")
    private long expiration;

    @Autowired
    private UserService userService;


    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ApiOperation(value = "登录验证", notes = "根据单位编码、工号、密码")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "dwbm", value = "单位编码", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "gh", value = "工号", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "pwd", value = "密码", dataType = "String", paramType = "query")
    })
    public String login(@RequestParam("dwbm") String dwbm, @RequestParam("gh") String gh, @RequestParam("pwd") String pwd, HttpServletRequest request, HttpServletResponse response) throws ExecutionException {
        User user = userService.check(dwbm, gh, pwd);
        if (user != null) {
            //生成token
            String jwtToken = Jwts.builder().setSubject(dwbm + "-" + gh)
                    .claim("dwbm", dwbm).claim("gh", gh).claim("xm", user.getXm())
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))//有效时间
                    .signWith(SignatureAlgorithm.HS256, secret).compact();

            System.out.println("登录验证成功:生成token：" + jwtToken);

            return retData(SUCCESS_CODE, "{\"token\":\"" + jwtToken + "\"}");
        } else {
            System.err.println("登录验证失败");
            return retMsg(ERROR_CODE, "登录验证失败");
        }
    }
}
