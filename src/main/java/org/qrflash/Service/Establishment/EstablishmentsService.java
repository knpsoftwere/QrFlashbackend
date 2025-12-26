package org.qrflash.Service.Establishment;

import lombok.RequiredArgsConstructor;
import org.qrflash.Entity.EstablishmentsEntity;
import org.qrflash.Entity.UserEntity;
import org.qrflash.Repository.EstablishmentsRepository;
import org.qrflash.Repository.UserRepository;
import org.qrflash.Service.DataBase.DataBaseService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EstablishmentsService {
    private final EstablishmentsRepository establishmentsRepository;
    private final UserRepository userRepository;
    private final DataBaseService dataBaseService;

    public EstablishmentsEntity addAdminToEstablishment(UUID establishmentUuid, Long adminId) {
        // Знаходимо заклад
        EstablishmentsEntity establishment = establishmentsRepository.findById(establishmentUuid)
                .orElseThrow(() -> new RuntimeException("Заклад не знайдено"));

        // Знаходимо адміністратора
        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Користувач не знайдений"));

        // Перевіряємо, чи вже існує адміністратор у закладі
        if (!establishment.getAdmins().contains(admin)) {
            establishment.getAdmins().add(admin); // Додаємо адміністратора
        }
        // Зберігаємо заклад
        return establishmentsRepository.save(establishment);
    }

    public EstablishmentsEntity createEstablishmentForUser(Long adminId) {
        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Користувач не знайдений"));
        //Створюємо новий заклад
        EstablishmentsEntity establishment = new EstablishmentsEntity();
        establishment.setName("Заклад 1");
        establishment.setLanguage("ua");
        establishment.setCreatedAt(LocalDateTime.now());
        establishment.setStatus("active");

        //Додаємо адміна
        Set<UserEntity> admins = new HashSet<>();
        admins.add(admin);
        establishment.setAdmins(admins);

        //Зберігає заклад в основній базі
        establishment = establishmentsRepository.save(establishment);

        //Створюємо нову базу даних користувача
        String databaseName = "est_" + establishment.getUuid().toString().replace("-", "_");

        //Створюємо базу даних і таблиці
        dataBaseService.createDatabase(databaseName);
        dataBaseService.createCategoriesTable(databaseName);
        dataBaseService.createMenuItemTable(databaseName);
        dataBaseService.createOpeningHoursTable(databaseName);
        dataBaseService.createTagsTable(databaseName);
        dataBaseService.createMenuItemTagsTable(databaseName);
        dataBaseService.createOrdersTable(databaseName);
        dataBaseService.createPaymentsTable(databaseName);
        dataBaseService.createMethod_payment(databaseName);
        //dataBaseService.createCash_register(databaseName);
        //dataBaseService.createcash_register_method_payment(databaseName);


        dataBaseService.createConfigTable(databaseName);
        dataBaseService.insertDefaultConfigData(databaseName);
        dataBaseService.initializeOpeningHours(databaseName);
        dataBaseService.insertDefaultTags(databaseName);
        dataBaseService.insertDefaultCategories(databaseName);
        dataBaseService.insertDefaultMenuItems(databaseName);
        dataBaseService.insertDefaultPaymentAndCash_register(databaseName);


        return establishment;
    }

    public String getEstablishmentUuidForUser(String phoneNumber) {
        UserEntity user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new RuntimeException("Користувач не знайдений"));

        EstablishmentsEntity establishment = establishmentsRepository.findByAdminId(user.getId())
                .orElseThrow(() -> new RuntimeException("Заклад не знайдено для користувача"));

        return establishment.getUuid().toString();
    }

}
