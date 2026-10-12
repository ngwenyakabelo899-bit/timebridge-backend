package com.timebridge.repository;

import com.timebridge.entity.EmailVerificationToken;
import com.timebridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EmailVerificationTokenRepository extends JpaRepository<EmailVerificationToken, Long> {

    Optional<EmailVerificationToken> findByTokenHash(String tokenHash);

    List<EmailVerificationToken> findByUserAndUsedAtIsNull(User user);

    Optional<EmailVerificationToken> findTopByUserOrderByCreatedAtDesc(User user);

    long countByUserAndCreatedAtAfter(User user, LocalDateTime since);
}
