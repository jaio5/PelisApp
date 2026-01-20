// src/main/java/alicanteweb/pelisapp/entity/ValoracionResena.java
package alicanteweb.pelisapp.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "valoracion_resena",
        uniqueConstraints = @UniqueConstraint(columnNames = { "resena_id", "valorador_id" }))
public class ValoracionResena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "resena_id", nullable = false)
    private Resena resena;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "valorador_id", nullable = false)
    private Usuario valorador;

    @Min(1)
    @Max(5)
    @Column(name = "puntuacion", nullable = false)
    private Integer puntuacion; // 1..5

    @Column(columnDefinition = "text")
    private String comentario;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant fecha;
}