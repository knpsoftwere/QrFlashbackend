package org.qrflash.DTO.Admin.MenuDTO;

import lombok.Data;

import java.util.List;

@Data
public class MenuItemCreateDTO {
    private String name;
    private String description;
    private String unit;
    private String item_type;
    private Double price;
    private Boolean isActive;
    private Boolean isPinned;
    private Boolean isQuantity;
    private String photo;
    private Long categoryId; // ID категорії (опціонально)
}
