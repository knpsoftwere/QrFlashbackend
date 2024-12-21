package org.qrflash.DTO.Admin.MenuDTO;

import lombok.Data;
import org.qrflash.DTO.Admin.CategoryDTO;
import org.qrflash.DTO.Admin.TagsDTO;

import java.util.List;

@Data
public class MenuItemDTO {
    private Long id;
    private String photo;
    private String name;
    private String description;
    private String unit;
    private String item_type;
    private Double price;
    private Boolean isActive;
    private Boolean isPinned;
    private Boolean isQuantity;
    private CategoryDTO category;
    private List<TagsDTO> tags;
}
