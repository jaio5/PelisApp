package alicanteweb.pelisapp.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CrewDTO {
    private Long tmdbId;
    private String name;
    private String job;
    private String department;
    private String profilePath;
    private String profileLocalPath;
    private String profileUrl; // URL completa para mostrar la imagen
}
