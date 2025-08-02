package com.andev.comment;

import com.andev.comment.config.envloader.EnvLoader;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CommentServiceApplication {

    public static void main(String[] args) {
        new EnvLoader();
        SpringApplication.run(CommentServiceApplication.class, args);
    }
}
