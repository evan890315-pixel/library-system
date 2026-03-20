package com.library.model;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class Book {
    private String    isbn;
    private String    name;
    private String    author;
    private String    introduction;
    private String    publisher;
    private LocalDate publishDate;
    private String    coverUrl;
    // computed
    private int       totalCopies;
    private int       availableCopies;
}
