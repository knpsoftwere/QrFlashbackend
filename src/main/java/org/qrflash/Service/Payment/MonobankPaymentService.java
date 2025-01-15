package org.qrflash.Service.Payment;

import org.json.JSONObject;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import java.util.HashMap;
import java.util.Map;

@Service
public class MonobankPaymentService {
    private static final String MONOBANK_API_URL = "https://api.monobank.ua/api/merchant/invoice/create";
    private static final String API_KEY = "u5JFZqf1x_rLViME77bki5Rp9MZNa8VZwVMWIlCcItck";

    public JSONObject createInvoice(long totalAmount) {
        System.out.println("createInvoice: Start");
        try {
            // Створення запиту до API Monobank
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("amount", totalAmount); // Сума
            //requestBody.put("currency", currency); // Валюта
            //requestBody.put("order_items", orderItemsJson); // Список товарів у JSON

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Token", API_KEY);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Виконання запиту на сервер Monobank
            ResponseEntity<String> response = restTemplate.exchange(MONOBANK_API_URL, HttpMethod.POST, entity, String.class);

            // Обробка результату
            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                if (responseBody != null && !responseBody.isEmpty()) {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String invoiceId = jsonResponse.getString("invoiceId");
                    String pageUrl = jsonResponse.getString("pageUrl");
                    jsonResponse.put("invoiceId", invoiceId);
                    jsonResponse.put("pageUrl", pageUrl); // Ви можете повернути ці значення або сам об'єкт JSON
                    return jsonResponse;
                } else {
                    throw new RuntimeException("Response body is empty");
                }
            } else {
                throw new RuntimeException("Error from Monobank API: " + response.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null; // Повертає null, якщо сталася помилка
        }
    }
}
