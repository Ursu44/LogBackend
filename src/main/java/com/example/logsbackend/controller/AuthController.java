package com.example.logsbackend.controller;

import com.example.logsbackend.security.CookieService;
import com.example.logsbackend.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService   authService;
    private final CookieService cookieService;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>>
    register(
            @RequestBody Map<String,
                    String> req) {

        authService.register(
                req.get("username"),
                req.get("password"),
                req.get("email")
        );

        return ResponseEntity.ok(
                Map.of("message",
                        "User înregistrat cu succes"));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthService.LoginResponse>
    login(
            @RequestBody Map<String,
                    String> req) {

        AuthService.LoginResponse response =
                authService.login(
                        req.get("username"),
                        req.get("password")
                );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-totp")
    public ResponseEntity<Map<String, String>>
    verifyTotp(
            @RequestBody Map<String,
                    String> req,
            HttpServletResponse response) {

        AuthService.TotpVerifyResult result =
                authService.verifyTotp(
                        req.get("tempToken"),
                        req.get("code")
                );

        cookieService.setAccessTokenCookie(
                response, result.accessToken());
        cookieService.setRefreshTokenCookie(
                response, result.refreshToken());

        return ResponseEntity.ok(
                Map.of("message",
                        "Autentificat cu succes"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>>
    refresh(
            HttpServletRequest  request,
            HttpServletResponse response) {

        String refreshToken =
                cookieService.extractTokenFromCookies(
                        request.getCookies(),
                        "refresh_token"
                );

        String newAccessToken =
                authService.refreshAccessToken(
                        refreshToken);

        cookieService.setAccessTokenCookie(
                response, newAccessToken);

        return ResponseEntity.ok(
                Map.of("message", "Token reînnoit"));
    }

    @GetMapping("/check")
    public ResponseEntity<Map<String, Boolean>>
    check(HttpServletRequest request) {

        String token =
                cookieService.extractTokenFromCookies(
                        request.getCookies(),
                        "access_token"
                );

        boolean valid =
                authService.checkToken(token);

        return ResponseEntity.ok(
                Map.of("valid", valid));
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>>
    logout(HttpServletResponse response) {

        cookieService.clearCookies(response);

        return ResponseEntity.ok(
                Map.of("message",
                        "Delogat cu succes"));
    }
}