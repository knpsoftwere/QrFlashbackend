package org.qrflash.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@Data
public class MenuItemDTO {
    private String photo;
    @NotBlank(message = "Назва не може бути порожньою")
    private String name;
    private String description;
    @NotBlank(message = "Категорія не може бути порожня")
    private String category;
    @NotBlank(message = "Одиниця виміру не може бути порожньою")
    private String until;
    @NotBlank(message = "Тип товару не може бути порожнім")
    private String itemType;
    private List<String> tags; // Теги товару
    private Boolean isActive = true; // Виставити на продаж
    private Boolean isPinned = false; // Закріпити на головному екрані
}
