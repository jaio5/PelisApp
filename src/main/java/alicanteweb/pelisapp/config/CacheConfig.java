package alicanteweb.pelisapp.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Configuración de caché y procesamiento asíncrono.
 */
@Configuration
@EnableCaching
@EnableAsync
public class CacheConfig {
}
