package alicanteweb.pelisapp.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
@Getter
@Setter
public class TmdbMovieDetail {
    private Integer id;
    private String title;
    @JsonProperty("original_title")
    private String originalTitle;
    @JsonProperty("release_date")
    private String releaseDate;
    @JsonProperty("poster_path")
    private String posterPath;
    @JsonProperty("backdrop_path")
    private String backdropPath;
    @JsonProperty("original_language")
    private String originalLanguage;
    private Double popularity;

    private List<TmdbGenre> genres;
}
