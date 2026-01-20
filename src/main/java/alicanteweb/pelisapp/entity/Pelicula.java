package alicanteweb.pelisapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "pelicula", indexes = { @Index(name = "idx_pelicula_tmdb_id", columnList = "tmdb_id") })
public class Pelicula {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false)
    private Integer anio;

    private Integer duracion;

    @Lob
    @Column(name = "sinopsis")
    private String sinopsis;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pais_id")
    private Pais pais;

    private LocalDate fechaEstreno;

    // Campos TMDb
    @Column(name = "tmdb_id", unique = true)
    private Integer tmdbId;

    @Column(name = "poster_path", length = 512)
    private String posterPath;

    @Column(name = "backdrop_path", length = 512)
    private String backdropPath;

    @Column(name = "original_language", length = 10)
    private String originalLanguage;

    private Double popularity;

    private LocalDate releaseDate;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "pelicula_categoria",
            joinColumns = @JoinColumn(name = "pelicula_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private Set<Categoria> categorias = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "pelicula_director",
            joinColumns = @JoinColumn(name = "pelicula_id"),
            inverseJoinColumns = @JoinColumn(name = "director_id")
    )
    private Set<Director> directores = new LinkedHashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "pelicula_actor",
            joinColumns = @JoinColumn(name = "pelicula_id"),
            inverseJoinColumns = @JoinColumn(name = "actor_id")
    )
    private Set<Actor> actores = new LinkedHashSet<>();

    @OneToMany(mappedBy = "pelicula", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Resena> resenas = new LinkedHashSet<>();
}
