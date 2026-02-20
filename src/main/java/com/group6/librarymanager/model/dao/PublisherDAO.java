package com.group6.librarymanager.model.dao;

import com.group6.librarymanager.model.entity.Publisher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PublisherDAO extends JpaRepository<Publisher, Integer> {
}
