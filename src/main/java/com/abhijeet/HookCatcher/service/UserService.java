package com.abhijeet.HookCatcher.service;

import com.abhijeet.HookCatcher.dto.RegisterRequest;
import com.abhijeet.HookCatcher.entity.UserEntity;
import com.abhijeet.HookCatcher.exception.UsernameAlreadyExistsException;
import com.abhijeet.HookCatcher.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException("Username already exists: " + request.getUsername());
        }

        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole("USER");

        userRepository.save(user);

    }
}
