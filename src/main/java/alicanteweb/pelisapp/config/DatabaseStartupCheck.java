package alicanteweb.pelisapp.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;

@Configuration
public class DatabaseStartupCheck {

    private static final Logger log = LoggerFactory.getLogger(DatabaseStartupCheck.class);

    @Bean
    public CommandLineRunner checkDataSource(DataSource dataSource) {
        return args -> {
            try (Connection c = dataSource.getConnection()) {
                boolean valid = c.isValid(2);
                log.info("Database connection valid={} (catalog={})", valid, c.getCatalog());
            } catch (Exception e) {
                log.error("Failed to obtain a database connection at startup: {}", e, e);
                // don't rethrow: let the app continue to start if configured to do so in dev-mode
            }
        };
    }
}
