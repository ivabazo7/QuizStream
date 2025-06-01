package hr.fer.ppks.quizstream.controller;

import hr.fer.ppks.quizstream.dto.ModeratorDTO;
import hr.fer.ppks.quizstream.model.Moderator;
import hr.fer.ppks.quizstream.repository.ModeratorRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

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

        ModeratorDTO moderatorDTO = new ModeratorDTO(savedModerator);
        return ResponseEntity.ok(moderatorDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        Optional<Moderator> optModerator = moderatorRepository.findByEmail(request.getEmail());
        if (optModerator.isEmpty()) {
            return ResponseEntity.badRequest().body("Pogreška prijave");
        }
        Moderator moderator = optModerator.get();

        if (!request.getPassword().equals(moderator.getPassword())) {
            return ResponseEntity.badRequest().body("Pogreška prijave.");
        }

        ModeratorDTO moderatorDTO = new ModeratorDTO(moderator);
        return ResponseEntity.ok(moderatorDTO);
    }

    @Data
    public static class LoginRequest {
        private String email;
        private String password;
    }
}