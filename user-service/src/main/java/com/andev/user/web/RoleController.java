package com.andev.user.web;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.andev.user.model.constants.api.ApiConstants;
import com.andev.user.model.domain.dto.RoleDto;
import com.andev.user.model.domain.dto.request.RoleRequestDto;
import com.andev.user.service.RoleService;
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
 * REST Controller for Role operations
 */
@Slf4j
@RestController
@RequestMapping(ApiConstants.API_BASE_PATH + ApiConstants.ROLES_PATH)
@RequiredArgsConstructor
@Tag(name = "Role Management", description = "APIs for managing roles")
public class RoleController {

    private final RoleService roleService;

    @PostMapping
    @Operation(summary = "Create a new role", description = "Creates a new role with the provided information")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "201",
                        description = "Role created successfully",
                        content = @Content(schema = @Schema(implementation = RoleDto.class))),
                @ApiResponse(responseCode = "400", description = "Invalid input data"),
                @ApiResponse(responseCode = "409", description = "Role already exists")
            })
    public ResponseEntity<AppResponse<RoleDto>> createRole(@Valid @RequestBody RoleRequestDto requestDto) {
        log.info("Creating new role: {}", requestDto.getName());
        RoleDto role = roleService.createRole(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(AppResponse.successful(role));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get role by ID", description = "Retrieves a role by its ID")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Role found",
                        content = @Content(schema = @Schema(implementation = RoleDto.class))),
                @ApiResponse(responseCode = "404", description = "Role not found")
            })
    public ResponseEntity<AppResponse<RoleDto>> getRoleById(
            @Parameter(description = "Role ID", example = "1") @PathVariable Long id) {
        log.info("Getting role by id: {}", id);
        return roleService
                .findById(id)
                .map(role -> ResponseEntity.ok(AppResponse.successful(role)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @Operation(summary = "Get all roles", description = "Retrieves all roles with pagination and sorting")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Roles retrieved successfully",
                        content = @Content(schema = @Schema(implementation = PaginationResponse.class)))
            })
    public ResponseEntity<AppResponse<PaginationResponse<RoleDto>>> getAllRoles(
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

        log.info("Getting all roles - page: {}, size: {}, sort: {} {}", page, size, sortBy, sortDir);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<RoleDto> rolesPage = roleService.findAll(pageable);

        PaginationResponse<RoleDto> response = PaginationResponse.fromPage(rolesPage);
        return ResponseEntity.ok(AppResponse.successful(response));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update role", description = "Updates an existing role with new information")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Role updated successfully",
                        content = @Content(schema = @Schema(implementation = RoleDto.class))),
                @ApiResponse(responseCode = "404", description = "Role not found"),
                @ApiResponse(responseCode = "400", description = "Invalid input data")
            })
    public ResponseEntity<AppResponse<RoleDto>> updateRole(
            @Parameter(description = "Role ID", example = "1") @PathVariable Long id,
            @Valid @RequestBody RoleDto roleDto) {
        log.info("Updating role with id: {}", id);
        RoleDto role = roleService.update(id, roleDto);
        return ResponseEntity.ok(AppResponse.successful(role));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete role", description = "Permanently deletes a role")
    @ApiResponses(
            value = {
                @ApiResponse(responseCode = "204", description = "Role deleted successfully"),
                @ApiResponse(responseCode = "404", description = "Role not found")
            })
    public ResponseEntity<Void> deleteRole(@Parameter(description = "Role ID", example = "1") @PathVariable Long id) {
        log.info("Deleting role with id: {}", id);
        roleService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/name/{name}")
    @Operation(summary = "Get role by name", description = "Retrieves a role by its name")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Role found",
                        content = @Content(schema = @Schema(implementation = RoleDto.class))),
                @ApiResponse(responseCode = "404", description = "Role not found")
            })
    public ResponseEntity<AppResponse<RoleDto>> getRoleByName(
            @Parameter(description = "Role name", example = "ADMIN") @PathVariable String name) {
        log.info("Getting role by name: {}", name);
        return roleService
                .findByName(name)
                .map(role -> ResponseEntity.ok(AppResponse.successful(role)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get role by user ID", description = "Retrieves the role assigned to a specific user")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Role retrieved successfully",
                        content = @Content(schema = @Schema(implementation = RoleDto.class))),
                @ApiResponse(responseCode = "404", description = "User not found or has no role")
            })
    public ResponseEntity<AppResponse<RoleDto>> getRoleByUserId(
            @Parameter(description = "User ID", example = "1") @PathVariable Long userId) {
        log.info("Getting role by user id: {}", userId);
        return roleService
                .findByUserId(userId)
                .map(role -> ResponseEntity.ok(AppResponse.successful(role)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search/name")
    @Operation(
            summary = "Search roles by name pattern",
            description = "Searches for roles whose name matches the pattern")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Roles found",
                        content = @Content(schema = @Schema(implementation = PaginationResponse.class)))
            })
    public ResponseEntity<AppResponse<PaginationResponse<RoleDto>>> searchRolesByName(
            @Parameter(description = "Name pattern", example = "admin") @RequestParam String pattern,
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
        log.info(
                "Searching roles by name pattern: {} - page: {}, size: {}, sort: {} {}",
                pattern,
                page,
                size,
                sortBy,
                sortDir);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<RoleDto> rolesPage = roleService.searchByName(pattern, pageable);

        PaginationResponse<RoleDto> response = PaginationResponse.fromPage(rolesPage);
        return ResponseEntity.ok(AppResponse.successful(response));
    }

    @GetMapping("/search/description")
    @Operation(
            summary = "Search roles by description pattern",
            description = "Searches for roles whose description matches the pattern")
    @ApiResponses(
            value = {
                @ApiResponse(
                        responseCode = "200",
                        description = "Roles found",
                        content = @Content(schema = @Schema(implementation = PaginationResponse.class)))
            })
    public ResponseEntity<AppResponse<PaginationResponse<RoleDto>>> searchRolesByDescription(
            @Parameter(description = "Description pattern", example = "administrator") @RequestParam String pattern,
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
        log.info(
                "Searching roles by description pattern: {} - page: {}, size: {}, sort: {} {}",
                pattern,
                page,
                size,
                sortBy,
                sortDir);

        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<RoleDto> rolesPage = roleService.searchByDescription(pattern, pageable);

        PaginationResponse<RoleDto> response = PaginationResponse.fromPage(rolesPage);
        return ResponseEntity.ok(AppResponse.successful(response));
    }
}
