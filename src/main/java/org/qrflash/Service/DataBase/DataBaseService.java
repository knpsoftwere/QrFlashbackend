package org.qrflash.Service.DataBase;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.sql.*;

@Service
public class DataBaseService {
    @PersistenceContext
    private EntityManager em;

    @Value("${custom.datasource.urlConn}")
    private String DB_URL;
    @Value("${spring.datasource.username}")
    private String DB_USERNAME;
    @Value("${spring.datasource.password}")
    private String DB_PASSWORD;

    public Connection getConnection(String databaseName) throws SQLException {
        return DriverManager.getConnection(String.format(DB_URL, databaseName), DB_USERNAME, DB_PASSWORD);
    }
    //Створення самої бази даних
    public void createDatabase(String databaseName) {
        System.out.println("createDatabase: Запустився");
        String createDbSql = "CREATE DATABASE " + databaseName +
                " WITH ENCODING 'UTF8' LC_COLLATE 'en_US.UTF-8' LC_CTYPE 'en_US.UTF-8' TEMPLATE template0";
        String dropDbSql = "DROP DATABASE IF EXISTS " + databaseName;

        try (Connection connection = DriverManager.getConnection(String.format(DB_URL, "postgres"), DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            System.out.println("createDatabase: База даних видалена : " + databaseName);
            statement.executeUpdate(dropDbSql);

            System.out.println("createDatabase: Створена нова база: " + databaseName);
            statement.executeUpdate(createDbSql);

        } catch (SQLException e) {
            throw new RuntimeException("createDatabase: Помилка створення: " + e.getMessage(), e);
        }
    }
    //Створює таблицю меню
    public void createMenuItemTable(String databaseName) {
        System.out.println("createMenuItemTable Запустився");
        String createTableSQL = """
            CREATE TABLE IF NOT EXISTS menu_items (
              id BIGSERIAL PRIMARY KEY,
              name VARCHAR(255) NOT NULL,
              photo VARCHAR(500),
              description TEXT,
              unit VARCHAR(50) NOT NULL CHECK (unit IN ('sht', 'kg', 'g', 'l', 'ml', 'prc')),
              item_type VARCHAR(50) NOT NULL CHECK (item_type IN ('warh', 'tech', 'mod', 'serv')),
              price DECIMAL DEFAULT 0.00,
              is_active BOOLEAN DEFAULT TRUE,
              is_pinned BOOLEAN DEFAULT FALSE,
              is_quantity BOOLEAN DEFAULT FALSE,
              category_id BIGINT REFERENCES categories(id) ON DELETE SET NULL
          );
        """;

        try (Connection connection = DriverManager.getConnection(String.format(DB_URL, databaseName), DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            System.out.println("createMenuItemTable: Створена Таблиця MenuItemTable");
            statement.executeUpdate(createTableSQL);

        } catch (SQLException e) {
            throw new RuntimeException("createMenuItemTable: Error create MenuItemTable: "+ e);
        }
    }

    public void createOpeningHoursTable(String databaseName) {
        System.out.println("createOpeningHoursTable Запустився");
        String sql = """
        CREATE TABLE opening_hours (
            id SERIAL PRIMARY KEY,
            day VARCHAR(10) NOT NULL UNIQUE CHECK (day IN ('Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday')),
            work_hours JSONB DEFAULT '{}',
            breaks JSONB DEFAULT '[]',
            status VARCHAR(10) CHECK (status IN ('open', 'closed', 'paused')) DEFAULT 'closed',
            checkout BOOLEAN NOT NULL DEFAULT false
        );
    """;

        try (Connection connection = DriverManager.getConnection(String.format(DB_URL, databaseName), DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(sql);
            System.out.println("createOpeningHoursTable: Створено таблицю opening Hours");
        } catch (SQLException e) {
            throw new RuntimeException("createOpeningHoursTable: Помилка створення opening Hours таблиці", e);
        }
    }


    //Створення для таблиці початкових меню
    public void insertDefaultMenuItems(String databaseName) {
        String insertDefaultItemsSQL = """
        INSERT INTO menu_items (name, photo, description, unit, item_type, price, category_id)
        VALUES 
        ('Product 1', 'example.png', 'Basic product', 'kg', 'warh', 50.0, 1);
        """;

        try (Connection connection = DriverManager.getConnection(String.format(DB_URL, databaseName), DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            System.out.println("insertDefaultMenuItems: Стандартні товари для таблиці створені");
            statement.executeUpdate(insertDefaultItemsSQL);

        } catch (SQLException e) {
            throw new RuntimeException("insertDefaultMenuItems: Помилка створення стандартних значень товару: " + e);
        }
    }

    //Метод створення таблиці opening hours
    public void initializeOpeningHours(String databaseName) {
        System.out.println("initializeOpeningHours: Запустився");
        String sql = """
        INSERT INTO opening_hours (day, work_hours, breaks)
        VALUES
        ('Monday', '{}', '[]'),
        ('Tuesday', '{}', '[]'),
        ('Wednesday', '{}', '[]'),
        ('Thursday', '{}', '[]'),
        ('Friday', '{}', '[]'),
        ('Saturday', '{}', '[]'),
        ('Sunday', '{}', '[]')
        ON CONFLICT (day) DO NOTHING;
    """;

        try (Connection connection = DriverManager.getConnection(String.format(DB_URL, databaseName), DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(sql);
            System.out.println("initializeOpeningHours: Ініціалізовано дані в таблиці opening hours");
        } catch (SQLException e) {
            throw new RuntimeException("initializeOpeningHours: Помилка ініціалізації opening hours ", e);
        }
    }


    //Метод на створення config для закладу
    public void createConfigTable(String databaseName) {
        System.out.println("createConfigTable: Запустився");
        String createConfigSQL = """
        CREATE TABLE IF NOT EXISTS config (
            id SERIAL PRIMARY KEY,
            key TEXT NOT NULL UNIQUE,
            data JSONB NOT NULL
        );
        """;

        try (Connection connection = DriverManager.getConnection(String.format(DB_URL, databaseName), DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            System.out.println("createConfigTable: Створюємо таблицю config");
            statement.executeUpdate(createConfigSQL);

        } catch (SQLException e) {
            throw new RuntimeException("createConfigTable: Помилка творення таблиці: " + e);
        }
    }

    //Створення стандартних значень для Config
    public void insertDefaultConfigData(String databaseName) {
        // JSON дані по замовчуванню

        // establishment_properties
        System.out.println("insertDefaultConfigData: Запустився");
        String establishmentPropertiesJson = """
        {
          "name": "My establishment",
          "address": "",
          "description": "",
          "contact_info": []
        }
        """;
        System.out.println("insertDefaultConfigData: establishmentPropertiesJson додано");

        // color_schemes
        String colorSchemesJson = """
        {
          "schemes": [
            {
              "name": "Default",
              "primary_color": "#ca1414",
              "secondary_color": "#c8723d",
              "text_color": "#FFFFFF",
              "background_url": "https://cdn.example.com/default_background.jpg"
            }
          ],
          "active_scheme_name": "Default"
        }
        """;
        System.out.println("insertDefaultConfigData: colorSchemesJson додано");

        // Вставляємо всі два записи
        insertConfigRow(databaseName, "establishment_properties", establishmentPropertiesJson);
        insertConfigRow(databaseName, "color_schemes", colorSchemesJson);
        System.out.println("insertDefaultConfigData: Завершився");
    }
    //Присвоєння значень (ключ - значення), зверху приклад
    private void insertConfigRow(String databaseName, String key, String jsonData) {
        System.out.println("insertConfigRow: Запустився");
        String sql = "INSERT INTO config (key, data) VALUES (?, ?::jsonb)";
        try (Connection connection = DriverManager.getConnection(String.format(DB_URL, databaseName), DB_USERNAME, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, key);
            preparedStatement.setString(2, jsonData);
            preparedStatement.executeUpdate();

            System.out.println("insertConfigRow: Добавлений кофіг ключ: " + key);
        } catch (SQLException e) {
            throw new RuntimeException("insertConfigRow: Помилка створення ключа (" + key + ")", e);
        }
    }

    public void recreateDatabase(String databaseName) {
        // Видаляємо базу, якщо вона існує
        System.out.println("recreateDatabase: Запустився");
        dropDatabaseIfExists(databaseName);
        System.out.println("recreateDatabase: Видалили базу");

        // Створюємо нову базу даних
        String createDbSql = "CREATE DATABASE " + databaseName;
        try (Connection connection = DriverManager.getConnection(String.format(DB_URL, "postgres"), DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(createDbSql);
            System.out.println("recreateDatabase: База даних успішно створена.");

        } catch (SQLException e) {
            throw new RuntimeException("recreateDatabase: Помилка створення бази даних: " + databaseName, e);
        }

        // Підключаємось до новоствореної бази для подальших дій
        try {
            System.out.println("Підключення до нової бази даних: " + databaseName);

            // Створення всіх таблиць
            createCategoriesTable(databaseName);
            createMenuItemTable(databaseName);
            createOpeningHoursTable(databaseName);
            createConfigTable(databaseName);
            createTagsTable(databaseName);
            createMenuItemTagsTable(databaseName);
            createOrdersTable(databaseName);
            createPaymentsTable(databaseName);
            createMethod_payment(databaseName);
            //createCash_register(databaseName);
            //createcash_register_method_payment(databaseName);

            // Заповнення дефолтними даними
            initializeOpeningHours(databaseName);
            insertDefaultConfigData(databaseName);
            insertDefaultTags(databaseName);
            insertDefaultCategories(databaseName);
            insertDefaultMenuItems(databaseName);
            insertDefaultPaymentAndCash_register(databaseName);

            System.out.println("recreateDatabase: База даних успішно пересоздана та ініціалізована!");

        } catch (Exception e) {
            throw new RuntimeException("recreateDatabase: Error recreated database: " + databaseName, e);
        }
    }

    public void createTagsTable(String databaseName) {
        System.out.println("createTagsTable: Запустився");

        String createTagsSQL = """
            CREATE TABLE IF NOT EXISTS tags (
                id BIGSERIAL PRIMARY KEY,
                name VARCHAR(100) NOT NULL UNIQUE,
                description TEXT,
                emoji VARCHAR(10) UNIQUE NOT NULL
            );
            """;

        try (Connection connection = DriverManager.getConnection(String.format(DB_URL, databaseName), DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(createTagsSQL);
            System.out.println("createTagsTable: Таблиця `tags` успішно створена в базі");

        } catch (SQLException e) {
            throw new RuntimeException("createTagsTable: Error create table `tags`", e);
        }
    }


    public void createMenuItemTagsTable(String databaseName) {
        System.out.println("createMenuItemTagsTable: Запустився");
        String createMenuItemTagsSQL = """
        CREATE TABLE IF NOT EXISTS menu_item_tags ( 
            menu_item_id BIGINT REFERENCES menu_items(id) ON DELETE CASCADE,
            tag_id BIGINT REFERENCES tags(id) ON DELETE CASCADE,
            PRIMARY KEY (menu_item_id, tag_id)
        );
    """;

        try (Connection connection = DriverManager.getConnection(String.format(DB_URL, databaseName), DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(createMenuItemTagsSQL);
            System.out.println("createMenuItemTagsTable: Таблиця `menu_item_tags` успішно створена в базі");

        } catch (SQLException e) {
            throw new RuntimeException("createMenuItemTagsTable: Помилка створення `menu_item_tags` у базі", e);
        }
    }

    public void insertDefaultTags(String databaseName) {
        System.out.println("insertDefaultTags: Запустився");
        String insertTagsSQL = """
        INSERT INTO tags (name, description, emoji)
        VALUES 
            ('Spicy', 'Dishes with hot peppers', '🌶️'),
            ('Allergens', 'Contains potential allergens', '⚠️'),
            ('Alcohol', 'Contains alcohol', '🍷')
        ON CONFLICT (name) DO NOTHING;
    """;

        try (Connection connection = DriverManager.getConnection(String.format(DB_URL, databaseName), DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(insertTagsSQL);
            System.out.println("insertDefaultTags: Дефолтні теги успішно додані в базу");

        } catch (SQLException e) {
            throw new RuntimeException("insertDefaultTags: Помилка вставки дефолтних тегів", e);
        }
    }

    public void createCategoriesTable(String databaseName) {
        System.out.println("createCategoriesTable: Запустився");
        String createCategoriesSQL = """
        CREATE TABLE IF NOT EXISTS categories (
              id BIGSERIAL PRIMARY KEY,
              name VARCHAR(255) NOT NULL UNIQUE,
              description TEXT,
              image_url VARCHAR(500) NOT NULL
          );
    """;

        try (Connection connection = DriverManager.getConnection(String.format(DB_URL, databaseName), DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(createCategoriesSQL);
            System.out.println("createCategoriesTable: Таблиця `categories` успішно створена в базі");

        } catch (SQLException e) {
            throw new RuntimeException("createCategoriesTable: Помилка створення таблиці `categories` у базі", e);
        }
    }

    public void insertDefaultCategories(String databaseName) {
        System.out.println("insertDefaultCategories: Запустився");
        String insertCategoriesSQL = """
            INSERT INTO categories (name, description, image_url)
            VALUES ('Categories', 'Default categories', '.ex')
            ON CONFLICT (name) DO NOTHING;
        """;

        try (Connection connection = DriverManager.getConnection(String.format(DB_URL, databaseName), DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(insertCategoriesSQL);
            System.out.println("insertDefaultCategories: Дефолтні категорії успішно додані в базу");

        } catch (SQLException e) {
            throw new RuntimeException("insertDefaultCategories: Помилка вставки дефолтних категорій у базу", e);
        }
    }

    public void dropDatabaseIfExists(String databaseName) {
        System.out.println("dropDatabaseIfExists: Запустився");
        try (Connection connection = DriverManager.getConnection(String.format(DB_URL, "postgres"), DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            // Завершуємо активні з'єднання
            String terminateConnections = String.format(
                    "DO $$ " +
                            "DECLARE db_name TEXT := '%s'; " +
                            "BEGIN " +
                            "EXECUTE format('SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = ''%s'' AND pid <> pg_backend_pid()', db_name); " +
                            "END $$;",
                    databaseName, databaseName);

            statement.execute(terminateConnections);

            // Видаляємо базу
            String dropDbSql = "DROP DATABASE IF EXISTS " + databaseName;
            statement.executeUpdate(dropDbSql);
            System.out.println("dropDatabaseIfExists: База даних успішно видалена (якщо існувала).");

        } catch (SQLException e) {
            System.out.println("dropDatabaseIfExists: Попередження: Неможливо видалити базу, вона може не існувати.");
            e.printStackTrace();
        }
    }

    //Створення таблиці orders яка зберігатиме в собі основну інформацію про замовлення
    public void createOrdersTable(String databaseName) {
        System.out.println("createOrdersTable: Запустився ");
        String createOrdersTable = """
                CREATE TABLE orders(
                    id BIGSERIAL PRIMARY KEY,
                    total_amount DOUBLE PRECISION NOT NULL,
                    currency VARCHAR(3) NOT NULL,
                    status VARCHAR(50) DEFAULT 'PENDING',
                    order_items JSONB,
                    created_at TIMESTAMP DEFAULT NOW(),
                    updated_at TIMESTAMP DEFAULT NOW()
                );
                """;
        try (Connection connection = DriverManager.getConnection(String.format(DB_URL, databaseName), DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(createOrdersTable);
            System.out.println("createorderstable: Таблиця `orders` успішно створена в базі");

        } catch (SQLException e) {
            throw new RuntimeException("createorderstable: Помилка створення таблиці `orders` у базі", e);
        }
    }

    //Створення таблиці payments  яка зберігає в собі інформацію по транзакції
    public void createPaymentsTable(String databaseName) {
        System.out.println("createPaymentsTable: Запуск");
        String createPaymentsTable = """
                CREATE TABLE payments(
                    id BIGSERIAL PRIMARY KEY,
                    order_id BIGINT NOT NULL,
                    invoice_id VARCHAR(100),
                    payment_url TEXT,
                    status VARCHAR(50) DEFAULT 'PENDING',
                    amount DOUBLE PRECISION NOT NULL,
                    current VARCHAR(3) NOT NULL,
                    created_at TIMESTAMP DEFAULT NOW(),
                    updated_at TIMESTAMP DEFAULT NOW(),
                    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE
                );""";

        try (Connection connection = DriverManager.getConnection(String.format(DB_URL, databaseName), DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(createPaymentsTable);
            System.out.println("createPaymentsTable: Таблиця `payments` успішно створена в базі");

        } catch (SQLException e) {
            throw new RuntimeException("createPaymentsTable: Помилка створення таблиці `payments` у базі", e);
        }
    }

    //Ствоерння таблиці method_payment
    public void createMethod_payment(String databaseName){
        System.out.println("createMethod_payment: Запуск");
        String createMethodpayment = """
                CREATE TABLE method_payment (
                    id SERIAL PRIMARY KEY,
                    name VARCHAR(100) NOT NULL,       
                    active BOOLEAN NOT NULL DEFAULT TRUE,
                    json_setting JSONB                
                );
                """;
        try (Connection connection = DriverManager.getConnection(String.format(DB_URL, databaseName), DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(createMethodpayment);
            System.out.println("createPaymentsTable: Таблиця `payments` успішно створена в базі");

        } catch (SQLException e) {
            throw new RuntimeException("createPaymentsTable: Помилка створення таблиці `payments` у базі", e);
        }
    }

//    //Ствоерння таблиці createCash_register
//    public void createCash_register (String databaseName){
//        System.out.println("createCash_register: Запуск");
//        String createCash_register = """
//                CREATE TABLE cash_register (
//                      id SERIAL PRIMARY KEY,
//                      name VARCHAR(100) NOT NULL
//                  );
//                """;
//        try (Connection connection = DriverManager.getConnection(DB_URL + databaseName, DB_USERNAME, DB_PASSWORD);
//             Statement statement = connection.createStatement()) {
//
//            statement.executeUpdate(createCash_register);
//            System.out.println("createPaymentsTable: Таблиця `payments` успішно створена в базі");
//
//        } catch (SQLException e) {
//            throw new RuntimeException("createPaymentsTable: Помилка створення таблиці `payments` у базі", e);
//        }
//    }
//
//    //Ствоерння таблиці createcash_register_method_payment
//    public void createcash_register_method_payment(String databaseName){
//        System.out.println("createcash_register_method_payment: Запуск");
//        String createcash_register_method_payment = """
//                CREATE TABLE cash_register_method_payment (
//                      cash_register_id INT NOT NULL,
//                      method_payment_id INT NOT NULL,
//                      PRIMARY KEY (cash_register_id, method_payment_id),
//                      FOREIGN KEY (cash_register_id) REFERENCES cash_register (id),
//                      FOREIGN KEY (method_payment_id) REFERENCES method_payment (id)
//                  );
//                """;
//        try (Connection connection = DriverManager.getConnection(DB_URL + databaseName, DB_USERNAME, DB_PASSWORD);
//             Statement statement = connection.createStatement()) {
//
//            statement.executeUpdate(createcash_register_method_payment);
//            System.out.println("createPaymentsTable: Таблиця `payments` успішно створена в базі");
//
//        } catch (SQLException e) {
//            throw new RuntimeException("createPaymentsTable: Помилка створення таблиці `payments` у базі", e);
//        }
//    }

    public void insertDefaultPaymentAndCash_register(String databaseName){
        System.out.println("insertDefaultPaymentAndCash_register: Запустився");
        String insertMethodPayment = """
            INSERT INTO method_payment (name, active, json_setting)
                VALUES('Monobank', TRUE, '{"token": "XXX"}');
        """;

//        String insertCashRegister = """
//            INSERT INTO cash_register (name)
//                    VALUES ('Main Cash Desk');
//        """;



        try (Connection connection = DriverManager.getConnection(String.format(DB_URL, databaseName), DB_USERNAME, DB_PASSWORD);
             Statement statement = connection.createStatement()) {

            statement.executeUpdate(insertMethodPayment);
            //statement.executeUpdate(insertCashRegister);
            System.out.println("insertDefaultPaymentAndCash_register: Дефолтні оплати і каса створена");

        } catch (SQLException e) {
            throw new RuntimeException("insertDefaultPaymentAndCash_register: Помилка вставки дефолтних оплати чи каси у базу", e);
        }
    }
}
