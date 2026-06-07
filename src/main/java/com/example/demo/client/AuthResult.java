package com.example.demo.client;

import org.springframework.security.core.GrantedAuthority;

import java.util.List;

public record AuthResult(String subject, List<GrantedAuthority> authorities) {}
