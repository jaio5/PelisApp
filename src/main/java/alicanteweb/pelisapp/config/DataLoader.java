package alicanteweb.pelisapp.config;

import alicanteweb.pelisapp.entity.Movie;
import alicanteweb.pelisapp.entity.CategoryEntity;
import alicanteweb.pelisapp.repository.MovieRepository;
import alicanteweb.pelisapp.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Carga datos de prueba al iniciar la aplicación en modo desarrollo
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements ApplicationRunner {

    private final MovieRepository movieRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(ApplicationArguments args) {
        try {
            long movieCount = movieRepository.count();
            log.info("Películas existentes en la base de datos: {}", movieCount);

            if (movieCount < 20) { // Cargar más películas si hay menos de 20
                log.info("Cargando datos de prueba adicionales...");
                loadSampleData();
                long newCount = movieRepository.count();
                log.info("Datos de prueba cargados. Total de películas ahora: {}", newCount);
            } else {
                log.info("Ya hay suficientes películas en la base de datos ({}), omitiendo carga de datos de prueba", movieCount);
            }
        } catch (Exception e) {
            log.error("Error en DataLoader: {}. La aplicación continuará ejecutándose.", e.getMessage());
            // No relanzar la excepción para evitar que falle el inicio de la aplicación
        }
    }

    private void loadSampleData() {
        // Crear categorías
        CategoryEntity accion = createCategory("Acción");
        CategoryEntity drama = createCategory("Drama");
        CategoryEntity comedia = createCategory("Comedia");
        CategoryEntity thriller = createCategory("Thriller");
        CategoryEntity scifi = createCategory("Ciencia Ficción");
        CategoryEntity horror = createCategory("Terror");

        // Crear películas de ejemplo
        createMovie(
            "The Matrix",
            "Un programador descubre la verdad sobre su realidad y su papel en la guerra contra las máquinas.",
            LocalDate.of(1999, 3, 31),
            "https://image.tmdb.org/t/p/w500/f89U3ADr1oiB1s9GkdPOEpXUk5H.jpg",
            Arrays.asList(accion, scifi),
            603L // TMDB ID real de The Matrix
        );

        createMovie(
            "Pulp Fiction",
            "Las vidas de dos sicarios, un boxeador, la esposa de un gángster y dos bandidos se entrelazan.",
            LocalDate.of(1994, 10, 14),
            "https://image.tmdb.org/t/p/w500/d5iIlFn5s0ImszYzBPb8JPIfbXD.jpg",
            Arrays.asList(drama, thriller),
            680L
        );

        createMovie(
            "Forrest Gump",
            "Las presidencias de Kennedy y Johnson a través de la perspectiva de un hombre de Alabama con coeficiente intelectual de 75.",
            LocalDate.of(1994, 7, 6),
            "https://image.tmdb.org/t/p/w500/arw2vcBveWOVZr6pxd9XTd1TdQa.jpg",
            Arrays.asList(drama, comedia),
            13L
        );

        createMovie(
            "El Padrino",
            "La historia de una familia de la mafia italoamericana en Nueva York desde 1945 hasta 1955.",
            LocalDate.of(1972, 3, 24),
            "https://image.tmdb.org/t/p/w500/3bhkrj58Vtu7enYsRolD1fZdja1.jpg",
            Arrays.asList(drama, thriller),
            238L
        );

        createMovie(
            "Inception",
            "Un ladrón que entra en los sueños de las personas para robar secretos debe realizar una misión imposible.",
            LocalDate.of(2010, 7, 16),
            "https://image.tmdb.org/t/p/w500/9gk7adHYeDvHkCSEqAvQNLV5Uge.jpg",
            Arrays.asList(accion, scifi, thriller),
            27205L
        );

        createMovie(
            "El Resplandor",
            "Una familia se muda a un hotel aislado durante el invierno donde fuerzas siniestras influyen en el padre.",
            LocalDate.of(1980, 5, 23),
            "https://image.tmdb.org/t/p/w500/b6ko0IKC8MdYBBPkkA1aBPLe2yz.jpg",
            Arrays.asList(horror, thriller),
            694L
        );

        createMovie(
            "Interstellar",
            "Un grupo de exploradores viaja a través de un agujero de gusano para asegurar la supervivencia de la humanidad.",
            LocalDate.of(2014, 11, 7),
            "https://image.tmdb.org/t/p/w500/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
            Arrays.asList(scifi, drama),
            157336L
        );

        createMovie(
            "Avengers: Endgame",
            "Los Vengadores restantes deben encontrar una manera de derrotar a Thanos de una vez por todas.",
            LocalDate.of(2019, 4, 26),
            "https://image.tmdb.org/t/p/w500/or06FN3Dka5tukK1e9sl16pB3iy.jpg",
            Arrays.asList(accion, scifi),
            299534L
        );

        // Agregar más películas populares
        createMovie(
            "Titanic",
            "Una historia de amor épica entre Jack y Rose a bordo del famoso barco.",
            LocalDate.of(1997, 12, 19),
            "https://image.tmdb.org/t/p/w500/9xjZS2rlVxm8SFx8kPC3aIGCOYQ.jpg",
            Arrays.asList(drama),
            597L
        );

        createMovie(
            "The Dark Knight",
            "Batman debe enfrentarse al Joker, un criminal psicópata que quiere sembrar el caos en Gotham.",
            LocalDate.of(2008, 7, 18),
            "https://image.tmdb.org/t/p/w500/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
            Arrays.asList(accion, thriller),
            155L
        );

        createMovie(
            "Spider-Man: No Way Home",
            "Peter Parker deberá enfrentarse a enemigos de diferentes universos cuando el multiverso se abre.",
            LocalDate.of(2021, 12, 17),
            "https://image.tmdb.org/t/p/w500/1g0dhYtq4irTY1GPXvft6k4YLjm.jpg",
            Arrays.asList(accion, scifi),
            634649L
        );

        createMovie(
            "Avatar",
            "Un parapléjico marino se une al programa Avatar y se enamora de una princesa Na'vi.",
            LocalDate.of(2009, 12, 18),
            "https://image.tmdb.org/t/p/w500/6EiRUJpuoeQPghrs3YNktfnqOVh.jpg",
            Arrays.asList(accion, scifi),
            19995L
        );

        createMovie(
            "Joker",
            "La historia del origen del icónico villano de Batman en un Gotham City sórdido.",
            LocalDate.of(2019, 10, 4),
            "https://image.tmdb.org/t/p/w500/udDclJoHjfjb8Ekgsd4FDteOkCU.jpg",
            Arrays.asList(drama, thriller),
            475557L
        );

        createMovie(
            "Top Gun: Maverick",
            "Después de más de 30 años de servicio, Pete 'Maverick' Mitchell entrena a una nueva generación.",
            LocalDate.of(2022, 5, 27),
            "https://image.tmdb.org/t/p/w500/62HCnUTziyWcpDaBO2i1DX17ljH.jpg",
            Arrays.asList(accion, drama),
            361743L
        );

        createMovie(
            "Black Panther",
            "T'Challa regresa a Wakanda para ser coronado rey, pero un viejo enemigo emerge.",
            LocalDate.of(2018, 2, 16),
            "https://image.tmdb.org/t/p/w500/uxzzxijgPIY7slzFvMotPv8wjKA.jpg",
            Arrays.asList(accion, scifi),
            284054L
        );

        createMovie(
            "Star Wars: The Rise of Skywalker",
            "La conclusión de la saga Skywalker mientras la Resistencia enfrenta a la Primera Orden.",
            LocalDate.of(2019, 12, 20),
            "https://image.tmdb.org/t/p/w500/db32LaOibwEliAmSL2jjDF6oDdj.jpg",
            Arrays.asList(accion, scifi),
            181812L
        );

        createMovie(
            "Dune",
            "Paul Atreides viaja al planeta más peligroso del universo para asegurar el futuro de su pueblo.",
            LocalDate.of(2021, 10, 22),
            "https://image.tmdb.org/t/p/w500/d5NXSklXo0qyIYkgV94XAgMIckC.jpg",
            Arrays.asList(accion, scifi),
            438631L
        );

        createMovie(
            "The Lion King",
            "El joven león Simba debe reclamar su lugar como rey tras la muerte de su padre.",
            LocalDate.of(2019, 7, 19),
            "https://image.tmdb.org/t/p/w500/2bXbqYdUdNVa8VIWXVfclP2ICtT.jpg",
            Arrays.asList(drama),
            420818L
        );

        createMovie(
            "Frozen II",
            "Elsa debe descubrir el origen de sus poderes mágicos para salvar su reino.",
            LocalDate.of(2019, 11, 22),
            "https://image.tmdb.org/t/p/w500/pjeMs3yqRmFL3giJy4PMXWZTTPa.jpg",
            Arrays.asList(comedia),
            330457L
        );

        createMovie(
            "Wonder Woman 1984",
            "Diana Prince se enfrenta a nuevos enemigos en los años 80.",
            LocalDate.of(2020, 12, 25),
            "https://image.tmdb.org/t/p/w500/8UlWHLMpgZm9bx6QYh0NFoq67TZ.jpg",
            Arrays.asList(accion, scifi),
            464052L
        );

        createMovie(
            "Fast & Furious 9",
            "Dom y su familia deben enfrentarse a su pasado cuando regresa su hermano.",
            LocalDate.of(2021, 6, 25),
            "https://image.tmdb.org/t/p/w500/bOFaAXmWWXC3Rbv4u4uM9ZSzRXP.jpg",
            Arrays.asList(accion, thriller),
            385128L
        );

        createMovie(
            "No Time to Die",
            "James Bond ha dejado el servicio activo, pero un viejo amigo le pide ayuda.",
            LocalDate.of(2021, 10, 8),
            "https://image.tmdb.org/t/p/w500/iUgygt3fscRoKWCV1d0C7FbM9TP.jpg",
            Arrays.asList(accion, thriller),
            370172L
        );

        createMovie(
            "Eternals",
            "Los Eternals, seres inmortales, emergen de las sombras para proteger la Tierra.",
            LocalDate.of(2021, 11, 5),
            "https://image.tmdb.org/t/p/w500/6AdXwFTRTAzggD2QUTt5B7JFGKL.jpg",
            Arrays.asList(accion, scifi),
            524434L
        );

        // Agregar películas de terror
        createMovie(
            "It",
            "Un grupo de niños debe enfrentarse a un payaso asesino en su pueblo natal.",
            LocalDate.of(2017, 9, 8),
            "https://image.tmdb.org/t/p/w500/9E2y5Q7WlCVNEhP5GiVTjhEhx1o.jpg",
            Arrays.asList(horror, thriller),
            346364L
        );

        createMovie(
            "A Quiet Place",
            "Una familia vive en silencio para evitar a criaturas que cazan por el sonido.",
            LocalDate.of(2018, 4, 6),
            "https://image.tmdb.org/t/p/w500/nAU74GmpUk7t5iklEp3bufwDq4n.jpg",
            Arrays.asList(horror, thriller),
            447332L
        );

        // Agregar comedias
        createMovie(
            "Deadpool",
            "Un ex-militar convertido en mercenario busca venganza tras un experimento que lo desfigura.",
            LocalDate.of(2016, 2, 12),
            "https://image.tmdb.org/t/p/w500/fSRb7vyIP8rQpL0I47P3qUsEKX3.jpg",
            Arrays.asList(accion, comedia),
            293660L
        );

        createMovie(
            "Guardians of the Galaxy",
            "Un grupo de inadaptados debe salvar la galaxia de un fanático alienígena.",
            LocalDate.of(2014, 8, 1),
            "https://image.tmdb.org/t/p/w500/r7vmZjiyZw9rpJMQJdXpjgiCOk9.jpg",
            Arrays.asList(accion, comedia, scifi),
            118340L
        );
    }

    private CategoryEntity createCategory(String name) {
        return categoryRepository.findByName(name)
            .orElseGet(() -> {
                CategoryEntity category = new CategoryEntity();
                category.setName(name);
                return categoryRepository.save(category);
            });
    }

    private void createMovie(String title, String description, LocalDate releaseDate,
                           String posterPath, List<CategoryEntity> categories, Long tmdbId) {
        // Verificar si la película ya existe por tmdbId para evitar duplicados
        if (tmdbId != null && movieRepository.findByTmdbId(tmdbId).isPresent()) {
            log.debug("Película con TMDB ID {} ya existe, omitiendo: {}", tmdbId, title);
            return;
        }

        // Verificar si ya existe por título para evitar duplicados por título
        Optional<Movie> existingByTitle = movieRepository.findByTitle(title);
        if (existingByTitle.isPresent()) {
            log.debug("Película con título '{}' ya existe, omitiendo", title);
            return;
        }

        try {
            Movie movie = new Movie();
            movie.setTitle(title);
            movie.setDescription(description);
            movie.setReleaseDate(releaseDate);
            movie.setPosterPath(posterPath);
            movie.setTmdbId(tmdbId);
            movie.setCategories(new HashSet<>(categories));

            movieRepository.save(movie);
            log.debug("Película creada exitosamente: {}", title);

        } catch (Exception e) {
            log.warn("Error creando película '{}': {}", title, e.getMessage());
        }
    }
}
