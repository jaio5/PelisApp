package alicanteweb.pelisapp.tmdb;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TmdbCast {
    @JsonProperty("cast_id")
    private Integer castId;

    private String character;

    @JsonProperty("credit_id")
    private String creditId;

    private Integer gender;

    private Integer id;

    private String name;

    @JsonProperty("profile_path")
    private String profilePath;
}