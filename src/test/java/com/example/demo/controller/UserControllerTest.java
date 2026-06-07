package com.example.demo.controller;

import com.example.demo.client.AuthServiceClient;
import com.example.demo.dto.UserRequest;
import com.example.demo.dto.UserResponse;
import com.example.demo.exception.UserNotFoundException;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Web-layer slice test — only the MVC layer is loaded.
 * UserService and AuthServiceClient are mocked so no real DB or auth service is needed.
 */
@WebMvcTest(UserController.class)
@DisplayName("UserController")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthServiceClient authServiceClient;

    private UserResponse user1;
    private UserResponse user2;
    private UserRequest validRequest;

    @BeforeEach
    void setUp() {
        user1 = new UserResponse(1L, "John Doe", "john@example.com");
        user2 = new UserResponse(2L, "Jane Doe", "jane@example.com");

        validRequest = new UserRequest();
        validRequest.setName("John Doe");
        validRequest.setEmail("john@example.com");
    }

    // -------------------------------------------------------------------------
    // GET /api/users  — authenticated
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/users")
    class GetAllUsers {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 200 with full user list")
        void returnsOkWithUsers() throws Exception {
            when(userService.getAllUsers()).thenReturn(List.of(user1, user2));

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.length()").value(2))
                    .andExpect(jsonPath("$[0].id").value(1))
                    .andExpect(jsonPath("$[0].name").value("John Doe"))
                    .andExpect(jsonPath("$[0].email").value("john@example.com"))
                    .andExpect(jsonPath("$[1].id").value(2))
                    .andExpect(jsonPath("$[1].name").value("Jane Doe"));

            verify(userService).getAllUsers();
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 200 with empty list when no users exist")
        void returnsEmptyList() throws Exception {
            when(userService.getAllUsers()).thenReturn(List.of());

            mockMvc.perform(get("/api/users"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(0));
        }
    }

    // -------------------------------------------------------------------------
    // GET /api/users/{id}  — authenticated
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /api/users/{id}")
    class GetUserById {

        @Test
        @WithMockUser
        @DisplayName("returns 200 when user exists")
        void returnsOkWhenFound() throws Exception {
            when(userService.getUserById(1L)).thenReturn(user1);

            mockMvc.perform(get("/api/users/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("John Doe"))
                    .andExpect(jsonPath("$.email").value("john@example.com"));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 404 with error body when user does not exist")
        void returns404WhenNotFound() throws Exception {
            when(userService.getUserById(99L)).thenThrow(new UserNotFoundException(99L));

            mockMvc.perform(get("/api/users/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("User not found with id: 99"));
        }
    }

    // -------------------------------------------------------------------------
    // POST /api/users  — authenticated
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("POST /api/users")
    class CreateUser {

        @Test
        @WithMockUser
        @DisplayName("returns 201 with created user on valid request")
        void returns201WithCreatedUser() throws Exception {
            when(userService.createUser(any())).thenReturn(user1);

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("John Doe"))
                    .andExpect(jsonPath("$.email").value("john@example.com"));

            verify(userService).createUser(any());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when name is blank")
        void returns400WhenNameBlank() throws Exception {
            validRequest.setName("  ");

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("name")));

            verify(userService, never()).createUser(any());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when email is blank")
        void returns400WhenEmailBlank() throws Exception {
            validRequest.setEmail("");

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));

            verify(userService, never()).createUser(any());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when email format is invalid")
        void returns400WhenEmailInvalid() throws Exception {
            validRequest.setEmail("not-an-email");

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when name exceeds 100 characters")
        void returns400WhenNameTooLong() throws Exception {
            validRequest.setName("A".repeat(101));

            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when request body is missing")
        void returns400WhenBodyMissing() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 4xx when no authentication provided")
        void returns4xxWhenUnauthenticated() throws Exception {
            mockMvc.perform(post("/api/users")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().is4xxClientError());

            verify(userService, never()).createUser(any());
        }
    }

    // -------------------------------------------------------------------------
    // PUT /api/users/{id}  — authenticated
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("PUT /api/users/{id}")
    class UpdateUser {

        @Test
        @WithMockUser
        @DisplayName("returns 200 with updated user on valid request")
        void returnsOkWithUpdatedUser() throws Exception {
            UserResponse updated = new UserResponse(1L, "John Updated", "john.updated@example.com");
            UserRequest updateRequest = new UserRequest();
            updateRequest.setName("John Updated");
            updateRequest.setEmail("john.updated@example.com");

            when(userService.updateUser(eq(1L), any())).thenReturn(updated);

            mockMvc.perform(put("/api/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.name").value("John Updated"))
                    .andExpect(jsonPath("$.email").value("john.updated@example.com"));

            verify(userService).updateUser(eq(1L), any());
        }

        @Test
        @WithMockUser
        @DisplayName("returns 404 when user does not exist")
        void returns404WhenNotFound() throws Exception {
            when(userService.updateUser(eq(99L), any())).thenThrow(new UserNotFoundException(99L));

            mockMvc.perform(put("/api/users/99")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("User not found with id: 99"));
        }

        @Test
        @WithMockUser
        @DisplayName("returns 400 when request body is invalid")
        void returns400WhenInvalidBody() throws Exception {
            UserRequest invalid = new UserRequest();
            invalid.setName("");
            invalid.setEmail("bad-email");

            mockMvc.perform(put("/api/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("returns 4xx when no authentication provided")
        void returns4xxWhenUnauthenticated() throws Exception {
            mockMvc.perform(put("/api/users/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest)))
                    .andExpect(status().is4xxClientError());

            verify(userService, never()).updateUser(anyLong(), any());
        }
    }

    // -------------------------------------------------------------------------
    // DELETE /api/users/{id}  — authenticated
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("DELETE /api/users/{id}")
    class DeleteUser {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 204 on successful deletion")
        void returns204OnSuccess() throws Exception {
            doNothing().when(userService).deleteUser(1L);

            mockMvc.perform(delete("/api/users/1"))
                    .andExpect(status().isNoContent());

            verify(userService, times(1)).deleteUser(1L);
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("returns 404 when user does not exist")
        void returns404WhenNotFound() throws Exception {
            doThrow(new UserNotFoundException(99L)).when(userService).deleteUser(99L);

            mockMvc.perform(delete("/api/users/99"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("User not found with id: 99"));
        }

        @Test
        @DisplayName("returns 4xx when no authentication provided")
        void returns4xxWhenUnauthenticated() throws Exception {
            mockMvc.perform(delete("/api/users/1"))
                    .andExpect(status().is4xxClientError());

            verify(userService, never()).deleteUser(anyLong());
        }
    }
}
