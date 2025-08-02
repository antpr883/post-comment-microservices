package com.andev.comment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.andev.comment.config.envloader.EnvLoader;

@SpringBootApplication
public class CommentServiceApplication {

    public static void main(String[] args) {
        new EnvLoader();
        SpringApplication.run(CommentServiceApplication.class, args);
    }
}
