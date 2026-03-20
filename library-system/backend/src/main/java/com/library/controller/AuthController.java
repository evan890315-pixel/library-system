package com.library.controller;

import com.library.common.ApiResponse;
import com.library.dto.AuthDto;
import com.library.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 展示層 - 身份驗證 Controller
 * POST /api/auth/register  - 註冊
 * POST /api/auth/login     - 登入
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody AuthDto.RegisterRequest req) {
        ApiResponse<Void> resp = authService.register(req);
        return ResponseEntity.status(resp.isSuccess() ? 200 : resp.getCode()).body(resp);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDto.LoginResponse>> login(
            @Valid @RequestBody AuthDto.LoginRequest req) {
        ApiResponse<AuthDto.LoginResponse> resp = authService.login(req);
        return ResponseEntity.status(resp.isSuccess() ? 200 : resp.getCode()).body(resp);
    }
}
