package com.library.feature.catalog;

import com.library.domain.model.Book;
import com.library.domain.model.BookFile;
import com.library.domain.model.Staff;
import com.library.domain.repository.BookFileRepository;
import com.library.domain.repository.BookRepository;
import com.library.domain.repository.StaffRepository;
import com.library.feature.catalog.BookFileForm;
import com.library.feature.catalog.BookFileView;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookFileService {

    private final BookFileRepository bookFileRepository;
    private final BookRepository bookRepository;
    private final StaffRepository staffRepository;

    @Transactional(readOnly = true)
    public List<BookFileView> findAllViews() {
        return bookFileRepository.findAllByOrderByBookFileIdDesc().stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BookFileView> findActiveByBook(Integer bookId) {
        return bookFileRepository.findByBookBookIdAndIsActiveTrueOrderByUploadAtDescBookFileIdDesc(bookId).stream()
                .map(this::toView)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookFile findById(Integer id) {
        return bookFileRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tệp sách."));
    }

    @Transactional(readOnly = true)
    public BookFile findActiveEntity(Integer id) {
        return bookFileRepository.findByBookFileIdAndIsActiveTrue(id)
                .orElseThrow(() -> new IllegalArgumentException("Tệp sách không khả dụng."));
    }

    @Transactional
    public BookFile create(BookFileForm form, Integer staffId) {
        return bookFileRepository.save(buildEntity(new BookFile(), form, staffId));
    }

    @Transactional
    public BookFile update(Integer id, BookFileForm form, Integer staffId) {
        BookFile file = findById(id);
        return bookFileRepository.save(buildEntity(file, form, staffId));
    }

    @Transactional
    public void delete(Integer id) {
        bookFileRepository.deleteById(id);
    }

    private BookFile buildEntity(BookFile target, BookFileForm form, Integer staffId) {
        Book book = bookRepository.findById(form.getBookId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sách để gắn file."));
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy nhân viên thao tác."));

        target.setBook(book);
        target.setStaff(staff);
        target.setFileName(form.getFileName().trim());
        target.setFileUrl(form.getFileUrl().trim());
        target.setFileType(form.getFileType());
        target.setFileSize(form.getFileSize());
        target.setIsActive(form.getActive() == null ? Boolean.TRUE : form.getActive());
        return target;
    }

    private BookFileView toView(BookFile file) {
        return new BookFileView(
                file.getBookFileId(),
                file.getBook().getBookId(),
                file.getBook().getBookName(),
                file.getStaff().getStaffName(),
                file.getFileName(),
                file.getFileUrl(),
                file.getFileType(),
                file.getFileSize(),
                file.getUploadAt(),
                file.getIsActive()
        );
    }
}
