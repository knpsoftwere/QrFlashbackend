package org.qrflash.Service.DataBase;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import org.qrflash.DTO.TableItemDTO;
import org.qrflash.Entity.CategoryEntity;
import org.qrflash.Entity.MenuItemEntity;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DynamicDatabaseService {
    @PersistenceContext
    private EntityManager entityManager;
    private final JdbcTemplate jdbcTemplate;

    @Value("${custom.datasource.urlConn}")
    private String DB_URL_TEMPLATE;
    @Value("${spring.datasource.username}")
    private String DB_USERNAME;
    @Value("${spring.datasource.password}")
    private String DB_PASSWORD;

    public Connection getConnection(String databaseName) throws SQLException {
        String dbUrl = String.format(DB_URL_TEMPLATE, databaseName);
        return DriverManager.getConnection(dbUrl, DB_USERNAME, DB_PASSWORD);
    }
    public void saveMenuItem(String databaseName, MenuItemEntity menuItemEntity) {
        String sql = "INSERT INTO menu_items " +
                "(name, description, category, is_active, unit, item_type, is_pinned, price) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection connection = getConnection(databaseName);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, menuItemEntity.getName());
            preparedStatement.setString(2, menuItemEntity.getDescription());
            //preparedStatement.setString(3, menuItemEntity.getCategory());
            preparedStatement.setBoolean(4, menuItemEntity.getIsActive());
            preparedStatement.setString(5, menuItemEntity.getUnit());
            preparedStatement.setString(6, menuItemEntity.getItem_type());
            //preparedStatement.setBoolean(7, menuItemEntity.isPinned());
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

    public void updateMenuItem(String databaseName, MenuItemEntity menuItemEntity) {
        String sql = "UPDATE menu_items SET name = ?, description = ?, category = ?, is_active = ?, unit = ?, item_type = ?, is_pinned = ?, price = ? WHERE id = ?";

        try (Connection connection = getConnection(databaseName);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, menuItemEntity.getName());
            preparedStatement.setString(2, menuItemEntity.getDescription());
            //preparedStatement.setString(3, menuItemEntity.getCategory());
            preparedStatement.setBoolean(4, menuItemEntity.getIsActive());
            preparedStatement.setString(5, menuItemEntity.getUnit());
            preparedStatement.setString(6, menuItemEntity.getItem_type());
            //preparedStatement.setBoolean(7, menuItemEntity.isPinned());
            preparedStatement.setDouble(8, menuItemEntity.getPrice());
            preparedStatement.setLong(9, menuItemEntity.getId());

            // Виконуємо оновлення
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Updated " + rowsAffected + " row(s) with ID " + menuItemEntity.getId() + " in database: " + databaseName);

        } catch (SQLException e) {
            // Логування та обробка помилки
            throw new RuntimeException("Failed to update menu item in database: " + databaseName, e);
        }
    }
    //todo Created Table_item
    public void ensureTableExists(String databaseName) {
        String sql = "CREATE TABLE IF NOT EXISTS table_items (" +
                "id SERIAL PRIMARY KEY, " +
                "table_number INT NOT NULL UNIQUE, " +
                "qr_code VARCHAR(255) NOT NULL UNIQUE, " +
                "is_active BOOLEAN DEFAULT TRUE, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";

        try (Connection connection = getConnection(databaseName);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
            System.out.println("Table 'table_items' ensured in database: " + databaseName);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to ensure table exists in database: " + databaseName, e);
        }
    }

    public void createTableItem(String databaseName, int tableNumber, String qrCode, Timestamp createdAt) {
        String sql = "INSERT INTO table_items (table_number, qr_code, is_active, created_at) " +
                "VALUES (?, ?, ?, ?)";

        try (Connection connection = getConnection(databaseName);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setInt(1, tableNumber);
            preparedStatement.setString(2, qrCode);
            preparedStatement.setBoolean(3, true); // is_active за замовчуванням true
            preparedStatement.setTimestamp(4, createdAt);

            preparedStatement.executeUpdate();
            System.out.println("New table item created in database: " + databaseName);
        } catch (SQLException e) {
            if (e.getSQLState().equals("23505")) { // SQL код для порушення унікальності
                throw new RuntimeException("Duplicate entry for table_number or qr_code", e);
            }
            throw new RuntimeException("Failed to create table item in database: " + databaseName, e);
        }
    }

    public Map<String, Object> getAllTables(String databaseName) {
        String sql = "SELECT * FROM table_items";

        try (Connection connection = getConnection(databaseName);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            List<Map<String, Object>> tables = new ArrayList<>();
            while (resultSet.next()) {
                Map<String, Object> table = new HashMap<>();
                table.put("id", resultSet.getLong("id"));
                table.put("tableNumber", resultSet.getInt("table_number"));
                table.put("qrCode", resultSet.getString("qr_code"));
                table.put("isActive", resultSet.getBoolean("is_active"));
                table.put("createdAt", resultSet.getTimestamp("created_at"));
                tables.add(table);
            }

            Map<String, Object> response = new HashMap<>();
            response.put("tables", tables);
            return response;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch tables from database: " + databaseName, e);
        }
    }

    public void updateTableItem(String databaseName, TableItemDTO tableItemDTO) {
        // Збираємо список пар "поле = ?" і список параметрів
        List<String> setClauses = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        // Перевірка та оновлення tableNumber
        // Якщо tableNumber не null, перевіримо, чи вже існує такий номер столу (крім поточного id)
        if (tableItemDTO.getTableNumber() != null) {
            if (isTableNumberExists(databaseName, tableItemDTO.getTableNumber(), tableItemDTO.getId())) {
                throw new RuntimeException("Table number already exists");
            }
            setClauses.add("table_number = ?");
            parameters.add(tableItemDTO.getTableNumber());
        }

        // Якщо qrCode передано і не пустий - оновлюємо
        if (tableItemDTO.getQrCode() != null && !tableItemDTO.getQrCode().isEmpty()) {
            setClauses.add("qr_code = ?");
            parameters.add(tableItemDTO.getQrCode());
        }

        // Поле is_active вважаємо обов'язковим або таким, що завжди оновлюється
        setClauses.add("is_active = ?");
        parameters.add(tableItemDTO.is_Active());

        if (setClauses.isEmpty()) {
            throw new RuntimeException("No fields provided to update for id: " + tableItemDTO.getId());
        }

        // Формуємо динамічний SQL
        String sql = "UPDATE table_items SET " + String.join(", ", setClauses) + " WHERE id = ?";

        try (Connection connection = getConnection(databaseName);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            int paramIndex = 1;
            for (Object param : parameters) {
                if (param instanceof Integer) {
                    preparedStatement.setInt(paramIndex, (Integer) param);
                } else if (param instanceof Boolean) {
                    preparedStatement.setBoolean(paramIndex, (Boolean) param);
                } else if (param instanceof String) {
                    preparedStatement.setString(paramIndex, (String) param);
                }
                paramIndex++;
            }

            // Останній параметр — id
            preparedStatement.setLong(paramIndex, tableItemDTO.getId());

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected == 0) {
                throw new RuntimeException("No table found with id: " + tableItemDTO.getId());
            }

            System.out.println("Table with id " + tableItemDTO.getId() + " updated in database: " + databaseName);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update table in database: " + databaseName, e);
        }
    }



    private boolean isTableNumberExists(String databaseName, int tableNumber, Long excludeId) {
        String sql = "SELECT COUNT(*) FROM table_items WHERE table_number = ? AND (id != ? OR ? IS NULL)";
        try (Connection connection = getConnection(databaseName);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setInt(1, tableNumber);
            preparedStatement.setObject(2, excludeId, java.sql.Types.BIGINT);
            preparedStatement.setObject(3, excludeId, java.sql.Types.BIGINT);

            try (ResultSet resultSet = preparedStatement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check table number existence in database: " + databaseName, e);
        }
        return false;
    }


    public void deleteTableItem(String databaseName, Long id) {
        String sql = "DELETE FROM table_items WHERE id = ?";

        try (Connection connection = getConnection(databaseName);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setLong(1, id);

            int rowsAffected = preparedStatement.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("No table found with id: " + id);
            }

            System.out.println("Table with id " + id + " deleted from database: " + databaseName);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete table in database: " + databaseName, e);
        }
    }




}
