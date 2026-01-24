package it.subito.orders.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "lo username è obbligatorio per la registrazione")
    private String username;

    @NotBlank(message = "la password è obbligatoria per la registrazione")
    private String password;

    @NotBlank(message = "il role è obbligatorio in fase di registrazione")
    private String role;
}
