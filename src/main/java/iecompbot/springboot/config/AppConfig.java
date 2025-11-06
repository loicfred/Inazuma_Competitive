package iecompbot.springboot.config;

import iecompbot.springboot.data.CacheService;
import iecompbot.springboot.data.DatabaseObject;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;

@Configuration
public class AppConfig {
    public static CacheService cacheService;

    public ApplicationContext context;
    public AppConfig(ApplicationContext context) {
        this.context = context;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void setStaticReference() {
        cacheService = context.getBean(CacheService.class);
        DatabaseObject.setJdbcTemplate(context.getBean(JdbcTemplate.class));
    }
}