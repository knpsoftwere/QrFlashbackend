package org.qrflash.DTO;

import lombok.Data;

@Data
public class JwtRequest {
    private String phoneNumber;
    private String password;
}
