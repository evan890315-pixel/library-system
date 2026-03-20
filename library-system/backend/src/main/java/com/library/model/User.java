package com.library.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class User {
    private Long          userId;
    private String        phoneNumber;
    private String        passwordHash;
    private String        userName;
    private LocalDateTime regTime;
    private LocalDateTime lastLogin;
    private Boolean       isActive;
}
