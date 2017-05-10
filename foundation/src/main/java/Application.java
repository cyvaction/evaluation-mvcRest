import com.google.common.base.Preconditions;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Controller;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * Spring Boot 应用启动类
 * @author zgx
 * @Description:
 * @date 2017/4/27
 */

@Controller
@EnableWebMvc
@SpringBootApplication
@ComponentScan(basePackages={"swx"})
public class Application extends WebMvcConfigurerAdapter {

    public static void main(String[] args) {
        Preconditions.checkNotNull()
        SpringApplication.run(Application.class, args);
    }
}