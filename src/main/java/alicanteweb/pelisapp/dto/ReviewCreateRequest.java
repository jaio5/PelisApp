package alicanteweb.pelisapp.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ReviewCreateRequest {
    @NotNull
    private Long userId;

    @NotNull
    private Long movieId;

    @NotBlank
    private String text;

    @Min(1)
    @Max(5)
    private int stars;

}
