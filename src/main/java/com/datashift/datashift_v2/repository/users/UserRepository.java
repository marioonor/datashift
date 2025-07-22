package com.datashift.datashift_v2.repository.users;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.datashift.datashift_v2.entity.users.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
