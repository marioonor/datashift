package data_shift.service;

import org.springframework.stereotype.Service;

import data_shift.dto.UserDTO;

@Service
public interface UserService {

    UserDTO createUser(UserDTO userDTO);

}
