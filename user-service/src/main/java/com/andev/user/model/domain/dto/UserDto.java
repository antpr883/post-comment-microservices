package com.andev.user.model.domain.dto;

import java.util.Set;

import com.andev.user.model.enums.UserStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for User entity
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class UserDto extends BaseDto {

    private String email;
    private String nickname;
    private UserStatus status;
    private Set<RoleDto> roles;
}
