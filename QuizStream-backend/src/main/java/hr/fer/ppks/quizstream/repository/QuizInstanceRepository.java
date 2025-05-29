package hr.fer.ppks.quizstream.repository;

import hr.fer.ppks.quizstream.model.QuizInstance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizInstanceRepository extends JpaRepository<QuizInstance, Long> {

    boolean existsByCodeAndEndTimestampIsNull(String code);

    Optional<QuizInstance> findByCodeAndEndTimestampIsNull(String code);
}
