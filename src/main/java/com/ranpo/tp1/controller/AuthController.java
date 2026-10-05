package com.ranpo.tp1.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ranpo.tp1.entity.User;
import com.ranpo.tp1.repository.UserRepository;
import com.ranpo.tp1.security.JwtDTO;
import com.ranpo.tp1.security.TokenGenerator;

@RestController
@RequestMapping("/auth")
public class AuthController {

    record Credentials(String username, String password) {}

    private final AuthenticationManager authenticationManager;
    private final TokenGenerator tokenGenerator;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager, TokenGenerator tokenGenerator,
            UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.tokenGenerator = tokenGenerator;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody Credentials credentials) {
        if (userRepository.findByUsername(credentials.username()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Identifiant déjà utilisé");
        }
        User user = new User();
        user.setUsername(credentials.username());
        user.setPassword(passwordEncoder.encode(credentials.password()));
        user.getRoles().add("USER");
        userRepository.save(user);
        return ResponseEntity.status(HttpStatus.CREATED).body("Utilisateur créé");
    }

    @PostMapping("/login")
    public JwtDTO login(@RequestBody Credentials credentials) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(credentials.username(), credentials.password()));

        String token = tokenGenerator.generateJwtToken(authentication);
        UserDetails principal = (UserDetails) authentication.getPrincipal();
        List<String> roles = principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return new JwtDTO(token, principal.getUsername(), roles);
    }
}