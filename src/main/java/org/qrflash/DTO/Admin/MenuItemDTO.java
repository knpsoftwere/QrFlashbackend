package org.qrflash.DTO.Admin;

import lombok.Data;

@Data
public class MenuItemDTO {
    private Long id;
    private String name;
    private String description;
    private double price;
    private String category;
    private boolean isAvailable;
    private Long categoryId;
}
