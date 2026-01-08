package org.qrflash.Service.Client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.qrflash.DTO.Client.OrderRequest;
import org.qrflash.Service.DataBase.DataBaseService;
import org.qrflash.Service.Payment.MonobankPaymentService;
import org.qrflash.properties.CustomServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.sql.*;


@Service
@RequiredArgsConstructor
public class AcquiringMonoService {
    private final DataBaseService dataBaseService; // Сервіс для роботи з базою даних
    private final MonobankPaymentService monobankPaymentService;  //Сервіс для створення запиту на monobank

    public String createOrder(String databaseName, OrderRequest orderRequest) throws JsonProcessingException {
        // Створюємо запит на вставку замовлення в базу
        String sql = "INSERT INTO orders (currency, order_items, total_amount) VALUES (?, ?::jsonb, ?)";

        // Перетворюємо список товарів в JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String orderItemsJson = objectMapper.writeValueAsString(orderRequest.getItems());

        try (Connection connection = dataBaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, "UAH");
            ps.setString(2, orderItemsJson);
            ps.setDouble(3, orderRequest.getTotalAmount());
            ps.executeUpdate();

            //Отримуємо Id замовлення
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) {
                long orderId = rs.getLong(1);
                //Створення рахунку банку
                long totalAmount = (long) (orderRequest.getTotalAmount() * 100);
                JSONObject jsonObject = monobankPaymentService.createInvoice(totalAmount, databaseName, orderRequest.getQrCode());
                String invoiceId = jsonObject.getString("invoiceId");
                String pageUrl = jsonObject.getString("pageUrl");

                //Зберігаємо дані в таблицю "payments"
                savePaymentDetails(invoiceId, pageUrl, orderId, orderRequest.getTotalAmount(), "UAH", databaseName);
                return pageUrl;
            } else {
                throw new SQLException("createOrder: Помилка збереження замовлення");
            }
        } catch (SQLException e) {
            throw new RuntimeException("createOrder: Помилка створення замовлення або під'єднання до бази: " + databaseName, e);
        }
    }

    private void savePaymentDetails(String invoiceId, String pageUrl, Long orderId, Double amount, String currency, String databaseName) {
        String sql = "INSERT INTO payments (order_id, invoice_id, payment_url, status, amount, current, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, NOW(), NOW())";

        try (Connection connection = dataBaseService.getConnection(databaseName); // Замініть на ім'я бази
             PreparedStatement ps = connection.prepareStatement(sql)) {

            // Вставка значень в запит
            ps.setLong(1, orderId); // order_id
            ps.setString(2, invoiceId); // invoice_id
            ps.setString(3, pageUrl); // payment_url
            ps.setString(4, "PENDING"); // статус (можна змінити за необхідності)
            ps.setDouble(5, amount); // amount
            ps.setString(6, currency); // current (валюта)

            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("savePaymentDetails: Помилка зберігання деталей: ", e);
        }
    }


}