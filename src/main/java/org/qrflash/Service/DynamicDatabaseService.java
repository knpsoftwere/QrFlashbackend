package org.qrflash.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.qrflash.Entity.MenuItemEntity;
import org.springframework.stereotype.Service;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DynamicDatabaseService {
    @PersistenceContext
    private EntityManager entityManager;
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
}
