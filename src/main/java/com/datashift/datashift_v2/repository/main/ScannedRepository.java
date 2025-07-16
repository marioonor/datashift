package com.datashift.datashift_v2.repository.main;

import org.springframework.data.jpa.repository.JpaRepository;

import com.datashift.datashift_v2.entity.main.ScannedEntity;

public interface ScannedRepository extends JpaRepository<ScannedEntity, Long> {
    
}
