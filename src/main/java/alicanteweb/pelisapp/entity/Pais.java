package alicanteweb.pelisapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "pais")
public class Pais {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 100)
    private String nombre;

    @OneToMany(mappedBy = "pais")
    private Set<Actor> actors = new LinkedHashSet<>();

    @OneToMany(mappedBy = "pais")
    private Set<Director> directors = new LinkedHashSet<>();

    @OneToMany(mappedBy = "pais")
    private Set<Pelicula> peliculas = new LinkedHashSet<>();

}