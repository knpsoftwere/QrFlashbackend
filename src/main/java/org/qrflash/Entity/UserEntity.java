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
    private String passwordHash;
    private String salt;
    private Boolean isActive;
    private java.sql.Timestamp lastLogin;

}
