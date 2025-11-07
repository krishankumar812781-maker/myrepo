package com.example.MovieBooking.dto.RequestDto;

import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class RegisterDto {
    private String username;
    private String email;
    private String password;
    private Set<String> roles=new HashSet<>() ; // e.g., ["ROLE_ADMIN", "ROLE_USER"]
}
