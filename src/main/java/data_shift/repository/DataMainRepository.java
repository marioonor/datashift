package data_shift.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import data_shift.entity.DataMainEntity;

public interface DataMainRepository extends JpaRepository<DataMainEntity, Long> {

}
