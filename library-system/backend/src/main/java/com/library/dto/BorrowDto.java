package com.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

public class BorrowDto {

    @Data
    public static class BorrowRequest {
        @NotBlank(message = "ISBN 不可為空")
        private String isbn;
    }

    @Data
    public static class ReturnRequest {
        @NotNull(message = "庫存 ID 不可為空")
        private Long inventoryId;
    }
}
