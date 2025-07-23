package com.datashift.datashift_v2.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.datashift.datashift_v2.dto.users.UserDTO;
import com.datashift.datashift_v2.entity.users.UserEntity;
import com.datashift.datashift_v2.repository.users.UserRepository;
import com.datashift.datashift_v2.service.users.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;


    @Override
    public UserEntity createUser(UserDTO userDTO) {
        UserEntity userEntity = mapToUserEntity(userDTO);
        userEntity.setUserId(UUID.randomUUID().toString());
        userEntity.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        UserEntity savedEntity = userRepository.save(userEntity);
        log.info("Printing the user entity details {}", savedEntity);
        return savedEntity;
    }

    private UserEntity mapToUserEntity(UserDTO userDTO) {
        return modelMapper.map(userDTO, UserEntity.class);
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
}
