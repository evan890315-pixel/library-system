package com.library.repository;

import com.library.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final JdbcTemplate jdbcTemplate;

    private SimpleJdbcCall spRegister;
    private SimpleJdbcCall spGetByPhone;
    private SimpleJdbcCall spUpdateLogin;

    @PostConstruct
    private void init() {
        jdbcTemplate.getDataSource(); // warm up

        spRegister = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_register_user");

        spGetByPhone = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_user_by_phone")
                .returningResultSet("user", userRowMapper());

        spUpdateLogin = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_update_last_login");
    }

    /**
     * 呼叫 SP 進行使用者註冊
     * @return result_code: 0=成功, 1=手機號碼重複, -1=錯誤
     */
    public Map<String, Object> register(String phoneNumber, String passwordHash, String userName) {
        return spRegister.execute(Map.of(
                "p_phone_number",  phoneNumber,
                "p_password_hash", passwordHash,
                "p_user_name",     userName
        ));
    }

    /**
     * 依手機號碼查詢使用者
     */
    @SuppressWarnings("unchecked")
    public Optional<User> findByPhone(String phoneNumber) {
        Map<String, Object> result = spGetByPhone.execute(
                Map.of("p_phone_number", phoneNumber)
        );
        var list = (java.util.List<User>) result.get("user");
        return (list == null || list.isEmpty()) ? Optional.empty() : Optional.of(list.get(0));
    }

    /**
     * 更新最後登入時間
     */
    public void updateLastLogin(Long userId) {
        spUpdateLogin.execute(Map.of("p_user_id", userId));
    }

    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> {
            User u = new User();
            u.setUserId(rs.getLong("user_id"));
            u.setPhoneNumber(rs.getString("phone_number"));
            u.setPasswordHash(rs.getString("password_hash"));
            u.setUserName(rs.getString("user_name"));
            u.setIsActive(rs.getBoolean("is_active"));
            var regTs = rs.getTimestamp("reg_time");
            if (regTs != null) u.setRegTime(regTs.toLocalDateTime());
            var loginTs = rs.getTimestamp("last_login");
            if (loginTs != null) u.setLastLogin(loginTs.toLocalDateTime());
            return u;
        };
    }
}
