package org.qrflash.Repository;

import org.qrflash.Entity.EstablishmentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstablishmentsRepository extends JpaRepository<EstablishmentsEntity, UUID> {
    @Query("SELECT e FROM EstablishmentsEntity e JOIN e.admins a WHERE a.id = :adminId")
    Optional<EstablishmentsEntity> findByAdminId(Long adminId);

    @Modifying
    @Transactional
    @Query("UPDATE EstablishmentsEntity e SET e.language = :language WHERE e.uuid = :uuid")
    void updateLanguageByUuid(UUID uuid, String language);
}