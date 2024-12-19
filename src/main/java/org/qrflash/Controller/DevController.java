package org.qrflash.Controller;

import org.qrflash.Service.DataBase.DataBaseService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/dev")
public class DevController {

    private final DataBaseService dataBaseService;

    public DevController(DataBaseService dataBaseService) {
        this.dataBaseService = dataBaseService;
    }

    @GetMapping("/recreate-database")
    public ResponseEntity<?> recreateDatabase(@RequestParam("db_name") UUID establishmentId) {
        try {
            String databaseName = "est_" + establishmentId.toString().replace("-", "_");
            dataBaseService.recreateDatabase(databaseName);
            return ResponseEntity.ok(Map.of("message", "База даних " + databaseName + " успішно оновлена"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Помилка оновлення бази даних", "details", e.getMessage()));
        }
    }
}
