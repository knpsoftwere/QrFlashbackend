package org.qrflash.Repository;

import org.qrflash.Entity.EstablishmentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstablishmentsRepository extends JpaRepository<EstablishmentsEntity, UUID> {
    @Query("SELECT e FROM EstablishmentsEntity e JOIN e.admins a WHERE a.id = :adminId")
    Optional<EstablishmentsEntity> findByAdminId(Long adminId);
}