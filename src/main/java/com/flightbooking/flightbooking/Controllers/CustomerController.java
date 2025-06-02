package com.flightbooking.flightbooking.Controllers;
import com.flightbooking.flightbooking.Entity.User;
import com.flightbooking.flightbooking.Services.AuthService;
import com.flightbooking.flightbooking.Util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;

@RestController
@RequestMapping("/api/customer")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CustomerController {
    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getDashboard(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Welcome to Customer Dashboard",
                    "user", Map.of(
                            "email", currentUser.getEmail(),
                            "role", currentUser.getRole(),
                            "lastLogin", currentUser.getLastLogin()
                    )
            ));
        } catch (Exception e) {
            log.error("Customer dashboard error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/profile")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> getProfile(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "data", Map.of(
                            "id", currentUser.getId(),
                            "email", currentUser.getEmail(),
                            "role", currentUser.getRole(),
                            "isVerified", currentUser.getIsVerified(),
                            "createdAt", currentUser.getCreatedAt(),
                            "lastLogin", currentUser.getLastLogin()
                    )
            ));
        } catch (Exception e) {
            log.error("Get profile error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            String sessionId = (String) request.getAttribute("sessionId");
            String result = authService.logout(sessionId);

            return ResponseEntity.ok(Map.of("success", true, "message", result));
        } catch (Exception e) {
            log.error("Customer logout error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
class AdminController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getDashboard(HttpServletRequest request) {
        try {
            User currentUser = (User) request.getAttribute("currentUser");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Welcome to Admin Dashboard",
                    "user", Map.of(
                            "email", currentUser.getEmail(),
                            "role", currentUser.getRole(),
                            "lastLogin", currentUser.getLastLogin()
                    )
            ));
        } catch (Exception e) {
            log.error("Admin dashboard error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getAllUsers(HttpServletRequest request) {
        try {
            // This would typically fetch all users from repository
            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Admin can access all users data",
                    "note", "Implement user listing logic here"
            ));
        } catch (Exception e) {
            log.error("Get users error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            String sessionId = (String) request.getAttribute("sessionId");
            String result = authService.logout(sessionId);

            return ResponseEntity.ok(Map.of("success", true, "message", result));
        } catch (Exception e) {
            log.error("Admin logout error", e);
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", e.getMessage()));
        }
    }
}
