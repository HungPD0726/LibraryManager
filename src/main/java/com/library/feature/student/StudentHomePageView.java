package com.library.feature.student;

import com.library.domain.model.Category;
import com.library.domain.model.Publisher;
import com.library.feature.catalog.BookCardView;

import java.util.List;

public record StudentHomePageView(
        List<BookCardView> books,
        List<Category> categories,
        List<Publisher> publishers,
        int currentPage,
        int totalPages,
        String search,
        String letter,
        Integer categoryId,
        Integer publisherId,
        String author,
        StudentHomeSummary summary,
        String studentDisplayName
) {
}
