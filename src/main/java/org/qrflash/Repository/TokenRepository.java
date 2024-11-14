package org.qrflash.Repository;

import org.qrflash.Entity.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TokenRepository  extends JpaRepository <TokenEntity, Long>{
    Optional<TokenEntity> findByUserId(Long userId);
}
