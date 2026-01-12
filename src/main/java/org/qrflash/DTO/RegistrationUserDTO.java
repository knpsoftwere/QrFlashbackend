package org.qrflash.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class RegistrationUserDTO {
    private String phoneNumber;
    private String password;
    @NotNull(message = "Language is required")
    private CompanyLanguage language;
}
