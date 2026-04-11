package com.library.feature.catalog;

import com.library.domain.model.Publisher;
import com.library.domain.repository.PublisherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PublisherService {

    private final PublisherRepository publisherRepository;

    @Transactional(readOnly = true)
    public List<Publisher> findAll() {
        return publisherRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<PublisherOptionView> findAllOptions() {
        return publisherRepository.findAllOptions();
    }

    @Transactional(readOnly = true)
    public Optional<Publisher> findById(Integer id) {
        return publisherRepository.findById(id);
    }

    public Publisher save(Publisher publisher) {
        return publisherRepository.save(publisher);
    }

    public void deleteById(Integer id) {
        publisherRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return publisherRepository.count();
    }
}
