package org.qrflash.Controller;

import lombok.RequiredArgsConstructor;
import org.qrflash.DTO.AppError;
import org.qrflash.Entity.MenuItemEntity;
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

    private UUID formatUUID(String rawUuid) {
        return UUID.fromString(rawUuid.replace("_", "-"));
    }

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
    public ResponseEntity<?> createMenuItem(@RequestParam UUID establishmentId,
                                         @RequestBody MenuItemEntity menuItemEntity,
                                         @RequestHeader("Authorization") String token) {
        menuItemsService.createMenuItem(establishmentId, menuItemEntity);
        return ResponseEntity.ok("Menu item created");
    }
}
