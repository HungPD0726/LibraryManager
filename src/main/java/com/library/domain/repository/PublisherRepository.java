package com.library.domain.repository;

import com.library.domain.model.Publisher;
import com.library.feature.catalog.PublisherOptionView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PublisherRepository extends JpaRepository<Publisher, Integer> {
    Optional<Publisher> findByPublisherName(String publisherName);

    @Query("""
            SELECT new com.library.feature.catalog.PublisherOptionView(p.publisherId, p.publisherName)
            FROM Publisher p
            ORDER BY p.publisherName, p.publisherId
            """)
    List<PublisherOptionView> findAllOptions();
}
