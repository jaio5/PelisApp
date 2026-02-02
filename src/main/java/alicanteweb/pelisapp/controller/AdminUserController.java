package alicanteweb.pelisapp.controller;

import alicanteweb.pelisapp.entity.Role;
import alicanteweb.pelisapp.entity.Tag;
import alicanteweb.pelisapp.entity.User;
import alicanteweb.pelisapp.repository.RoleRepository;
import alicanteweb.pelisapp.repository.TagRepository;
import alicanteweb.pelisapp.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final TagRepository tagRepository;

    public AdminUserController(UserRepository userRepository, RoleRepository roleRepository, TagRepository tagRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.tagRepository = tagRepository;
    }

    @PostMapping("/{userId}/roles")
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> assignRole(@PathVariable Long userId, @RequestBody Map<String, Long> body) {
        Long roleId = body.get("roleId");
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role == null) return ResponseEntity.badRequest().body("role not found");
        user.addRole(role);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/roles/{roleId}")
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> removeRole(@PathVariable Long userId, @PathVariable Long roleId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role == null) return ResponseEntity.badRequest().body("role not found");
        user.removeRole(role);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{userId}/tags")
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> assignTag(@PathVariable Long userId, @RequestBody Map<String, Long> body) {
        Long tagId = body.get("tagId");
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        Tag tag = tagRepository.findById(tagId).orElse(null);
        if (tag == null) return ResponseEntity.badRequest().body("tag not found");
        user.addTag(tag);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{userId}/tags/{tagId}")
    @Transactional
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<?> removeTag(@PathVariable Long userId, @PathVariable Long tagId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        Tag tag = tagRepository.findById(tagId).orElse(null);
        if (tag == null) return ResponseEntity.badRequest().body("tag not found");
        user.removeTag(tag);
        userRepository.save(user);
        return ResponseEntity.ok().build();
    }
}
