package data_shift.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import data_shift.entity.DataShiftExtractedDataEntity;

public interface DataShiftExtractedDataRepository extends JpaRepository<DataShiftExtractedDataEntity, Long> {
    
}
