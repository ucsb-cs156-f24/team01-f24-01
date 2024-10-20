package edu.ucsb.cs156.example.entities;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name="ucsborganizations")
public class UCSBOrganization {
    @Id
    private String orgField;
    private String orgTranslationShort;
    private String orgTranslation;
    private boolean inactive;
}
