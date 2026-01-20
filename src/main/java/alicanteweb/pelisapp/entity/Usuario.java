package alicanteweb.pelisapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(name = "nivel_critico", nullable = false)
    private Integer nivelCritico = 1;

    @CreationTimestamp
    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private Instant fechaRegistro;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "usuario_seguidores",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "seguido_id")
    )
    private Set<Usuario> seguidos = new HashSet<>();

    @ManyToMany(mappedBy = "seguidos", fetch = FetchType.LAZY)
    private Set<Usuario> seguidores = new HashSet<>();

    // ...nuevo: roles del usuario (USER, CRITIC, EXPERT) almacenados como colección de enums
    @ElementCollection(fetch = FetchType.LAZY, targetClass = Role.class)
    @CollectionTable(name = "usuario_roles", joinColumns = @JoinColumn(name = "usuario_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "role")
    private Set<Role> roles = new HashSet<>();

    // ...nuevo: etiquetas/logros que el usuario puede ganar
    @ElementCollection(fetch = FetchType.LAZY)
    @CollectionTable(name = "usuario_etiquetas", joinColumns = @JoinColumn(name = "usuario_id"))
    @Column(name = "etiqueta")
    private Set<String> etiquetas = new HashSet<>();
}
