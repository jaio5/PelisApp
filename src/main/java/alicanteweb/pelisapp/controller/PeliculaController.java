package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.entity.Pelicula;
import alicanteweb.pelisapp.repository.PeliculaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/peliculas")
public class PeliculaController {

    private final PeliculaRepository peliculaRepository;

    public PeliculaController(PeliculaRepository peliculaRepository) {
        this.peliculaRepository = peliculaRepository;
    }

    @GetMapping
    public List<Pelicula> list() {
        return peliculaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Integer id) {
        Optional<Pelicula> p = peliculaRepository.findById(id);
        return p.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_EXPERTO')")
    public ResponseEntity<?> create(@RequestBody PeliculaDTO dto) {
        Pelicula p = new Pelicula();
        p.setTitulo(dto.titulo());
        p.setAnio(dto.anio());
        p.setDuracion(dto.duracion());
        p.setSinopsis(dto.sinopsis());
        peliculaRepository.save(p);
        return ResponseEntity.ok(p);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN') or hasAuthority('ROLE_EXPERTO')")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody PeliculaDTO dto) {
        Optional<Pelicula> pOpt = peliculaRepository.findById(id);
        if (pOpt.isEmpty()) return ResponseEntity.notFound().build();
        Pelicula p = pOpt.get();
        p.setTitulo(dto.titulo());
        p.setAnio(dto.anio());
        p.setDuracion(dto.duracion());
        p.setSinopsis(dto.sinopsis());
        peliculaRepository.save(p);
        return ResponseEntity.ok(p);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> delete(@PathVariable Integer id) {
        peliculaRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }

    public record PeliculaDTO(String titulo, Integer anio, Integer duracion, String sinopsis){}
}

