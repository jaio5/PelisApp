package alicanteweb.pelisapp.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class LoginResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long expiresAt;
    private String refreshToken;

    public LoginResponse() {}

    public LoginResponse(String accessToken, Long expiresAt, String refreshToken) {
        this.accessToken = accessToken;
        this.expiresAt = expiresAt;
        this.refreshToken = refreshToken;
    }

}
