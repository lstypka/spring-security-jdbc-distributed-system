package pl.lstypka.springSecurityDistributedSystem.client1.config;
/**
 * Created by Lukasz Stypka on 2015-11-20.
 */
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import pl.lstypka.springSecurityDistributedSystem.config.config.SecurityConfig;

@SpringBootApplication
@ComponentScan({"pl.lstypka.springSecurityDistributedSystem"})
@Import({SecurityConfig.class})
public class Application {

    public static void main(String[] args) throws Throwable {
        SpringApplication.run(Application.class, args);
    }

}