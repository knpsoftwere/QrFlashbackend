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

    @Column(length = 10)
    private String emoji;

    @ManyToMany(mappedBy = "tags")
    @JsonIgnore
    private List<MenuItemEntity> menuItems = new ArrayList<>();
}
