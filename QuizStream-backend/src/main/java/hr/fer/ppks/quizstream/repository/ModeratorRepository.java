package hr.fer.ppks.quizstream.repository;

import hr.fer.ppks.quizstream.model.Moderator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModeratorRepository extends JpaRepository<Moderator, Long> {
    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    Optional<Moderator> findByEmail(String email);
}
