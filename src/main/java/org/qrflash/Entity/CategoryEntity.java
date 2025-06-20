package org.qrflash.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
@Data
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(length = 500)
    private String image_url;

    // Зв’язок з MenuItemEntity
    //Має OneToMany до MenuItemEntity, оскільки одна категорія може мати багато товарів
    @OneToMany(mappedBy = "category", fetch = FetchType.LAZY)
    @JsonIgnore // Якщо використовуєш JSON-серіалізацію, щоб уникнути циклічних посилань
    private List<MenuItemEntity> menuItems = new ArrayList<>();
}

