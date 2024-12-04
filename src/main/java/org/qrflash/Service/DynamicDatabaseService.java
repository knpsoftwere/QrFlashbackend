package org.qrflash.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.qrflash.Entity.MenuItemEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DynamicDatabaseService {
    @PersistenceContext
    private EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;
    private static final String DB_URL_TEMPLATE = "jdbc:postgresql://138.201.118.129:5432/%s";
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "R3cv77m6F3Ys6MfV";

    private Connection getConnection(String databaseName) throws SQLException {
        String dbUrl = String.format(DB_URL_TEMPLATE, databaseName);
        return DriverManager.getConnection(dbUrl, DB_USERNAME, DB_PASSWORD);
    }

    public List<MenuItemEntity> getMenuItems(String databaseName) {
        System.out.println("Fetching menu items for database: " + databaseName);
        String query = "SELECT * FROM menu_items";

        try (Connection connection = getConnection(databaseName);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            List<MenuItemEntity> menuItems = new ArrayList<>();
            while (resultSet.next()) {
                MenuItemEntity menuItem = new MenuItemEntity();
                menuItem.setId(resultSet.getLong("id"));
                menuItem.setName(resultSet.getString("name"));
                menuItem.setDescription(resultSet.getString("description"));
                menuItem.setCategory(resultSet.getString("category"));
                menuItem.setActive(resultSet.getBoolean("is_active"));
                menuItem.setUnit(resultSet.getString("unit"));
                menuItem.setItemType(resultSet.getString("item_type"));
                menuItem.setPinned(resultSet.getBoolean("is_pinned"));
                menuItem.setPrice(resultSet.getDouble("price"));
                menuItems.add(menuItem);
            }
            return menuItems;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch menu items from database: " + databaseName, e);
        }
    }
    public void saveMenuItem(String databaseName, MenuItemEntity menuItemEntity) {
        String sql = "INSERT INTO menu_items " +
                "(name, description, category, is_active, unit, item_type, is_pinned, price) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection(databaseName);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, menuItemEntity.getName());
            preparedStatement.setString(2, menuItemEntity.getDescription());
            preparedStatement.setString(3, menuItemEntity.getCategory());
            preparedStatement.setBoolean(4, menuItemEntity.isActive());
            preparedStatement.setString(5, menuItemEntity.getUnit());
            preparedStatement.setString(6, menuItemEntity.getItemType());
            preparedStatement.setBoolean(7, menuItemEntity.isPinned());
            preparedStatement.setDouble(8, menuItemEntity.getPrice());

            preparedStatement.executeUpdate();
            System.out.println("Menu item saved to database: " + databaseName);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save menu item to database: " + databaseName, e);
        }
    }

    public void deleteMenuItems(String databaseName, Long itemId) {
        // SQL-запит без вказання імені бази
        String sql = "DELETE FROM menu_items WHERE id = ?";

        try (Connection connection = getConnection(databaseName);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            // Встановлюємо параметр ID
            preparedStatement.setLong(1, itemId);

            // Виконуємо запит
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Deleted " + rowsAffected + " row(s) with ID " + itemId + " from database: " + databaseName);

        } catch (SQLException e) {
            // Логування та обробка помилки
            throw new RuntimeException("Failed to delete menu item from database: " + databaseName, e);
        }
    }


}
