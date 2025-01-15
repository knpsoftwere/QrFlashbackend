package org.qrflash.Controller;

import lombok.RequiredArgsConstructor;
import org.qrflash.DTO.Admin.CategoryDTO;
import org.qrflash.DTO.Admin.MenuDTO.MenuItemDTO;
import org.qrflash.DTO.TableItemDTO;
import org.qrflash.Entity.MenuItemEntity;
import org.qrflash.Entity.TagEntity;
import org.qrflash.Service.Admin.CategoryService;
import org.qrflash.Service.Admin.MenuItemsService;
import org.qrflash.Service.Admin.TagService;
import org.qrflash.Service.Client.ClientService;
import org.qrflash.Service.Client.ConfigService;
import org.qrflash.Service.DataBase.ClientDynamicDataBaseService;
import org.qrflash.Source.Multi_tenancy.TenantContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/client")
public class ClientTableController {

    private final ClientDynamicDataBaseService clientDynamicDataBaseService;
    private final ConfigService configService;
    private final ClientService clientService;
    private final TagService tagService;
    private final CategoryService categoryService;
    @GetMapping("/{est_uuid}/{qr_Code}")
    public ResponseEntity<?> getTableItems(
            @PathVariable("est_uuid") String establishmentUuid,
            @PathVariable("qr_Code") String qr_Code) {
        try {
            // Декодуємо qr_Code
            try {
                qr_Code = URLDecoder.decode(qr_Code, StandardCharsets.UTF_8.name());
            } catch (Exception e) {
                throw new IllegalArgumentException("CTC getTableItems? Помилка декодування: " + qr_Code);
            }

            // Формуємо назву бази даних
            String databaseName = "est_" + establishmentUuid.toString().replace("-", "_");

            // Перевірка столика
            TableItemDTO tableItemDTO = clientDynamicDataBaseService.getTableItemByQrCode(databaseName, qr_Code);
            if (tableItemDTO == null || !tableItemDTO.is_Active()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("CTC: getTableItem? Столи не знайдені або немає активних.");
            }

            // Отримуємо конфігурацію
            Map<String, Object> config = clientDynamicDataBaseService.getConfig(databaseName);

            // Витягаємо активну схему
            Map<String, Object> activeColorScheme = getActiveColorScheme(config);

            // Отримуємо opening_hours без поля "checkout"
            List<Map<String, Object>> openingHours = configService.getOpeningHours(databaseName);
            List<Map<String, Object>> filteredOpeningHours = filterCheckoutField(openingHours);

            // Формуємо відповідь
            Map<String, Object> response = new HashMap<>();
            response.put("table", tableItemDTO);
            response.put("establishment_properties", config.get("establishment_properties"));
            response.put("active_color_scheme", activeColorScheme);
            response.put("opening_hours", filteredOpeningHours);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    private Map<String, Object> getActiveColorScheme(Map<String, Object> config) {
        Map<String, Object> colorSchemes = (Map<String, Object>) config.get("color_schemes");
        String activeSchemeName = (String) colorSchemes.get("active_scheme_name");
        List<Map<String, Object>> schemes = (List<Map<String, Object>>) colorSchemes.get("schemes");

        return schemes.stream()
                .filter(scheme -> activeSchemeName.equals(scheme.get("name")))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Активна схема не знайдена."));
    }
    private List<Map<String, Object>> filterCheckoutField(List<Map<String, Object>> openingHours) {
        return openingHours.stream()
                .map(day -> {
                    day.remove("checkout"); // Видаляємо поле "checkout"
                    return day;
                })
                .collect(Collectors.toList());
    }

    @GetMapping("/menu")
    public ResponseEntity<?> getActiveMenuItems(@RequestParam("est_uuid") UUID establishmentId) {
        try {
            String dbName = AdminController.formatedUUid(establishmentId);

            // Викликаємо новий метод, який повертає тільки активні товари
            List<MenuItemDTO> menuItems = clientService.getActiveMenuItems(dbName);
            // Якщо треба – тегів і категорій беремо все так само
            List<TagEntity> tagEntity = tagService.getTags(dbName);
            List<CategoryDTO> categoryDTO = categoryService.getCategoriesUsedByActiveMenuItems(dbName);
            Map<String, Object> config = clientDynamicDataBaseService.getConfig(dbName);
            Map<String, Object> activeColorScheme = getActiveColorScheme(config);

            // Складаємо разом у Map (аналогічно до попереднього прикладу)
            Map<String, Object> response = Map.of(
                    "menu_items", menuItems,
                    "tags", tagEntity,
                    "categories", categoryDTO,
                    "active_color_scheme",activeColorScheme
            );

            return ResponseEntity.ok(Map.of("data", response));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch menu items: " + e.getMessage()));
        } finally {
            TenantContext.clear();
        }
    }

}
