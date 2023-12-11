package com.example.shopapp.responses;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Data
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class CategoryResponseDTO { //gtri tra ve -> ko can validate nua
    private Long id;
    private String name;
}
