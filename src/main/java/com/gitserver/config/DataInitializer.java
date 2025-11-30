package com.gitserver.config;

import com.gitserver.entity.User;
import com.gitserver.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

/**
 * Initializes application data on startup.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${git.repositories.base-path:./repositories}")
    private String repositoriesBasePath;

    @Value("${spring.security.user.name:admin}")
    private String defaultAdminUsername;

    @Value("${spring.security.user.password:admin123}")
    private String defaultAdminPassword;

    @Override
    public void run(String... args) throws Exception {
        initializeRepositoriesDirectory();
        initializeDefaultAdmin();
    }

    private void initializeRepositoriesDirectory() {
        File reposDir = new File(repositoriesBasePath);
        if (!reposDir.exists()) {
            boolean created = reposDir.mkdirs();
            if (created) {
                log.info("Created repositories directory: {}", reposDir.getAbsolutePath());
            } else {
                log.warn("Failed to create repositories directory: {}", reposDir.getAbsolutePath());
            }
        } else {
            log.info("Repositories directory exists: {}", reposDir.getAbsolutePath());
        }
    }

    private void initializeDefaultAdmin() {
        if (!userRepository.existsByUsername(defaultAdminUsername)) {
            Set<String> roles = new HashSet<>();
            roles.add("ROLE_ADMIN");
            roles.add("ROLE_USER");

            User admin = User.builder()
                    .username(defaultAdminUsername)
                    .email(defaultAdminUsername + "@gitserver.local")
                    .password(passwordEncoder.encode(defaultAdminPassword))
                    .displayName("Administrator")
                    .enabled(true)
                    .roles(roles)
                    .build();

            userRepository.save(admin);
            log.info("Created default admin user: {}", defaultAdminUsername);
        } else {
            log.info("Default admin user already exists: {}", defaultAdminUsername);
        }
    }
}
