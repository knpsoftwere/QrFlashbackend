package org.qrflash.DTO;

import lombok.Getter;

@Getter
public enum CompanyLanguage{
    UKRAINIAN("uk", "UA"),
    ENGLISH("en", "US"),
    SWEDISH("sv", "SE");

    private final String code;
    private final String name;

    CompanyLanguage(String code, String name){
        this.code = code;
        this.name = name;
    }
}
