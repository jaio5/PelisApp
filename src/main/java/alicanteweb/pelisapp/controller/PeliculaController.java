package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.dto.ActorDTO;
import alicanteweb.pelisapp.dto.DirectorDTO;
import alicanteweb.pelisapp.dto.PeliculaDetailDTO;
import alicanteweb.pelisapp.dto.ResenaDTO;
import alicanteweb.pelisapp.entity.Pelicula;
import alicanteweb.pelisapp.entity.Resena;
import alicanteweb.pelisapp.mapper.ResenaMapper;
import alicanteweb.pelisapp.repository.PeliculaRepository;
import alicanteweb.pelisapp.repository.ResenaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/peliculas")
public class PeliculaController {

    private final PeliculaRepository peliculaRepository;
    private final ResenaRepository resenaRepository;

    public PeliculaController(PeliculaRepository peliculaRepository, ResenaRepository resenaRepository) {
        this.peliculaRepository = peliculaRepository;
        this.resenaRepository = resenaRepository;
    }

    @GetMapping
    public List<Pelicula> list() {
        return peliculaRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Integer id) {
        Optional<Pelicula> pOpt = peliculaRepository.findById(id);
        if (pOpt.isEmpty()) return ResponseEntity.notFound().build();
        Pelicula p = pOpt.get();
        double avg = resenaRepository.avgPuntuacionByPeliculaId(id);
        List<Resena> resenas = resenaRepository.findByPeliculaIdFetchUsuario(id);
        List<ResenaDTO> resenasDto = resenas.stream().map(ResenaMapper::toDto).collect(Collectors.toList());

        // Mapear actores y directores a DTOs (aseguramos accesos a lazy collections)
        List<ActorDTO> actores = p.getActores().stream().map(a -> new ActorDTO(a.getId(), a.getNombre(), a.getFotoUrl())).collect(Collectors.toList());
        List<DirectorDTO> directores = p.getDirectores().stream().map(d -> new DirectorDTO(d.getId(), d.getNombre(), d.getFotoUrl())).collect(Collectors.toList());

        PeliculaDetailDTO dto = new PeliculaDetailDTO(p, avg, resenasDto, actores, directores);
        return ResponseEntity.ok(dto);
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
