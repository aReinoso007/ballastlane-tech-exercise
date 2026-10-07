package com.ballastlane.pokedex.web;

import com.ballastlane.pokedex.application.user.AuthenticateUser;
import com.ballastlane.pokedex.application.user.RegisterUser;
import com.ballastlane.pokedex.web.dto.LoginRequest;
import com.ballastlane.pokedex.web.dto.LoginResponse;
import com.ballastlane.pokedex.web.dto.RegisterRequest;
import com.ballastlane.pokedex.web.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Registration and login (public)")
public class AuthController {

    private final RegisterUser registerUser;
    private final AuthenticateUser authenticateUser;

    public AuthController(RegisterUser registerUser, AuthenticateUser authenticateUser) {
        this.registerUser = registerUser;
        this.authenticateUser = authenticateUser;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Register a new user")
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        return UserResponse.from(registerUser.execute(request.username(), request.email(), request.password()));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate and receive a JWT")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return LoginResponse.from(authenticateUser.execute(request.username(), request.password()));
    }
}
