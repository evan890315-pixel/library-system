-- ============================================================
-- Library Borrowing System - Stored Procedures
-- ============================================================

USE library_system;

DELIMITER $$

-- ============================================================
-- SP: sp_register_user
-- 使用者註冊
-- ============================================================
DROP PROCEDURE IF EXISTS sp_register_user $$
CREATE PROCEDURE sp_register_user(
    IN  p_phone_number  VARCHAR(20),
    IN  p_password_hash VARCHAR(255),
    IN  p_user_name     VARCHAR(100),
    OUT p_user_id       BIGINT UNSIGNED,
    OUT p_result_code   INT,        -- 0=成功, 1=手機號碼已存在, -1=其他錯誤
    OUT p_result_msg    VARCHAR(255)
)
BEGIN
    DECLARE v_count INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result_code = -1;
        SET p_result_msg  = '系統錯誤，請稍後再試';
        SET p_user_id     = 0;
    END;

    START TRANSACTION;

    -- 檢查手機號碼是否已存在
    SELECT COUNT(*) INTO v_count
    FROM users
    WHERE phone_number = p_phone_number;

    IF v_count > 0 THEN
        ROLLBACK;
        SET p_result_code = 1;
        SET p_result_msg  = '此手機號碼已被註冊';
        SET p_user_id     = 0;
    ELSE
        INSERT INTO users (phone_number, password_hash, user_name, reg_time)
        VALUES (p_phone_number, p_password_hash, p_user_name, NOW());

        SET p_user_id     = LAST_INSERT_ID();
        SET p_result_code = 0;
        SET p_result_msg  = '註冊成功';

        COMMIT;
    END IF;
END $$

-- ============================================================
-- SP: sp_get_user_by_phone
-- 依手機號碼查詢使用者（登入用）
-- ============================================================
DROP PROCEDURE IF EXISTS sp_get_user_by_phone $$
CREATE PROCEDURE sp_get_user_by_phone(
    IN p_phone_number VARCHAR(20)
)
BEGIN
    SELECT user_id, phone_number, password_hash, user_name,
           reg_time, last_login, is_active
    FROM users
    WHERE phone_number = p_phone_number
      AND is_active = 1
    LIMIT 1;
END $$

-- ============================================================
-- SP: sp_update_last_login
-- 更新最後登入時間
-- ============================================================
DROP PROCEDURE IF EXISTS sp_update_last_login $$
CREATE PROCEDURE sp_update_last_login(
    IN p_user_id BIGINT UNSIGNED
)
BEGIN
    UPDATE users
    SET last_login = NOW()
    WHERE user_id = p_user_id;
END $$

