package org.qrflash.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@Table(name="tags")
public class TagEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private String description;

    @Column(length = 10, nullable = false, unique = true)
    private String emoji;

    //Має ManyToMany до MenuItemEntity, але один бік зв’язку має бути “власником” (owner) зв’язку.
    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private List<MenuItemEntity> menuItems = new ArrayList<>();
}
