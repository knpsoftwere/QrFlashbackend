package org.qrflash.DTO;

import lombok.Data;

@Data
public class UserRegistrationRequest {
    private String phoneNumber;
    private String password;
}
