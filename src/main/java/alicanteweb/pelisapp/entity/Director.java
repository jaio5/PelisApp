package alicanteweb.pelisapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "director", indexes = {
    @Index(columnList = "tmdbId"),
    @Index(columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
public class Director {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private Long tmdbId;

    @Column(nullable = false)
    private String name;

    private String profilePath; // URL de TMDB

    @Column(name = "profile_local_path")
    private String profileLocalPath; // Ruta local de la imagen descargada

    // Información adicional del director
    private String biography; // Biografía del director
    private String birthday; // Fecha de nacimiento
    private String placeOfBirth; // Lugar de nacimiento

    @ManyToMany(mappedBy = "directors", fetch = FetchType.LAZY)
    private Set<Movie> movies = new HashSet<>();
}
