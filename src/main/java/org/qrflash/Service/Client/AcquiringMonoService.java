package org.qrflash.Service.Client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.qrflash.DTO.Client.OrderItem;
import org.qrflash.DTO.Client.OrderRequest;
import org.qrflash.Service.DataBase.DataBaseService;
import org.qrflash.Service.Payment.MonobankPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Service
public class AcquiringMonoService {
    @Autowired
    private DataBaseService dataBaseService; // Сервіс для роботи з базою даних
    @Autowired
    private MonobankPaymentService monobankPaymentService;  //Сервіс для створення запиту на monobank

    public String createOrder(String databaseName, OrderRequest orderRequest) throws JsonProcessingException {
        // Створюємо запит на вставку замовлення в базу
        String sql = "INSERT INTO orders (currency, order_items, total_amount) VALUES (?, ?::jsonb, ?)";

        // Перетворюємо список товарів в JSON
        ObjectMapper objectMapper = new ObjectMapper();
        String orderItemsJson = objectMapper.writeValueAsString(orderRequest.getItems());

        try (Connection connection = dataBaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            // Валюта
            ps.setString(1, "UAH");  // Можна зробити динамічним, якщо потрібно підтримувати інші валюти
            // Список товарів у JSON
            ps.setString(2, orderItemsJson);
            // Загальна сума
            ps.setDouble(3, orderRequest.getTotalAmount());

            ps.executeUpdate();
            System.out.println("Замовлення створено");

            // Створюємо рахунок на Monobank
            if (orderRequest != null && orderRequest.getTotalAmount() != null) {
                long totalAmount = (long) (orderRequest.getTotalAmount() * 100);
                //monobankPaymentService.createInvoice(totalAmount);
                JSONObject jsonObject = monobankPaymentService.createInvoice(totalAmount);
                String invoiceId = jsonObject.getString("invoiceId");
                String pageUrl = jsonObject.getString("pageUrl");
                return pageUrl;
            } else {
                // Виводимо повідомлення про помилку або налаштовуємо значення за замовчуванням
                System.out.println("Total amount is missing or orderRequest is null");
                return null;
            }
//            String invoiceId = jsonObject.getString("invoiceId");
//            String pageUrl = jsonObject.getString("pageUrl");

            // Повертаємо pageUrl клієнту
            // Можна також зберегти invoiceId в базі, якщо потрібно
            //return pageUrl;
        } catch (SQLException e) {
            throw new RuntimeException("Помилка створення замовлення: " + databaseName, e);
        }
    }
}