package com.library.web.view;

import java.time.LocalDateTime;

public record BookFileView(
        Integer bookFileId,
        Integer bookId,
        String bookName,
        String staffName,
        String fileName,
        String fileUrl,
        String fileType,
        Long fileSize,
        LocalDateTime uploadAt,
        Boolean active
) {
}
