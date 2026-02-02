package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.constants.AppConstants;
import alicanteweb.pelisapp.entity.CategoryEntity;
import alicanteweb.pelisapp.entity.Movie;
import alicanteweb.pelisapp.repository.CategoryRepository;
import alicanteweb.pelisapp.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de la página principal.
 * Implementa principios de código limpio usando constantes centralizadas.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final MovieRepository movieRepository;
    private final CategoryRepository categoryRepository;

    @GetMapping("/")
    public String home(Model model,
                       @RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "size", defaultValue = "12") int size,
                       @RequestParam(value = "genre", required = false) String genre) {

        try {
            PaginationParams pagination = validatePaginationParams(page, size);
            Page<Movie> moviesPage = getMoviesByGenre(genre, pagination);

            addMoviesDataToModel(model, moviesPage, genre);
            addCategoriesDataToModel(model);
            addPaginationDataToModel(model, moviesPage);

            log.debug("Página principal cargada - Página: {}, Tamaño: {}, Género: {}, Total películas: {}",
                    pagination.page(), pagination.size(), genre, moviesPage.getTotalElements());

            return "index";

        } catch (Exception e) {
            log.error("Error cargando página principal: {}", e.getMessage());
            model.addAttribute("error", "Error cargando el catálogo de películas");
            return "error";
        }
    }

    /**
     * Valida y sanitiza los parámetros de paginación usando constantes.
     */
    private PaginationParams validatePaginationParams(int page, int size) {
        int safePage = Math.max(AppConstants.DEFAULT_PAGE_NUMBER, page);
        int safeSize = Math.min(Math.max(AppConstants.MIN_PAGE_SIZE, size), AppConstants.MAX_PAGE_SIZE);
        return new PaginationParams(safePage, safeSize);
    }

    /**
     * Obtiene películas por género con paginación.
     */
    private Page<Movie> getMoviesByGenre(String genre, PaginationParams pagination) {
        Pageable pageable = PageRequest.of(pagination.page(), pagination.size());

        if (genre != null && !genre.isBlank()) {
            log.debug("Buscando películas por género: {}", genre);
            return movieRepository.findByCategories_Name(genre, pageable);
        } else {
            log.debug("Buscando todas las películas - página: {}", pagination.page());
            return movieRepository.findAll(pageable);
        }
    }

    /**
     * Añade datos de películas al modelo de la vista.
     */
    private void addMoviesDataToModel(Model model, Page<Movie> moviesPage, String selectedGenre) {
        model.addAttribute("movies", moviesPage.getContent());
        model.addAttribute("moviesPage", moviesPage);
        model.addAttribute("selectedGenre", selectedGenre);
    }

    /**
     * Añade datos de categorías al modelo de la vista.
     */
    private void addCategoriesDataToModel(Model model) {
        List<CategoryEntity> categories = categoryRepository.findAll();
        model.addAttribute("categories", categories);
    }

    /**
     * Añade datos de paginación al modelo de la vista.
     */
    private void addPaginationDataToModel(Model model, Page<Movie> moviesPage) {
        int totalPages = moviesPage.getTotalPages();
        int currentPage = moviesPage.getNumber();

        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", moviesPage.getSize());
        model.addAttribute("pageNumbers", calculatePageNumbers(currentPage, totalPages));
    }

    /**
     * Calcula los números de página a mostrar en la paginación.
     */
    private List<Integer> calculatePageNumbers(int currentPage, int totalPages) {
        final int WINDOW_SIZE = 5;
        int start = Math.max(0, currentPage - WINDOW_SIZE / 2);
        int end = Math.min(totalPages - 1, start + WINDOW_SIZE - 1);

        // Ajustar el inicio si no hay suficientes páginas al final
        if (end - start < WINDOW_SIZE - 1) {
            start = Math.max(0, end - WINDOW_SIZE + 1);
        }

        List<Integer> pageNumbers = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            pageNumbers.add(i);
        }
        return pageNumbers;
    }

    /**
     * Record inmutable para parámetros de paginación validados.
     */
    private record PaginationParams(int page, int size) {}
}
