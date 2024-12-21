package org.qrflash.DTO.Admin.MenuDTO;

import lombok.Data;

@Data
public class MenuItemUpdateDTO {
    private String name;
    private String description;
    private String unit;
    private String item_type;
    private Double price;
    private Boolean isActive;
    private Boolean isPinned;
    private Boolean isQuantity;
    private String photo;
    private Long categoryId;
}
