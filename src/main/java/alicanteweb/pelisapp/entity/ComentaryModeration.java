package alicanteweb.pelisapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "comentary_moderation")
@Getter
@Setter
@NoArgsConstructor
public class ComentaryModeration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false, unique = true)
    private Review review;

    private Double aiScore; // 0.0 - 1.0
    private String aiDecision; // APPROVE, FLAG, REJECT
    private String aiReason;

    private boolean humanReviewed;
    private String humanDecision;
    private String humanNotes;

    private Instant reviewedAt;
}
