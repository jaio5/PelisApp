package alicanteweb.pelisapp.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Getter
@Setter
public class TmdbMovieShort {
    private Integer id;
    private String title;
    @JsonProperty("release_date")
    private String releaseDate;
    @JsonProperty("poster_path")
    private String posterPath;
}
