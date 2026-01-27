package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.entity.Pelicula;
import alicanteweb.pelisapp.repository.PeliculaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PeliculaService {

    private final PeliculaRepository peliculaRepository;

    public PeliculaService(PeliculaRepository peliculaRepository) {
        this.peliculaRepository = peliculaRepository;
    }

    public List<Pelicula> findAll() {
        return peliculaRepository.findAll();
    }

    public Optional<Pelicula> findById(Integer id) {
        return peliculaRepository.findById(id);
    }

    public Optional<Pelicula> findByTmdbId(Integer tmdbId) {
        return peliculaRepository.findByTmdbId(tmdbId);
    }

    @Transactional
    public Pelicula save(Pelicula pelicula) {
        return peliculaRepository.save(pelicula);
    }

    @Transactional
    public void deleteById(Integer id) {
        peliculaRepository.deleteById(id);
    }
}