package com.example.demo;

import com.example.todo.entity.User;
import com.example.todo.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userMapper.findByUsername("user") == null) {
            User user = new User();
            user.setUsername("user");
            user.setPassword(passwordEncoder.encode("password"));
            user.setRole("USER");
            user.setEnabled(true);
            userMapper.insert(user);
        }
        if (userMapper.findByUsername("admin") == null) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("adminpass"));
            admin.setRole("ADMIN");
            admin.setEnabled(true);
            userMapper.insert(admin);
        }
    }
}
