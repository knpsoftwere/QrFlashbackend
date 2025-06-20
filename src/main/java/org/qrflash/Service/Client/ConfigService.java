package org.qrflash.Service.Client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import lombok.RequiredArgsConstructor;
import org.qrflash.Service.DataBase.DataBaseService;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class ConfigService {

    private final DataBaseService dataBaseService;

    private String getConfigData(String databaseName, String key) {
        String sql = "SELECT data FROM config WHERE key = ?";
        try (Connection connection = dataBaseService.getConnection(databaseName);
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, key);
            try (ResultSet rs = preparedStatement.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("data");
                } else {
                    throw new RuntimeException("No config found for key: " + key);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get config data for key: " + key + " from database: " + databaseName, e);
        }
    }

    // Видалення поля або елемента (оператор '-')
    private void jsonbRemoveField(String databaseName, String key, String jsonRemoveExpression) {
        // Для видалення поля: data = data - 'FieldName'
        // Для видалення елемента масиву: data = data #- '{path,to,element}'
        String sql = "UPDATE config SET data = data " + jsonRemoveExpression + " WHERE key = '" + key + "'";
        try (Connection connection = dataBaseService.getConnection(databaseName);
             Statement statement = connection.createStatement()) {
            int rows = statement.executeUpdate(sql);
            if (rows == 0) {
                throw new RuntimeException("No config row updated for key: " + key);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove field for key: " + key + " in database: " + databaseName, e);
        }
    }

    public Map<String, Object> getEstablishmentProperties(String databaseName) {
        String jsonString = getConfigData(databaseName, "establishment_properties");; // як і раніше дістаємо JSON
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Map<String, Object> map = objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>(){});
            return map;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON for establishment properties", e);
        }
    }

    public void updateEstablishmentProperties(String databaseName, String newName, String newAddress, String description, List<String> contactInfo) {
        StringBuilder sql = new StringBuilder("UPDATE config SET data = ");
        String baseJsonb = "data"; // Початкова JSONB структура
        List<String> updates = new ArrayList<>();

        // Додавання оновлень
        if (newName != null) {
            baseJsonb = String.format("jsonb_set(%s, '{name}', ?::jsonb)", baseJsonb);
        }
        if (newAddress != null) {
            baseJsonb = String.format("jsonb_set(%s, '{address}', ?::jsonb)", baseJsonb);
        }
        if (description != null) {
            baseJsonb = String.format("jsonb_set(%s, '{description}', ?::jsonb)", baseJsonb);
        }
        if (contactInfo != null) {
            baseJsonb = String.format("jsonb_set(%s, '{contact-info}', ?::jsonb)", baseJsonb);
        }

        sql.append(baseJsonb);
        sql.append(" WHERE key = 'establishment_properties';");

        try (Connection connection = dataBaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {

            // Додавання параметрів
            int paramIndex = 1;
            if (newName != null) {
                ps.setString(paramIndex++, "\"" + newName + "\"");
            }
            if (newAddress != null) {
                ps.setString(paramIndex++, "\"" + newAddress + "\"");
            }
            if (description != null) {
                ps.setString(paramIndex++, "\"" + description + "\"");
            }
            if (contactInfo != null) {
                String contactInfoJson = contactInfo.stream()
                        .map(num -> "\"" + num + "\"")
                        .collect(Collectors.joining(",", "[", "]"));
                ps.setString(paramIndex++, contactInfoJson);
            }

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("No config row updated for establishment_properties");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update establishment properties in database: " + databaseName, e);
        }
    }

    //-------------------------------
    // Методи для color_schemes
    //-------------------------------

    // Отримати color_schemes
    public Map<String, Object> getColorSchemes(String databaseName) {
        String jsonString = getConfigData(databaseName, "color_schemes");;
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonString, new TypeReference<Map<String, Object>>(){});
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse JSON for color_schemes", e);
        }
    }

    // Змінити активну схему
    public void updateActiveColorSchemeByIndex(String databaseName, int index) {
        String fetchSql = "SELECT data->'schemes' AS schemes FROM config WHERE key = 'color_schemes'";
        String updateSql = "UPDATE config SET data = jsonb_set(data, '{active_scheme_name}', ?::jsonb) WHERE key = 'color_schemes'";

        try (Connection connection = dataBaseService.getConnection(databaseName);
             PreparedStatement fetchStatement = connection.prepareStatement(fetchSql);
             PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {

            ResultSet resultSet = fetchStatement.executeQuery();
            if (resultSet.next()) {
                ObjectMapper objectMapper = new ObjectMapper();
                ArrayNode schemes = (ArrayNode) objectMapper.readTree(resultSet.getString("schemes"));

                // Перевірка на валідний індекс
                if (index < 0 || index >= schemes.size()) {
                    throw new IllegalArgumentException("Індекса : " + index + " не існує.");
                }

                String newActiveSchemeName = schemes.get(index).get("name").asText();
                updateStatement.setString(1, "\"" + newActiveSchemeName + "\"");
                updateStatement.executeUpdate();
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Error updating active color scheme by index", e);
        }
    }



    public void updateColorSchemeAtIndex(String databaseName, int index, Map<String, Object> updatedFields) {
        String checkSql = "SELECT jsonb_array_length(data->'schemes') AS length " +
                "FROM config WHERE key = 'color_schemes'";

        StringBuilder updateSql = new StringBuilder("UPDATE config SET data = ");

        // Ланцюжок для jsonb_set
        String jsonbChain = "data";
        for (String key : updatedFields.keySet()) {
            jsonbChain = String.format("jsonb_set(%s, '{schemes,%d,%s}', to_jsonb(?))", jsonbChain, index, key);
        }
        updateSql.append(jsonbChain).append(" WHERE key = 'color_schemes'");

        try (Connection connection = dataBaseService.getConnection(databaseName);
             PreparedStatement checkStatement = connection.prepareStatement(checkSql);
             PreparedStatement updateStatement = connection.prepareStatement(updateSql.toString())) {

            // Перевіряємо наявність індексу
            ResultSet rs = checkStatement.executeQuery();
            if (rs.next()) {
                int length = rs.getInt("length");
                if (index < 0 || index >= length) {
                    throw new IllegalArgumentException("Invalid index: " + index + ". Index out of bounds.");
                }
            } else {
                throw new RuntimeException("Failed to fetch schemes array length.");
            }

            // Призначаємо значення для JSONB
            int paramIndex = 1;
            for (Object value : updatedFields.values()) {
                updateStatement.setObject(paramIndex++, value);
            }

            // Виконуємо оновлення
            int rows = updateStatement.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("No rows updated for 'color_schemes'");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update color scheme at index: " + index, e);
        }
    }

    // Додати нову схему
    public void addColorScheme(String databaseName, String newSchemeJson) {
        String sql = "UPDATE config " +
                "SET data = jsonb_set(" +
                "data, '{schemes}', " +
                "(CASE " +
                "WHEN data->'schemes' IS NOT NULL " +
                "THEN (data->'schemes') || ?::jsonb " +
                "ELSE jsonb_build_array(?::jsonb) " +
                "END), true) " +
                "WHERE key = 'color_schemes'";

        try (Connection connection = dataBaseService.getConnection(databaseName);
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Передаємо об'єкт двічі: один для існуючого масиву, другий для нового масиву
            statement.setString(1, newSchemeJson);
            statement.setString(2, newSchemeJson);

            int rows = statement.executeUpdate();

            if (rows == 0) {
                throw new RuntimeException("No config row updated for 'color_schemes'");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add color scheme in database: " + databaseName, e);
        }
    }


    // Видалити схему за індексом у масиві
    public void removeColorSchemeAtIndex(String databaseName, int index) {
        // SQL-запит для видалення схеми за індексом
        String fetchSql = "SELECT data->'schemes' AS schemes, data->>'active_scheme_name' AS active_scheme_name " +
                "FROM config WHERE key = 'color_schemes'";
        String updateSql = "UPDATE config " +
                "SET data = jsonb_set(data #- '{schemes," + index + "}', '{active_scheme_name}', ?::jsonb) " +
                "WHERE key = 'color_schemes'";

        try (Connection connection = dataBaseService.getConnection(databaseName);
             PreparedStatement fetchStatement = connection.prepareStatement(fetchSql);
             PreparedStatement updateStatement = connection.prepareStatement(updateSql)) {

            ResultSet resultSet = fetchStatement.executeQuery();
            if (resultSet.next()) {
                ObjectMapper objectMapper = new ObjectMapper();
                ArrayNode schemes = (ArrayNode) objectMapper.readTree(resultSet.getString("schemes"));
                String activeSchemeName = resultSet.getString("active_scheme_name");

                // Заборона видалення "Default"
                if (index == 0) {
                    throw new IllegalArgumentException("Cannot delete the default color scheme.");
                }

                // Перевірка на валідний індекс
                if (index < 0 || index >= schemes.size()) {
                    throw new IllegalArgumentException("Invalid scheme index: " + index);
                }

                String schemeToRemove = schemes.get(index).get("name").asText();

                // Видалити схему та оновити active_scheme_name
                schemes.remove(index);
                String newActiveSchemeName = activeSchemeName.equals(schemeToRemove)
                        ? schemes.get(0).get("name").asText()
                        : activeSchemeName;

                updateStatement.setString(1, "\"" + newActiveSchemeName + "\"");
                updateStatement.executeUpdate();
            }
        } catch (SQLException | IOException e) {
            throw new RuntimeException("Error removing color scheme at index", e);
        }
    }

    public List<Map<String, Object>> getOpeningHours(String databaseName) {
        String sql = "SELECT day, work_hours, breaks, checkout, status FROM opening_hours ORDER BY id";

        List<Map<String, Object>> result = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();

        try (Connection connection = dataBaseService.getConnection(databaseName);
             Statement statement = connection.createStatement();
             ResultSet rs = statement.executeQuery(sql)) {

            while (rs.next()) {
                Map<String, Object> dayInfo = new HashMap<>();
                dayInfo.put("day", rs.getString("day"));

                // Обробка work_hours: якщо null, повертаємо порожній об'єкт {}
                String workHoursJson = rs.getString("work_hours");
                dayInfo.put("work_hours", workHoursJson != null
                        ? objectMapper.readValue(workHoursJson, Map.class)
                        : new HashMap<>());

                // Обробка breaks: якщо null, повертаємо порожній масив []
                String breaksJson = rs.getString("breaks");
                dayInfo.put("breaks", breaksJson != null
                        ? objectMapper.readValue(breaksJson, List.class)
                        : new ArrayList<>());

                dayInfo.put("checkout", rs.getBoolean("checkout"));
                dayInfo.put("status", rs.getString("status"));

                result.add(dayInfo);
            }
        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException("Помилка отримання opening_hours", e);
        }

        return result;
    }


    public void updatePartialOpeningHours(String databaseName, String day, String workHours, String breaks, Boolean checkout, String status) {
        StringBuilder sql = new StringBuilder("UPDATE opening_hours SET ");
        List<Object> parameters = new ArrayList<>();

        if (workHours != null) {
            sql.append("work_hours = ?::jsonb, ");
            parameters.add(workHours);
        }
        if (breaks != null) {
            sql.append("breaks = ?::jsonb, ");
            parameters.add(breaks);
        }
        if (checkout != null) {
            sql.append("checkout = ?, ");
            parameters.add(checkout);
        }
        if (status != null) {
            sql.append("status = ?, ");
            parameters.add(status);
        }

        sql.setLength(sql.length() - 2); // Видаляємо останню кому
        sql.append(" WHERE day = ?");
        parameters.add(day);

        try (Connection connection = dataBaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(sql.toString())) {

            for (int i = 0; i < parameters.size(); i++) {
                ps.setObject(i + 1, parameters.get(i));
            }

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("День " + day + " не знайдено для оновлення");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка оновлення даних для дня: " + day, e);
        }
    }
}