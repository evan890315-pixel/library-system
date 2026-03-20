package com.library.controller;

import com.library.common.ApiResponse;
import com.library.dto.BorrowDto;
import com.library.model.BorrowingRecord;
import com.library.service.BorrowService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 展示層 - 借還書 Controller
 *
 * POST   /api/borrow          - 借書（需登入）
 * POST   /api/borrow/return   - 還書（需登入）
 * GET    /api/borrow/my       - 我的借閱紀錄（需登入）
 */
@RestController
@RequestMapping("/borrow")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> borrowBook(
            @Valid @RequestBody BorrowDto.BorrowRequest req,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        ApiResponse<Map<String, Object>> resp = borrowService.borrowBook(userId, req);
        return ResponseEntity.status(resp.isSuccess() ? 200 : resp.getCode()).body(resp);
    }

    @PostMapping("/return")
    public ResponseEntity<ApiResponse<Void>> returnBook(
            @Valid @RequestBody BorrowDto.ReturnRequest req,
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        ApiResponse<Void> resp = borrowService.returnBook(userId, req);
        return ResponseEntity.status(resp.isSuccess() ? 200 : resp.getCode()).body(resp);
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<BorrowingRecord>>> myBorrowings(
            Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(borrowService.getUserBorrowings(userId));
    }
}
