package org.qrflash.DTO.Admin;

import lombok.Data;

@Data
public class CategoryDTO {
    private Long id;
    private String name;
    private String description;
    private String image_url;
}
