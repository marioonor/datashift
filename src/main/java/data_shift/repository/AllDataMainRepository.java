package data_shift.repository;

import data_shift.entity.DataMainEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AllDataMainRepository extends JpaRepository<DataMainEntity, Long> {

}