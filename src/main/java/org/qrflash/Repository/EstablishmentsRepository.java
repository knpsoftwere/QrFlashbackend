package org.qrflash.Repository;

import org.qrflash.Entity.EstablishmentsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstablishmentsRepository extends JpaRepository<EstablishmentsEntity, String> {
    Optional<EstablishmentsEntity> findByUuid(String uuid);
    @Query("SELECT e FROM EstablishmentsEntity e JOIN e.admins a WHERE a.id = :adminId")
    List<EstablishmentsEntity> findByAdminId(@Param("adminId") Long adminId);
}
