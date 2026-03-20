-- ============================================================
-- Library Borrowing System - DDL
-- Database: MySQL 8.0+
-- ============================================================

CREATE DATABASE IF NOT EXISTS library_system
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE library_system;

-- ============================================================
-- Table: users
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    user_id       BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    phone_number  VARCHAR(20)  NOT NULL UNIQUE COMMENT '手機號碼，作為登入帳號',
    password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt 雜湊密碼（含salt）',
    user_name     VARCHAR(100) NOT NULL COMMENT '使用者名稱',
    reg_time      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '註冊日期時間',
    last_login    DATETIME     NULL COMMENT '最後登入時間',
    is_active     TINYINT(1)   NOT NULL DEFAULT 1 COMMENT '帳號狀態 1=啟用 0=停用',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_phone (phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='使用者資料表';

-- ============================================================
-- Table: book
-- ============================================================
CREATE TABLE IF NOT EXISTS book (
    isbn          VARCHAR(20)   NOT NULL PRIMARY KEY COMMENT 'ISBN 國際標準書號',
    name          VARCHAR(255)  NOT NULL COMMENT '書名',
    author        VARCHAR(255)  NOT NULL COMMENT '作者',
    introduction  TEXT          NULL COMMENT '書籍內容簡介',
    publisher     VARCHAR(255)  NULL COMMENT '出版社',
    publish_date  DATE          NULL COMMENT '出版日期',
    cover_url     VARCHAR(500)  NULL COMMENT '封面圖片 URL',
    created_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_name (name),
    INDEX idx_author (author)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='書籍基本資料表';

-- ============================================================
-- Table: inventory
-- ============================================================
CREATE TABLE IF NOT EXISTS inventory (
    inventory_id  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    isbn          VARCHAR(20)  NOT NULL COMMENT 'ISBN 外鍵',
    store_time    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '書籍入庫日期時間',
    status        ENUM('AVAILABLE','BORROWED','PROCESSING','LOST','DAMAGED','DISCARDED')
                               NOT NULL DEFAULT 'AVAILABLE'
                               COMMENT '在庫/出借中/整理中/遺失/損毀/廢棄',
    location      VARCHAR(100) NULL COMMENT '存放位置',
    note          TEXT         NULL COMMENT '備註',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_book FOREIGN KEY (isbn) REFERENCES book(isbn) ON UPDATE CASCADE,
    INDEX idx_isbn (isbn),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='書籍庫存資料表';

-- ============================================================
-- Table: borrowing_record
-- ============================================================
CREATE TABLE IF NOT EXISTS borrowing_record (
    record_id      BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT UNSIGNED NOT NULL COMMENT '使用者 ID',
    inventory_id   BIGINT UNSIGNED NOT NULL COMMENT '庫存 ID',
    borrowing_time DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '借出日期時間',
    return_time    DATETIME        NULL COMMENT '歸還日期時間 (NULL 表示未歸還)',
    due_date       DATETIME        NULL COMMENT '應還日期',
    created_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_br_user      FOREIGN KEY (user_id)      REFERENCES users(user_id),
    CONSTRAINT fk_br_inventory FOREIGN KEY (inventory_id) REFERENCES inventory(inventory_id),
    INDEX idx_user_id      (user_id),
    INDEX idx_inventory_id (inventory_id),
    INDEX idx_borrowing_time (borrowing_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借閱紀錄資料表';
