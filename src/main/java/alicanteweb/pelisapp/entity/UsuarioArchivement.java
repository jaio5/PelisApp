package alicanteweb.pelisapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "usuario_archivement", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"usuario_id", "archivement_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class UsuarioArchivement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "archivement_id", nullable = false)
    private Archivement archivement;

    private Instant awardedAt;

    private boolean pinnedToProfile;
}
