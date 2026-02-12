package alicanteweb.pelisapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CastDTO {
    private Long tmdbId;
    private String name;
    private String character;
    private String profilePath;
    private String profileLocalPath;
    private String profileUrl;
}
