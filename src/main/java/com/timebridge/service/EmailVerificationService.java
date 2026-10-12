package com.timebridge.service;

import com.timebridge.entity.EmailVerificationToken;
import com.timebridge.entity.User;
import com.timebridge.repository.EmailVerificationTokenRepository;
import com.timebridge.repository.UserRepository;
import com.timebridge.security.VerificationTokenUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class EmailVerificationService {

    public enum VerifyResult { VERIFIED, ALREADY_VERIFIED, EXPIRED, INVALID }

    private static final int RESEND_COOLDOWN_SECONDS = 60;
    private static final int MAX_EMAILS_PER_HOUR = 5;

    private final EmailVerificationTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final int tokenMinutes;
    private final String frontendUrl;

    public EmailVerificationService(EmailVerificationTokenRepository tokenRepository,
                                    UserRepository userRepository,
                                    EmailService emailService,
                                    @Value("${app.verification.token-minutes}") int tokenMinutes,
                                    @Value("${app.frontend-url}") String frontendUrl) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.tokenMinutes = tokenMinutes;
        this.frontendUrl = frontendUrl.endsWith("/")
                ? frontendUrl.substring(0, frontendUrl.length() - 1)
                : frontendUrl;
    }

    // Creates a fresh token, cancels older unused ones, and emails the link.
    @Transactional
    public boolean issueAndSend(User user) {
        LocalDateTime now = LocalDateTime.now();

        for (EmailVerificationToken old : tokenRepository.findByUserAndUsedAtIsNull(user)) {
            old.setUsedAt(now);
        }

        String rawToken = VerificationTokenUtil.generateRawToken();
        tokenRepository.save(new EmailVerificationToken(
                user, VerificationTokenUtil.hash(rawToken), now.plusMinutes(tokenMinutes)));

        String verifyUrl = frontendUrl + "/verify-email?token=" + rawToken;
        return emailService.sendVerificationEmail(user.getEmail(), user.getFirstName(), verifyUrl, tokenMinutes);
    }

    @Transactional
    public VerifyResult verify(String rawToken) {
        if (rawToken == null || rawToken.isBlank() || rawToken.length() > 100) {
            return VerifyResult.INVALID;
        }

        Optional<EmailVerificationToken> found =
                tokenRepository.findByTokenHash(VerificationTokenUtil.hash(rawToken.trim()));
        if (found.isEmpty()) {
            return VerifyResult.INVALID;
        }

        EmailVerificationToken token = found.get();
        User user = token.getUser();
        LocalDateTime now = LocalDateTime.now();

        if (token.getUsedAt() != null) {
            return user.isEmailVerified() ? VerifyResult.ALREADY_VERIFIED : VerifyResult.INVALID;
        }
        if (token.getExpiresAt().isBefore(now)) {
            return VerifyResult.EXPIRED;
        }

        user.setEmailVerified(true);
        userRepository.save(user);
        token.setUsedAt(now);
        return VerifyResult.VERIFIED;
    }

    // Always behaves the same from the outside, so it cannot be used to discover which emails are registered.
    @Transactional
    public void resend(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        Optional<User> found = userRepository.findByEmail(email.trim());
        if (found.isEmpty() || found.get().isEmailVerified()) {
            return;
        }

        User user = found.get();
        LocalDateTime now = LocalDateTime.now();

        Optional<EmailVerificationToken> latest = tokenRepository.findTopByUserOrderByCreatedAtDesc(user);
        if (latest.isPresent() && latest.get().getCreatedAt().isAfter(now.minusSeconds(RESEND_COOLDOWN_SECONDS))) {
            return;
        }
        if (tokenRepository.countByUserAndCreatedAtAfter(user, now.minusHours(1)) >= MAX_EMAILS_PER_HOUR) {
            return;
        }

        issueAndSend(user);
    }
}
