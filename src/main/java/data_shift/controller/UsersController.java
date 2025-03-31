package data_shift.controller;

import org.springframework.web.bind.annotation.RestController;

import data_shift.entity.Users;
import data_shift.requests.LoginRequests;
import data_shift.service.UsersService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class UsersController {

    @Autowired
    UsersService usersService;    
    
    @PostMapping("/addUser")
    @CrossOrigin(origins = "http://localhost:4200")
    public Users addUser(@RequestBody Users user) {
        return usersService.addUser(user);
    }

    @PostMapping("/loginUser")
    @CrossOrigin(origins = "http://localhost:4200")
    public Boolean loginUser(@RequestBody LoginRequests loginRequests) {
        return usersService.loginUser(loginRequests);
    }
       
}
