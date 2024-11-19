package org.qrflash.Entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Data
@Table(name="users")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String phoneNumber;
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;
    // Видаліть це
    @Column(name = "salt", nullable = false)
    private String salt;

    private Boolean isActive;
    private java.sql.Timestamp lastLogin;

}
