package com.andev.post.config.envloader;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EnvLoader {
    static {
        String profile = System.getProperty("spring.profiles.active");

        if (profile == null) {
            profile = System.getenv("SPRING_PROFILES_ACTIVE");
        }

        if (profile == null) {
            profile = "dev";
        }

        log.info("Loading environment variables for profile: {}", profile);

        String filename = profile + ".env";

        Dotenv dotenv = Dotenv.configure()
                .filename(filename)
                //  .filename("post-service/" + filename)
                .ignoreIfMissing()
                .load();

        dotenv.entries().forEach(entry -> System.setProperty(entry.getKey(), entry.getValue()));
    }
}
