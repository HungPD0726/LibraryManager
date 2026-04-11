package com.library.feature.student;

import com.library.feature.catalog.BookCardView;
import com.library.feature.catalog.CategoryOptionView;
import com.library.feature.catalog.PublisherOptionView;

import java.util.List;

public record StudentHomePageView(
        List<BookCardView> books,
        List<CategoryOptionView> categories,
        List<PublisherOptionView> publishers,
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
