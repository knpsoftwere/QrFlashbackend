package org.qrflash.Service.DataBase;

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

    public Connection getConnection(String databaseName) throws SQLException {
        return DriverManager.getConnection(DB_URL + databaseName, DB_USERNAME, DB_PASSWORD);
    }
    //Створення самої бази даних
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
    //Створює таблицю меню
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
    public void createOpeningHourseTable(String databaseName) {
        String sql = """
                CREATE TABLE opening_hours (
                    id SERIAL PRIMARY KEY,
                    day VARCHAR(10) NOT NULL UNIQUE CHECK (day IN ('Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday')),
                    work_hours TEXT,          -- Робочі години, як '09:00-12:00,13:00-18:00'
                    breaks TEXT,              -- Час обіду, як '12:00-13:00'
                    status VARCHAR(10) CHECK (status IN ('open', 'closed', 'paused')) DEFAULT 'closed',
                    checkout BOOLEAN NOT NULL DEFAULT false
                );
                """;
        try (Connection connection = DriverManager.getConnection(DB_URL + databaseName, DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            System.out.println("Створення таблиці opening_hourse для : " + databaseName);
            statement.executeUpdate(sql);

        } catch (SQLException e) {
            throw new RuntimeException("Помилка створення opening_hourse: " + databaseName, e);
        }

    }

    //Створення для таблиці початкових меню
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

    //Метод створення таблиці opening hours
    public void initializeOpeningHours(String databaseName) {
        String sql = "INSERT INTO opening_hours (day) VALUES " +
                "('Monday'), ('Tuesday'), ('Wednesday'), " +
                "('Thursday'), ('Friday'), ('Saturday'), ('Sunday')";
        try (Connection connection = DriverManager.getConnection(DB_URL + databaseName, DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize opening_hours table in database: " + databaseName, e);
        }
    }

    //Метод на створення config для закладу
    public void createConfigTable(String databaseName) {
        String createConfigSQL = """
        CREATE TABLE IF NOT EXISTS config (
            id SERIAL PRIMARY KEY,
            key TEXT NOT NULL UNIQUE,
            data JSONB NOT NULL
        );
        """;

        try (Connection connection = DriverManager.getConnection(DB_URL + databaseName, DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            System.out.println("Створюємо таблицю config в базі: " + databaseName);
            statement.executeUpdate(createConfigSQL);

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create config table in database: " + databaseName, e);
        }
    }

    //Створення стандартних значень для Config
    public void insertDefaultConfigData(String databaseName) {
        // JSON дані по замовчуванню

        // establishment_properties
        String establishmentPropertiesJson = """
        {
          "name": "Мій заклад",
          "address": "",
          "contact_info": []
        }
        """;

        // color_schemes
        String colorSchemesJson = """
        {
          "schemes": [
            {
              "name": "Default",
              "primary_color": "#FF5733",
              "secondary_color": "#333333",
              "text_color": "#FFFFFF",
              "background_url": "https://cdn.example.com/default_background.jpg"
            }
          ],
          "active_scheme_name": "Default"
        }
        """;

        // Вставляємо всі два записи
        insertConfigRow(databaseName, "establishment_properties", establishmentPropertiesJson);
        insertConfigRow(databaseName, "color_schemes", colorSchemesJson);
    }
    //Присвоєння значень (ключ - значення), зверху приклад
    private void insertConfigRow(String databaseName, String key, String jsonData) {
        String sql = "INSERT INTO config (key, data) VALUES (?, ?::jsonb)";
        try (Connection connection = DriverManager.getConnection(DB_URL + databaseName, DB_USERNAME, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, key);
            preparedStatement.setString(2, jsonData);
            preparedStatement.executeUpdate();

            System.out.println("Inserted default config for key: " + key + " into database: " + databaseName);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to insert config row (" + key + ") into database: " + databaseName, e);
        }
    }
}
