package org.qrflash.Service.Admin;

import lombok.RequiredArgsConstructor;
import org.qrflash.DTO.Admin.CategoryDTO;
import org.qrflash.DTO.Admin.MenuDTO.MenuItemCreateDTO;
import org.qrflash.DTO.Admin.MenuDTO.MenuItemDTO;
import org.qrflash.DTO.Admin.MenuDTO.MenuItemUpdateDTO;
import org.qrflash.DTO.Admin.TagsDTO;
import org.qrflash.Service.DataBase.DynamicDatabaseService;
import org.qrflash.Service.DataBase.ImageService;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.*;

@Service
@RequiredArgsConstructor
public class MenuItemsService {
    private final DynamicDatabaseService dynamicDatabaseService;
    private final ImageService imageService;

    public Long addMenuItem(String databaseName, MenuItemCreateDTO menuItemCreateDTO) {
        if (isMenuItemNameExists(databaseName, menuItemCreateDTO.getName())) {
            throw new RuntimeException("Товар з такою назвою вже існує");
        }

        String query = """
        INSERT INTO menu_items (name, description, unit, item_type, price, is_active, is_pinned, is_quantity, photo, category_id)
        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id
    """;

        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, menuItemCreateDTO.getName());
            ps.setString(2, menuItemCreateDTO.getDescription());
            ps.setString(3, menuItemCreateDTO.getUnit());
            ps.setString(4, menuItemCreateDTO.getItem_type());
            ps.setDouble(5, menuItemCreateDTO.getPrice());
            ps.setBoolean(6, menuItemCreateDTO.getIsActive());
            ps.setBoolean(7, menuItemCreateDTO.getIsPinned());
            ps.setBoolean(8, menuItemCreateDTO.getIsQuantity());
            ps.setString(9, menuItemCreateDTO.getPhoto());
            if (menuItemCreateDTO.getCategoryId() != null) {
                ps.setLong(10, menuItemCreateDTO.getCategoryId());
            } else {
                ps.setNull(10, Types.BIGINT);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка створення товару", e);
        }

