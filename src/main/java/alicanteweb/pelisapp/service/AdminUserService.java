package alicanteweb.pelisapp.service;

import alicanteweb.pelisapp.dto.AssignRoleRequest;
import alicanteweb.pelisapp.dto.AssignTagRequest;
import alicanteweb.pelisapp.entity.Role;
import alicanteweb.pelisapp.entity.Tag;
import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.exception.ResourceNotFoundException;
import alicanteweb.pelisapp.repository.RoleRepository;
import alicanteweb.pelisapp.repository.TagRepository;
import alicanteweb.pelisapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio dedicado para la gestión administrativa de usuarios.
 * Separa la lógica de negocio del controlador siguiendo principios SOLID.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TagRepository tagRepository;

    /**
     * Asigna un rol a un usuario.
     *
     * @param userId ID del usuario
     * @param request Datos del rol a asignar
     * @throws ResourceNotFoundException si el usuario o rol no existe
     */
    @Transactional
    public void assignRole(Long userId, AssignRoleRequest request) {
        log.info("Asignando rol {} a usuario {}", request.getRoleId(), userId);

        User user = findUserByIdOrThrow(userId);
        Role role = findRoleByIdOrThrow(request.getRoleId());

        if (user.getRoles().stream().anyMatch(r -> r.getId().equals(role.getId()))) {
            log.warn("Usuario {} ya tiene el rol {}", userId, role.getName());
            return; // Idempotente - no es error
        }

        user.addRole(role);
        userRepository.save(user);

        log.info("Rol {} asignado exitosamente a usuario {}", role.getName(), user.getUsername());
    }

    /**
     * Remueve un rol de un usuario.
     *
     * @param userId ID del usuario
     * @param roleId ID del rol a remover
     * @throws ResourceNotFoundException si el usuario o rol no existe
     */
    @Transactional
    public void removeRole(Long userId, Long roleId) {
        log.info("Removiendo rol {} de usuario {}", roleId, userId);

        User user = findUserByIdOrThrow(userId);
        Role role = findRoleByIdOrThrow(roleId);

        user.removeRole(role);
        userRepository.save(user);

        log.info("Rol {} removido exitosamente de usuario {}", role.getName(), user.getUsername());
    }

    /**
     * Asigna una etiqueta a un usuario.
     *
     * @param userId ID del usuario
     * @param request Datos de la etiqueta a asignar
     * @throws ResourceNotFoundException si el usuario o etiqueta no existe
     */
    @Transactional
    public void assignTag(Long userId, AssignTagRequest request) {
        log.info("Asignando tag {} a usuario {}", request.getTagId(), userId);

        User user = findUserByIdOrThrow(userId);
        Tag tag = findTagByIdOrThrow(request.getTagId());

        if (user.getTags().stream().anyMatch(t -> t.getId().equals(tag.getId()))) {
            log.warn("Usuario {} ya tiene el tag {}", userId, tag.getName());
            return; // Idempotente
        }

        user.addTag(tag);
        userRepository.save(user);

        log.info("Tag {} asignado exitosamente a usuario {}", tag.getName(), user.getUsername());
    }

    /**
     * Remueve una etiqueta de un usuario.
     *
     * @param userId ID del usuario
     * @param tagId ID de la etiqueta a remover
     * @throws ResourceNotFoundException si el usuario o etiqueta no existe
     */
    @Transactional
    public void removeTag(Long userId, Long tagId) {
        log.info("Removiendo tag {} de usuario {}", tagId, userId);

        User user = findUserByIdOrThrow(userId);
        Tag tag = findTagByIdOrThrow(tagId);

        user.removeTag(tag);
        userRepository.save(user);

        log.info("Tag {} removido exitosamente de usuario {}", tag.getName(), user.getUsername());
    }

    /**
     * Busca un usuario por ID o lanza excepción.
     */
    private User findUserByIdOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + userId));
    }

    /**
     * Busca un rol por ID o lanza excepción.
     */
    private Role findRoleByIdOrThrow(Long roleId) {
        return roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado con ID: " + roleId));
    }

    /**
     * Busca una etiqueta por ID o lanza excepción.
     */
    private Tag findTagByIdOrThrow(Long tagId) {
        return tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Etiqueta no encontrada con ID: " + tagId));
    }
}
