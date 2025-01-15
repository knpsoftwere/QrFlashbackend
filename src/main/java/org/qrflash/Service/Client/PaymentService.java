package org.qrflash.Service.Client;

import org.qrflash.DTO.Client.PaymentRequest;
import org.qrflash.Service.DataBase.DataBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private DataBaseService dataBaseService;  // Сервіс для роботи з базою даних

    public void createPayment(Long orderId, PaymentRequest paymentRequest) {
        // Обчислюємо суму і валюту
        double amount = paymentRequest.getAmount();
        String currency = paymentRequest.getCurrency();

        // Генеруємо інвойс (наприклад, через API Monobank)
        String invoiceId = UUID.randomUUID().toString();
        String paymentUrl = "https://payment.url/..." + invoiceId; // URL для оплати

        // Створюємо SQL запит для вставки оплати в базу
        String sql = "INSERT INTO payments (order_id, invoice_id, payment_url, status, amount, currency) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = dataBaseService.getConnection("est_" + orderId);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, orderId);  // Вказуємо ID замовлення
            ps.setString(2, invoiceId); // Згенерований ID інвойсу
            ps.setString(3, paymentUrl); // URL для оплати
            ps.setString(4, "PENDING"); // Статус оплати
            ps.setDouble(5, amount);  // Сума оплати
            ps.setString(6, currency);  // Валюта оплати

            ps.executeUpdate();
            System.out.println("Payment successfully created.");
        } catch (SQLException e) {
            throw new RuntimeException("Error creating payment in database: ", e);
        }
    }
}
