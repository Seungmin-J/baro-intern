package com.example.intern_assignment.auth.dto;

import com.example.intern_assignment.user.enums.UserRole;
import lombok.Builder;
import lombok.Getter;

import java.util.Set;

@Getter
@Builder
public class SignUpResponse {

    private String username;
    private String nickname;
    private Set<UserRole> userRoles;
}
