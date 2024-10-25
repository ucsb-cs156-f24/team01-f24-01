package edu.ucsb.cs156.example.controllers;

import edu.ucsb.cs156.example.entities.UCSBArticles;
import edu.ucsb.cs156.example.entities.UCSBDate;
import edu.ucsb.cs156.example.errors.EntityNotFoundException;
import edu.ucsb.cs156.example.repositories.UCSBArticlesRepository;
import edu.ucsb.cs156.example.repositories.UCSBDateRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import java.time.LocalDateTime;

/**
 * This is a REST controller for UCSBArticles
 */

@Tag(name = "UCSBArticles")
@RequestMapping("/api/ucsbarticles")
@RestController
@Slf4j
public class UCSBArticlesController extends ApiController {

    @Autowired
    UCSBArticlesRepository ucsbArticlesRepository;

    /**
     * List all UCSB articles
     * 
     * @return an iterable of UCSBArticles
     */
    @Operation(summary= "List all ucsb articles")
    @PreAuthorize("hasRole('ROLE_USER')")
    @GetMapping("/all")
    public Iterable<UCSBArticles> allUCSBArticles() {
        Iterable<UCSBArticles> articles = ucsbArticlesRepository.findAll();
        return articles;
    }


    /**
     * Create a new article
     * 
     * @param title  the title of the article
     * @param url          the url of the article
     * @param explanation    a summary of the article
     * @param email          an email address
     * @param dateAdded the date
     * @return the saved ucsbarticle
     */
    @Operation(summary= "Create a new article")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @PostMapping("/post")
    public UCSBArticles postUCSBArticles(
            @Parameter(name="title") @RequestParam String title,
            @Parameter(name="url") @RequestParam String url,
            @Parameter(name="explanation") @RequestParam String explanation,
            @Parameter(name="email") @RequestParam String email,
            @Parameter(name="dateAdded", description="date (in iso format, e.g. YYYY-mm-ddTHH:MM:SS; see https://en.wikipedia.org/wiki/ISO_8601)") @RequestParam("dateAdded") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateAdded)
            throws JsonProcessingException {

        // For an explanation of @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        // See: https://www.baeldung.com/spring-date-parameters

        log.info("dateAdded={}", dateAdded);

        UCSBArticles ucsbArticles = new UCSBArticles();
        ucsbArticles.setTitle(title);
        ucsbArticles.setUrl(url);
        ucsbArticles.setExplanation(explanation);
        ucsbArticles.setEmail(email);
        ucsbArticles.setDateAdded(dateAdded);

        UCSBArticles savedUcsbArticles = ucsbArticlesRepository.save(ucsbArticles);

        return savedUcsbArticles;
    }

}