package org.qrflash.Controller;

import lombok.RequiredArgsConstructor;
import org.qrflash.DTO.TableItem;
import org.qrflash.Service.DynamicDatabaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/client/")
@CrossOrigin(origins = "https://qrflash.online")
@RequiredArgsConstructor
public class TableController {

    @Autowired
    private DynamicDatabaseService dynamicDatabaseService;

    @GetMapping("/{est_uuid}")
    public ResponseEntity<?> getAllTables(@PathVariable("est_uuid") UUID establishmentUuid) {
        try {
            String databaseName = "est_" + establishmentUuid.toString().replace("-", "_");
            Map<String, Object> tables = dynamicDatabaseService.getAllTables(databaseName);
            return ResponseEntity.ok(tables);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PostMapping("/{est_uuid}")
    public ResponseEntity<?> createTableItem(
            @PathVariable("est_uuid") UUID establishmentUuid,
            @RequestBody Map<String, Object> tableData) {

        try {
            // Перевірка даних
            if (!tableData.containsKey("tableNumber") || !tableData.containsKey("qrCode") || !tableData.containsKey("createdAt")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing required fields: tableNumber, qrCode, or createdAt");
            }

            // Формуємо назву бази даних
            String databaseName = "est_" + establishmentUuid.toString().replace("-", "_");

            // Перевіряємо та створюємо таблицю, якщо її немає
            dynamicDatabaseService.ensureTableExists(databaseName);

            // Отримуємо дані з запиту
            int tableNumber = (int) tableData.get("tableNumber");
            String qrCode = (String) tableData.get("qrCode");
            Timestamp createdAt;
            try {
                createdAt = Timestamp.valueOf((String) tableData.get("createdAt"));
            } catch (IllegalArgumentException e) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid createdAt format. Expected format: yyyy-[m]m-[d]d hh:mm:ss[.f...]");
            }

            // Додаємо новий запис
            dynamicDatabaseService.createTableItem(databaseName, tableNumber, qrCode, createdAt);

            return ResponseEntity.status(HttpStatus.CREATED).body("Table created successfully");

        } catch (RuntimeException e) {
            if (e.getMessage().contains("Duplicate entry")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @PutMapping("/{est_uuid}/{id}")
    public ResponseEntity<?> updateTableItem(
            @PathVariable("est_uuid") UUID establishmentUuid,
            @PathVariable("id") Long id, // ID передається в запиті
            @RequestBody TableItem tableItem) {
        try {
            // Переконуємося, що передане `id` відповідає ID у об'єкті `TableItem`
            tableItem.setId(id);

            // Формуємо назву бази
            String databaseName = "est_" + establishmentUuid.toString().replace("-", "_");

            // Викликаємо метод оновлення
            dynamicDatabaseService.updateTableItem(databaseName, tableItem);

            return ResponseEntity.ok("Table updated successfully");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Table number already exists")) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            } else if (e.getMessage().contains("No table found with id")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    @DeleteMapping("/{est_uuid}/{id}")
    public ResponseEntity<?> deleteTableItem(
            @PathVariable("est_uuid") UUID establishmentUuid,
            @PathVariable("id") Long id) {
        try {
            String databaseName = "est_" + establishmentUuid.toString().replace("-", "_");
            dynamicDatabaseService.deleteTableItem(databaseName, id);
            return ResponseEntity.ok("Table deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

}
