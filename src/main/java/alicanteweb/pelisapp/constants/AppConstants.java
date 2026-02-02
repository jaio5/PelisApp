package alicanteweb.pelisapp.constants;

/**
 * Constantes para la aplicación PelisApp.
 * Centraliza valores mágicos y mejora la mantenibilidad del código.
 */
public final class AppConstants {

    private AppConstants() {
        // Constructor privado para prevenir instanciación
    }

    // Constantes de paginación
    public static final int DEFAULT_PAGE_SIZE = 12;
    public static final int MAX_PAGE_SIZE = 48;
    public static final int MIN_PAGE_SIZE = 1;
    public static final int DEFAULT_PAGE_NUMBER = 0;

    // Constantes de roles
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_SUPERADMIN = "ROLE_SUPERADMIN";
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_MODERATOR = "ROLE_MODERATOR";
    public static final String ROLE_CRITIC = "ROLE_CRITIC";
    public static final String ROLE_TOP_CRITIC = "ROLE_TOP_CRITIC";

    // Constantes de logros/achievements
    public static final String ACHIEVEMENT_FIRST_REVIEW = "FIRST_REVIEW";
    public static final String ACHIEVEMENT_REVIEWER_10 = "REVIEWER_10";
    public static final String ACHIEVEMENT_REVIEWER_50 = "REVIEWER_50";
    public static final String ACHIEVEMENT_CRITIC_100 = "CRITIC_100";
    public static final String ACHIEVEMENT_FIRST_LIKE = "FIRST_LIKE";
    public static final String ACHIEVEMENT_POPULAR_25 = "POPULAR_25";
    public static final String ACHIEVEMENT_INFLUENCER_100 = "INFLUENCER_100";
    public static final String ACHIEVEMENT_VIRAL_REVIEW = "VIRAL_REVIEW";
    public static final String ACHIEVEMENT_POPULAR_10_FOLLOWERS = "POPULAR_10_FOLLOWERS";
    public static final String ACHIEVEMENT_CELEBRITY_50_FOLLOWERS = "CELEBRITY_50_FOLLOWERS";

    // Constantes de reviews y ratings
    public static final int MIN_STARS_RATING = 1;
    public static final int MAX_STARS_RATING = 5;
    public static final int VIRAL_REVIEW_LIKES_THRESHOLD = 20;
    public static final double CRITIC_ROLE_AVG_LIKES_THRESHOLD = 3.0;
    public static final double TOP_CRITIC_ROLE_AVG_LIKES_THRESHOLD = 5.0;

    // Constantes de moderación
    public static final double DEFAULT_TOXICITY_THRESHOLD = 0.7;
    public static final double DEFAULT_REVIEW_THRESHOLD = 0.5;
    public static final double AI_REJECTION_THRESHOLD = 0.2;

    // Constantes de URLs y rutas
    public static final String LOGIN_REDIRECT_PATH = "redirect:/login";
    public static final String PROFILE_TEMPLATE_PATH = "usuario/profile";
    public static final String INDEX_TEMPLATE_PATH = "index";
    public static final String ERROR_TEMPLATE_PATH = "error";

    // Constantes de archivos e imágenes
    public static final String DEFAULT_IMAGES_PATH = "./data/images";
    public static final String POSTERS_SUBFOLDER = "posters";
    public static final String BACKDROPS_SUBFOLDER = "backdrops";
    public static final String DEFAULT_SERVE_BASE = "/images";

    // Constantes de TMDB
    public static final String TMDB_DEFAULT_BASE_URL = "https://api.themoviedb.org/3";
    public static final String TMDB_IMAGE_BASE_URL = "https://image.tmdb.org/t/p";
    public static final String TMDB_POSTER_SIZE_W500 = "w500";
    public static final String TMDB_BACKDROP_SIZE_W780 = "w780";

    // Constantes de caché
    public static final String CACHE_MOVIES_PAGE_BY_GENRE = "moviesPageByGenre";
    public static final String CACHE_ALL_CATEGORIES = "allCategories";
    public static final String CACHE_MOVIE_DETAILS = "movieDetails";

    // Constantes de límites de texto
    public static final int MAX_REVIEW_TEXT_LENGTH = 2000;
    public static final int MIN_REVIEW_TEXT_LENGTH = 10;
    public static final int MAX_USERNAME_LENGTH = 100;
    public static final int MAX_EMAIL_LENGTH = 200;
    public static final int MAX_MOVIE_TITLE_LENGTH = 255;

    // Mensajes de error comunes
    public static final String ERROR_USER_NOT_FOUND = "Usuario no encontrado";
    public static final String ERROR_MOVIE_NOT_FOUND = "Película no encontrada";
    public static final String ERROR_REVIEW_NOT_FOUND = "Reseña no encontrada";
    public static final String ERROR_INVALID_RATING = "La puntuación debe estar entre 1 y 5";
    public static final String ERROR_REVIEW_TOO_TOXIC = "Reseña rechazada por moderación";
}
