package hr.fer.ppks.quizstream.controller;

import hr.fer.ppks.quizstream.model.Moderator;
import hr.fer.ppks.quizstream.repository.ModeratorRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private ModeratorRepository moderatorRepository;


    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Moderator moderator) {
        if (moderatorRepository.existsByEmail(moderator.getEmail())) {
            return ResponseEntity.badRequest().body("Email već postoji");
        }
        if (moderatorRepository.existsByUsername(moderator.getUsername())) {
            return ResponseEntity.badRequest().body("Korisničko ime već postoji");
        }

        moderator.setPassword(moderator.getPassword());
        moderator.setCreatedAt(LocalDateTime.now());
        Moderator savedModerator = moderatorRepository.save(moderator);

        // Spremi u session
        //session.setAttribute("user", savedModerator);
        savedModerator.setPassword(null); // Sakrij lozinku u odgovoru
        return ResponseEntity.ok(savedModerator);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Moderator moderator = moderatorRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Korisnik nije pronađen"));

        if (!request.getPassword().equals(moderator.getPassword())) {
            return ResponseEntity.badRequest().body("Pogrešna lozinka");
        }

        //session.setAttribute("user", moderator);
        moderator.setPassword(null); // Sakrij lozinku u odgovoru
        return ResponseEntity.ok(moderator);
    }

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }
}