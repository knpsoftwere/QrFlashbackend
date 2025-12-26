package org.qrflash.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Set;

@Entity
@Table(name="users", schema = "personal")
@Data
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    @ManyToMany
    @JoinTable(
            name = "establishment_admins",
            joinColumns = @JoinColumn(name = "establishment_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<UserEntity> admins;

    @Column
    private Boolean isActive = true;

    @ManyToMany(mappedBy = "admins")
    private Set<EstablishmentsEntity> establishments;
}
