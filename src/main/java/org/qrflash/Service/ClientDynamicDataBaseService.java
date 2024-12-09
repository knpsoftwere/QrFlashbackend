package org.qrflash.Service;

import lombok.RequiredArgsConstructor;
import org.qrflash.DTO.MenuItemDTO;
import org.qrflash.DTO.TableItemDTO;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientDynamicDataBaseService {
    private static final String DB_URL_TEMPLATE = "jdbc:postgresql://138.201.118.129:5432/%s";
    private static final String DB_USERNAME = "postgres";
    private static final String DB_PASSWORD = "R3cv77m6F3Ys6MfV";

    private Connection getConnection(String databaseName) throws SQLException {
        String dbUrl = String.format(DB_URL_TEMPLATE, databaseName);
        return DriverManager.getConnection(dbUrl, DB_USERNAME, DB_PASSWORD);
    }

    public boolean doesTableExist(String databaseName, String tableName) {
        String sql = "SELECT to_regclass(?)";

        try (Connection connection = getConnection(databaseName);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, tableName);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next() && resultSet.getString(1) != null;
            // Повертає true, якщо таблиця існує

        } catch (SQLException e) {
            throw new RuntimeException("CDDBS: doesTableExist? Помилка перевірки таблиці столів в базі: " + databaseName, e);
        }
    }

    public boolean doesDatabaseExist(String databaseName) {
        String dbUrl = String.format(DB_URL_TEMPLATE, "postgres"); // або інша системна БД
        String sql = "SELECT 1 FROM pg_database WHERE datname = ?";

        try (Connection connection = DriverManager.getConnection(dbUrl, DB_USERNAME, DB_PASSWORD);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, databaseName);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException("CDDBS: doesDatabaseExist? Помилка перевірки бази даних: " + databaseName, e);
        }
    }

    public TableItemDTO getTableItemByQrCode(String databaseName, String qrCode) {
        //Перевіряємо, чи існує база даних
        if (!doesDatabaseExist(databaseName)) {
            throw new RuntimeException("CDDBS: getTableItemByQrCode? Відсутня база даних: " + databaseName);
        }

        // Перевіряємо, чи існує таблиця
        if (!doesTableExist(databaseName, "table_items")) {
            throw new RuntimeException("CDDBS: getTableItemByQrCode? Відсутня таблиця в базі: " + databaseName);
        }

        // SQL-запит на отримання запису
        String sql = "SELECT * FROM table_items WHERE qr_code = ?";

        try (Connection connection = getConnection(databaseName);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

            preparedStatement.setString(1, qrCode);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                TableItemDTO tableItemDTO = new TableItemDTO();
                tableItemDTO.setId(resultSet.getLong("id"));
                tableItemDTO.setTableNumber(resultSet.getInt("table_number"));
                tableItemDTO.setQrCode(resultSet.getString("qr_code"));
                tableItemDTO.set_Active(resultSet.getBoolean("is_active"));
                return tableItemDTO;
            } else {
                throw new RuntimeException("CDDBS: getTableItemByQrCode? Не знайдено столів за qrCode: " + qrCode);
            }

        } catch (SQLException e) {
            throw new RuntimeException("SQLExeption CDDBS: getTableItemByQrCode? Помилка, не знайдено столів за Qr code в базі: " + databaseName, e);
        }
    }


    public List<MenuItemDTO> getMenuItemsCLient(String databaseName) {
        String sql = "SELECT * FROM menu_items";

        try (Connection connection = getConnection(databaseName);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            List<MenuItemDTO> menuItems = new ArrayList<>();
            while (resultSet.next()) {
                MenuItemDTO menuItem = new MenuItemDTO();
                menuItem.setId(resultSet.getLong("id"));
                menuItem.setName(resultSet.getString("name"));
                menuItem.setDescription(resultSet.getString("description"));
                menuItem.setPrice(resultSet.getDouble("price"));
                menuItem.setCategory(resultSet.getString("category"));
                menuItem.setAvailable(resultSet.getBoolean("is_active"));
                menuItems.add(menuItem);
            }

            return menuItems;

        } catch (SQLException e) {
            throw new RuntimeException("CDDBS getMenuItemsClient? Помилка отримання інформації позицій меню з бази: " + databaseName, e);
        }
    }

}
