package com.library.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BorrowingRecord {
    private Long          recordId;
    private Long          userId;
    private Long          inventoryId;
    private LocalDateTime borrowingTime;
    private LocalDateTime returnTime;
    private LocalDateTime dueDate;
    // joined fields
    private String        isbn;
    private String        inventoryStatus;
    private String        location;
    private String        bookName;
    private String        bookAuthor;
    private String        coverUrl;
}
