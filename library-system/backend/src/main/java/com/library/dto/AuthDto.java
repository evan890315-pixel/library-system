package com.library.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

// ── Auth DTOs ─────────────────────────────────────────────────────────────

public class AuthDto {

    @Data
    public static class RegisterRequest {
        @NotBlank(message = "手機號碼不可為空")
        @Pattern(regexp = "^09\\d{8}$|^\\+886\\d{9}$",
                 message = "請輸入有效的手機號碼")
        private String phoneNumber;

        @NotBlank(message = "密碼不可為空")
        @Size(min = 8, max = 100, message = "密碼長度需在 8~100 字元之間")
        private String password;

        @NotBlank(message = "使用者名稱不可為空")
        @Size(max = 100, message = "名稱不可超過 100 字元")
        private String userName;
    }

    @Data
    public static class LoginRequest {
        @NotBlank(message = "手機號碼不可為空")
        private String phoneNumber;

        @NotBlank(message = "密碼不可為空")
        private String password;
    }

    @Data
    public static class LoginResponse {
        private String token;
        private Long   userId;
        private String userName;
        private String phoneNumber;

        public LoginResponse(String token, Long userId, String userName, String phoneNumber) {
            this.token       = token;
            this.userId      = userId;
            this.userName    = userName;
            this.phoneNumber = phoneNumber;
        }
    }
}
