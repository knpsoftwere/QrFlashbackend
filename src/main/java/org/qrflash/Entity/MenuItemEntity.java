package org.qrflash.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "menu_items")
@Data
public class MenuItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;


    @Column(nullable = false)
    private String unit;

    @Column(nullable = false)
    private String item_type;

    @Column(nullable = false)
    private Double price; // На рівні БД DECIMAL, в коді Double. Можеш використати BigDecimal.

    @Column(nullable = false)
    private Boolean isActive = true;

    @Column(nullable = false)
    private Boolean isPinned = false;

    @Column(nullable = false)
    private Boolean isQuantity = false;

    private String photo;

    //Багато товарів можуть належати до однієї категорії). На стороні категорії буде OneToMany
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CategoryEntity category;

    // Зв’язок з TagEntity
    // Один товар може мати кілька тегів, і один тег може бути застосований до багатьох товарів
    @ManyToMany
    @JoinTable(
            name = "menu_item_tags",
            joinColumns = @JoinColumn(name = "menu_item_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @JsonIgnore // Якщо потрібно уникнути циклічної серіалізації
    private List<TagEntity> tags = new ArrayList<>();

    public Long getCategoryId() {
        return category != null ? category.getId() : null;
    }

}

