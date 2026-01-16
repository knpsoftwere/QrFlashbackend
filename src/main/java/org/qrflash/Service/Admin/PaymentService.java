package org.qrflash.Service.Admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.qrflash.Service.DataBase.DataBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Service
public class PaymentService {

    @Autowired
    private DataBaseService dataBaseService;
    public List<Map<String, Object>> getPaymentMethods(String databaseName) {
        String query = """
        SELECT 
            id AS payment_method_id,
            name AS payment_method_name,
            active AS is_active,
            json_setting AS settings
        FROM 
            method_payment
    """;

        try (Connection connection = dataBaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            List<Map<String, Object>> paymentMethods = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper(); // Використовуємо Jackson ObjectMapper

            while (rs.next()) {
                Map<String, Object> paymentMethod = new HashMap<>();
                paymentMethod.put("paymentMethodId", rs.getInt("payment_method_id"));
                paymentMethod.put("paymentMethodName", rs.getString("payment_method_name"));
                paymentMethod.put("isActive", rs.getBoolean("is_active"));

                // Парсимо поле settings
                String settingsJson = rs.getString("settings");
                if (settingsJson != null && !settingsJson.isEmpty()) {
                    Map<String, Object> settingsMap = objectMapper.readValue(settingsJson, new TypeReference<>() {});
                    paymentMethod.put("settings", settingsMap);
                } else {
                    paymentMethod.put("settings", null);
                }

                paymentMethods.add(paymentMethod);
            }

            return paymentMethods;

        } catch (SQLException | JsonProcessingException e) {
            throw new RuntimeException("Error fetching payment methods: " + e.getMessage(), e);
        }
    }


    public List<String> getActivePaymentMethodNames(String databaseName) {
        String query = "SELECT name FROM method_payment WHERE active = TRUE;";

        try (Connection connection = dataBaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(query);
             ResultSet rs = ps.executeQuery()) {

            List<String> paymentMethodNames = new ArrayList<>();

            while (rs.next()) {
                paymentMethodNames.add(rs.getString("name"));
            }
            return paymentMethodNames;
        } catch (SQLException e) {
            throw new RuntimeException("getActivePaymentMethodNames: Помилка пошуку способу оплати: " + e.getMessage(), e);
        }
    }

    public void refactorPaymentActive(String databaseName, Long id, boolean active) {
        String query = "UPDATE method_payment SET active = ? WHERE id = ?";

        try (Connection connection = dataBaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(query)) {

            ps.setBoolean(1, active); // Встановлюємо значення активності
            ps.setLong(2, id);        // Встановлюємо ID для оновлення

            int rowsUpdated = ps.executeUpdate();
            if (rowsUpdated == 0) {
                throw new RuntimeException("Помилка: Не знайдено запису з ID " + id);
            }

            System.out.println("Статус активності оновлено для ID: " + id);
        } catch (SQLException e) {
            throw new RuntimeException("Помилка оновлення статусу активності для ID: " + id, e);
        }
    }

    public List<Map<String, Object>> getAllOrders(String databaseName) {
        String query = """
                SELECT 
                    id as id,
                    total_amount AS total_amount,
                    currency as currency,
                    status as status,
                    order_items as order_items,
                    created_at as created_at
                FROM
                    orders
                """;

        try(Connection connection = dataBaseService.getConnection(databaseName);
            PreparedStatement ps = connection.prepareStatement(query);
            ResultSet rs = ps.executeQuery()){
            List<Map<String, Object>> orders = new ArrayList<>();
            ObjectMapper objectMapper = new ObjectMapper();

            while (rs.next()) {
                Map<String, Object> orderObject = new HashMap<>();
                orderObject.put("order_id", rs.getInt("id"));
                orderObject.put("total_order_amount", rs.getLong("total_amount"));
                orderObject.put("currency", rs.getString("currency"));
                orderObject.put("status", rs.getString("status"));
                orderObject.put("createdAt", rs.getTimestamp("created_at"));

                String orderItemsJSON = rs.getString("order_items");
                if (orderItemsJSON != null && !orderItemsJSON.isEmpty()) {
                    // Змінюємо Map на List
                    List<Map<String, Object>> settingsList = objectMapper.readValue(orderItemsJSON, new TypeReference<List<Map<String, Object>>>() {});
                    orderObject.put("orderItems", settingsList);
                } else {
                    orderObject.put("orderItems", Collections.emptyList());
                }

                orders.add(orderObject);
            }
            return orders;
        }catch (Exception e){
            throw new RuntimeException("getAllOrders: " + e.getMessage(), e);
        }
    }

    public Map<String, Object> getOrder(String databaseName, long id) {
        String query = """
                SELECT 
                    id as id,
                    total_amount AS total_amount,
                    currency as currency,
                    status as status,
                    order_items as order_items,
                    created_at as created_at
                FROM
                    orders
                WHERE id = ? ;
                """;

        try (Connection connection = dataBaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(query)) {

            // Встановлюємо ID у запит
            ps.setLong(1, id);

            try (ResultSet rs = ps.executeQuery()) {
                ObjectMapper objectMapper = new ObjectMapper();

                if (rs.next()) { // Використовуємо if замість while, бо очікуємо один рядок
                    Map<String, Object> orderObject = new HashMap<>();
                    orderObject.put("order_id", rs.getLong("id"));
                    orderObject.put("total_order_amount", rs.getDouble("total_amount"));
                    orderObject.put("currency", rs.getString("currency"));
                    orderObject.put("status", rs.getString("status"));
                    orderObject.put("createdAt", rs.getTimestamp("created_at"));

                    String orderItemsJSON = rs.getString("order_items");
                    if (orderItemsJSON != null && !orderItemsJSON.isEmpty()) {
                        // Виправлено: десеріалізація у список мап
                        List<Map<String, Object>> itemsList = objectMapper.readValue(
                                orderItemsJSON,
                                new TypeReference<List<Map<String, Object>>>() {}
                        );
                        orderObject.put("orderItems", itemsList);
                    } else {
                        orderObject.put("orderItems", Collections.emptyList());
                    }

                    return orderObject;
                }
                return null; // Якщо замовлення не знайдено
            }
        } catch (Exception e) {
            throw new RuntimeException("getOrder error (id=" + id + "): " + e.getMessage(), e);
        }
    }

    public Object checkOrder(String databaseName, Long orderId) {
        Map<String, Object> order = getOrder(databaseName, orderId);
        return order.get("status");
    }
}
