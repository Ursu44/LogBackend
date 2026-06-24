package com.example.logsbackend.service;

import com.example.logsbackend.model.User;
import com.example.logsbackend.repository.UserRepository;
import com.example.logsbackend.security.JwtService;
import dev.samstevens.totp.code.*;
import dev.samstevens.totp.exceptions.QrGenerationException;
import dev.samstevens.totp.qr.*;
import dev.samstevens.totp.secret.SecretGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static dev.samstevens.totp.util.Utils
        .getDataUriForImage;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecretGenerator secretGenerator;
    private final CodeVerifier    codeVerifier;
    private final JwtService      jwtService;

    public void register(String username,
                         String password,
                         String email) {

        if (userRepository
                .existsByUsername(username)) {
            throw new RuntimeException(
                    "Username deja folosit");
        }

        User user = User.builder()
                .id(UUID.randomUUID().toString())
                .username(username)
                .password(passwordEncoder
                        .encode(password))
                .email(email)
                .role("USER")
                .totpEnabled(false)
                .build();

        userRepository.save(user);
        log.info("User înregistrat: {}", username);
    }

    public LoginResponse login(String username,
                               String password) {

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Credențiale incorecte"));

        if (!passwordEncoder.matches(
                password, user.getPassword())) {
            throw new RuntimeException(
                    "Credențiale incorecte");
        }

        if (!user.isTotpEnabled()) {
            String secret =
                    secretGenerator.generate();
            user.setTotpSecret(secret);
            userRepository.save(user);

            String qrUrl = generateQrUrl(
                    username, secret);
            String tempToken =
                    jwtService.generateTempToken(
                            username);

            return new LoginResponse(
                    true, false,
                    tempToken, qrUrl);
        }

        String tempToken =
                jwtService.generateTempToken(username);

        return new LoginResponse(
                false, true, tempToken, null);
    }

    public TotpVerifyResult verifyTotp(
            String tempToken,
            String code) {

        if (!"temp".equals(
                jwtService.extractType(tempToken))) {
            throw new RuntimeException(
                    "Token invalid");
        }

        String username =
                jwtService.extractUsername(tempToken);

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User negăsit"));

        if (!codeVerifier.isValidCode(
                user.getTotpSecret(), code)) {
            throw new RuntimeException(
                    "Cod TOTP invalid");
        }

        if (!user.isTotpEnabled()) {
            user.setTotpEnabled(true);
            userRepository.save(user);
        }

        String accessToken =
                jwtService.generateToken(
                        username, user.getRole());
        String refreshToken =
                jwtService.generateRefreshToken(
                        username);

        return new TotpVerifyResult(
                accessToken, refreshToken);
    }

    public String refreshAccessToken(
            String refreshToken) {

        if (!jwtService
                .isValidRefreshToken(refreshToken)) {
            throw new RuntimeException(
                    "Refresh token invalid");
        }

        String username =
                jwtService.extractUsername(refreshToken);

        User user = userRepository
                .findByUsername(username)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User negăsit"));

        return jwtService.generateToken(
                username, user.getRole());
    }

    public boolean checkToken(String accessToken) {
        return accessToken != null
                && jwtService
                .isValidAccessToken(accessToken);
    }

    private String generateQrUrl(String username,
                                 String secret) {
        try {
            QrData data = new QrData.Builder()
                    .label(username)
                    .secret(secret)
                    .issuer("Log Analyzer")
                    .algorithm(HashingAlgorithm.SHA1)
                    .digits(6)
                    .period(30)
                    .build();

            QrGenerator generator =
                    new ZxingPngQrGenerator();

            return getDataUriForImage(
                    generator.generate(data),
                    generator.getImageMimeType());

        } catch (QrGenerationException e) {
            throw new RuntimeException(
                    "Eroare generare QR code", e);
        }
    }

    public record LoginResponse(
            boolean requiresSetup,
            boolean requiresTotp,
            String  tempToken,
            String  qrCodeUrl
    ) {}

    public record TotpVerifyResult(
            String accessToken,
            String refreshToken
    ) {}
}