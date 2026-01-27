package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.entity.Pais;
import alicanteweb.pelisapp.repository.PaisRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class PaisService {

    private final PaisRepository paisRepository;

    public PaisService(PaisRepository paisRepository) {
        this.paisRepository = paisRepository;
    }

    public List<Pais> findAll() {
        return paisRepository.findAll();
    }

    public Optional<Pais> findById(Integer id) {
        return paisRepository.findById(id);
    }

    public Optional<Pais> findByNombre(String nombre) {
        return paisRepository.findByNombre(nombre);
    }

    @Transactional
    public Pais save(Pais pais) {
        return paisRepository.save(pais);
    }

    @Transactional
    public void deleteById(Integer id) {
        paisRepository.deleteById(id);
    }
}