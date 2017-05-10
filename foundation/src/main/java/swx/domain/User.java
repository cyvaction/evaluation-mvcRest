package swx.domain;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 人员编码实体类
 */

public class User {
    /**
     * 单位编码
     */
    private String dwbm;

    /**
     * 工号
     */
    private String gh;

    /**
     * 密码
     */
    private String pwd;

    /**
     * 姓名
     */
    private String xm;

    public User() {
    }

    public User(String dwbm, String gh) {
        this.dwbm = dwbm;
        this.gh = gh;
    }

    @JsonCreator
    public User(@JsonProperty("dwbm") String dwbm,
                @JsonProperty("gh") String gh,
                @JsonProperty("pwd") String pwd,
                @JsonProperty("xm") String xm) {
        this.dwbm = dwbm;
        this.gh = gh;
        this.pwd = pwd;
        this.xm = xm;
    }

    public String getDwbm() {
        return dwbm;
    }

    public void setDwbm(String dwbm) {
        this.dwbm = dwbm;
    }

    public String getGh() {
        return gh;
    }

    public void setGh(String gh) {
        this.gh = gh;
    }

    public String getPwd() {
        return pwd;
    }

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public String getXm() {
        return xm;
    }

    public void setXm(String xm) {
        this.xm = xm;
    }
}
