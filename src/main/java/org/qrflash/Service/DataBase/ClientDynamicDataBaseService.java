package org.qrflash.Service.DataBase;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.qrflash.DTO.TableItemDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ClientDynamicDataBaseService {
    @Value("${custom.datasource.urlConn}")
    private String DB_URL_TEMPLATE;
    @Value("${spring.datasource.username}")
    private String DB_USERNAME;
    @Value("${spring.datasource.password}")
    private String DB_PASSWORD;

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
        String dbUrl = String.format(DB_URL_TEMPLATE, "postgresDb"); // або інша системна БД
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

    public Map<String, Object> getConfig(String databaseName) {
        String sql = "SELECT key, data FROM config";
        try (Connection connection = getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            try (ResultSet rs = ps.executeQuery()) {
                Map<String, Object> config = new HashMap<>();
                // Використовуємо Jackson для парсингу JSON з ResultSet
                ObjectMapper objectMapper = new ObjectMapper();

                while (rs.next()) {
                    String key = rs.getString("key");
                    String data = rs.getString("data");
                    // Перетворюємо JSON з колонки data у Map (або іншу структуру)
                    Map<String, Object> jsonData = objectMapper.readValue(data, new TypeReference<Map<String,Object>>(){});
                    config.put(key, jsonData);
                }

                return config;
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Failed to fetch config from database: " + databaseName, e);
        }
    }

}
