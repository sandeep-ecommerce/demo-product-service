package com.example.demo.mapper;

import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.model.User;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    @NonNull
    public UserResponse toResponse(@NonNull User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }

    @NonNull
    public User toEntity(@NonNull UserRequest request) {
        return new User(request.getName(), request.getEmail());
    }
}
