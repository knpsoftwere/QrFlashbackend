package org.qrflash.Controller.MonobankPayment;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.qrflash.Service.DataBase.DataBaseService;
import org.qrflash.Service.Payment.MonobankPaymentService;
import org.qrflash.Service.Payment.WebhookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

@RestController
@RequestMapping("/api/acquiring")

public class WebhookController {
    private WebhookService webhookService;

    @Autowired
    private DataBaseService dataBaseService;
    @Autowired
    private MonobankPaymentService monobankPaymentService;

    @Autowired
    public WebhookController(WebhookService webhookService) {
        this.webhookService = webhookService;
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String webhookData,
            @RequestHeader("X-Sign") String xSignBase64) {
        try {
            // Парсимо webhookData, щоб отримати reference
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(webhookData);
            String reference = rootNode.path("reference").asText();

            if (reference == null || reference.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("handleWebhook: Reference відсутня в відповіді сервера");
            }

            // Отримуємо publicKey з бази даних
            String publicKeyBase64 = monobankPaymentService.getPublicKey(reference);

            if (publicKeyBase64 == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("handleWebhook: публічний ключ не знайдений в відповіді з сервера");
            }

            // Конвертуємо Base64 в PublicKey
            PublicKey publicKey = decodePublicKey(publicKeyBase64);

            // Перевіряємо підпис
            boolean valid = verifySignature(webhookData, xSignBase64, publicKey);
            if (!valid) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("handleWebhook: Не вірний підпис");
            }

            // Обробляємо payload
            webhookService.processWebhookData(webhookData);
            return ResponseEntity.ok("handleWebhook: Дані оновлені");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("handleWebhook: Помилка процесу " + e.getMessage());
        }
    }

    private PublicKey decodePublicKey(String publicKeyBase64) throws Exception {
        byte[] pemBytes = Base64.getDecoder().decode(publicKeyBase64);
        String pemString = new String(pemBytes, StandardCharsets.UTF_8);

        // Очищаємо ключ
        String cleanPem = pemString
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] derBytes = Base64.getDecoder().decode(cleanPem);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(derBytes);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return kf.generatePublic(keySpec);
    }

    private boolean verifySignature(String webhookData, String xSignBase64, PublicKey publicKey) throws Exception {
        byte[] signatureBytes = Base64.getDecoder().decode(xSignBase64);

        Signature verifier = Signature.getInstance("SHA256withECDSA");
        verifier.initVerify(publicKey);
        verifier.update(webhookData.getBytes(StandardCharsets.UTF_8));

        return verifier.verify(signatureBytes);
    }
}
