package org.qrflash.Service.Payment;

import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.qrflash.Service.DataBase.DataBaseService;
import org.qrflash.properties.CustomServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MonobankPaymentService {
    private final CustomServerProperties cSP;
    private static final String MONOBANK_API_URL_POST_CREATE_TRANSACTION = "https://api.monobank.ua/api/merchant/invoice/create";
    private static final String MONOBANK_API_URL_GET_PUB_KEY = "https://api.monobank.ua/api/merchant/pubkey";
    //https://api.monobank.ua/docs/acquiring.html#/paths/~1api~1merchant~1invoice~1create/post - MonobankAPi

    @Autowired
    private DataBaseService dataBaseService;

    //Створюємо запит на API monobank для створення платежу
    public JSONObject createInvoice(long totalAmount, String databaseName, String qrCode) {
        String API_KEY = getToken(databaseName);
        try {
            HashMap merchantPaymInfo = new HashMap();
            merchantPaymInfo.put("reference", databaseName); // Ідентифікатор бази даних
            merchantPaymInfo.put("destination", "Призначення платежу");
            //merchantPaymInfo.put("comment", databaseName);

            //todo
            String redirectUrl = cSP.getIpServer() + "/client?est_uuid=" + databaseName.replace("est_", "").replace("_", "-") + "&table=" + qrCode.toLowerCase();
            // Створення запиту до API Monobank
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("amount", totalAmount); // Сума
            requestBody.put("ccy", 980);
            requestBody.put("merchantPaymInfo", merchantPaymInfo);
            requestBody.put("redirectUrl", redirectUrl);
            requestBody.put("webHookUrl", cSP.getIpServer() + "/api/acquiring/webhook");
            //requestBody.put("order_items", orderItemsJson); // Список товарів у JSON

            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Token", API_KEY);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            // Виконання запиту на сервер Monobank
            ResponseEntity<String> response = restTemplate.exchange(MONOBANK_API_URL_POST_CREATE_TRANSACTION, HttpMethod.POST, entity, String.class);

            // Обробка результату
            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                if (responseBody != null && !responseBody.isEmpty()) {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String invoiceId = jsonResponse.getString("invoiceId");
                    String pageUrl = jsonResponse.getString("pageUrl");
                    jsonResponse.put("invoiceId", invoiceId);
                    jsonResponse.put("pageUrl", pageUrl);
                    return jsonResponse;
                } else {
                    throw new RuntimeException("createInvoice: Пуста відповідь з сервера");
                }
            } else {
                throw new RuntimeException("createInvoice: Помилка з'єднання з Monobank " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("createInvoice: Не відома помилка: " + e.getMessage());
        }
    }

    public String createPublicKey(String token, String databaseName) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-Token", token);
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(MONOBANK_API_URL_GET_PUB_KEY, HttpMethod.GET, entity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                String responseBody = response.getBody();
                if (responseBody != null && !responseBody.isEmpty()) {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    System.out.println("Key: " + jsonResponse.getString("key"));
                    addTokenAndPublicKey(token, databaseName, jsonResponse.getString("key"));

                    return jsonResponse.getString("key"); // Повертає тільки значення "key"
                } else {
                    throw new RuntimeException("createPublicKey: Відповідь з сервера пуста");
                }
            } else {
                throw new RuntimeException("createPublicKey: Помилка зчитування публічного ключа: " + response.getStatusCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("createPublicKey: Невідома помилка зчитування ключа");
        }
    }

    public void addTokenAndPublicKey(String token, String databaseName, String publicKey) {
        String sql = "UPDATE method_payment " +
                "SET json_setting = jsonb_set(" +
                "jsonb_set(COALESCE(json_setting, '{}'::jsonb), '{token}', to_jsonb(?::text)), " +
                "'{publicKey}', to_jsonb(?::text)) " +
                "WHERE id = ?";

        try (Connection connection = dataBaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            // Встановлюємо значення параметрів
            ps.setString(1, token);
            ps.setString(2, publicKey);
            ps.setLong(3, 1);

            // Виконуємо оновлення
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected > 0) {
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка підключення до бази: ", e);
        }
    }

    private String getToken(String databaseName) {
        String sql = "SELECT json_setting ->> 'token' AS token FROM method_payment WHERE id = ?";
        try (Connection connection = dataBaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, 1);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("token");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Помилка зчитування токену: ", e);
        }
        return null;
    }

    public String getPublicKey(String databaseName) {
        String sql = "SELECT json_setting ->> 'publicKey' AS publicKey FROM method_payment WHERE id = ?";
        try (Connection connection = dataBaseService.getConnection(databaseName);
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setLong(1, 1);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("publicKey");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("getPublicKey: Помилка зчитування публічного ключу: ", e);
        }
        return null;
    }

}
