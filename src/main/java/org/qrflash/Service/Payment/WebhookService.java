package org.qrflash.Service.Payment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.qrflash.Service.DataBase.DataBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

@Service
public class WebhookService {
    @Autowired
    private DataBaseService dataBaseService;

    public void processWebhookData(String webhookData){
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            //Парсимо JSON об'єкт
            JsonNode rootNode = objectMapper.readTree(webhookData);

            // Отримуємо поля з JSON
            String invoiceId = rootNode.path("invoiceId").asText();
            String status = rootNode.path("status").asText();
            int amount = rootNode.path("amount").asInt();
            int currency = rootNode.path("ccy").asInt();
            String createdDate = rootNode.path("createdDate").asText();
            String modifiedDate = rootNode.path("modifiedDate").asText();
            String reference = rootNode.path("reference").asText();
            String destination = rootNode.path("destination").asText();

            try (Connection connection = dataBaseService.getConnection(reference)) {
                // Оновлення статусу в таблиці payments
                String updatePaymentStatusSql = "UPDATE payments SET status = ?, updated_at = NOW() WHERE invoice_id = ?";
                try (PreparedStatement ps = connection.prepareStatement(updatePaymentStatusSql)) {
                    ps.setString(1, status); // Новий статус
                    ps.setString(2, invoiceId); // Ідентифікатор рахунку
                    int updatedRows = ps.executeUpdate();
                    if (updatedRows > 0) {
                        System.out.println("Payment статус успішно оновлений: " + invoiceId);
                    } else {
                        System.out.println("Немає запису за invoiceId в payment: " + invoiceId);
                    }
                }

                // Оновлення статусу в таблиці orders (за потреби)
                String updateOrderStatusSql = "UPDATE orders SET status = ? WHERE id = (SELECT order_id FROM payments WHERE invoice_id = ?)";
                try (PreparedStatement ps = connection.prepareStatement(updateOrderStatusSql)) {
                    ps.setString(1, status); // Встановлюємо статус замовлення
                    ps.setString(2, invoiceId); // Ідентифікатор рахунку
                    int updatedRows = ps.executeUpdate();
                    if (updatedRows > 0) {
                        System.out.println("Order статус успішно оновлений: " + invoiceId);
                    } else {
                        System.out.println("Не знайдено запису за invoiceId в orders: " + invoiceId);
                    }
                }
            } catch (SQLException e) {
                System.err.println("Помилка підключення до бази: " + e.getMessage());
                e.printStackTrace();
            }
        } catch (JsonProcessingException e) {
            System.err.println("Помилка парсингу даних з webhookdata: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
