package com.library.controller;

import com.library.common.ApiResponse;
import com.library.model.Book;
import com.library.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 展示層 - 書籍 Controller
 * GET /api/books  - 取得所有書籍（含可借閱數量），公開端點
 */
@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Book>>> getAllBooks() {
        return ResponseEntity.ok(bookService.getAllBooks());
    }
}
