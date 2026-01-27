package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.entity.Actor;
import alicanteweb.pelisapp.repository.ActorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ActorService {

    private final ActorRepository actorRepository;

    public ActorService(ActorRepository actorRepository) {
        this.actorRepository = actorRepository;
    }

    public List<Actor> findAll() {
        return actorRepository.findAll();
    }

    public Optional<Actor> findById(Integer id) {
        return actorRepository.findById(id);
    }

    public Optional<Actor> findByNombre(String nombre) {
        return actorRepository.findByNombre(nombre);
    }

    public Optional<Actor> findByTmdbId(Integer tmdbId) {
        return actorRepository.findByTmdbId(tmdbId);
    }

    @Transactional
    public Actor save(Actor actor) {
        return actorRepository.save(actor);
    }

    @Transactional
    public void deleteById(Integer id) {
        actorRepository.deleteById(id);
    }
}