package com.sivamani.docai.service;

import com.sivamani.docai.dto.Dtos.AuthResponse;
import com.sivamani.docai.dto.Dtos.LoginRequest;
import com.sivamani.docai.dto.Dtos.RegisterRequest;
import com.sivamani.docai.model.AppUser;
import com.sivamani.docai.repository.UserRepository;
import com.sivamani.docai.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UserRepository users, PasswordEncoder encoder, JwtService jwt) {
        this.users = users;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    public AuthResponse register(RegisterRequest req) {
        String email = req.email().trim().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        AppUser user = users.save(new AppUser(email, encoder.encode(req.password())));
        return new AuthResponse(jwt.generate(user), user.getEmail());
    }

    public AuthResponse login(LoginRequest req) {
        AppUser user = users.findByEmail(req.email().trim().toLowerCase())
                .filter(u -> encoder.matches(req.password(), u.getPasswordHash()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return new AuthResponse(jwt.generate(user), user.getEmail());
    }
}
