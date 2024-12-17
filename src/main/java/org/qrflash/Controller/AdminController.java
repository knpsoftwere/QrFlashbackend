package org.qrflash.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.qrflash.DTO.AppError;
import org.qrflash.Entity.MenuItemEntity;
import org.qrflash.Service.Client.ConfigService;
import org.qrflash.Service.MenuItemsService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin(origins = "https://qrflash.online")
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {
    private final MenuItemsService menuItemsService;
    private final ConfigService configService;

    private UUID formatUUID(String rawUuid) {
        return UUID.fromString(rawUuid.replace("_", "-"));
    }

    // ---------------------------
    // Menu
    // ---------------------------
    @GetMapping("/menu")
    public ResponseEntity<?> getMenuItems(@RequestParam("est_uuid") String establishmentId,
                                          @RequestHeader("Authorization") String token) {
        try {
            UUID formattedUuid = formatUUID(establishmentId);
            System.out.println(formattedUuid);
            // Видаляємо префікс "Bearer " із токена
            token = token.replace("Bearer ", "");

            // Отримуємо список меню з сервісу
            List<MenuItemEntity> menuItems = menuItemsService.getMenuItems(formattedUuid, token);

            //Обгортка JSON для зручності парсування
            Map<String, Object> response = new HashMap<>();
            response.put("menu_items", menuItems);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new AppError(HttpStatus.UNAUTHORIZED.value(), e.getMessage())
            );
        }
    }

    @PostMapping("/menu/items")
    public ResponseEntity<?> createMenuItem(@RequestParam("est_uuid") UUID establishmentId,
                                         @RequestBody MenuItemEntity menuItemEntity,
                                         @RequestHeader("Authorization") String token) {
        menuItemsService.createMenuItem(establishmentId, menuItemEntity);
        return ResponseEntity.ok("Menu item created");
    }

    @DeleteMapping("/menu/items/{id}")
    public ResponseEntity<?> deleteMenuItem(
            @PathVariable Long id,
            @RequestParam("est_uuid") UUID establishmentId,
            @RequestHeader("Authorization") String token) {
        menuItemsService.deleteMenuItem(establishmentId, id, token);

        return ResponseEntity.ok("Menu item deleted successfully");
    }

    @PutMapping("/menu/items/{id}")
    public ResponseEntity<?> updateMenuItem(
            @PathVariable Long id,
            @RequestParam("est_uuid") UUID establishmentId,
            @RequestBody MenuItemEntity menuItemEntity,
            @RequestHeader("Authorization") String token) {
        try {
            // Видаляємо префікс "Bearer " із токену
            token = token.replace("Bearer ", "");

            // Викликаємо сервіс для оновлення
            menuItemEntity.setId(id); // <--- Присвоюємо отримане `id` до об'єкта `menuItemEntity`
            menuItemsService.updateMenuItem(establishmentId, menuItemEntity, token);
            return ResponseEntity.ok("Menu item updated successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AppError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    // ---------------------------
    // Establishment Properties
    // ---------------------------
    @GetMapping("/establishment/{est_uuid}/properties")
    public ResponseEntity<?> getEstablishmentProperties(@PathVariable("est_uuid") UUID establishmentId) {
        String databaseName = "est_" + establishmentId.toString().replace("-", "_");
        Map<String, Object> props = configService.getEstablishmentProperties(databaseName);
        return ResponseEntity.ok(Map.of("properties", props));
    }

    @PutMapping("/establishment/{est_uuid}/properties")
    public ResponseEntity<?> updateEstablishmentProperties(@PathVariable("est_uuid") UUID establishmentId,
                                                           @RequestBody Map<String, String> payload) {
        String databaseName = "est_" + establishmentId.toString().replace("-", "_");
        String name = payload.get("name");
        String address = payload.get("address");
        if (name == null && address == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "At least one field ('name' or 'address') must be provided"));
        }
        configService.updateEstablishmentProperties(databaseName, name, address);
        return ResponseEntity.ok(Map.of("message", "Інформація по закладу успішно оновлена."));
    }

    @PostMapping("/establishment/{est_uuid}/properties/contact-info")
    public ResponseEntity<?> addContactInfo(@PathVariable("est_uuid") UUID establishmentId,
                                            @RequestBody Map<String, List<String>> payload) {
        String databaseName = "est_" + establishmentId.toString().replace("-", "_");
        List<String> numbers = payload.get("numbers");
        if (numbers == null || numbers.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Numbers array cannot be empty"));
        }
        configService.addContactInfo(databaseName, numbers);
        return ResponseEntity.ok(Map.of("message", "Номера додані"));
    }

    @DeleteMapping("/establishment/{est_uuid}/properties/contact-info/{index}")
    public ResponseEntity<?> removeContactInfo(@PathVariable("est_uuid") UUID establishmentId,
                                               @PathVariable int index) {
        String databaseName = "est_" + establishmentId.toString().replace("-", "_");
        configService.removeContactInfoAtIndex(databaseName, index);
        return ResponseEntity.ok(Map.of("message", "Контактний номер видалено"));
    }

    // ---------------------------
    // Color Schemes
    // ---------------------------

    @GetMapping("/establishment/{est_uuid}/color-schemes")
    public ResponseEntity<?> getColorSchemes(@PathVariable("est_uuid") UUID establishmentId) {
        String databaseName = "est_" + establishmentId.toString().replace("-", "_");
        Map<String, Object> schemes = configService.getColorSchemes(databaseName);
        return ResponseEntity.ok(Map.of("schemes", schemes));
    }

    @PutMapping("/establishment/{est_uuid}/color-schemes/active/{index}")
    public ResponseEntity<?> updateActiveColorScheme(@PathVariable("est_uuid") UUID establishmentId,
                                                     @PathVariable int index) {
        String databaseName = "est_" + establishmentId.toString().replace("-", "_");
        configService.updateActiveColorSchemeByIndex(databaseName, index);
        return ResponseEntity.ok(Map.of("message", "Активна схема оновлена"));
    }

    @PutMapping("/establishment/{est_uuid}/color-schemes/{index}")
    public ResponseEntity<?> updateColorScheme(
            @PathVariable("est_uuid") UUID establishmentId,
            @PathVariable("index") int index,
            @RequestBody Map<String, Object> updatedFields) {
        String databaseName = "est_" + establishmentId.toString().replace("-", "_");

        try {
            configService.updateColorSchemeAtIndex(databaseName, index, updatedFields);
            return ResponseEntity.ok(Map.of("message", "Схема успішно оновлена"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/establishment/{est_uuid}/color-schemes")
    public ResponseEntity<?> addColorScheme(@PathVariable("est_uuid") UUID establishmentId,
                                            @RequestBody Map<String, Object> newScheme) {
        String databaseName = "est_" + establishmentId.toString().replace("-", "_");
        ObjectMapper mapper = new ObjectMapper();
        String newSchemeJson;
        try {
            newSchemeJson = mapper.writeValueAsString(newScheme);
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Незнайдена JSON схема"));
        }
        configService.addColorScheme(databaseName, newSchemeJson);
        return ResponseEntity.ok(Map.of("message", "Схема успішно додана"));
    }

    @DeleteMapping("/establishment/{est_uuid}/color-schemes/{index}")
    public ResponseEntity<?> removeColorScheme(@PathVariable("est_uuid") UUID establishmentId,
                                               @PathVariable int index) {
        String databaseName = "est_" + establishmentId.toString().replace("-", "_");
        configService.removeColorSchemeAtIndex(databaseName, index);
        return ResponseEntity.ok(Map.of("message", "Схема успішно видалена"));
    }

    // ---------------------------
    // Opening Hours
    // ---------------------------

    @GetMapping("/establishment/{est_uuid}/opening-hours")
    public ResponseEntity<?> getOpeningHours(@PathVariable("est_uuid") UUID establishmentId) {
        String databaseName = "est_" + establishmentId.toString().replace("-", "_");
        List<Map<String, Object>> hours = configService.getOpeningHours(databaseName);
        return ResponseEntity.ok(Map.of("opening_hours", hours));
    }

    @PutMapping("/establishment/{est_uuid}/opening-hours/partial")
    public ResponseEntity<?> updateOpeningHoursPartial(
            @PathVariable("est_uuid") UUID establishmentId,
            @RequestBody Map<String, Map<String, Object>> updates) {
        String databaseName = "est_" + establishmentId.toString().replace("-", "_");
        for (Map.Entry<String, Map<String, Object>> entry : updates.entrySet()) {
            String day = entry.getKey(); // Наприклад: "Monday"
            Map<String, Object> dayUpdates = entry.getValue(); // Значення для оновлення
            configService.updateOpeningHoursPartial(databaseName, day, dayUpdates);
        }
        return ResponseEntity.ok(Map.of("message", "Робочі години оновлено"));
    }
}
