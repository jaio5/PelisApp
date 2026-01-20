package alicanteweb.pelisapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TmdbImageService {

    private final String imageBaseUrl;

    public TmdbImageService(@Value("${app.tmdb.image-base-url}") String imageBaseUrl) {
        this.imageBaseUrl = imageBaseUrl;
    }

    public String posterUrl(String path, String size) {
        if (path == null) return null;
        return String.format("%s/%s%s", imageBaseUrl, size, path);
    }

    public String posterUrl(String path) {
        return posterUrl(path, "w500");
    }
}

