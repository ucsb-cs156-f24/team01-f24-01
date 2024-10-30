package edu.ucsb.cs156.example.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
/** 
 * This is a JPA entity that represents a UCSBArticle
 * 
 * A UCSBArticles is an article at UCSB
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "ucsbarticles")
public class UCSBArticles {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  private String title;
  private String url;
  private String explanation;
  private String email;
  private LocalDateTime dateAdded;
}