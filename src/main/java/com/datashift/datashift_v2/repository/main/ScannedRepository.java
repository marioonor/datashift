package com.datashift.datashift_v2.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;

import com.datashift.datashift_v2.entity.main.ScannedEntity;
import java.util.Optional;


public interface ScannedRepository extends JpaRepository<ScannedEntity, Long> {
    
    Optional<ScannedEntity> findByScannedId(Long scannedId);
}
