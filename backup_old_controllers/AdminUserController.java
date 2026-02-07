package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.dto.AssignRoleRequest;
import alicanteweb.pelisapp.dto.AssignTagRequest;
import alicanteweb.pelisapp.exception.ResourceNotFoundException;
import alicanteweb.pelisapp.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para la administración de usuarios.
 * Delega toda la lógica de negocio al servicio correspondiente.
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Slf4j
public class AdminUserController {

    private final AdminUserService adminUserService;

    /**
     * Asigna un rol a un usuario.
     *
     * @param userId ID del usuario
     * @param request Datos del rol a asignar
     * @return ResponseEntity vacío con código de estado apropiado
     */
    @PostMapping("/{userId}/roles")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignRole(
            @PathVariable Long userId,
            @Valid @RequestBody AssignRoleRequest request) {

        try {
            adminUserService.assignRole(userId, request);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            log.warn("Error asignando rol: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Remueve un rol de un usuario.
     *
     * @param userId ID del usuario
     * @param roleId ID del rol a remover
     * @return ResponseEntity vacío con código de estado apropiado
     */
    @DeleteMapping("/{userId}/roles/{roleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeRole(
            @PathVariable Long userId,
            @PathVariable Long roleId) {

        try {
            adminUserService.removeRole(userId, roleId);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            log.warn("Error removiendo rol: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Asigna una etiqueta a un usuario.
     *
     * @param userId ID del usuario
     * @param request Datos de la etiqueta a asignar
     * @return ResponseEntity vacío con código de estado apropiado
     */
    @PostMapping("/{userId}/tags")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> assignTag(
            @PathVariable Long userId,
            @Valid @RequestBody AssignTagRequest request) {

        try {
            adminUserService.assignTag(userId, request);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            log.warn("Error asignando etiqueta: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Remueve una etiqueta de un usuario.
     *
     * @param userId ID del usuario
     * @param tagId ID de la etiqueta a remover
     * @return ResponseEntity vacío con código de estado apropiado
     */
    @DeleteMapping("/{userId}/tags/{tagId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeTag(
            @PathVariable Long userId,
            @PathVariable Long tagId) {

        try {
            adminUserService.removeTag(userId, tagId);
            return ResponseEntity.ok().build();
        } catch (ResourceNotFoundException e) {
            log.warn("Error removiendo etiqueta: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        }
    }
}
