package alicanteweb.pelisapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private Long id;
    private String username;
    private String displayName;
    private Integer criticLevel;

    public UserDTO() {}

    public UserDTO(Long id, String username, String displayName, Integer criticLevel) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.criticLevel = criticLevel;
    }

}
