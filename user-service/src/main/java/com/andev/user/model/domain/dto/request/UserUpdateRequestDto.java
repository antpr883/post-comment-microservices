package com.andev.user.model.domain.dto.request;

import java.util.Set;

import com.andev.user.model.enums.UserStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

/**
 * DTO for user update request
 */
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequestDto {

    @Email(message = "Invalid email format")
    private String email;

    @Size(min = 3, max = 50, message = "Nickname must be between 3 and 50 characters")
    private String nickname;

    @Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
    private String password;

    private UserStatus status;
    private Set<Long> roleIds;
}
