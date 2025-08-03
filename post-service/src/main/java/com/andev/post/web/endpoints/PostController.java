package com.andev.post.web.endpoints;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.andev.post.model.domain.dto.PostDto;
import com.andev.post.model.domain.dto.request.PostRequestDto;
import com.andev.post.model.domain.dto.request.PostUpdateRequestDto;
import com.andev.post.service.PostService;
import com.andev.post.web.response.AppResponse;
import com.andev.post.web.response.PaginationResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("${end.point.posts}")
@RequiredArgsConstructor
public class PostController implements PostEndpoints {

    private final PostService postService;

    @Override
    public ResponseEntity<AppResponse<PostDto>> findById(Long id) {
        AppResponse<PostDto> byId = postService.findById(id);
        return ResponseEntity.ok(byId);
    }

    @Override
    public ResponseEntity<AppResponse<PostDto>> createPost(PostRequestDto requestDto) {
        AppResponse<PostDto> createdPost = postService.create(requestDto);
        return ResponseEntity.ok(createdPost);
    }

    @Override
    public ResponseEntity<AppResponse<PostDto>> updatePost(Long id, PostUpdateRequestDto requestUpdateDto) {
        AppResponse<PostDto> updatedPost = postService.update(id, requestUpdateDto);
        return ResponseEntity.ok(updatedPost);
    }

    @Override
    public ResponseEntity<AppResponse<PostDto>> deletePost(Long id) {
        AppResponse<PostDto> deletedPost = postService.delete(id);
        return ResponseEntity.ok(deletedPost);
    }

    @Override
    public ResponseEntity<AppResponse<PostDto>> softDeletePost(Long id) {
        AppResponse<PostDto> softDeletedPost = postService.softDelete(id);
        return ResponseEntity.ok(softDeletedPost);
    }

    @Override
    public ResponseEntity<AppResponse<PaginationResponse<PostDto>>> getAllPosts(int page, int size, String sort) {
        Pageable pageable = createPageable(page, size, sort);
        AppResponse<PaginationResponse<PostDto>> posts = postService.findAll(pageable);
        return ResponseEntity.ok(posts);
    }

    @Override
    public ResponseEntity<AppResponse<PaginationResponse<PostDto>>> getPostsByAuthor(
            Long authorId, int page, int size, String sort) {
        Pageable pageable = createPageable(page, size, sort);
        AppResponse<PaginationResponse<PostDto>> posts = postService.findByAuthorId(authorId, pageable);
        return ResponseEntity.ok(posts);
    }

    @Override
    public ResponseEntity<AppResponse<PaginationResponse<PostDto>>> getPostsByIds(
            List<Long> ids, int page, int size, String sort) {
        Pageable pageable = createPageable(page, size, sort);
        AppResponse<PaginationResponse<PostDto>> posts = postService.findByIds(ids, pageable);
        return ResponseEntity.ok(posts);
    }

    @Override
    public ResponseEntity<AppResponse<PaginationResponse<PostDto>>> getPostsByStatus(
            String status, int page, int size, String sort) {
        Pageable pageable = createPageable(page, size, sort);
        AppResponse<PaginationResponse<PostDto>> posts = postService.findByStatus(status, pageable);
        return ResponseEntity.ok(posts);
    }

    /**
     * Creates a Pageable object with proper sorting handling
     */
    private Pageable createPageable(int page, int size, String sort) {
        if (sort != null && !sort.trim().isEmpty()) {
            String[] sortParts = sort.split(",");
            if (sortParts.length == 2) {
                String field = sortParts[0].trim();
                String direction = sortParts[1].trim().toUpperCase();

                // Map entity field names to database column names
                String sortField = mapFieldToColumn(field);

                Sort.Direction sortDirection = "DESC".equals(direction) ? Sort.Direction.DESC : Sort.Direction.ASC;

                return PageRequest.of(page, size, Sort.by(sortDirection, sortField));
            }
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "id"));
    }

    /**
     * Maps entity field names to database column names for sorting
     */
    private String mapFieldToColumn(String field) {
        return switch (field.toLowerCase()) {
            case "id" -> "id";
            case "title" -> "title";
            case "content" -> "content";
            case "authorid", "author_id" -> "authorId";
            case "poststatus", "post_status", "status" -> "postStatus";
            case "likes" -> "likes";
            case "created", "created_at", "createdat" -> "created";
            case "updated", "updated_at", "updatedat" -> "updated";
            default -> "id"; // Default to id if field not recognized
        };
    }

    @Override
    public ResponseEntity<Void> updatePostStatuses(List<Long> ids, String newStatus) {
        postService.updateStatuses(ids, newStatus);
        return ResponseEntity.ok().build();
    }
}
