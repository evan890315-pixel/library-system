package com.library.service;

import com.library.common.ApiResponse;
import com.library.model.Book;
import com.library.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;

    /**
     * 取得所有書籍及可借閱數量
     */
    public ApiResponse<List<Book>> getAllBooks() {
        List<Book> books = bookRepository.findAllWithAvailability();
        return ApiResponse.ok(books);
    }
}
