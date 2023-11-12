package com.example.demo.service;

import java.util.Objects;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.UserCreationDto;
import com.example.demo.exception.AppException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Iterable<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public void save(UserCreationDto dto) {
        User user = userRepository.findByEmail(dto.getEmail());

        if (Objects.nonNull(user)) {
            throw new AppException("Email address is already registered.", HttpStatus.BAD_REQUEST);
        }

        userRepository.save(dto);
    }

}
