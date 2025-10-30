package com.tiffin_sathi.services;
import org.springframework.stereotype.Service;

import com.tiffin_sathi.model.User;
import com.tiffin_sathi.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServices {
    private final UserRepository userRepository;

    public UserServices(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> allUsers() {
        List<User> users = new ArrayList<>();

        userRepository.findAll().forEach(users::add);

        return users;
    }
}