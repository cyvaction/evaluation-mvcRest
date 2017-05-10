package swx.service;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import swx.dbaccess.SqlHelper;
import swx.domain.User;

import java.sql.SQLException;
import java.util.List;

/**
 * 人员编码业务逻辑类
 *
 * @author zgx
 * @Description:
 * @date 2017/4/27
 */
@Service
public class UserService {
    private static Logger logger = Logger.getLogger(UserService.class);

    public String findJsonList() {
        String json = null;
        try {
            json = SqlHelper.ExecuteJson("select dwbm,gh,xm,pwd from t_user");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return json;
    }

    public List<User> findAllUser() {
        List<User> list = null;
        try {
            list = SqlHelper.ExecuteEntity(User.class, "select dwbm,gh,xm from t_user");
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return list;
    }

    public User findOne(String dwbm, String gh) {
        User user = null;
        try {
            List<User> list = SqlHelper.ExecuteEntity(User.class, "select * from t_user where dwbm=? and gh=?", dwbm, gh);
            if (list.size() > 0)
                user = list.get(0);
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return user;
    }

    public String saveUser(User user) {
        String res = null;
        try {
            SqlHelper.BeginTransaction();
            int i = SqlHelper.ExecuteNonQuery("INSERT INTO t_user  (DWBM, GH, PWD, XM) VALUES  (?,?,?,?)"
                    , user.getDwbm(), user.getGh(), "111111", user.getXm());
            if (i > 0) {
                SqlHelper.CommitTransaction(SqlHelper.GetConnection());
            } else {
                SqlHelper.RollbackTransaction(SqlHelper.GetConnection());
                res = "保存用户失败！";
            }

        } catch (SQLException e) {
            res = "保存用户失败！" + e.getLocalizedMessage();
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return res;
    }


    public String updateUser(User user) {
        String res = null;
        try {
            SqlHelper.BeginTransaction();
            int i = SqlHelper.ExecuteNonQuery("update t_user set XM=? where trim(dwbm)=? and trim(gh)=?"
                    , user.getXm(), user.getDwbm(), user.getGh());

            if (i > 0) {
                SqlHelper.CommitTransaction(SqlHelper.GetConnection());
            } else {
                SqlHelper.RollbackTransaction(SqlHelper.GetConnection());
                res = "更新用户失败！";
            }

        } catch (SQLException e) {
            res = "更新用户失败！" + e.getLocalizedMessage();
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return res;
    }

    public User check(String dwbm, String gh, String pwd) {
        User user = null;
        try {
            List<User> list = SqlHelper.ExecuteEntity(User.class, "select * from t_user where trim(dwbm)=? and trim(gh)=?", dwbm, gh);

            if (list.size() > 0 && pwd.equals(list.get(0).getPwd())) {
                user = list.get(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return user;
    }


    public String deleteUser(String dwbm, String gh) {
        String res = null;
        try {
            SqlHelper.BeginTransaction();
            int i = SqlHelper.ExecuteNonQuery("DELETE FROM t_user  where trim(dwbm)=? and trim(gh)=?"
                    , dwbm, gh);

            if (i > 0) {
                SqlHelper.CommitTransaction(SqlHelper.GetConnection());
            } else {
                SqlHelper.RollbackTransaction(SqlHelper.GetConnection());
                res = "删除用户失败！";
            }
        } catch (SQLException e) {
            res = "删除用户失败！" + e.getLocalizedMessage();
            e.printStackTrace();
            logger.error(e.getMessage());
        }
        return res;
    }

}
