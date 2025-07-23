package com.datashift.datashift_v2.repository.main;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.datashift.datashift_v2.entity.main.ScannedEntity;

public interface ScannedRepository extends JpaRepository<ScannedEntity, Long> {
    List<ScannedEntity> findByUserId(Long id);
}
