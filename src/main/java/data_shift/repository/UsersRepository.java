package data_shift.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import data_shift.entity.Users;

@Repository
public interface UsersRepository extends JpaRepository<Users, String> {

}
