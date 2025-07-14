package data_shift.service.impl;

import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import data_shift.dto.UserDTO;
import data_shift.entity.Users;
import data_shift.repository.UsersRepository;
import data_shift.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UsersRepository usersRepository;
    private final ModelMapper modelMapper;
    
    @Override
    public UserDTO createUser(UserDTO userDTO) {
        Users users = mapToUserEntity(userDTO);
        users.setUserId(UUID.randomUUID().toString());
        users = usersRepository.save(users);
        return mapToUserDTO(users);
    }

    private UserDTO mapToUserDTO(Users users) {
        return modelMapper.map(users, UserDTO.class);
    }

    private Users mapToUserEntity(UserDTO userDTO) {
        return modelMapper.map(userDTO, Users.class);
    }
}
