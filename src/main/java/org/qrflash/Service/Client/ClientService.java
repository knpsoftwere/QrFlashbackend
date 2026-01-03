package org.qrflash.Service.Client;

import lombok.RequiredArgsConstructor;
import org.qrflash.DTO.Admin.CategoryDTO;
import org.qrflash.DTO.Admin.MenuDTO.MenuItemDTO;
import org.qrflash.DTO.Admin.TagsDTO;
import org.qrflash.Service.DataBase.DynamicDatabaseService;
import org.qrflash.Service.DataBase.ImageService;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final DynamicDatabaseService dynamicDatabaseService;
    private final ImageService imageService;

    public List<MenuItemDTO> getActiveMenuItems(String databaseName) throws SQLException {
        String query = """
        SELECT mi.id AS menu_item_id,
               mi.name,
               mi.description,
               mi.unit,
               mi.item_type,
               mi.price,
               mi.is_active,
               mi.is_pinned,
               mi.is_quantity,
               mi.photo,
               c.id AS category_id,
               c.name AS category_name,
               c.description AS category_description,
               c.image_url AS category_image_url,
               t.id AS tag_id,
               t.name AS tag_name,
               t.description AS tag_description,
               t.emoji AS tag_emoji
        FROM menu_items mi
            LEFT JOIN categories c ON mi.category_id = c.id
            LEFT JOIN menu_item_tags mit ON mi.id = mit.menu_item_id
            LEFT JOIN tags t ON mit.tag_id = t.id
        WHERE mi.is_active = TRUE
    """;

        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            Map<Long, MenuItemDTO> menuItemsMap = new HashMap<>();

            while (resultSet.next()) {
                Long menuItemId = resultSet.getLong("menu_item_id");
                // Якщо меню-ітема з таким id в колекції ще немає – створюємо
                MenuItemDTO menuItem = menuItemsMap.computeIfAbsent(menuItemId, id -> {
                    try {
                        MenuItemDTO dto = new MenuItemDTO();
                        dto.setId(id);
                        dto.setName(resultSet.getString("name"));
                        dto.setDescription(resultSet.getString("description"));
                        dto.setUnit(resultSet.getString("unit"));
                        dto.setItem_type(resultSet.getString("item_type"));
                        dto.setPrice(resultSet.getDouble("price"));
                        dto.setIsActive(resultSet.getBoolean("is_active"));
                        dto.setIsPinned(resultSet.getBoolean("is_pinned"));
                        dto.setIsQuantity(resultSet.getBoolean("is_quantity"));
                        dto.setPhoto(imageService.generatePresignedUrl(resultSet.getString("photo")));

                        // Додаємо категорію
                        if (resultSet.getObject("category_id") != null) {
                            CategoryDTO category = new CategoryDTO();
                            category.setId(resultSet.getLong("category_id"));
                            category.setName(resultSet.getString("category_name"));
                            category.setDescription(resultSet.getString("category_description"));
                            category.setImage_url(resultSet.getString("category_image_url"));
                            dto.setCategory(category);
                        }

                        // Готуємо список тегів
                        dto.setTags(new ArrayList<>());
                        return dto;
                    } catch (SQLException e) {
                        throw new RuntimeException("Error while mapping result set to DTO", e);
                    }
                });

                // Додаємо тег (якщо він є)
                Long tagId = resultSet.getLong("tag_id");
                if (tagId != 0) {
                    TagsDTO tag = new TagsDTO();
                    tag.setId(tagId);
                    tag.setName(resultSet.getString("tag_name"));
                    tag.setDescription(resultSet.getString("tag_description"));
                    tag.setEmoji(resultSet.getString("tag_emoji"));
                    menuItem.getTags().add(tag);
                }
            }

            // Сортуємо теги у кожному товарі
            menuItemsMap.values().forEach(menuItem ->
                    menuItem.getTags().sort(Comparator.comparingLong(TagsDTO::getId))
            );

            return new ArrayList<>(menuItemsMap.values());
        }
    }

}
