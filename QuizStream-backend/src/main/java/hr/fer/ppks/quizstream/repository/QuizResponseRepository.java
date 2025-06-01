package hr.fer.ppks.quizstream.repository;

import hr.fer.ppks.quizstream.model.QuizResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuizResponseRepository extends JpaRepository<QuizResponse, Long> {
}
