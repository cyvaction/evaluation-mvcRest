package swx.controller;

import io.jsonwebtoken.Claims;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import swx.conmmon.BaseController;
import swx.conmmon.Consts;
import swx.conmmon.RespInfo;
import swx.conmmon.UploadFile;
import swx.domain.User;
import swx.service.UserService;
import swx.utils.DownUploadUtil;
import swx.utils.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import static swx.conmmon.Consts.*;
import static swx.conmmon.Consts.ERROR_CODE;

/**
 * 人员编码Controller
 *
 * @author zgx
 * @Description: 实现 Restful HTTP 服务
 * @date 2017/4/27
 */
@RestController
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @RequestMapping(value = "/fundation/users/json", method = RequestMethod.GET)
    @ApiOperation(value = "获取用户列表(数据库查询返回JSON字符串)", notes = "数据库查询返回json字符串")
    @ApiImplicitParam(name = "token", value = "token", dataType = "String", paramType = "header")
    public String findJsonList() throws SQLException {
        String json = userService.findJsonList();
        return retData(SUCCESS_CODE, json);
    }

    @RequestMapping(value = "/fundation/users", method = RequestMethod.GET)
    @ApiOperation(value = "获取用户列表（SqlHleper返回对象方式）", notes = "数据库查询返回对象list")
    @ApiImplicitParam(name = "token", value = "token", dataType = "String", paramType = "header")
    public String findAllUser() {
        Claims claims = (Claims) request.getAttribute("user");//从token解析出来的用户信息
        return retData(SUCCESS_CODE, userService.findAllUser());
    }

    @RequestMapping(value = "/fundation/user", method = RequestMethod.GET)
    @ApiOperation(value = "获取指定单位编码和工号用户详细信息", notes = "根据user的dwbm、gh来获取用户详细信息")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "dwbm", value = "单位编码", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "gh", value = "工号", dataType = "String", paramType = "query")
    })
    public String findOneUser(@RequestParam("dwbm") String dwbm, @RequestParam("gh") String gh) {
        return retData(SUCCESS_CODE, userService.findOne(dwbm, gh));
    }

    @RequestMapping(value = "/fundation/user", method = RequestMethod.POST)
    @ApiOperation(value = "创建用户", notes = "根据User对象创建用户")
    @ApiImplicitParam(name = "token", value = "token", dataType = "String", paramType = "header")
    @ApiImplicitParams({@ApiImplicitParam(dataType = "User", name = "user", value = "用户信息", required = true)})
    public String createUser(@RequestBody User user) {
        String res = userService.saveUser(user);
        if (null == res)
            return retData(SUCCESS_CODE, user);
        else
            return retMsg(ERROR_CODE, res);
    }

    @RequestMapping(value = "/fundation/user", method = RequestMethod.PUT)
    @ApiOperation(value = "更新用户", notes = "根据User对象更新用户")
    @ApiImplicitParam(name = "token", value = "token", dataType = "String", paramType = "header")
    @ApiImplicitParams({@ApiImplicitParam(dataType = "User", name = "user", value = "待更新的用户信息", required = true)})
    public String modifyUser(@RequestBody User user) {
        String res = userService.updateUser(user);
        if (null == res)
            return retData(SUCCESS_CODE, user);
        else
            return retMsg(ERROR_CODE, res);
    }

    @RequestMapping(value = "/fundation/user", method = RequestMethod.DELETE)
    @ApiOperation(value = "删除用户", notes = "根据dwbm、gh删除")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "dwbm", value = "单位编码", dataType = "String", paramType = "query"),
            @ApiImplicitParam(name = "gh", value = "工号", dataType = "String", paramType = "query")
    })
    public String modifyUser(@PathVariable("dwbm") String dwbm, @PathVariable("gh") String gh) {
        String res = userService.deleteUser(dwbm, gh);
        if (null == res)
            return retData(SUCCESS_CODE, null);
        else
            return retMsg(ERROR_CODE, res);
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    @ApiOperation(value = "附件上传", notes = "")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "token", value = "token", dataType = "String", paramType = "header"),
            @ApiImplicitParam(name = "file", value = "文件(上传到工程根目录下)", dataType = "File", paramType = "form", required = true)
    })
    public String upload(@RequestParam("file") MultipartFile file) throws IOException {
        RespInfo res = new RespInfo(ERROR_CODE, null);
        if (file.isEmpty()) {
            res.setStatus(ERROR_CODE);
            res.setMessage("文件为空");
        }
        // 获取文件名
        String fileName = file.getOriginalFilename();
        logger.info("上传的文件名为：" + fileName);

        // 获取文件的后缀名
        String suffixName = fileName.substring(fileName.lastIndexOf("."));
        logger.info("上传的后缀名为：" + suffixName);

        // 文件上传后的路径 当前项目路径
        String filePath = System.getProperty("user.dir");

        // 解决中文问题，liunx下中文路径，显示问题
        // fileName = UUID.randomUUID() + suffixName;
        File dest = new File(filePath + "//" + fileName);

        // 检测是否存在目录
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        try {
            file.transferTo(dest);
            res.setStatus(SUCCESS_CODE);

        } catch (IllegalStateException e) {
            res.setStatus(ERROR_CODE);
            res.setMessage(e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            res.setStatus(ERROR_CODE);
            res.setMessage(e.getMessage());
            e.printStackTrace();
        }
        return res.toString();
    }


}
