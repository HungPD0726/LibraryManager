package com.library.service;

import com.library.entity.Book;
import com.library.entity.BookPrice;
import com.library.entity.Price;
import com.library.repository.BookPriceRepository;
import com.library.repository.BookRepository;
import com.library.repository.PriceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BookService {

    private final BookRepository bookRepository;
    private final PriceRepository priceRepository;
    private final BookPriceRepository bookPriceRepository;

    public Page<Book> findAll(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("bookId").descending());
        return bookRepository.findAll(pageable);
    }

    public Page<Book> searchByName(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("bookId").descending());
        return bookRepository.searchByName(keyword, pageable);
    }

    public Page<Book> searchCatalog(String search,
                                    String letter,
                                    Integer categoryId,
                                    Integer publisherId,
                                    String authorKeyword,
                                    int page,
                                    int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("bookName").ascending());
        return bookRepository.searchCatalog(
                blankToNull(search),
                blankToNull(letter),
                categoryId,
                publisherId,
                blankToNull(authorKeyword),
                pageable
        );
    }

    public Optional<Book> findById(Integer id) {
        return bookRepository.findById(id);
    }

    public Optional<Book> findDetailedById(Integer id) {
        return bookRepository.findDetailedByBookId(id);
    }

    public List<Book> findAll() {
        return bookRepository.findAll(Sort.by(Sort.Direction.ASC, "bookName"));
    }

    @Transactional
    public Book createBook(Book book, BigDecimal priceAmount, String currency, String note) {
        Book savedBook = bookRepository.save(book);
        createOrUpdatePrice(savedBook.getBookId(), priceAmount, currency, note);
        return savedBook;
    }

    @Transactional
    public Book updateBook(Book book, BigDecimal priceAmount, String currency, String note) {
        Book savedBook = bookRepository.save(book);
        createOrUpdatePrice(savedBook.getBookId(), priceAmount, currency, note);
        return savedBook;
    }

    @Transactional
    public void deleteBook(Integer id) {
        bookRepository.deleteById(id);
    }

    public long countAll() {
        return bookRepository.countAllBooks();
    }

    public long countTotalAvailable() {
        return bookRepository.countTotalAvailable();
    }

    public Optional<BookPrice> getCurrentPrice(Integer bookId) {
        return bookPriceRepository.findCurrentByBookId(bookId);
    }

    public Price getCurrentPriceValue(Integer bookId) {
        return getCurrentPrice(bookId)
                .flatMap(bookPrice -> priceRepository.findById(bookPrice.getPriceId()))
                .orElse(null);
    }

    private void createOrUpdatePrice(Integer bookId, BigDecimal priceAmount, String currency, String note) {
        Optional<BookPrice> currentBp = bookPriceRepository.findCurrentByBookId(bookId);
        if (currentBp.isPresent()) {
            BookPrice bookPrice = currentBp.get();
            Price currentPrice = priceRepository.findById(bookPrice.getPriceId()).orElse(null);

            if (currentPrice != null && (currentPrice.getAmount().compareTo(priceAmount) != 0
                    || !safeEquals(currentPrice.getCurrency(), currency)
                    || !safeEquals(currentPrice.getNote(), note))) {

                if (LocalDate.now().equals(bookPrice.getStartDate())) {
                    currentPrice.setAmount(priceAmount);
                    currentPrice.setCurrency(currency != null ? currency : "VND");
                    currentPrice.setNote(note);
                    priceRepository.save(currentPrice);
                    return;
                }

                bookPrice.setEndDate(LocalDate.now().minusDays(1));
                bookPriceRepository.save(bookPrice);
            } else {
                return;
            }
        }

        Price price = new Price();
        price.setAmount(priceAmount);
        price.setCurrency(currency != null ? currency : "VND");
        price.setNote(note);
        Price savedPrice = priceRepository.save(price);

        BookPrice link = new BookPrice();
        link.setBookId(bookId);
        link.setPriceId(savedPrice.getPriceId());
        link.setStartDate(LocalDate.now());
        bookPriceRepository.save(link);
    }

    private boolean safeEquals(String left, String right) {
        if (left == null) {
            return right == null;
        }
        return left.equals(right);
    }

    private String blankToNull(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        return value.trim();
    }
}
