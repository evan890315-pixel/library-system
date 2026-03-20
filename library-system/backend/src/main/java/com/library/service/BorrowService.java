package com.library.service;

import com.library.common.ApiResponse;
import com.library.dto.BorrowDto;
import com.library.model.BorrowingRecord;
import com.library.repository.BorrowRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BorrowService {

    private final BorrowRepository borrowRepository;

    /**
     * 借書
     * SP 內部已使用 Transaction 確保資料一致性：
     *  1. SELECT ... FOR UPDATE 鎖定庫存列
     *  2. UPDATE inventory status -> BORROWED
     *  3. INSERT borrowing_record
     */
    public ApiResponse<Map<String, Object>> borrowBook(Long userId, BorrowDto.BorrowRequest req) {
        Map<String, Object> result = borrowRepository.borrowBook(userId, req.getIsbn());

        int code = toInt(result.get("p_result_code"));
        String msg = (String) result.get("p_result_msg");

        return switch (code) {
            case 0 -> {
                Map<String, Object> data = new HashMap<>();
                data.put("recordId",    toLong(result.get("p_record_id")));
                data.put("inventoryId", toLong(result.get("p_inventory_id")));
                yield ApiResponse.ok(msg, data);
            }
            case 1 -> ApiResponse.fail(409, msg);   // 無庫存
            case 2 -> ApiResponse.fail(409, msg);   // 已借此書
            default -> ApiResponse.fail(500, msg);
        };
    }

    /**
     * 還書
     * SP 內部已使用 Transaction 確保資料一致性：
     *  1. SELECT ... FOR UPDATE 鎖定借閱紀錄
     *  2. UPDATE borrowing_record return_time
     *  3. UPDATE inventory status -> PROCESSING
     */
    public ApiResponse<Void> returnBook(Long userId, BorrowDto.ReturnRequest req) {
        Map<String, Object> result = borrowRepository.returnBook(userId, req.getInventoryId());

        int code = toInt(result.get("p_result_code"));
        String msg = (String) result.get("p_result_msg");

        return switch (code) {
            case 0  -> ApiResponse.ok(msg, null);
            case 1  -> ApiResponse.fail(404, msg);
            default -> ApiResponse.fail(500, msg);
        };
    }

    /**
     * 查詢使用者借閱紀錄
     */
    public ApiResponse<List<BorrowingRecord>> getUserBorrowings(Long userId) {
        List<BorrowingRecord> records = borrowRepository.findByUserId(userId);
        return ApiResponse.ok(records);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private int toInt(Object val) {
        if (val == null) return -1;
        if (val instanceof Integer i) return i;
        if (val instanceof BigInteger bi) return bi.intValue();
        if (val instanceof Long l) return l.intValue();
        return Integer.parseInt(val.toString());
    }

    private long toLong(Object val) {
        if (val == null) return 0L;
        if (val instanceof Long l) return l;
        if (val instanceof BigInteger bi) return bi.longValue();
        if (val instanceof Integer i) return i.longValue();
        return Long.parseLong(val.toString());
    }
}
