package alicanteweb.pelisapp.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NullMarked;

@NullMarked
@Getter
@Setter
public class TmdbCrew {
    private Integer id;
    private String name;
    private String job;
    private String department;

    @JsonProperty("profile_path")
    private String profilePath;
}
