package alicanteweb.pelisapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Configuración para procesamiento asíncrono
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "tmdbTaskExecutor")
    public Executor tmdbTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("TMDB-");
        executor.initialize();
        return executor;
    }

    @Bean(name = "bulkLoaderExecutor")
    public Executor bulkLoaderExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("BulkLoader-");
        executor.initialize();
        return executor;
    }
}
