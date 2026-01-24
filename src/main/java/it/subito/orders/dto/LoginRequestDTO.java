package it.subito.orders.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {

    @NotBlank(message = "lo username è obbligatorio per il login")
    private String username;

    @NotBlank(message = "la password è obbligatoria per il login")
    private String password;
}
