package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.service.EtiquetaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/etiquetas")
public class AdminEtiquetaController {

    private final EtiquetaService etiquetaService;

    public AdminEtiquetaController(EtiquetaService etiquetaService) {
        this.etiquetaService = etiquetaService;
    }

    @PostMapping("/recalcular/{usuarioId}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> recalcular(@PathVariable Integer usuarioId) {
        etiquetaService.assignBadgesToUser(usuarioId);
        return ResponseEntity.ok().build();
    }
}

