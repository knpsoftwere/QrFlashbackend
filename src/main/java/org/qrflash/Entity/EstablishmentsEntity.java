package org.qrflash.Entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="establishments")
@Data
public class EstablishmentsEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    @UuidGenerator
    private String uuid;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDateTime created_at;

    @ManyToMany
    @JoinTable(
            name = "establishment_admins",
            joinColumns = @JoinColumn(name = "establishment_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserEntity> admins = new HashSet<>();


    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private String language;

    @Column
    private String address;
}
