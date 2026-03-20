package com.library.repository;

import com.library.model.BorrowingRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Repository;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class BorrowRepository {

    private final JdbcTemplate jdbcTemplate;

    private SimpleJdbcCall spBorrowBook;
    private SimpleJdbcCall spReturnBook;
    private SimpleJdbcCall spGetUserBorrowings;

    @PostConstruct
    private void init() {
        spBorrowBook = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_borrow_book");

        spReturnBook = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_return_book");

        spGetUserBorrowings = new SimpleJdbcCall(jdbcTemplate)
                .withProcedureName("sp_get_user_borrowings")
                .returningResultSet("records", (rs, rowNum) -> {
                    BorrowingRecord r = new BorrowingRecord();
                    r.setRecordId(rs.getLong("record_id"));
                    r.setUserId(rs.getLong("user_id"));
                    r.setInventoryId(rs.getLong("inventory_id"));
                    r.setIsbn(rs.getString("isbn"));
                    r.setInventoryStatus(rs.getString("inventory_status"));
                    r.setLocation(rs.getString("location"));
                    r.setBookName(rs.getString("book_name"));
                    r.setBookAuthor(rs.getString("book_author"));
                    r.setCoverUrl(rs.getString("cover_url"));
                    var bt = rs.getTimestamp("borrowing_time");
                    if (bt != null) r.setBorrowingTime(bt.toLocalDateTime());
                    var rt = rs.getTimestamp("return_time");
                    if (rt != null) r.setReturnTime(rt.toLocalDateTime());
                    var dd = rs.getTimestamp("due_date");
                    if (dd != null) r.setDueDate(dd.toLocalDateTime());
                    return r;
                });
    }

    /**
     * 呼叫 SP 借書（SP 內含 Transaction）
     */
    public Map<String, Object> borrowBook(Long userId, String isbn) {
        return spBorrowBook.execute(Map.of(
                "p_user_id", userId,
                "p_isbn",    isbn
        ));
    }

    /**
     * 呼叫 SP 還書（SP 內含 Transaction）
     */
    public Map<String, Object> returnBook(Long userId, Long inventoryId) {
        return spReturnBook.execute(Map.of(
                "p_user_id",      userId,
                "p_inventory_id", inventoryId
        ));
    }

    /**
     * 查詢使用者所有借閱紀錄
     */
    @SuppressWarnings("unchecked")
    public List<BorrowingRecord> findByUserId(Long userId) {
        Map<String, Object> result = spGetUserBorrowings.execute(
                Map.of("p_user_id", userId)
        );
        return (List<BorrowingRecord>) result.get("records");
    }
}
