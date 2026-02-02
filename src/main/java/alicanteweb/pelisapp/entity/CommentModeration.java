package alicanteweb.pelisapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "comment_moderation", indexes = {
    @Index(columnList = "status"),
    @Index(columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
public class CommentModeration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ModerationStatus status = ModerationStatus.PENDING;

    @Column(name = "toxicity_score")
    private Double toxicityScore;

    @Column(length = 1000)
    private String moderationReason;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "ai_processed")
    private Boolean aiProcessed = false;

    public enum ModerationStatus {
        PENDING,      // Esperando moderación
        APPROVED,     // Aprobado (por IA o humano)
        REJECTED,     // Rechazado (por IA o humano)
        MANUAL_REVIEW // Requiere revisión manual
    }
}
