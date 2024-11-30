package org.qrflash.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
public class DataBaseService {
    @PersistenceContext
    private EntityManager em;

    public void createDatabase(String databaseName) {
        String url = "jdbc:postgresql://138.201.118.129:5432/postgres";
        String username = "postgres";
        String password = "R3cv77m6F3Ys6MfV";

        String sql = "CREATE DATABASE " + databaseName +
                " WITH ENCODING 'UTF8' " +
                " LC_COLLATE 'en_US.UTF-8' " +
                " LC_CTYPE 'en_US.UTF-8' " +
                " TEMPLATE template0";

        try (Connection connection = DriverManager.getConnection(url, username, password);
             Statement statement = connection.createStatement()) {

            // Видаляємо базу, якщо вона вже існує
            String dropSql = "DROP DATABASE IF EXISTS " + databaseName;
            statement.executeUpdate(dropSql);

            // Створюємо нову базу
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Не вдалося створити базу даних: " + e.getMessage(), e);
        }
    }

    public void createMenuItemTable(String databaseName) {
        String createTableSQL = """
        CREATE TABLE IF NOT EXISTS menu_item (
            id SERIAL PRIMARY KEY,
            photo TEXT,
            name VARCHAR(255) NOT NULL,
            description TEXT,
            category VARCHAR(100) NOT NULL,
            is_active BOOLEAN DEFAULT TRUE,
            unit VARCHAR(50) NOT NULL,
            item_type VARCHAR(50) NOT NULL CHECK (item_type IN ('stock', 'tech_card', 'modifier', 'service')),
            tags TEXT[],
            is_pinned BOOLEAN DEFAULT FALSE
        );
    """;

        String insertDefaultItemsSQL = """
        INSERT INTO menu_item (name, category, unit, item_type, tags)
        VALUES 
        ('Товар 1', 'Hot Dish', 'kg', 'stock', ARRAY['#hotfood']);
    """;

        try (Connection connection = DriverManager.getConnection(
                "jdbc:postgresql://138.201.118.129:5432/" + databaseName, "postgres", "R3cv77m6F3Ys6MfV");
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(createTableSQL);
            statement.executeUpdate(insertDefaultItemsSQL);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create `menu_item` table or insert default data in database: " + databaseName, e);
        }
    }


}
