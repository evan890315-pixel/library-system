package com.library.service;

import com.library.common.ApiResponse;
import com.library.common.JwtUtil;
import com.library.dto.AuthDto;
import com.library.model.User;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository  userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil         jwtUtil;

    /**
     * 使用者註冊
     * - 密碼以 BCrypt 加鹽雜湊後儲存
     * - 手機號碼唯一性由 SP 保障
     */
    public ApiResponse<Void> register(AuthDto.RegisterRequest req) {
        // BCrypt 自帶 salt，cost=12，防止彩虹表攻擊
        String hash = passwordEncoder.encode(req.getPassword());

        Map<String, Object> result = userRepository.register(
                req.getPhoneNumber(), hash, req.getUserName()
        );

        int code = toInt(result.get("p_result_code"));
        String msg = (String) result.get("p_result_msg");

        return switch (code) {
            case 0  -> ApiResponse.ok(msg, null);
            case 1  -> ApiResponse.fail(409, msg);
            default -> ApiResponse.fail(500, msg);
        };
    }

    /**
     * 使用者登入
     * - 驗證密碼雜湊
     * - 成功後發放 JWT Token
     */
    public ApiResponse<AuthDto.LoginResponse> login(AuthDto.LoginRequest req) {
        Optional<User> userOpt = userRepository.findByPhone(req.getPhoneNumber());

        if (userOpt.isEmpty()) {
            return ApiResponse.fail(401, "手機號碼或密碼錯誤");
        }

        User user = userOpt.get();

        // 比對 BCrypt 雜湊
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            return ApiResponse.fail(401, "手機號碼或密碼錯誤");
        }

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            return ApiResponse.fail(403, "帳號已停用，請聯絡管理員");
        }

        // 更新最後登入時間
        userRepository.updateLastLogin(user.getUserId());

        String token = jwtUtil.generateToken(user.getUserId(), user.getPhoneNumber());

        return ApiResponse.ok("登入成功",
                new AuthDto.LoginResponse(token, user.getUserId(),
                                          user.getUserName(), user.getPhoneNumber()));
    }

    // ── helper ──────────────────────────────────────────────────────────────

    private int toInt(Object val) {
        if (val == null) return -1;
        if (val instanceof Integer i) return i;
        if (val instanceof BigInteger bi) return bi.intValue();
        if (val instanceof Long l) return l.intValue();
        return Integer.parseInt(val.toString());
    }
}
