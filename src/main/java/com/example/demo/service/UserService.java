package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.dto.UserCreationDto;
import com.example.demo.exception.AppException;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
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

    public void saveInBulk(Integer n) {
        List<UserCreationDto> dtoList = new ArrayList<>();
        long start = System.currentTimeMillis();
        for (int i = 0; i < n; i++) {
            UserCreationDto dto = new UserCreationDto();
            dto.setEmail(RandomStringUtils.randomAlphanumeric(12));
            dto.setName("テストタロウ");
            dto.setPassword("p@ssw0rd");
            dtoList.add(dto);
            if ((i + 1) % 1000 == 0 || (i + 1) == n) {
                doSaveInBulk(dtoList);
                dtoList.clear();
                long end = System.currentTimeMillis();
                String msg = String.format("%d/%d %d[ms]", i + 1, n, end - start);
                log.info(msg);
                start = System.currentTimeMillis();
            }
        }
    }

    @Transactional
    private void doSaveInBulk(List<UserCreationDto> dtoList) {
        userRepository.saveInBulk(dtoList);
    }

}
