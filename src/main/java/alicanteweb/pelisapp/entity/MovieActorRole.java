package alicanteweb.pelisapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad de relación entre Movie y Actor que almacena información específica
 * de la participación del actor en la película (personaje, orden en créditos, etc.)
 */
@Entity
@Table(name = "movie_actor_role",
       indexes = {
           @Index(columnList = "movie_id"),
           @Index(columnList = "actor_id"),
           @Index(columnList = "cast_order")
       })
@Getter
@Setter
@NoArgsConstructor
public class MovieActorRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    private Actor actor;

    @Column(name = "character_name", length = 500)
    private String characterName; // Nombre del personaje interpretado

    @Column(name = "cast_order")
    private Integer castOrder; // Orden en los créditos (0 = protagonista principal)

    @Column(name = "credit_id", length = 100)
    private String creditId; // ID único del crédito en TMDB

    // Constructor de conveniencia
    public MovieActorRole(Movie movie, Actor actor, String characterName, Integer castOrder, String creditId) {
        this.movie = movie;
        this.actor = actor;
        this.characterName = characterName;
        this.castOrder = castOrder;
        this.creditId = creditId;
    }
}
