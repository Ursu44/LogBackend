package com.example.logsbackend.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class CookieService {

    @Value("${app.cookie.secure:false}")
    private boolean secureCookie;

    public void setAccessTokenCookie(
            HttpServletResponse response,
            String token) {

        ResponseCookie cookie = ResponseCookie
                .from("access_token", token)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/")
                .maxAge(Duration.ofMinutes(30))
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString());
    }

    public void setRefreshTokenCookie(
            HttpServletResponse response,
            String token) {

        ResponseCookie cookie = ResponseCookie
                .from("refresh_token", token)
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/api/auth/refresh")
                .maxAge(Duration.ofDays(7))
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                cookie.toString());
    }

    public void clearCookies(
            HttpServletResponse response) {

        ResponseCookie accessCookie = ResponseCookie
                .from("access_token", "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/")
                .maxAge(0)
                .build();

        ResponseCookie refreshCookie = ResponseCookie
                .from("refresh_token", "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Strict")
                .path("/api/auth/refresh")
                .maxAge(0)
                .build();

        response.addHeader(
                HttpHeaders.SET_COOKIE,
                accessCookie.toString());
        response.addHeader(
                HttpHeaders.SET_COOKIE,
                refreshCookie.toString());
    }

    public String extractTokenFromCookies(
            jakarta.servlet.http.Cookie[] cookies,
            String cookieName) {

        if (cookies == null) return null;

        for (jakarta.servlet.http.Cookie cookie
                : cookies) {
            if (cookieName.equals(
                    cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}