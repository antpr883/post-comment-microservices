package com.andev.user.web;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.andev.user.model.constants.api.ApiConstants;
import com.andev.user.model.domain.dto.UserDto;
import com.andev.user.model.domain.dto.request.UserRequestDto;
import com.andev.user.model.domain.dto.request.UserUpdateRequestDto;
import com.andev.user.model.enums.UserStatus;
import com.andev.user.service.UserService;
import com.andev.user.service.rsql.RsqlParserService;
import com.andev.user.web.response.AppResponse;
import com.andev.user.web.response.PaginationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * REST Controller for User operations
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.API_BASE_PATH + ApiConstants.USERS_PATH)
@RequiredArgsConstructor
@Tag(name = "User Management", description = "APIs for managing users")
public class UserController {

    private final UserService userService;
    private final RsqlParserService rsqlParserService;

    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user with the provided information")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "User created successfully",
                        content = @Content(schema = @Schema(implementation = UserDto.class))),
                @ApiResponse(responseCode = "400", description = "Invalid input data"),
                @ApiResponse(responseCode = "409", description = "User already exists")
            })
    public ResponseEntity<AppResponse<UserDto>> createUser(@Valid @RequestBody UserRequestDto requestDto) {
        log.info("Creating new user: {}", requestDto.getEmail());
        UserDto user = userService.createUser(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponse.successful(user));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Retrieves a user by their ID")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "User found",
                        content = @Content(schema = @Schema(implementation = UserDto.class))),
                @ApiResponse(responseCode = "404", description = "User not found")
            })
    public ResponseEntity<AppResponse<UserDto>> getUserById(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        log.info("Getting user by id: {}", id);
        return userService
                .findById(id)
                .map(user -> ResponseEntity.ok(AppResponse.successful(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all users", description = "Retrieves all users with pagination and sorting")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Users retrieved successfully",
                        content = @Content(schema = @Schema(implementation = PaginationResponse.class)))
            })
    public ResponseEntity<AppResponse<PaginationResponse<UserDto>>> getAllUsers(
            @Parameter(description = "Page number (0-based)", example = "0")
                    @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_NUMBER)
                    int page,
            @Parameter(description = "Page size", example = "10")
                    @RequestParam(defaultValue = ApiConstants.DEFAULT_PAGE_SIZE)
                    int size,
            @Parameter(description = "Sort field", example = "id")
                    @RequestParam(defaultValue = ApiConstants.DEFAULT_SORT_FIELD)
                    String sortBy,
            @Parameter(description = "Sort direction", example = "ASC")
                    @RequestParam(defaultValue = ApiConstants.DEFAULT_SORT_DIRECTION)
                    String sortDir) {

        log.info("Getting all users - page: {}, size: {}, sort: {} {}", page, size, sortBy, sortDir);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<UserDto> usersPage = userService.findAll(pageable);

        PaginationResponse<UserDto> response = PaginationResponse.fromPage(usersPage);
        return ResponseEntity.ok(AppResponse.successful(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user", description = "Updates an existing user with new information")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "User updated successfully",
                        content = @Content(schema = @Schema(implementation = UserDto.class))),
                @ApiResponse(responseCode = "404", description = "User not found"),
                @ApiResponse(responseCode = "400", description = "Invalid input data")
            })
    public ResponseEntity<AppResponse<UserDto>> updateUser(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequestDto updateDto) {
        log.info("Updating user with id: {}", id);
        UserDto user = userService.updateUser(id, updateDto);
        return ResponseEntity.ok(AppResponse.successful(user));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete user", description = "Permanently deletes a user")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "User deleted successfully"),
                @ApiResponse(responseCode = "404", description = "User not found")
            })
    public ResponseEntity<Void> deleteUser(@Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        log.info("Deleting user with id: {}", id);
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/email/{email}")
    @Operation(summary = "Get user by email", description = "Retrieves a user by their email address")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "User found",
                        content = @Content(schema = @Schema(implementation = UserDto.class))),
                @ApiResponse(responseCode = "404", description = "User not found")
            })
    public ResponseEntity<AppResponse<UserDto>> getUserByEmail(
            @Parameter(description = "User email", example = "john.doe@example.com") @PathVariable String email) {
        log.info("Getting user by email: {}", email);
        return userService
                .findByEmail(email)
                .map(user -> ResponseEntity.ok(AppResponse.successful(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/nickname/{nickname}")
    @Operation(summary = "Get user by nickname", description = "Retrieves a user by their nickname")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "User found",
                        content = @Content(schema = @Schema(implementation = UserDto.class))),
                @ApiResponse(responseCode = "404", description = "User not found")
            })
    public ResponseEntity<AppResponse<UserDto>> getUserByNickname(
            @Parameter(description = "User nickname", example = "johndoe") @PathVariable String nickname) {
        log.info("Getting user by nickname: {}", nickname);
        return userService
                .findByNickname(nickname)
                .map(user -> ResponseEntity.ok(AppResponse.successful(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get users by status", description = "Retrieves all users with a specific status")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Users retrieved successfully",
                        content = @Content(schema = @Schema(implementation = UserDto.class)))
            })
    public ResponseEntity<AppResponse<PaginationResponse<UserDto>>> getUsersByStatus(
            @Parameter(description = "User status", example = "ACTIVE") @PathVariable UserStatus status) {
        log.info("Getting users by status: {}", status);
        List<UserDto> users = userService.findByStatus(status);
        PaginationResponse<UserDto> response = new PaginationResponse<>();
        response.setContent(users);
        response.setTotalElements((long) users.size());
        response.setTotalPages(1);
        response.setPageSize(users.size());
        response.setPageNumber(0);
        return ResponseEntity.ok(AppResponse.successful(response));
    }

    @GetMapping("/search/email")
    @Operation(
            summary = "Search users by email pattern",
            description = "Searches for users whose email matches the pattern")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Users found",
                        content = @Content(schema = @Schema(implementation = UserDto.class)))
            })
    public ResponseEntity<AppResponse<PaginationResponse<UserDto>>> searchUsersByEmail(
            @Parameter(description = "Email pattern", example = "john") @RequestParam String pattern) {
        log.info("Searching users by email pattern: {}", pattern);
        List<UserDto> users = userService.searchByEmail(pattern);
        PaginationResponse<UserDto> response = new PaginationResponse<>();
        response.setContent(users);
        response.setTotalElements((long) users.size());
        response.setTotalPages(1);
        response.setPageSize(users.size());
        response.setPageNumber(0);
        return ResponseEntity.ok(AppResponse.successful(response));
    }

    @GetMapping("/search/nickname")
    @Operation(
            summary = "Search users by nickname pattern",
            description = "Searches for users whose nickname matches the pattern")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Users found",
                        content = @Content(schema = @Schema(implementation = UserDto.class)))
            })
    public ResponseEntity<AppResponse<PaginationResponse<UserDto>>> searchUsersByNickname(
            @Parameter(description = "Nickname pattern", example = "john") @RequestParam String pattern) {
        log.info("Searching users by nickname pattern: {}", pattern);
        List<UserDto> users = userService.searchByNickname(pattern);
        PaginationResponse<UserDto> response = new PaginationResponse<>();
        response.setContent(users);
        response.setTotalElements((long) users.size());
        response.setTotalPages(1);
        response.setPageSize(users.size());
        response.setPageNumber(0);
        return ResponseEntity.ok(AppResponse.successful(response));
    }

    @PatchMapping("/{id}/activate")
    @Operation(summary = "Activate user", description = "Activates a user by setting their status to ACTIVE")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "User activated successfully",
                        content = @Content(schema = @Schema(implementation = UserDto.class))),
                @ApiResponse(responseCode = "404", description = "User not found")
            })
    public ResponseEntity<AppResponse<UserDto>> activateUser(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        log.info("Activating user with id: {}", id);
        UserDto user = userService.activateUser(id);
        return ResponseEntity.ok(AppResponse.successful(user));
    }

    @PatchMapping("/{id}/deactivate")
    @Operation(summary = "Deactivate user", description = "Deactivates a user by setting their status to INACTIVE")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "User deactivated successfully",
                        content = @Content(schema = @Schema(implementation = UserDto.class))),
                @ApiResponse(responseCode = "404", description = "User not found")
            })
    public ResponseEntity<AppResponse<UserDto>> deactivateUser(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        log.info("Deactivating user with id: {}", id);
        UserDto user = userService.deactivateUser(id);
        return ResponseEntity.ok(AppResponse.successful(user));
    }

    @PatchMapping("/{id}/suspend")
    @Operation(summary = "Suspend user", description = "Suspends a user by setting their status to SUSPENDED")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "User suspended successfully",
                        content = @Content(schema = @Schema(implementation = UserDto.class))),
                @ApiResponse(responseCode = "404", description = "User not found")
            })
    public ResponseEntity<AppResponse<UserDto>> suspendUser(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        log.info("Suspending user with id: {}", id);
        UserDto user = userService.suspendUser(id);
        return ResponseEntity.ok(AppResponse.successful(user));
    }

    @DeleteMapping("/{id}/soft")
    @Operation(summary = "Soft delete user", description = "Soft deletes a user by setting their status to DELETED")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "User soft deleted successfully",
                        content = @Content(schema = @Schema(implementation = UserDto.class))),
                @ApiResponse(responseCode = "404", description = "User not found")
            })
    public ResponseEntity<AppResponse<UserDto>> softDeleteUser(
            @Parameter(description = "User ID", example = "1") @PathVariable Long id) {
        log.info("Soft deleting user with id: {}", id);
        UserDto user = userService.softDeleteUser(id);
        return ResponseEntity.ok(AppResponse.successful(user));
    }

    @GetMapping("/search/rsql")
    @Operation(summary = "Search users with RSQL", description = "Searches for users using RSQL query language")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Users found",
                        content = @Content(schema = @Schema(implementation = UserDto.class)))
            })
    public ResponseEntity<AppResponse<PaginationResponse<UserDto>>> searchUsersWithRsql(
            @Parameter(description = "RSQL query", example = "email==*@example.com;status==ACTIVE") @RequestParam
                    String query) {
        log.info("Searching users with RSQL query: {}", query);
        List<UserDto> users = userService.searchWithRsql(query);
        PaginationResponse<UserDto> response = new PaginationResponse<>();
        response.setContent(users);
        response.setTotalElements((long) users.size());
        response.setTotalPages(1);
        response.setPageSize(users.size());
        response.setPageNumber(0);
        return ResponseEntity.ok(AppResponse.successful(response));
    }
}
