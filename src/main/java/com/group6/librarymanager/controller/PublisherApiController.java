package com.group6.librarymanager.controller;

import com.group6.librarymanager.model.dao.PublisherDAO;
import com.group6.librarymanager.model.entity.Publisher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/publishers")
public class PublisherApiController {

    @Autowired
    private PublisherDAO publisherDAO;

    @GetMapping
    public ResponseEntity<List<Publisher>> getAllPublishers() {
        return ResponseEntity.ok(publisherDAO.findAll());
    }

    @PostMapping
    public ResponseEntity<Publisher> createPublisher(@RequestBody Publisher publisher) {
        return ResponseEntity.ok(publisherDAO.save(publisher));
    }
}
