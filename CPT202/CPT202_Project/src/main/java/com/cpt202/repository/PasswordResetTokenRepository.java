package com.cpt202.repository;

import com.cpt202.model.entity.PasswordResetToken;
import com.cpt202.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    List<PasswordResetToken> findAllByUserAndUsedAtIsNull(User user);

    void deleteByExpiresAtBefore(LocalDateTime expiresAt);
}
