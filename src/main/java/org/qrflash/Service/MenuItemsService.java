package org.qrflash.Service;

import lombok.RequiredArgsConstructor;
import org.qrflash.Entity.EstablishmentsEntity;
import org.qrflash.Entity.MenuItemEntity;
import org.qrflash.Entity.UserEntity;
import org.qrflash.JWT.JwtUtil;
import org.qrflash.Repository.EstablishmentsRepository;
import org.qrflash.Repository.MenuItemRepository;
import org.qrflash.Repository.UserRepository;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MenuItemsService {
    private final JwtUtil jwtUtil;
    private final EstablishmentsRepository establishmentsRepository;
    private final UserRepository usersRepository; // Репозиторій для доступу до таблиці `users`
    private final DynamicDatabaseService dynamicDatabaseService;
    private final MenuItemRepository menuItemRepository;


    public List<MenuItemEntity> getMenuItems(UUID establishmentId, String token) {
        System.out.println("==============================================");
        System.out.println("Token: " + token);

        // Перевіряємо токен
        if (!jwtUtil.isValidToken(token)) {
            throw new RuntimeException("Не валідний або протермінований токен");
        }

        // Отримуємо номер телефону з токену
        String phoneNumber = jwtUtil.getUserPhoneNumber(token);
        System.out.println("Phone from token: " + phoneNumber);

        // Витягуємо ID користувача, використовуючи phoneNumber
        Long userId = usersRepository.findByPhoneNumber(phoneNumber)
                .map(UserEntity::getId)
                .orElseThrow(() -> new RuntimeException("Користувач не знайдений за номером телефону"));
        System.out.println("User ID from phone: " + userId);

        // (Без перевірки доступу на даний момент)

        // Перемикаємося на базу даних закладу
        String databaseName = "est_" + establishmentId.toString().replace("-", "_");

        // Лог для перевірки правильності назви бази даних
        System.out.println("Switching to database: " + databaseName);

        // Повертаємо список меню
        List<MenuItemEntity> menuItems = dynamicDatabaseService.getMenuItems(databaseName);

        // Лог для перевірки отриманого меню
        System.out.println("Menu items retrieved: " + menuItems);

        return menuItems;
    }

    public void createMenuItem(UUID establishmentId, MenuItemEntity menuItemEntity) {
        System.out.println("est_" + establishmentId + " Отримали з запиту! =============");
        String databaseName = "est_" + establishmentId.toString().replace("-", "_");
        System.out.println("Using database: " + databaseName);

        dynamicDatabaseService.saveMenuItem(databaseName, menuItemEntity);
        System.out.println("Menu item added to table in database: " + databaseName);
    }

}
