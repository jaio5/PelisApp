package alicanteweb.pelisapp.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuración para servir archivos estáticos de imágenes
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.images.storage-path:./data/images}")
    private String imagesStoragePath;

    @Value("${app.images.serve-base:/images}")
    private String imagesServeBase;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Configurar para servir imágenes desde el directorio local
        String resourceLocation = "file:" + imagesStoragePath + "/";
        String pathPattern = imagesServeBase + "/**";

        registry.addResourceHandler(pathPattern)
                .addResourceLocations(resourceLocation)
                .setCachePeriod(3600); // Cache por 1 hora

        System.out.println("✅ Configurado servido de imágenes: " + pathPattern + " -> " + resourceLocation);
    }
}
