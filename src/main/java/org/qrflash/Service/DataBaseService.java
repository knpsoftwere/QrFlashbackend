package org.qrflash.Service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
public class DataBaseService {
    @PersistenceContext
    private EntityManager em;

    private static final String DB_URL = "jdbc:postgresql://138.201.118.129:5432/";
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "R3cv77m6F3Ys6MfV";

    public void createDatabase(String databaseName) {
        String createDbSql = "CREATE DATABASE " + databaseName +
                " WITH ENCODING 'UTF8' LC_COLLATE 'en_US.UTF-8' LC_CTYPE 'en_US.UTF-8' TEMPLATE template0";
        String dropDbSql = "DROP DATABASE IF EXISTS " + databaseName;

        try (Connection connection = DriverManager.getConnection(DB_URL + "postgres", DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            System.out.println("Deleting existing database: " + databaseName);
            statement.executeUpdate(dropDbSql);

            System.out.println("Creating new database: " + databaseName);
            statement.executeUpdate(createDbSql);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create database: " + e.getMessage(), e);
        }
    }

    public void createMenuItemTable(String databaseName) {
        String createTableSQL = """
        CREATE TABLE IF NOT EXISTS menu_items (
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(255) NOT NULL,
            description TEXT,
            category VARCHAR(50) NOT NULL,
            is_active BOOLEAN NOT NULL DEFAULT TRUE,
            unit VARCHAR(50) NOT NULL,
            item_type VARCHAR(50) NOT NULL,
            is_pinned BOOLEAN NOT NULL DEFAULT FALSE,
            price DOUBLE PRECISION NOT NULL
        );
        """;

        try (Connection connection = DriverManager.getConnection(DB_URL + databaseName, DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            System.out.println("Створена база данних: " + databaseName);
            statement.executeUpdate(createTableSQL);

        } catch (SQLException e) {
            throw new RuntimeException("Помилка створення menu_items: " + databaseName, e);
        }
    }

    public void insertDefaultMenuItems(String databaseName) {
        String insertDefaultItemsSQL = """
        INSERT INTO menu_items (name, category, unit, item_type, price)
        VALUES 
        ('Товар 1', 'Hot Dish', 'kg', 'stock', 50.0);
        """;

        try (Connection connection = DriverManager.getConnection(DB_URL + databaseName, DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            System.out.println("Inserting default items into `menu_item` table in database: " + databaseName);
            statement.executeUpdate(insertDefaultItemsSQL);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert default items into `menu_item` table in database: " + databaseName, e);
        }
    }
}
