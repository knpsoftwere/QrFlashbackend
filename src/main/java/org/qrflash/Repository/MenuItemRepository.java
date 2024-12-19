package org.qrflash.Repository;

import org.qrflash.Entity.MenuItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface MenuItemRepository extends JpaRepository<MenuItemEntity, Long> {
//    @Query(value = "SELECT * FROM :tableName", nativeQuery = true)
//    List<Map<String, Object>> findAllFromTable(@Param("tableName") String tableName);
//
//    @Query(value = "INSERT INTO :tableName (name, description, category, is_active, unit, item_type, is_pinned, price) " +
//            "VALUES (:name, :description, :category, :isActive, :unit, :itemType, :isPinned, :price)", nativeQuery = true)
//    void insertMenuItem(
//            @Param("tableName") String tableName,
//            @Param("name") String name,
//            @Param("description") String description,
//            @Param("category") String category,
//            @Param("isActive") boolean isActive,
//            @Param("unit") String unit,
//            @Param("itemType") String itemType,
//            @Param("isPinned") boolean isPinned,
//            @Param("price") double price
//    );
}
