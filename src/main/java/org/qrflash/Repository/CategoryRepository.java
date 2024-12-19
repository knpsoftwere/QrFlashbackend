package org.qrflash.Repository;

import org.qrflash.Entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {
    @Query("SELECT c FROM CategoryEntity c LEFT JOIN FETCH c.items WHERE c.id = :categoryId")
    Optional<CategoryEntity> findCategoryWithItems(@Param("categoryId") Long categoryId);
}
