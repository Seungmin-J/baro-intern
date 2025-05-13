package com.example.intern_assignment.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignInRequest {

    private String username;
    private String password;
}
