package com.library.feature.catalog;

import com.library.domain.model.Author;
import com.library.domain.repository.AuthorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthorService {

    private final AuthorRepository authorRepository;

    @Transactional(readOnly = true)
    public List<Author> findAll() {
        return authorRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<AuthorOptionView> findAllOptions() {
        return authorRepository.findAllOptions();
    }

    @Transactional(readOnly = true)
    public Optional<Author> findById(Integer id) {
        return authorRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Author> findAllByIds(List<Integer> ids) {
        return authorRepository.findAllById(ids);
    }

    public Author save(Author author) {
        return authorRepository.save(author);
    }

    public void deleteById(Integer id) {
        authorRepository.deleteById(id);
    }
}
