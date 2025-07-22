package com.datashift.datashift_v2.service.users;

import java.util.Optional;

import com.datashift.datashift_v2.dto.users.UserDTO;
import com.datashift.datashift_v2.entity.users.UserEntity;

public interface UserService {

    UserEntity createUser(UserDTO userDTO);
    Optional<UserEntity> findByEmail(String email);
}
