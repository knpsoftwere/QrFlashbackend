package org.qrflash.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.List;

@Entity
@Data
@Table(name="menu_items")
public class MenuItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private boolean isActive = true;

    @Column(nullable = false)
    private String unit;

    @Column(nullable = false)
    private String itemType;

    @Column(nullable = false)
    private boolean isPinned = false;

    @Column(nullable = false)
    private Double price;
}
