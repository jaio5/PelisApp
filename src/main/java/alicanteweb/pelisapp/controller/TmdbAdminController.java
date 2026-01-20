package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.service.TmdbBatchImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/tmdb")
public class TmdbAdminController {

    private final TmdbBatchImportService batchImportService;

    public TmdbAdminController(TmdbBatchImportService batchImportService) {
        this.batchImportService = batchImportService;
    }

    @PostMapping("/import/popular")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> importPopular(@RequestParam(defaultValue = "1") int pages) {
        int imported = batchImportService.importPopularPages(pages);
        return ResponseEntity.ok("Imported " + imported + " movies");
    }
}

