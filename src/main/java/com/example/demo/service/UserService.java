package com.example.demo.service;

import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import org.springframework.lang.NonNull;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers();
    UserResponse getUserById(@NonNull Long id);
    UserResponse createUser(@NonNull UserRequest request);
    UserResponse updateUser(@NonNull Long id, @NonNull UserRequest request);
    void deleteUser(@NonNull Long id);
}
