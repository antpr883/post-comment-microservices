package com.andev.post.web.endpoints;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController implements HealthEndpoints {

    @Override
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Post Service is running!");
    }
}
