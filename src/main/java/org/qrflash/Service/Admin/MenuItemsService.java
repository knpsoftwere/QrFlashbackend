package org.qrflash.Service.Admin;

import lombok.RequiredArgsConstructor;
import org.qrflash.DTO.Admin.MenuItemDTO;
import org.qrflash.Entity.CategoryEntity;
import org.qrflash.Entity.MenuItemEntity;
import org.qrflash.Entity.UserEntity;
import org.qrflash.JWT.JwtUtil;
import org.qrflash.Repository.CategoryRepository;
import org.qrflash.Repository.EstablishmentsRepository;
import org.qrflash.Repository.MenuItemRepository;
import org.qrflash.Repository.UserRepository;
import org.qrflash.Service.DataBase.DynamicDatabaseService;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuItemsService {
    private final JwtUtil jwtUtil;
    private final EstablishmentsRepository establishmentsRepository;
    private final UserRepository usersRepository; // Репозиторій для доступу до таблиці `users`
    private final DynamicDatabaseService dynamicDatabaseService;
    private final MenuItemRepository menuItemRepository;
    private final CategoryRepository categoryRepository;
//    public List<MenuItemEntity> getMenuItems(UUID establishmentId, String token) {
//        System.out.println("==============================================");
//        System.out.println("Token: " + token);
//
//        // Перевіряємо токен
//        if (!jwtUtil.isValidToken(token)) {
//            throw new RuntimeException("Не валідний або протермінований токен");
//        }
//
//        // Отримуємо номер телефону з токену
//        String phoneNumber = jwtUtil.getUserPhoneNumber(token);
//        System.out.println("Phone from token: " + phoneNumber);
//
//        // Витягуємо ID користувача, використовуючи phoneNumber
//        Long userId = usersRepository.findByPhoneNumber(phoneNumber)
//                .map(UserEntity::getId)
//                .orElseThrow(() -> new RuntimeException("Користувач не знайдений за номером телефону"));
//        System.out.println("User ID from phone: " + userId);
//
//        // (Без перевірки доступу на даний момент)
//
//        // Перемикаємося на базу даних закладу
//        String databaseName = "est_" + establishmentId.toString().replace("-", "_");
//
//        // Лог для перевірки правильності назви бази даних
//        System.out.println("Switching to database: " + databaseName);
//
//        // Повертаємо список меню
//        List<MenuItemEntity> menuItems = dynamicDatabaseService.getMenuItemsWithCategories(databaseName);
//
//        // Лог для перевірки отриманого меню
//        System.out.println("Menu items retrieved: " + menuItems);
//
//        return menuItems;
//    }
    public List<MenuItemEntity> getMenuItemsWithCategories(String databaseName) {
        System.out.println("Fetching menu items with categories for database: " + databaseName);
        String query = "SELECT mi.*, c.id as category_id, c.name as category_name, c.description as category_description " +
                "FROM menu_items mi " +
                "LEFT JOIN categories c ON mi.category_id = c.id";

        try (Connection connection = dynamicDatabaseService.getConnection(databaseName)) {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(query);

            List<MenuItemEntity> menuItems = new ArrayList<>();

            while (resultSet.next()) {
                MenuItemEntity menuItem = new MenuItemEntity();
                menuItem.setId(resultSet.getLong("id"));
                menuItem.setName(resultSet.getString("name"));
                menuItem.setDescription(resultSet.getString("description"));
                menuItem.setActive(resultSet.getBoolean("is_active"));
                menuItem.setUnit(resultSet.getString("unit"));
                menuItem.setItemType(resultSet.getString("item_type"));
                //menuItem.setPinned(resultSet.getBoolean("is_pinned"));
                menuItem.setPrice(resultSet.getDouble("price"));

                // Заповнюємо категорію
                CategoryEntity category = new CategoryEntity();
                category.setId(resultSet.getLong("category_id"));
                category.setName(resultSet.getString("category_name"));

                menuItem.setCategory(category); // Прив'язуємо категорію до товару
                menuItems.add(menuItem);
            }

            return menuItems;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch menu items with categories from database: " + databaseName, e);
        }
    }

//    public void createMenuItem(UUID establishmentId, MenuItemEntity menuItemEntity) {
//        System.out.println("est_" + establishmentId + " Отримали з запиту! =============");
//        String databaseName = "est_" + establishmentId.toString().replace("-", "_");
//        System.out.println("Using database: " + databaseName);
//
//        dynamicDatabaseService.saveMenuItem(databaseName, menuItemEntity);
//        System.out.println("Menu item added to table in database: " + databaseName);
//    }

    public MenuItemEntity createMenuItem(MenuItemDTO request) {
        CategoryEntity category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Категорія з ID " + request.getCategoryId() + " не знайдена"));

        MenuItemEntity menuItem = new MenuItemEntity();
        menuItem.setName(request.getName());
        menuItem.setDescription(request.getDescription());
        //menuItem.setUnit(request.getUnit());
        //menuItem.setItemType(request.getItemType());
        menuItem.setPrice(request.getPrice());
        menuItem.setCategory(category); // Прив'язка до категорії

        return menuItemRepository.save(menuItem);
    }

    public void deleteMenuItem(UUID establishmentId, Long itemId, String token) {
        String databaseName = "est_" + establishmentId.toString().replace("-", "_");
        dynamicDatabaseService.deleteMenuItems(databaseName, itemId);
    }

    public void updateMenuItem(UUID establishmentId, MenuItemEntity menuItemEntity, String token) {
        // Формуємо назву бази даних
        String databaseName = "est_" + establishmentId.toString().replace("-", "_");
        System.out.println("Using database: " + databaseName);

        // Викликаємо метод у DynamicDatabaseService для оновлення
        dynamicDatabaseService.updateMenuItem(databaseName, menuItemEntity);
        System.out.println("Menu item updated in database: " + databaseName);
    }

    public void addTagsToMenuItem(String databaseName, Long menuItemId, List<Long> tagIds) {
        try (Connection connection = dynamicDatabaseService.getConnection(databaseName)) {
            // Додаємо кожен тег у проміжну таблицю
            String query = "INSERT INTO menu_item_tags (menu_item_id, tag_id) VALUES (?, ?) ON CONFLICT DO NOTHING";
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                for (Long tagId : tagIds) {
                    ps.setLong(1, menuItemId);
                    ps.setLong(2, tagId);
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка додавання тегів до товару", e);
        }
    }


}
