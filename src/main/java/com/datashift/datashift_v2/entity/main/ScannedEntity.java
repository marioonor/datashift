package com.datashift.datashift_v2.entity.main;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "scanned_data")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScannedEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private Long scannedId; 
    private String keyword;
    private Long page;
    private String sentence;
}
