package com.timebridge.controller;

import com.timebridge.dto.AuthResponse;
import com.timebridge.dto.LoginRequest;
import com.timebridge.dto.RegisterRequest;
import com.timebridge.dto.ResendVerificationRequest;
import com.timebridge.dto.VerifyEmailRequest;
import com.timebridge.entity.Role;
import com.timebridge.entity.User;
import com.timebridge.repository.UserRepository;
import com.timebridge.security.JwtUtil;
import com.timebridge.service.EmailVerificationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final EmailVerificationService emailVerificationService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          AuthenticationManager authenticationManager,
                          EmailVerificationService emailVerificationService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message",
                            "An account with this email already exists. If you have not verified it yet, request a new verification email from the login page."));
        }

        User user = new User(
                request.getFirstName(),
                request.getLastName(),
                request.getEmail(),
                request.getPhoneNumber(),
                passwordEncoder.encode(request.getPassword()),
                Role.ROLE_CITIZEN
        );
        user.setEmailVerified(false);
        userRepository.save(user);

        boolean emailSent = emailVerificationService.issueAndSend(user);

        String message = emailSent
                ? "Registration received. We sent a verification link to your email address. Please open it to activate your account."
                : "Your account was created, but we could not send the verification email right now. Use 'Resend verification email' on the login page to try again.";

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", message, "emailSent", emailSent, "email", user.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );
        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid email or password"));
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        // Checked only after the password is correct, so it cannot be used to probe for accounts.
        if (!user.isEmailVerified()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of(
                            "code", "EMAIL_NOT_VERIFIED",
                            "message", "Please verify your email address before logging in. Check your inbox for the verification link."));
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        AuthResponse response = new AuthResponse(
                token, user.getEmail(), user.getFirstName(), user.getLastName(), user.getRole().name());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        EmailVerificationService.VerifyResult result = emailVerificationService.verify(request.getToken());

        return switch (result) {
            case VERIFIED -> ResponseEntity.ok(Map.of(
                    "status", "VERIFIED",
                    "message", "Your email address has been verified. You can now log in."));
            case ALREADY_VERIFIED -> ResponseEntity.ok(Map.of(
                    "status", "ALREADY_VERIFIED",
                    "message", "This email address is already verified. You can log in."));
            case EXPIRED -> ResponseEntity.badRequest().body(Map.of(
                    "status", "EXPIRED",
                    "message", "This verification link has expired. Request a new one from the login page."));
            case INVALID -> ResponseEntity.badRequest().body(Map.of(
                    "status", "INVALID",
                    "message", "This verification link is not valid. It may have been replaced by a newer one. Request a new one from the login page."));
        };
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@Valid @RequestBody ResendVerificationRequest request) {
        emailVerificationService.resend(request.getEmail());
        return ResponseEntity.ok(Map.of(
                "message", "If this email belongs to an unverified account, a new verification email has been sent. Please wait a minute between requests."));
    }
}
