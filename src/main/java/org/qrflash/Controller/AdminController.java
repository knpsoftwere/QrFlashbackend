package org.qrflash.Controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.qrflash.DTO.Admin.CategoryDTO;
import org.qrflash.DTO.Admin.MenuDTO.MenuItemCreateDTO;
import org.qrflash.DTO.Admin.MenuDTO.MenuItemDTO;
import org.qrflash.DTO.Admin.MenuDTO.MenuItemUpdateDTO;
import org.qrflash.DTO.AppError;
import org.qrflash.Entity.CategoryEntity;
import org.qrflash.Entity.MenuItemEntity;
import org.qrflash.Entity.TagEntity;
import org.qrflash.Exeption.DuplicateTagException;
import org.qrflash.JWT.JwtUtil;
import org.qrflash.Service.Admin.CategoryService;
import org.qrflash.Service.Admin.PaymentService;
import org.qrflash.Service.Admin.TagService;
import org.qrflash.Service.Client.ConfigService;
import org.qrflash.Service.Admin.MenuItemsService;
import org.qrflash.Source.Multi_tenancy.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.*;

import java.sql.SQLException;
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
    private final TagService tagService;
    private final CategoryService categoryService;
    private final JwtUtil jwtUtil;
    private final PaymentService paymentService;

    public static String formatedUUid(UUID establishmentId){
        return "est_" + establishmentId.toString().replace("-", "_");
    }

    // -----------Menu-----------
    @GetMapping("/menu/items")
    public ResponseEntity<?> getMenuItems(@RequestParam("est_uuid") UUID establishmentId) {
        try {
            List<MenuItemDTO> menuItems = menuItemsService.getMenuItems(formatedUUid(establishmentId));
            List<TagEntity> tagEntity = tagService.getTags(formatedUUid(establishmentId));
            List<CategoryDTO> categoryDTO = categoryService.getAllCategories(formatedUUid(establishmentId));
            Map<String, Object> response = Map.of(
                    "menu_items", menuItems,
                    "tags", tagEntity,
                    "categories", categoryDTO
            );
    

            return ResponseEntity.ok(Map.of("data", response));
        } catch (SQLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to fetch menu items: " + e.getMessage()));
        } finally {
            TenantContext.clear();
        }
    }
    @PostMapping("/menu/additems")
    public ResponseEntity<?> createMenuItem(
            @RequestParam("est_uuid") UUID establishmentId,
            @RequestBody MenuItemCreateDTO menuItemCreateDTO) {
        try {
            String databaseName = formatedUUid(establishmentId);
            menuItemsService.addMenuItem(databaseName, menuItemCreateDTO);

            return ResponseEntity.ok(Map.of("message", "Товар успішно створено"));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/menu/updateitem")
    public ResponseEntity<?> updateMenuItem(
            @RequestParam("est_uuid") UUID establishmentId,
            @RequestParam("menuId") Long menuId,
            @RequestBody MenuItemUpdateDTO menuItemUpdateDTO) {
        try {
            menuItemsService.updateMenuItem(formatedUUid(establishmentId), menuId, menuItemUpdateDTO);
            return ResponseEntity.ok("Меню оновлено");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AppError(HttpStatus.BAD_REQUEST.value(), e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AppError(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.getMessage()));
        }
    }

    @DeleteMapping("/menu/deleteitem")
    public ResponseEntity<?> deleteMenuItem(
            @RequestParam("est_uuid") UUID establishmentId,
            @RequestParam("menuId") Long menuId) {
        try {
                menuItemsService.deleteMenuItem(formatedUUid(establishmentId), menuId);
            return ResponseEntity.ok("Меню успішно видалено!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AppError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Помилка видалення меню: " + e.getMessage()));
        }
    }
    // ---------------------------

    // ----------Establishment Properties-------
    @GetMapping("/establishment/{est_uuid}/properties")
    public ResponseEntity<?> getEstablishmentProperties(@PathVariable("est_uuid") UUID establishmentId) {
        String databaseName = "est_" + establishmentId.toString().replace("-", "_");
        Map<String, Object> props = configService.getEstablishmentProperties(databaseName);
        return ResponseEntity.ok(Map.of("properties", props));
    }

    @PutMapping("/establishment/{est_uuid}/properties")
    public ResponseEntity<?> updateEstablishmentProperties(
            @PathVariable("est_uuid") UUID establishmentId,
            @RequestBody Map<String, Object> payload) {

        String name = (String) payload.get("name");
        String address = (String) payload.get("address");
        String description = (String) payload.get("description");
        List<String> contactInfo = (List<String>) payload.get("contact_info");

        if (name == null && address == null && description == null && contactInfo == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "At least one field ('name', 'address', 'description', 'contact_info') must be provided"));
        }

        try {
                configService.updateEstablishmentProperties(formatedUUid(establishmentId), name, address, description, contactInfo);
            return ResponseEntity.ok(Map.of("message", "Інформація по закладу успішно оновлена."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Помилка оновлення закладу: " + e.getMessage()));
        }
    }
    // ---------------------------

    // ----------Color Schemes--------
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

    // ---------Opening Hours------------
    @GetMapping("/establishment/{est_uuid}/opening-hours")
    public ResponseEntity<?> getOpeningHours(@PathVariable("est_uuid") UUID establishmentId) {
        String databaseName = "est_" + establishmentId.toString().replace("-", "_");

        List<Map<String, Object>> openingHours = configService.getOpeningHours(databaseName);

        return ResponseEntity.ok(Map.of("opening_hours", openingHours));
    }

    @PutMapping("/establishment/{est_uuid}/opening-hours/partial")
    public ResponseEntity<?> updatePartialOpeningHours(
            @PathVariable("est_uuid") UUID establishmentId,
            @RequestBody Map<String, Map<String, Object>> request) {

        String databaseName = "est_" + establishmentId.toString().replace("-", "_");
        ObjectMapper objectMapper = new ObjectMapper();

        for (Map.Entry<String, Map<String, Object>> entry : request.entrySet()) {
            String day = entry.getKey();
            Map<String, Object> updates = entry.getValue();

            try {
                // Ініціалізація параметрів як null
                String workHoursJson = null;
                String breaksJson = null;
                Boolean checkout = null;
                String status = null;

                // Оновлення лише переданих полів
                if (updates.containsKey("work_hours")) {
                    workHoursJson = objectMapper.writeValueAsString(updates.get("work_hours"));
                }
                if (updates.containsKey("breaks")) {
                    breaksJson = objectMapper.writeValueAsString(updates.get("breaks"));
                }
                if (updates.containsKey("checkout")) {
                    checkout = (Boolean) updates.get("checkout");
                }
                if (updates.containsKey("status")) {
                    status = (String) updates.get("status");
                }
                // Викликаємо сервіс для оновлення
                configService.updatePartialOpeningHours(databaseName, day, workHoursJson, breaksJson, checkout, status);

            } catch (JsonProcessingException e) {
                throw new RuntimeException("Помилка обробки JSON для дня: " + day, e);
            }
        }
        return ResponseEntity.ok(Map.of("message", "Дані оновлено успішно"));
    }
    // ---------------------------

    // ---------Tags in menu------------
    @PostMapping("/menu/tagsitems/add")
    public ResponseEntity<?> addTagToMenuItem(
            @RequestParam("est_uuid") UUID establishmentId,
            @RequestParam("menuItemId") Long menuItemId,
            @RequestParam("tagId") Long tagId){
        try {
            menuItemsService.addTagToMenuItem(formatedUUid(establishmentId), menuItemId, tagId);
            return ResponseEntity.ok("Тег успішно доданий до товару");
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AppError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Помилка додавання тегу: " + e.getMessage()));
        }
    }

    @DeleteMapping("/menu/tagsitems/del")
    public ResponseEntity<?> removeTagFromMenuItem(
            @RequestParam("est_uuid") UUID establishmentId,
            @RequestParam("menuItemId") Long menuItemId,
            @RequestParam("tagId") Long tagId) {
        try {
            menuItemsService.removeTagFromMenuItem(formatedUUid(establishmentId), menuItemId, tagId);
            return ResponseEntity.ok("Тег успішно видалено");
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new AppError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Помилка видаленні тегу: " + e.getMessage()));
        }
    }
    // ---------------------------------

    // ----------Tags----------
    @GetMapping("/menu/tags")
    public ResponseEntity<?> getTags(@RequestParam("est_uuid") UUID establishmentId) {
        String databaseName = "est_" + establishmentId.toString().replace("-", "_");
        List<TagEntity> tags = tagService.getTags(databaseName);
        return ResponseEntity.ok(Map.of("tags", tags));
    }

    @PostMapping("/menu/tags/add")
    public ResponseEntity<?> createTags(
            @RequestBody TagEntity tagEntity,
            @RequestParam("est_uuid") UUID establishmentId) {
        try {
            tagService.addTag(formatedUUid(establishmentId), tagEntity);
            return ResponseEntity.ok("Тег успішно додано");
            }catch (DuplicateTagException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("createTag: Невідома помилка: " + e.getMessage());
        }
    }

    @PutMapping("/menu/tags/add")
    public ResponseEntity<?> updateTags(
            @RequestParam("est_uuid") UUID establishmentId,
            @RequestParam("tagId") Long tagId,
            @RequestBody TagEntity tagEntity){
        try{
            tagService.updateTag(formatedUUid(establishmentId), tagId, tagEntity);
            return ResponseEntity.ok("Тег успішно оновлено");
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("updateTags: " + e.getMessage());
        }
    }

    @DeleteMapping("/menu/tags/del")
    public ResponseEntity<?> removeTags(
            @RequestParam("est_uuid") UUID establishmentId,
            @RequestParam("tagId") Long tagId){
        try{
            tagService.deleteTag(formatedUUid(establishmentId), tagId);
            return ResponseEntity.ok("Тег успішно видалено");
        }catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("removeTags: " + e.getMessage());
        }
    }
    // ---------------------------

    // -------------Categories----------
    @GetMapping("/menu/categories")
    public List<CategoryDTO> getAllCategories(@RequestParam("est_uuid") UUID establishmentId) {
        return categoryService.getAllCategories(formatedUUid(establishmentId));
    }

    @PostMapping("/menu/categories/add")
    public ResponseEntity<?> addCategory(
            @RequestParam("est_uuid") UUID establishmentId,
            @RequestBody CategoryEntity categoryEntity) {
        try {
            String result = categoryService.addCategory(formatedUUid(establishmentId), categoryEntity);
            if (result.contains("вже існує")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(result);
            }
            return ResponseEntity.ok(result);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("addCategory: " + e.getMessage());
        }
    }

    @PutMapping("/menu/categories/ref")
    public ResponseEntity<?> refCategory(@RequestParam("est_uuid") UUID establishmentId,
                                         @RequestParam("categoryId") Long categoryId,
                                         @RequestBody CategoryEntity categoryEntity) {
        try {
            categoryService.updateCategory(formatedUUid(establishmentId), categoryId, categoryEntity);
            return ResponseEntity.ok("Категорію успішно оновлено.");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("Не передано жодних полів для оновлення") ||
                    e.getMessage().contains("не знайдено")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Помилка: " + e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Помилка оновлення: " + e.getMessage());
        }
    }

    @DeleteMapping("/menu/categories/del")
    public ResponseEntity<?> removeCategory(@RequestParam("est_uuid") UUID establishmentId,
                                            @RequestParam("categoryId") Long categoryId) {
        try {
            categoryService.deleteCategory(formatedUUid(establishmentId), categoryId);
            return ResponseEntity.ok("Категорію успішно видалено.");
        } catch (RuntimeException e) {
            if (e.getMessage().contains("не знайдено")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Помилка: " + e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Помилка видалення: " + e.getMessage());
        }
    }
    // ----------------------------------

    @GetMapping("/personal")
    public ResponseEntity<?>getPersonalInfo(@RequestParam("est_uuid") UUID establishmentId,
                                            @RequestParam("token") String token){
        try{
            String phoneNumber = jwtUtil.getUserPhoneNumber(token);
            Map<String, Object> response = new HashMap<>();
            response.put("phoneNumber", phoneNumber);
            return ResponseEntity.ok(response);
        }catch (Exception e){
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Помилка отримання персональної інформації"));
        }
    }

    //Запити по оплаті
    @GetMapping("/payments")
    public ResponseEntity<?> getPayments(@RequestHeader("Uuid") UUID establishmentId){
        try{
            List<Map<String, Object>> paymentMethod = paymentService.getPaymentMethods(formatedUUid(establishmentId));
            return ResponseEntity.ok(Map.of("paymentMethod", paymentMethod));
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("cashregister:", e.getMessage()));
        }
    }

    @PostMapping("/payments")
    public ResponseEntity<?> refactorPayment(@RequestHeader("Uuid") UUID establishmentId,
                                             @RequestParam("id") Long id,
                                             @RequestParam("isActive") boolean iaActive){
        try {
            paymentService.refactorPaymentActive(formatedUUid(establishmentId), id, iaActive);
            return ResponseEntity.ok("Успішно змінено!");
        }catch (RuntimeException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Помилка змінення статусу активності");
        }
    }
}
