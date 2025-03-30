package data_shift.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import data_shift.entity.DataShiftExcelEntity;

public interface DataShiftExcelRepository extends JpaRepository<DataShiftExcelEntity, Long> {
    
}
