package alicanteweb.pelisapp.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "comments")
public class Comment {
    // getters / setters
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Setter
    @Column(name = "movie_id")
    private Long movieId;
    @Setter
    private String author;
    @Setter
    @Column(columnDefinition = "TEXT")
    private String text;
    @Setter
    private Integer rating; // 1..5

}
