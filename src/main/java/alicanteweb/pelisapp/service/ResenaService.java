package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.entity.Resena;
import alicanteweb.pelisapp.repository.ResenaRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class ResenaService {

    public final ResenaRepository resenaRepository;
    public final UsuarioService usuarioService;

    public ResenaService(ResenaRepository resenaRepository, UsuarioService usuarioService){
        this.resenaRepository = resenaRepository;
        this.usuarioService = usuarioService;
    }
    @Transactional
    public Resena saveResena(Resena r){
        Resena saved = resenaRepository.save(r);
        usuarioService.recalcularNivelCritico(saved.getUsuario().getId());
        return saved;
    }

    @Transactional
    public Resena deleteResena(Resena r){
        resenaRepository.delete(r);
        usuarioService.recalcularNivelCritico(r.getUsuario().getId());
        return r;
    }

    @Transactional
    public Resena updateResena(Resena r){
        Resena updated = resenaRepository.save(r);
        usuarioService.recalcularNivelCritico(r.getUsuario().getId());
        return updated;
    }

}
