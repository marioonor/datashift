package com.datashift.datashift_v2.io;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class AuthResponse {
    
    private Long id;
    private String role;
    private String token;
    private String name;
}
