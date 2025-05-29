package hr.fer.ppks.quizstream.repository;

import hr.fer.ppks.quizstream.model.AnswerOption;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnswerOptionRepository extends JpaRepository<AnswerOption, Long> {
    List<AnswerOption> findAllByQuestionId(Long questionId);
}
