package com.example.intern_assignment.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignUpRequest {

    private String username;
    private String password;
    private String nickname;
}
