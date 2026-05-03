package com.smartcampus.events.service;

import com.smartcampus.events.dto.UserRegistrationDto;
import com.smartcampus.events.exception.ResourceNotFoundException;
import com.smartcampus.events.model.Role;
import com.smartcampus.events.model.User;
import com.smartcampus.events.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    @Transactional
    public User registerStudent(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + dto.getEmail());
        }
        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.STUDENT)
                .department(dto.getDepartment())
                .phone(dto.getPhone())
                .build();
        User saved = userRepository.save(user);
        emailService.sendWelcomeEmail(saved);
        return saved;
    }

    @Transactional
    public User registerAdmin(UserRegistrationDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + dto.getEmail());
        }
        User user = User.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .username(dto.getUsername())
                .password(passwordEncoder.encode(dto.getPassword()))
                .role(Role.ADMIN)
                .department(dto.getDepartment())
                .phone(dto.getPhone())
                .build();
        return userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", -1L));
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public boolean usernameExists(String username) {
        if (username == null) return false;
        return userRepository.existsByUsername(username.trim());
    }
}