-- ============================================================
-- SP: sp_get_books_with_availability
-- 查詢所有書籍及可借閱數量
-- ============================================================
DROP PROCEDURE IF EXISTS sp_get_books_with_availability $$
CREATE PROCEDURE sp_get_books_with_availability()
BEGIN
    SELECT
        b.isbn,
        b.name,
        b.author,
        b.introduction,
        b.publisher,
        b.publish_date,
        b.cover_url,
        COUNT(i.inventory_id) AS total_copies,
        SUM(CASE WHEN i.status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available_copies
    FROM book b
    LEFT JOIN inventory i ON b.isbn = i.isbn
      AND i.status NOT IN ('DISCARDED','LOST')
    GROUP BY b.isbn, b.name, b.author, b.introduction,
             b.publisher, b.publish_date, b.cover_url
    ORDER BY b.name;
END $$

-- ============================================================
-- SP: sp_borrow_book
-- 借書（含 Transaction）
-- ============================================================
DROP PROCEDURE IF EXISTS sp_borrow_book $$
CREATE PROCEDURE sp_borrow_book(
    IN  p_user_id       BIGINT UNSIGNED,
    IN  p_isbn          VARCHAR(20),
    OUT p_record_id     BIGINT UNSIGNED,
    OUT p_inventory_id  BIGINT UNSIGNED,
    OUT p_result_code   INT,        -- 0=成功, 1=無可借冊, 2=已借此書, -1=錯誤
    OUT p_result_msg    VARCHAR(255)
)
BEGIN
    DECLARE v_inventory_id  BIGINT UNSIGNED DEFAULT NULL;
    DECLARE v_already_count INT DEFAULT 0;
    DECLARE v_due_date      DATETIME;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result_code  = -1;
        SET p_result_msg   = '系統錯誤，請稍後再試';
        SET p_record_id    = 0;
        SET p_inventory_id = 0;
    END;

    START TRANSACTION;

    -- 1. 確認該使用者尚未借閱此 ISBN（未還書）
    SELECT COUNT(*) INTO v_already_count
    FROM borrowing_record br
    JOIN inventory i ON br.inventory_id = i.inventory_id
    WHERE br.user_id = p_user_id
      AND i.isbn = p_isbn
      AND br.return_time IS NULL;

    IF v_already_count > 0 THEN
        ROLLBACK;
        SET p_result_code  = 2;
        SET p_result_msg   = '您已借閱此書，請先還書後再借';
        SET p_record_id    = 0;
        SET p_inventory_id = 0;
    ELSE
        -- 2. 找一本可借閱的庫存（FOR UPDATE 加鎖避免並發）
        SELECT inventory_id INTO v_inventory_id
        FROM inventory
        WHERE isbn = p_isbn
          AND status = 'AVAILABLE'
        ORDER BY inventory_id
        LIMIT 1
        FOR UPDATE;

        IF v_inventory_id IS NULL THEN
            ROLLBACK;
            SET p_result_code  = 1;
            SET p_result_msg   = '目前無可借閱的庫存';
            SET p_record_id    = 0;
            SET p_inventory_id = 0;
        ELSE
            -- 3. 更新庫存狀態
            UPDATE inventory
            SET status = 'BORROWED'
            WHERE inventory_id = v_inventory_id;

            -- 4. 新增借閱紀錄（借閱期限 30 天）
            SET v_due_date = DATE_ADD(NOW(), INTERVAL 30 DAY);
            INSERT INTO borrowing_record (user_id, inventory_id, borrowing_time, due_date)
            VALUES (p_user_id, v_inventory_id, NOW(), v_due_date);

            SET p_record_id    = LAST_INSERT_ID();
            SET p_inventory_id = v_inventory_id;
            SET p_result_code  = 0;
            SET p_result_msg   = '借閱成功';

            COMMIT;
        END IF;
    END IF;
END $$

-- ============================================================
-- SP: sp_return_book
-- 還書（含 Transaction）
-- ============================================================
DROP PROCEDURE IF EXISTS sp_return_book $$
CREATE PROCEDURE sp_return_book(
    IN  p_user_id      BIGINT UNSIGNED,
    IN  p_inventory_id BIGINT UNSIGNED,
    OUT p_result_code  INT,        -- 0=成功, 1=找不到借閱紀錄, -1=錯誤
    OUT p_result_msg   VARCHAR(255)
)
BEGIN
    DECLARE v_record_id  BIGINT UNSIGNED DEFAULT NULL;
    DECLARE v_rows       INT DEFAULT 0;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_result_code = -1;
        SET p_result_msg  = '系統錯誤，請稍後再試';
    END;

    START TRANSACTION;

    -- 1. 查詢未還的借閱紀錄（FOR UPDATE）
    SELECT record_id INTO v_record_id
    FROM borrowing_record
    WHERE user_id      = p_user_id
      AND inventory_id = p_inventory_id
      AND return_time IS NULL
    LIMIT 1
    FOR UPDATE;

    IF v_record_id IS NULL THEN
        ROLLBACK;
        SET p_result_code = 1;
        SET p_result_msg  = '找不到對應的借閱紀錄';
    ELSE
        -- 2. 更新還書時間
        UPDATE borrowing_record
        SET return_time = NOW()
        WHERE record_id = v_record_id;

        -- 3. 更新庫存狀態為「整理中」(PROCESSING)
        UPDATE inventory
        SET status = 'PROCESSING'
        WHERE inventory_id = p_inventory_id;

        SET p_result_code = 0;
        SET p_result_msg  = '還書成功';

        COMMIT;
    END IF;
END $$

-- ============================================================
-- SP: sp_get_user_borrowings
-- 查詢使用者借閱記錄
-- ============================================================
DROP PROCEDURE IF EXISTS sp_get_user_borrowings $$
CREATE PROCEDURE sp_get_user_borrowings(
    IN p_user_id BIGINT UNSIGNED
)
BEGIN
    SELECT
        br.record_id,
        br.user_id,
        br.inventory_id,
        br.borrowing_time,
        br.return_time,
        br.due_date,
        i.isbn,
        i.status AS inventory_status,
        i.location,
        b.name   AS book_name,
        b.author AS book_author,
        b.cover_url
    FROM borrowing_record br
    JOIN inventory i ON br.inventory_id = i.inventory_id
    JOIN book b      ON i.isbn = b.isbn
    WHERE br.user_id = p_user_id
    ORDER BY br.borrowing_time DESC;
END $$

-- ============================================================
-- SP: sp_get_inventory_by_id
-- 查詢庫存詳情
-- ============================================================
DROP PROCEDURE IF EXISTS sp_get_inventory_by_id $$
CREATE PROCEDURE sp_get_inventory_by_id(
    IN p_inventory_id BIGINT UNSIGNED
)
BEGIN
    SELECT i.*, b.name AS book_name, b.author, b.introduction
    FROM inventory i
    JOIN book b ON i.isbn = b.isbn
    WHERE i.inventory_id = p_inventory_id;
END $$

DELIMITER ;
