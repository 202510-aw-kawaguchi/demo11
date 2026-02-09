package com.example.demo;

import com.example.todo.entity.User;
import com.example.todo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        upsertUser("user", "password", "USER");
        upsertUser("admin", "admin", "ADMIN");
    }

    private void upsertUser(String username, String rawPassword, String role) {
        User existing = userMapper.findByUsername(username);
        if (existing == null) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(rawPassword));
            user.setRole(role);
            user.setEnabled(true);
            userMapper.insert(user);
            log.info("Created user {} with role {}", username, role);
            return;
        }
        existing.setPassword(passwordEncoder.encode(rawPassword));
        existing.setRole(role);
        existing.setEnabled(true);
        userMapper.update(existing);
        log.info("Updated user {} with role {}", username, role);
    }
}
