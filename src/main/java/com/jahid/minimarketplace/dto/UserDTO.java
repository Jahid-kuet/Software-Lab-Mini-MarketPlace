package com.jahid.minimarketplace.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
// User Data Transfer Object (DTO) for API responses
public class UserDTO {

    private Long id;
    private String username;
    private String email;
    private String fullName;
    private boolean enabled;
    private Set<String> roles;
    private LocalDateTime createdAt;
}
