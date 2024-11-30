package org.qrflash.Service;

import lombok.RequiredArgsConstructor;
import org.qrflash.Entity.EstablishmentsEntity;
import org.qrflash.Entity.UserEntity;
import org.qrflash.Repository.EstablishmentsRepository;
import org.qrflash.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class EstablishmentsService {
    private final EstablishmentsRepository establishmentsRepository;
    private final UserRepository userRepository;
    private final DataBaseService dataBaseService;

    public EstablishmentsEntity addAdminToEstablishment(String establishmentUuid, Long adminId) {
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
        EstablishmentsEntity establishment = new EstablishmentsEntity();

        UserEntity admin = userRepository.findById(adminId)
                .orElseThrow(() -> new RuntimeException("Користувач не знайдений"));

        Set<UserEntity> admins = new HashSet<>();
        admins.add(admin);
        establishment.setAdmins(admins);

        establishment.setName("Заклад 1");
        establishment.setLanguage("ua");
        establishment.setCreated_at(LocalDateTime.now());
        establishment.setStatus("active");

        establishment = establishmentsRepository.save(establishment);


        String databaseName = "est_" + establishment.getUuid().toString().replace("-", "_");
        dataBaseService.createDatabase(databaseName);

        dataBaseService.createMenuItemTable(databaseName);
        //dataBaseService.setupDatabase(databaseName);

        return establishment;
    }



    public EstablishmentsEntity checkAndCreateEstablishment(Long admin_id) {
        List<EstablishmentsEntity> existingEstablishments = establishmentsRepository.findByAdminId(admin_id);

        if(!existingEstablishments.isEmpty()) {
            return existingEstablishments.get(0);
        }

        return createEstablishmentForUser(admin_id);
    }
}
