package data_shift.service;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import data_shift.entity.Users;
import data_shift.repository.UsersRepository;
import data_shift.requests.LoginRequests;

@Service
public class UsersService {
    @Autowired
    private UsersRepository usersRepository;

    public Users addUser(Users user) {
        return usersRepository.save(user);
    }

    public Boolean loginUser(LoginRequests loginRequests) {

        Optional<Users> user = usersRepository.findById(loginRequests.getUserId());

        Users user_ = user.get();

        if (!user_.getPassword().equals(loginRequests.getPassword())) {
            return false;
        }
        return true;

    }

}
