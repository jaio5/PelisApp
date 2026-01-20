package alicanteweb.pelisapp.tmdb;

import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.NullMarked;

import java.util.List;

@NullMarked
@Getter
@Setter
public class TmdbCredits {
    private List<TmdbCast> cast;
    private List<TmdbCrew> crew;
}
