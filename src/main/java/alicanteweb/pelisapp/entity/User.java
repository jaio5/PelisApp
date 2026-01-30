package alicanteweb.pelisapp.entity;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuario", indexes = {
        @Index(columnList = "username"),
        @Index(columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    @EqualsAndHashCode.Include
    @ToString.Include
    private String username;

    @Column(unique = true, length = 200)
    private String email;

    @Column(nullable = false)
    private String password;

    private String displayName;

    private Instant registeredAt;

    private Integer criticLevel = 0; // computed based on likes per review

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "usuario_roles",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "usuario_etiquetas",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "etiqueta_id"))
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Review> reviews = new HashSet<>();

    @OneToMany(mappedBy = "follower", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Following> following = new HashSet<>();

    @OneToMany(mappedBy = "followed", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<Following> followers = new HashSet<>();

    // Replaced ManyToMany archievements with explicit entity to hold metadata
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private Set<UsuarioArchivement> usuarioArchievements = new HashSet<>();

    // Defensive getters for collections to avoid exposing internal mutable sets
    public Set<Role> getRoles() {
        return Collections.unmodifiableSet(roles == null ? Collections.emptySet() : new HashSet<>(roles));
    }

    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags == null ? Collections.emptySet() : new HashSet<>(tags));
    }

    // métodos helper para manipular las colecciones de forma segura
    public boolean addTag(Tag tag) {
        if (this.tags == null) this.tags = new HashSet<>();
        return this.tags.add(tag);
    }

    public boolean removeTag(Tag tag) {
        if (this.tags == null) return false;
        return this.tags.remove(tag);
    }

    public boolean addRole(Role role) {
        if (this.roles == null) this.roles = new HashSet<>();
        return this.roles.add(role);
    }

    public boolean removeRole(Role role) {
        if (this.roles == null) return false;
        return this.roles.remove(role);
    }

    public Set<Review> getReviews() {
        return Collections.unmodifiableSet(reviews == null ? Collections.emptySet() : new HashSet<>(reviews));
    }

    public Set<Following> getFollowing() {
        return Collections.unmodifiableSet(following == null ? Collections.emptySet() : new HashSet<>(following));
    }

    public Set<Following> getFollowers() {
        return Collections.unmodifiableSet(followers == null ? Collections.emptySet() : new HashSet<>(followers));
    }

    public Set<UsuarioArchivement> getUsuarioArchievements() {
        return Collections.unmodifiableSet(usuarioArchievements == null ? Collections.emptySet() : new HashSet<>(usuarioArchievements));
    }

}
