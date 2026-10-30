
package bybjerrum.controller;

import bybjerrum.dao.UserDAO;
import bybjerrum.dto.RegisterRequestDTO;
import bybjerrum.entity.User;
import bybjerrum.dto.LoginRequestDTO;
import bybjerrum.security.TokenService;
import com.auth0.jwt.exceptions.JWTVerificationException;
import io.javalin.http.Context;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.UnauthorizedResponse;
import lombok.RequiredArgsConstructor;

import java.util.Map;

@RequiredArgsConstructor
public class SecurityController {

    private final UserDAO userDAO;
    private final TokenService tokenService;

    public void register(Context ctx) {

        RegisterRequestDTO request =
                ctx.bodyAsClass(RegisterRequestDTO.class);

        validateCredentials(request.getUsername(), request.getPassword());

        User user = userDAO.createUser(
                request.getUsername().trim(),
                request.getPassword()
        );

        ctx.status(201).json(Map.of(
                "id", user.getId(),
                "username", user.getUsername()
        ));
    }

    public void login(Context ctx) {

        LoginRequestDTO request =
                ctx.bodyAsClass(LoginRequestDTO.class);

        validateCredentials(request.getUsername(), request.getPassword());

        User user = userDAO.findByUsername(request.getUsername().trim());

        if (user == null || !user.verifyPassword(request.getPassword())) {
            throw new UnauthorizedResponse("Invalid username or password");
        }

        String token = tokenService.generateToken(user.getId());

        ctx.json(Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "token", token
        ));
    }

    public void getCurrentUser(Context ctx) {

        String authHeader = ctx.header("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedResponse("Missing or invalid token");
        }

        String token = authHeader.substring(7);

        Long userId;

        try {
            userId = tokenService.verifyToken(token);
        } catch (JWTVerificationException | NumberFormatException e) {
            throw new UnauthorizedResponse("Invalid or expired token");
        }

        ctx.json(Map.of("userId", userId));
    }

    private void validateCredentials(String username, String password) {
        if (username == null || username.isBlank() ||
                password == null || password.isBlank()) {
            throw new BadRequestResponse("Username and password are required");
        }
    }

}