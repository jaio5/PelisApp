package alicanteweb.pelisapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "etiqueta", uniqueConstraints = @UniqueConstraint(columnNames = {"clave"}))
public class Etiqueta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "clave", nullable = false, length = 100)
    private String clave; // identificador único, p.ej. 'critic_recommended'

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "descripcion", length = 1000)
    private String descripcion;

    @Column(name = "icono_url", length = 512)
    private String iconoUrl;
}

