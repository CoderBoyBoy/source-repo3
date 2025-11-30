package com.gitserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for Git Server.
 * A GitHub-like backend service based on SpringBoot and JGit.
 */
@SpringBootApplication
public class GitServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(GitServerApplication.class, args);
    }
}