        throw new RuntimeException("Не вдалося створити товар");
    }

    public boolean isMenuItemNameExists(String databaseName, String name) {
        String query = "SELECT COUNT(*) FROM menu_items WHERE name = ?";
        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка при перевірці дублювання назви товару", e);
        }
        return false;
    }



    private Long addMenuItem(String databaseName, MenuItemDTO menuItemDTO) {
        String query = "INSERT INTO menu_items (name, description, unit, item_type, price, is_active, is_pinned, is_quantity, photo, category_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?) RETURNING id";

        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setString(1, menuItemDTO.getName());
            ps.setString(2, menuItemDTO.getDescription());
            ps.setString(3, menuItemDTO.getUnit());
            ps.setString(4, menuItemDTO.getItem_type());
            ps.setDouble(5, menuItemDTO.getPrice());
            ps.setBoolean(6, menuItemDTO.getIsActive());
            ps.setBoolean(7, menuItemDTO.getIsPinned());
            ps.setBoolean(8, menuItemDTO.getIsQuantity());
            ps.setString(9, menuItemDTO.getPhoto());
            ps.setObject(10, menuItemDTO.getCategory() != null ? menuItemDTO.getCategory().getId() : null);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Помилка створення товару", e);
        }

        throw new RuntimeException("Не вдалося створити товар.");
    }

    public void addTagsToMenuItem(Connection connection, Long menuItemId, List<Long> tagIds) {
        String query = "INSERT INTO menu_item_tags (menu_item_id, tag_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            for (Long tagId : tagIds) {
                ps.setLong(1, menuItemId);
                ps.setLong(2, tagId);
                ps.addBatch();
            }
            ps.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Помилка додавання тегів до товару", e);
        }
    }


    public void deleteMenuItem(String databaseName, Long menuId) {
        String query = "DELETE FROM menu_items WHERE id = ?";

        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setLong(1, menuId);
            int rowsDeleted = ps.executeUpdate();

            if (rowsDeleted == 0) {
                throw new RuntimeException("Меню з ID " + menuId + " не знайдено.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка видалення меню у базі: " + databaseName, e);
        }
    }


    public void updateMenuItem(String databaseName, Long menuItemId, MenuItemUpdateDTO menuItemUpdateDTO) {
        StringBuilder queryBuilder = new StringBuilder("UPDATE menu_items SET ");
        List<String> updates = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        if (menuItemUpdateDTO.getName() != null) {
            updates.add("name = ?");
            parameters.add(menuItemUpdateDTO.getName());
        }
        if (menuItemUpdateDTO.getDescription() != null) {
            updates.add("description = ?");
            parameters.add(menuItemUpdateDTO.getDescription());
        }
        if (menuItemUpdateDTO.getUnit() != null) {
            updates.add("unit = ?");
            parameters.add(menuItemUpdateDTO.getUnit());
        }
        if (menuItemUpdateDTO.getItem_type() != null) {
            updates.add("item_type = ?");
            parameters.add(menuItemUpdateDTO.getItem_type());
        }
        if (menuItemUpdateDTO.getPrice() != null) {
            updates.add("price = ?");
            parameters.add(menuItemUpdateDTO.getPrice());
        }
        if (menuItemUpdateDTO.getIsActive() != null) {
            updates.add("is_active = ?");
            parameters.add(menuItemUpdateDTO.getIsActive());
        }
        if (menuItemUpdateDTO.getIsPinned() != null) {
            updates.add("is_pinned = ?");
            parameters.add(menuItemUpdateDTO.getIsPinned());
        }
        if (menuItemUpdateDTO.getIsQuantity() != null) {
            updates.add("is_quantity = ?");
            parameters.add(menuItemUpdateDTO.getIsQuantity());
        }
        if (menuItemUpdateDTO.getPhoto() != null) {
            updates.add("photo = ?");
            parameters.add(menuItemUpdateDTO.getPhoto());
        }
        if (menuItemUpdateDTO.getCategoryId() != null) {
            updates.add("category_id = ?");
            parameters.add(menuItemUpdateDTO.getCategoryId());
        }

        // Перевіряємо, чи передано хоча б одне поле для оновлення
        if (!updates.isEmpty()) {
            queryBuilder.append(String.join(", ", updates)).append(" WHERE id = ?");
            parameters.add(menuItemId);

            try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
                 PreparedStatement ps = connection.prepareStatement(queryBuilder.toString())) {

                for (int i = 0; i < parameters.size(); i++) {
                    ps.setObject(i + 1, parameters.get(i));
                }

                int rowsUpdated = ps.executeUpdate();
                if (rowsUpdated == 0) {
                    throw new RuntimeException("Menu item with ID " + menuItemId + " not found.");
                }

                System.out.println("Menu item updated successfully in database: " + databaseName);
            } catch (SQLException e) {
                throw new RuntimeException("Error updating menu item in database: " + databaseName, e);
            }
        } else {
            throw new IllegalArgumentException("No fields provided to update.");
        }
    }



    //=============================================================================================================
    public void addTagToMenuItem(String databaseName, Long menuItemId, Long tagId) {
        String query = """
            INSERT INTO menu_item_tags (menu_item_id, tag_id)
            VALUES (?, ?)
            ON CONFLICT DO NOTHING;
        """;

        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, menuItemId);
            preparedStatement.setLong(2, tagId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to add tag to menu item in database: " + databaseName, e);
        }
    }

    public void removeTagFromMenuItem(String databaseName, Long menuItemId, Long tagId) {
        String query = """
        DELETE FROM menu_item_tags
        WHERE menu_item_id = ? AND tag_id = ?;
    """;

        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, menuItemId);
            preparedStatement.setLong(2, tagId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove tag from menu item in database: " + databaseName, e);
        }
    }

    public List<TagsDTO> getTagsForMenuItem(String databaseName, Long menuItemId) {
        String query = """
        SELECT t.id, t.name, t.description, t.emoji
        FROM tags t
        INNER JOIN menu_item_tags mit ON t.id = mit.tag_id
        WHERE mit.menu_item_id = ?;
    """;

        List<TagsDTO> tags = new ArrayList<>();
        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, menuItemId);
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                TagsDTO tag = new TagsDTO();
                tag.setId(resultSet.getLong("id"));
                tag.setName(resultSet.getString("name"));
                tag.setDescription(resultSet.getString("description"));
                tag.setEmoji(resultSet.getString("emoji"));
                tags.add(tag);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch tags for menu item in database: " + databaseName, e);
        }

        return tags;
    }

    public void removeAllTagsFromMenuItem(String databaseName, Long menuItemId) {
        String query = """
        DELETE FROM menu_item_tags
        WHERE menu_item_id = ?;
    """;

        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {

            preparedStatement.setLong(1, menuItemId);
            preparedStatement.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove all tags from menu item in database: " + databaseName, e);
        }
    }
    public List<MenuItemDTO> getMenuItems(String databaseName) throws SQLException {
        String query = """
        SELECT mi.id AS menu_item_id, mi.name, mi.description, mi.unit, mi.item_type, mi.price, mi.is_active, mi.is_pinned, mi.is_quantity,
               mi.photo, c.id AS category_id, c.name AS category_name, c.description AS category_description, c.image_url AS category_image_url,
               t.id AS tag_id, t.name AS tag_name, t.description AS tag_description, t.emoji AS tag_emoji
        FROM menu_items mi
        LEFT JOIN categories c ON mi.category_id = c.id
        LEFT JOIN menu_item_tags mit ON mi.id = mit.menu_item_id
        LEFT JOIN tags t ON mit.tag_id = t.id
    """;

        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            Map<Long, MenuItemDTO> menuItemsMap = new HashMap<>();

            while (resultSet.next()) {
                Long menuItemId = resultSet.getLong("menu_item_id");
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
                        //dto.setPhoto(resultSet.getString("photo"));
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

                        dto.setTags(new ArrayList<>());
                        return dto;
                    } catch (SQLException e) {
                        throw new RuntimeException("Error while mapping result set to DTO", e);
                    }
                });

                // Додаємо тег
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

    public boolean areTagsValid(String databaseName, List<Long> tagIds) {
        String query = "SELECT COUNT(*) FROM tags WHERE id = ANY (?)";
        try (Connection connection = dynamicDatabaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(query)) {

            Array array = connection.createArrayOf("BIGINT", tagIds.toArray());
            ps.setArray(1, array);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count == tagIds.size(); // Всі теги повинні існувати
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Помилка перевірки тегів у базі", e);
        }

        return false;
    }

}



