package data_shift.controller;

import org.springframework.web.bind.annotation.RestController;

import data_shift.dto.UserDTO;
import data_shift.io.UserRequest;
import data_shift.io.UserResponse;
import data_shift.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;


@RestController
@Slf4j
@RequiredArgsConstructor
public class UsersController {

    @Autowired
    UserService usersService;    

    private final ModelMapper modelMapper;
    private final UserService userService;
    
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/addUser")
    public UserResponse addUser(@Valid @RequestBody UserRequest userRequest) {
        log.info("Adding user: {}", userRequest);
        UserDTO userDTO = mapToUserDTO(userRequest);
        userDTO = userService.createUser(userDTO);
        return mapToUserResponse(userDTO);
    }

    // @PostMapping("/loginUser")
    // @CrossOrigin(origins = "http://localhost:4200")
    // public Boolean loginUser(@RequestBody LoginRequests loginRequests) {
    //     return usersService.loginUser(loginRequests);
    // }

    private UserDTO mapToUserDTO(UserRequest userRequest) {
        return modelMapper.map(userRequest, UserDTO.class);
    }

    private UserResponse mapToUserResponse(UserDTO userDTO) {
        return modelMapper.map(userDTO, UserResponse.class);
    }
       
}
