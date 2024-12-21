package org.qrflash.DTO.Admin;

import lombok.Data;

@Data
public class TagsDTO {
    private Long id;
    private String name;
    private String description;
    private String emoji;
}
