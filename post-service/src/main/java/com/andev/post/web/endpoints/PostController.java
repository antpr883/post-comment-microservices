package com.andev.post.web.endpoints;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.andev.post.model.domain.dto.PostDto;
import com.andev.post.model.domain.dto.request.PostRequestDto;
import com.andev.post.service.PostService;
import com.andev.post.web.response.AppResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${end.point.posts}")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping("${end.point.id}")
    public ResponseEntity<AppResponse<PostDto>> findById(@PathVariable Long id) {
        AppResponse<PostDto> byId = postService.findById(id);
        return ResponseEntity.ok(byId);
    }

    @PostMapping
    public ResponseEntity<AppResponse<PostDto>> createPost(@RequestBody PostRequestDto requestDto) {
        AppResponse<PostDto> createdPost = postService.create(requestDto);
        return ResponseEntity.ok(createdPost);
    }
}
